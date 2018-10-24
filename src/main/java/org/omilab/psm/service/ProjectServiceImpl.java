// File:         ProjectServiceImpl.java
// Created:      19.02.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.service;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.beanutils.converters.DateTimeConverter;
import org.omilab.psm.conf.OverlayUndefinedException;
import org.omilab.psm.model.db.*;
import org.omilab.psm.repo.*;
import org.omilab.psm.service.logging.LogMessage;
import org.omilab.psm.service.logging.LoggingService;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.List;
import javax.mail.*;
import javax.mail.internet.*;
import javax.validation.ConstraintViolationException;


@Component("ProjectService")
@Transactional
@SuppressWarnings("unused")
public final class ProjectServiceImpl implements ProjectService {

	private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

	private static final ArrayList<String> PROHIBITED_PROJ_FIELDS = new ArrayList<String>(
			Arrays.asList("created", "inConfig", "proposal", "id", "instances", "keywords", "project_navigation","projecttype", "navigation", "uniqueID"));

	private final KeywordRepository keywordRepo;

	private final LoggingService logService;

	private final GenericProjectRepository projectRepo;

	private final MainNavigationGenericRepository mniRepo;

	private final ProjectProposalRepository proposalRepo;

	private final ProjectTypeRepository typesRepo;

	private final UserService userService;

	private final Environment env;

	private final GlobalConfigurationService globalConf;

	private final Random generator = new Random();

	@Autowired
	public ProjectServiceImpl(final GenericProjectRepository projectRepo, final KeywordRepository keywordRepo, final UserService userService,
							  final LoggingService logService, final ProjectProposalRepository proposalRepo, final ProjectTypeRepository typesRepo,
							  final Environment env, final GlobalConfigurationService globalConf, final MainNavigationGenericRepository mniRepo) {
		this.projectRepo = projectRepo;
		this.keywordRepo = keywordRepo;
		this.userService = userService;
		this.logService = logService;
		this.proposalRepo = proposalRepo;
		this.typesRepo = typesRepo;
		this.env = env;
		this.globalConf = globalConf;
		this.mniRepo = mniRepo;
	}


	@Override
	public List<AbstractProject> getAllProjects() {
		return projectRepo.findAll();
	}

	@Override
	public List<Keyword> getAllKeywords() {
		return keywordRepo.findAll();
	}

	@Override
	public List<Keyword> getKeywordByProject(final AbstractProject project) {
		return keywordRepo.findByProject(project);
	}

	@Override
	public AbstractProject getProject(final String urlidentifier) {
		return projectRepo.findByUrlidentifier(urlidentifier);
	}

	@Override
	public AbstractProject getProject(Long id) {
		return projectRepo.findById(id);
	}

	@Override
	public AbstractProject createProject(final String name, final String abbreviation, final String urlidentifier, final ProjectProposal proposal) {
		AbstractProject ap = null;
		try {
			Class<?> clazz = Class.forName(proposal.getType().getOverlay());
			Object newProject = clazz.newInstance();
			ap = (AbstractProject) newProject;
		} catch (ClassNotFoundException e) {
			logger.error("Error finding the overlay(\"" + proposal.getType().getOverlay() + "\"): " + e.getMessage());
			logger.debug("Error finding the overlay(\"" + proposal.getType().getOverlay() + "\"): ", e);
			throw new OverlayUndefinedException("Project Template has no overlay defined! Contact administrator!");
		} catch (InstantiationException e) {
			logger.error("Failed to instantiate overlay(\""+ proposal.getType().getOverlay() +"\"): " + e.getMessage());
			logger.debug("Failed to instantiate overlay(\""+ proposal.getType().getOverlay() +"\"): ", e);
		} catch (IllegalAccessException e) {
			logger.error("Overlay could not be accessed(\""+ proposal.getType().getOverlay() +"\"): " + e.getMessage());
			logger.debug("Overlay could not be accessed(\""+ proposal.getType().getOverlay() +"\"): ", e);
		}

		if(ap != null) {
			ap.setName(name);
			ap.setAbbreviation(abbreviation);
			ap.setUrlidentifier(urlidentifier);
		} else
			logger.error("Failed to set initial attributes of project: " + name + " with overlay: " + proposal.getType().getOverlay());

		AbstractProject newP = projectRepo.save(ap);
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "create", newP.getClass().getSimpleName(), Long.toString(newP.getId())));
		return newP;
	}

	@Override
	public Page<AbstractProject> getProjectOverviewPage(final Pageable page) {
		return projectRepo.findAll(page);
	}

	@Override
	public void updateKeywords(final AbstractProject project, final List<String> keywords) {
		for(String keyword : keywords) {
			//keyword = keyword.replaceAll("\\s+", "");
			final Keyword tempKeyword = keywordRepo.findByContent(keyword);
			if(tempKeyword != null) {
				if(keywordRepo.findByProjectAndContent(project, keyword) == null) {
					project.addKeyword(keywordRepo.findByContent(keyword));
					logger.debug("Added keyword " + keywordRepo.findByContent(keyword).getContent() + " to " + project.getName());
				}
			} else {
				final Keyword newKeyword = new Keyword(keyword);
				project.addKeyword(newKeyword);
				logger.debug("Creating keyword " + keywordRepo.findByContent(keyword).getContent() + " and adding it to " + project.getName());
			}
		}
		for(Keyword keyword : keywordRepo.findByProject(project)) {
			if(!keywords.contains(keyword.getContent())) {
				project.removeKeyword(keyword);
				logger.debug("Removed keyword " + keyword + " from " + project.getName());
			}
		}
	}

	@Override
	public String getBackTileForProject(final AbstractProject project) {
		if(project.getProjecttype().getBackgroundTile() == null)
			return "";
		String output = project.getProjecttype().getBackgroundTile();
		output = output.replace("$PROJECTURLIDENTIFIER",project.getUrlidentifier());
		output = output.replace("$urlidentifier",project.getUrlidentifier());
		output = parseProjectStrings(output,project, this.env.getProperty("app.url"));
		return output;
	}

	@Override
	public String getFrontTileForProject(final AbstractProject project) {
		if(project.getProjecttype().getForegroundTile() == null)
			return "";
		String output = project.getProjecttype().getForegroundTile();
		output = output.replace("$PROJECTURLIDENTIFIER",project.getUrlidentifier());
		output = output.replace("$urlidentifier",project.getUrlidentifier());
		output = parseProjectStrings(output,project, this.env.getProperty("app.url"));
		return output;
	}

	@Override
	public List<AbstractProject> search(final String query) {
		List<AbstractProject> result = null;
		try {
			result = new ArrayList<>();
			if(query.equals(""))
				return projectRepo.findAll();
			List<AbstractProject> aProj = projectRepo.findAll();
			for(AbstractProject proj : aProj) {
				//low performance and very basic search prototype!
				if(proj.getName().toLowerCase().contains(query.toLowerCase()) || proj.getUrlidentifier().toLowerCase().contains(query.toLowerCase()) || proj.getAbbreviation().toLowerCase().contains(query)) {
					result.add(proj);
				}
			}
		} catch(NullPointerException e) {
			logger.error("Search failed, as one of the projects has one of the following fields set to null: name, abbreviation, urlidentifier, shortdescription ");
		}
		return result;
	}

	@Override
	public int getProjectTileStart() {
		return 1000 + generator.nextInt(450000 - 1000 + 1);
	}


	@Override
	public ProjectProposal createProposal(final String projectname, final String projectabstract, final String username,
							   final String email, final Long type, final MainNavigationItemTypes mni) {
		ProjectType pt = typesRepo.findById(type);
		if(pt == null)
			pt = mni.getTypes().get(0);
		ProjectProposal proposal = proposalRepo.save(new ProjectProposal(projectname, projectabstract, username, email, pt));
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "create", proposal.getClass().getSimpleName(), Long.toString(proposal.getId())));
		//approve(proposal);
		notifyFranchiseAdministrators(proposal);
		return proposal;
	}

	@Override
	public String getNumberOfUnreadProposals(final ProjectType type) {
		return Integer.toString(proposalRepo.findUnreadProposals(type.getId()).size());
	}

	@Override
	public List<ProjectProposal> getProposalsForProjectType(final ProjectType type) {
		return proposalRepo.findUnreadProposals(type.getId());
	}

	@Override
	public ProjectProposal getProposal(final Long id) {
		return proposalRepo.findById(id);
	}

	@Override
	public ProjectProposal getProposalbyUUID(final String UUID) {
		return proposalRepo.findByProposalID(UUID);
	}

	@Override
	public void approve(final ProjectProposal proposal) {
		if(proposal.getAcceptedStatus() == null) {
			proposal.setAcceptedStatus(true);
			proposal.setDateAccepted(new Date());
			proposal.setUserAccepted(userService.getCurrentUser().getUsername());
			notifyProjectContact(proposal);
			notifyAdminApproval(proposal);
		}
	}

	@Override
	public void deny(final ProjectProposal proposal) {
		if(proposal.getAcceptedStatus() == null) {
			proposal.setAcceptedStatus(false);
			proposal.setDateAccepted(new Date());
			proposal.setUserAccepted(userService.getCurrentUser().getUsername());
			notifyProjectContact(proposal);
			notifyAdminDecline(proposal);
		}
	}


	private String generateApprovalMail(ProjectProposal proposal) {
		final String tempMessage = globalConf.getValue("wizard_accepted_template");
		return tempMessage.replace("$FIRSTNAME",userService.querySpecificUser(proposal.getUserid()).getFirstName())
				.replace("$LASTNAME",userService.querySpecificUser(proposal.getUserid()).getLastName())
				.replace("$LINK",env.getProperty("app.url") + "/wizard/start?proposaluuid=" + proposal.getProposalID());
	}

	private String generateDeclineMail(ProjectProposal proposal) {
		final String tempMessage = globalConf.getValue("wizard_rejected_template");
		return tempMessage.replace("$FIRSTNAME",userService.querySpecificUser(proposal.getUserid()).getFirstName())
				.replace("$LASTNAME",userService.querySpecificUser(proposal.getUserid()).getLastName());
	}
	private void notifyProjectContact(ProjectProposal proposal) {
		final String sub = "Your OMiLAB Proposal: \"" + proposal.getName() + "\"";
		final String finalMessage;
		if(proposal.getAcceptedStatus()) {
			finalMessage = generateApprovalMail(proposal);
		} else {
			finalMessage = generateDeclineMail(proposal);
		}
		sendMail(proposal.getEmail(),sub,finalMessage);
	}

	private static String determineTextColor(final AbstractProject project) {
		final String hex;
		if(project.getProjecttype().getBackgroundColor() != null) {
			hex = project.getProjecttype().getBackgroundColor();
		} else {
			hex = project.getBackgroundColor();
		}
		Color c = Color.decode(hex);
		int red = c.getRed();
		int green = c.getGreen();
		int blue = c.getBlue();
		if( (red*0.299 + green*0.587 + blue*0.114) > 186)
			return "#000000";
		else
			return "#ffffff";
	}

	protected static String parseProjectStrings(final String input, final AbstractProject project, String appUrl) {
		String out = input;
		if(input.contains("$TEXTCOLOR")) {
			out = input.replace("$TEXTCOLOR", determineTextColor(project));
		}
		if(input.contains("$BACKGROUND") && project.getProjecttype().getBackgroundColor() != null){
			out = out.replace("$BACKGROUND", project.getProjecttype().getBackgroundColor());
		}
		out = out.replace("$","");
		List<String> parameters = new ArrayList<>();
		for(String st : input.replace("$TEXTCOLOR","").replace("$BACKGROUND", "").split(" ")){
			if(st.startsWith("$")){
				final String temp = st.replace("$","");
				parameters.add(temp);
			}
		}
		for(String entry : parameters) {
			try {
				Object obj = BeanUtils.getProperty(project,entry);
				if(obj != null) {
					out = out.replace(entry, BeanUtils.getProperty(project,entry));
				}
				else{
					if(entry.toLowerCase().equals("officialdownload") || entry.toLowerCase().equals("guidelines") || entry.toLowerCase().equals("usermanual") || entry.toLowerCase().equals("deploymentenv")){
						String type = entry.toLowerCase();
						if(type.equals("officialdownload")){
							type = "Download";
						} else if(type.equals("guidelines")){
							type = "Guide";
						} else if(type.equals("usermanual")){
							type = "Manual";
						} else if(type.equals("deploymentenv")){
						    type = "Deployment";
                        }
						out = out.replace(entry, appUrl + "/nocontent?project=" + project.getUrlidentifier() + "&type=" + type);
					} else {
						out = out.replace(entry, "$null");
					}
				}
			} catch (IllegalAccessException e) {
				out = out.replace(entry,"$invalidproperty");
				logger.error("Failed to parse property \"" + entry + "\" from project \"" + project.getName() + "\".");
			} catch (InvocationTargetException e) {
				out = out.replace(entry,"$invalidproperty");
				logger.error("Failed to parse property \"" + entry + "\" from project \"" + project.getName() + "\".");
			} catch (NoSuchMethodException e) {
				out = out.replace(entry,"$invalidproperty");
				logger.error("Failed to parse property \"" + entry + "\" from project \"" + project.getName() + "\".");
			}
		}
		return out;
	}

	private void notifyFranchiseAdministrators(ProjectProposal proposal) {

		final String emails = globalConf.getValue("wizard_notification_new");
		final String[] recipients = emails.split(",");

		final String sub = "New OMiLAB Proposal: \"" + proposal.getName() + "\"";
		final StringBuilder sb = new StringBuilder();
		sb.append("Dear Franchise Administrator,\n");
		sb.append("\na new proposal for an OMiLAB project has been made. ");
		sb.append("The details are: \n\n");
		sb.append("Proposed project name: " + proposal.getName());
		sb.append("\nProposed project abstract: " + proposal.getProjectabstract());
		if(userService.querySpecificUser(proposal.getUserid()).getFirstName() == null || userService.querySpecificUser(proposal.getUserid()).getLastName() == null)
			sb.append("\nProject contact name: (not specified by user)");
		else
			sb.append("\nProject contact name: " + userService.querySpecificUser(proposal.getUserid()).getFirstName() + " " + userService.querySpecificUser(proposal.getUserid()).getLastName());
		sb.append("\nProject contact email: " + proposal.getEmail());
		if (userService.querySpecificUser(proposal.getUserid()).getOrganization() != null)
			sb.append("\nProject contact affiliation: " + userService.querySpecificUser(proposal.getUserid()).getOrganization());
		else
			sb.append("\nProject contact affiliation: (not specified by user)");
		sb.append("\n\n\n");
		sb.append("You may approve or deny the project using the following link: " + env.getProperty("app.url") + "/settings?view=projecttypes&proposal=" + proposal.getId());
		sb.append("\nYou will receive an confirmation email depending on your action.");
		sb.append("\n\nBest regards,\n");
		sb.append("OMiLAB Notification System");
		for(final String rec : recipients) {
			sendMail(rec, sub, sb.toString());
		}
	}

	private void notifyAdminApproval(ProjectProposal proposal) {
		final String sub = "Confirmation of approval of proposal: \"" + proposal.getName() + "\"";
		final StringBuilder sb = new StringBuilder();
		sb.append("Dear Franchise Administrator,\n");
		sb.append("\nyou have approved the following proposal: " + proposal.getName());
		sb.append("\nThe following email has been sent to " + proposal.getEmail());
		sb.append("\n\n---------------------------------------------------------\n\n\n");
		sb.append(generateApprovalMail(proposal));
		sendMail(userService.querySpecificUser(proposal.getUserAccepted()).getEmailAddress(), sub, sb.toString());
	}

	private void notifyAdminDecline(ProjectProposal proposal) {
		final String sub = "Confirmation of rejection of proposal: \"" + proposal.getName() + "\"";
		final StringBuilder sb = new StringBuilder();
		sb.append("Dear Franchise Administrator,\n");
		sb.append("\nyou have rejected the following proposal: " + proposal.getName());
		sb.append("\nThe following email has been sent to " + proposal.getEmail());
		sb.append("\n\n---------------------------------------------------------\n\n\n");
		sb.append(generateDeclineMail(proposal));
		sendMail(userService.querySpecificUser(proposal.getUserAccepted()).getEmailAddress(), sub, sb.toString());
	}

	private class SMTPAuthenticator extends javax.mail.Authenticator {
		public PasswordAuthentication getPasswordAuthentication() {
			String username = env.getProperty("omilab.mail.user");
			String password = env.getProperty("omilab.mail.password");
			return new PasswordAuthentication(username, password);
		}
	}

	private void sendMail(final String recipient, final String subject, final String text) {

		final String from = env.getProperty("omilab.support");
		Properties properties = System.getProperties();
		properties.setProperty("mail.transport.protocol", "smtp");
		properties.setProperty("mail.smtp.host", env.getProperty("omilab.mail.server"));
		Session session = null;
		if(env.getProperty("omilab.mail.auth") != null && Boolean.parseBoolean(env.getProperty("omilab.mail.auth"))) {
			Authenticator auth = new SMTPAuthenticator();
			session = Session.getDefaultInstance(properties, auth);
		} else {
			session = Session.getDefaultInstance(properties);
		}

		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
			message.setSubject(subject);
			message.setText(text);
			Transport.send(message);
		} catch (MessagingException mex) {
			logger.error("Failed to send email with subject \"" + subject + "\" to \"" + recipient + "\" because " + mex.getMessage());
		}
	}

	@Override
	public void changeProjectAttributesPO(Map<String, String> attributes, AbstractProject project) {
		DateTimeConverter dtConverter = new DateConverter();
		//dtConverter.setPattern("yyyy-MM-dd HH:mm:ss");
		dtConverter.setPattern("dd/MM/yyyy");
		ConvertUtils.register(dtConverter, Date.class);
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "update", project.getClass().getSimpleName(), Long.toString(project.getId())));
		for (Map.Entry<String,String> entry : attributes.entrySet()) {
			if(PROHIBITED_PROJ_FIELDS.contains(entry.getKey()))
				return;
			if((!entry.getKey().equals("tags")) && (!entry.getKey().equals("urlidentifier"))) {
				try {
					BeanUtils.setProperty(project, entry.getKey(), entry.getValue());
				} catch (IllegalAccessException e) {
					logger.error("Error processing project settings. Do HTML-name and bean property really match?" + e.getMessage());
					logger.debug("Error processing project settings. Do HTML-name and bean property really match?", e);
				} catch (InvocationTargetException e) {
					logger.error("Error processing project settings. Do HTML-name and bean property really match?" + e.getMessage());
					logger.debug("Error processing project settings. Do HTML-name and bean property really match?", e);
				}
			}
		}
	}

	@Override
	public void changeProjectAttributesWizard(Map<String, String> attributes, AbstractProject project) {
		DateTimeConverter dtConverter = new DateConverter();
		//dtConverter.setPattern("yyyy-MM-dd HH:mm:ss");
		dtConverter.setPattern("dd/MM/yyyy");
		ConvertUtils.register(dtConverter, Date.class);
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "update", project.getClass().getSimpleName(), Long.toString(project.getId())));
		for (Map.Entry<String,String> entry : attributes.entrySet()) {
			if(PROHIBITED_PROJ_FIELDS.contains(entry.getKey()))
				return;
			if((!entry.getKey().equals("tags"))) {
				try {
					BeanUtils.setProperty(project, entry.getKey(), entry.getValue());
				} catch (IllegalAccessException e) {
					logger.error("Error processing project settings. Do HTML-name and bean property really match?" + e.getMessage());
					logger.debug("Error processing project settings. Do HTML-name and bean property really match?", e);
				} catch (InvocationTargetException e) {
					logger.error("Error processing project settings. Do HTML-name and bean property really match?" + e.getMessage());
					logger.debug("Error processing project settings. Do HTML-name and bean property really match?", e);
				}
			}
		}
	}

	@Override
	public Integer getOpenProposals() {
		String username = userService.getCurrentUser().getUsername();
		return proposalRepo.countProposals(username);
	}

	@Override
	public ProjectProposal getFirstOpenProposal() {
		String username = userService.getCurrentUser().getUsername();
		return proposalRepo.findUnfinishedUserProposals(username).get(0);
	}

	@Override
	public Boolean removeProject(AbstractProject p) {
		final Long id = p.getId();
		try {
			for(MainNavigationItem mni : p.getNavigation()) {
                mniRepo.deleteById(mni.getId());
            }
			ProjectProposal pp = proposalRepo.findProposalsByProjectId(p.getId());
			if(pp != null) {
				pp.setType(null);
				pp.setProject(null);
				p.setProposal(null);
				proposalRepo.deleteById(pp.getId());
			}
			if(p.getProjecttype() != null)
				p.setProjecttype(null);
			p.removeAllKeywords();
			projectRepo.deleteById(p.getId());
		} catch (Exception e) {
			logger.error("Could not remove project \"" + p.getName() + "\" because of the following error: " + e.getMessage());
			logger.debug("Could not remove project \"" + p.getName() + "\" because of the following error: ",e);
			return false;
		}
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "delete", p.getClass().getSimpleName(), Long.toString(id)));
		return true;
	}

	@Override
	public void changeUrlIdentifier(final AbstractProject p, final String url) {
		p.setUrlidentifier(url);
	}

	@Override
	public void toggleInConfig(AbstractProject p) {
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "update", p.getClass().getSimpleName(), Long.toString(p.getId())));
		if(p.getInConfig() == null) {
			p.setInConfig(false);
		}
		if(p.getInConfig())
			p.setInConfig(false);
		else
			p.setInConfig(true);
	}
}
