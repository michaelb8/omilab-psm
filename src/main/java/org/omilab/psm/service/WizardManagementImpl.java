// File:         WizardManagementImpl.java
// Created:      01.02.16
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2016 by OMiLAB.ORG
//

package org.omilab.psm.service;

import org.omilab.psm.model.db.*;
import org.omilab.psm.model.wrapper.UINavigationItem;
import org.omilab.psm.repo.ProjectTypeRepository;
import org.omilab.psm.repo.WizardConfigurationRepository;
import org.omilab.psm.service.ldap.LDAPUser;
import org.omilab.psm.service.logging.LogMessage;
import org.omilab.psm.service.logging.LoggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

@Component("WizardManagement")
@Transactional
public class WizardManagementImpl implements WizardManagement {

	private static final Logger logger = LoggerFactory.getLogger(WizardManagementImpl.class);

	private final ProjectTypeRepository typeRepo;

	private final WizardConfigurationRepository wizardRepo;

	private final ServiceManagementService serviceMgmt;

	private final UserService users;

	private final Environment env;

	private final GlobalConfigurationService globalConf;

	private final RequestorService requestService;

	private final ApplicationContext ctx;

	private final LoggingService logService;

	private final UserService userService;

	@Autowired
	public WizardManagementImpl(final ProjectTypeRepository typeRepo, final WizardConfigurationRepository wizardRepo,
								final ServiceManagementService serviceMgmt, final UserService users, final Environment env,
								final GlobalConfigurationService globalConf, final ApplicationContext ctx,
								final RequestorService requestService, final UserService userService, final LoggingService loggingService) {
		this.typeRepo = typeRepo;
		this.wizardRepo = wizardRepo;
		this.serviceMgmt = serviceMgmt;
		this.users = users;
		this.env = env;
		this.globalConf = globalConf;
		this.ctx = ctx;
		this.requestService = requestService;
		this.logService = loggingService;
		this.userService = userService;
	}

	@Override
	public List<UINavigationItem> getStepsForProposal(final ProjectProposal proposal, final int pos, final Locale locale) {

		List<UINavigationItem> uiItems = new ArrayList<UINavigationItem>();

		if(pos == 1)
			uiItems.add(new UINavigationItem("<b>1:</b> " + getMessage("general.wizard.start", locale),true));
		else
			uiItems.add(new UINavigationItem("<b>1:</b> " + getMessage("general.wizard.start", locale),false));


		if(pos == 2)
			uiItems.add(new UINavigationItem("<b>2:</b> " + getMessage("general.wizard.project", locale),true));
		else
			uiItems.add(new UINavigationItem("<b>2:</b> " + getMessage("general.wizard.project", locale),false));

		int counter = 4;
		if(proposal.getType().getwRoleStatus()) {
			if (pos == 3)
				uiItems.add(new UINavigationItem("<b>3:</b> " + getMessage("general.wizard.permissions", locale), true));
			else
				uiItems.add(new UINavigationItem("<b>3:</b> " + getMessage("general.wizard.permissions", locale), false));

		} else
			counter--;

		for(DBNavigationItem item : wizardRepo.findByType(proposal.getType())) {
			UINavigationItem tempUI = new UINavigationItem(item,false);
			tempUI.setName("<b>"+ counter+ ":</b> " + tempUI.getName());
			if(proposal.getType().getwRoleStatus()) {
				if (counter == pos)
					tempUI.setActive(true);
			} else {
				if (counter == (pos-1))
					tempUI.setActive(true);
			}
			uiItems.add(tempUI);
			counter++;
		}

		return uiItems;
	}

	@Override
	public List<DBNavigationItem> getEndpointsForProposal(final ProjectProposal proposal) {
		return wizardRepo.findByType(proposal.getType());
	}

	@Override
	public void instantiateWhenNecessary(final ProjectProposal proposal, final ServiceDefinition sd) {
		if(serviceMgmt.getServiceInstance(proposal.getProject(),sd) == null) {
			try {
				serviceMgmt.instantiateService(proposal.getProject(),sd);
			} catch(IOException e) {
				logger.warn("Creation of remote instance failed with: " + e.getMessage());
				logger.error("Creation of remote instance failed with: ", e);
			}
			for( DBNavigationItem item : wizardRepo.findBySD(sd,proposal.getType())) {
				final WizardConfigurationEntry wce = wizardRepo.findByDBNIandType(item.getId(),proposal.getType().getId());
				if(wce.getInstantiation() != null)
					requestService.processInitiationRequest(serviceMgmt.getServiceInstance(proposal.getProject(),sd), parseVariables(proposal,wce.getInstantiation()),item.getEndpoint());
			}
		}
	}

	@Override
	public void startWizard(final ProjectProposal proposal) {
		proposal.setInCreation(true);
		proposal.setUserStarted(users.getCurrentUser().getUsername());
		proposal.setDateStarted(new Date());
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "start", proposal.getClass().getSimpleName(), Long.toString(proposal.getId())));
	}

	@Override
	public void stopWizard(final ProjectProposal proposal) {
		proposal.setFinished(true);
		proposal.setInCreation(false);
		proposal.setUserStopped(users.getCurrentUser().getUsername());
		proposal.setDateFinished(new Date());
		proposal.getProject().setInConfig(false);
		notifyFranchiseAdministrator(proposal);
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "stop", proposal.getClass().getSimpleName(), Long.toString(proposal.getId())));
	}

	@Override
	public List<String> getServiceNames(final ProjectProposal proposal) {
		final Set<ServiceDefinition> uniqueServicenames = new HashSet<>();
		for(DBNavigationItem item : wizardRepo.findByType(proposal.getType())) {
			uniqueServicenames.add(item.getServicedefinition());
		}
		final List<String> servicenames = new ArrayList<>();
		for(ServiceDefinition def : uniqueServicenames) {
			servicenames.add(def.getName());
		}
		return servicenames;
	}

	@Override
	public List<String> getServiceEndpointNames(final ProjectProposal proposal) {
		final Set<String> uniqueNames = new HashSet<>();
		for(DBNavigationItem item : wizardRepo.findByType(proposal.getType())) {
			uniqueNames.add(item.getName());
		}
		return new ArrayList<String>(uniqueNames);
	}

	private void notifyFranchiseAdministrator(ProjectProposal proposal) {

		final String emails = globalConf.getValue("wizard_notification_finished");
		final String[] recipients = emails.split(",");

		final String sub = "New OMiLAB Project created: \"" + proposal.getName() + "\"";
		final StringBuilder sb = new StringBuilder();
		sb.append("Dear Franchise Administrator,\n");
		sb.append("\nthe creation of the project based on the proposal of \"" + proposal.getName() + "\" from \"" + users.querySpecificUser(proposal.getUserid()).getFirstName() + " " + users.querySpecificUser(proposal.getUserid()).getLastName() + "\" has been finished and resulted in the creation of the project \"" + proposal.getProject().getName() + "\".\n\n");
		sb.append("It was started by \"" + proposal.getUserStarted() + "\" and finished by the user \"" + proposal.getUserStopped()  + "\". \n");
		Format formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		String start = formatter.format(proposal.getDateStarted());
		String end = formatter.format(proposal.getDateFinished());
		sb.append("It was started at \"" + start + "\" and finished at \"" + end  + "\". \n");
		sb.append("It can be reached directly at \"" + env.getProperty("app.url") + "/content/" + proposal.getProject().getUrlidentifier() + "\" and is now publicly available. \n");

		sb.append("\nBest regards,\n");
		sb.append("OMiLAB Notification System");
		for(final String rec : recipients) {
			sendMail(rec, sub, sb.toString());
		}
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

	private String getMessage(final String key, final Locale locale) {
		final MessageSource messageSource = (MessageSource) ctx.getBean("messageSource");
		try {
			return messageSource.getMessage(key, null, locale);
		} catch(NoSuchMessageException e) {
			return messageSource.getMessage(key, null, Locale.ENGLISH);
		}
	}

	@Override
	public WizardConfigurationEntry getByDBNIAndType(final Long dbni, final Long type) {
		return wizardRepo.findByDBNIandType(dbni,type);
	}

	private String repl(final String input, final String var, final String val) {
		if(val != null) {
			return input.replace(var,val);
		} else {
			return input.replace(var,"$null");
		}
	}

	@Override
	public String parseVariables(final ProjectProposal proposal, final String entry) {
		LDAPUser user = users.querySpecificUser(users.getCurrentUser().getUsername());
		String out = entry;

		Map<String,String> variables = new HashMap<String,String>();
		variables.put("$USERNAME",users.getCurrentUser().getUsername());
		variables.put("$EMAIL",user.getEmailAddress());
		variables.put("$NAME",proposal.getName());
		variables.put("$ABSTRACT",proposal.getProjectabstract());
		if(proposal.getDateStarted() != null)
			variables.put("$DATESTARTED",proposal.getDateStarted().toString());
		variables.put("$ACCEPTEDUSER",proposal.getUserAccepted());
		variables.put("$USERSTARTED",proposal.getUserStarted());
		if(proposal.getPos() != null)
			variables.put("$POS",proposal.getPos().toString());
		variables.put("$TYPENAME",proposal.getType().getName());
		variables.put("$TYPEDESC",proposal.getType().getDescription());
		variables.put("$TYPEOVERLAY",proposal.getType().getOverlay());
		variables.put("$FIRSTNAME",user.getFirstName());
		variables.put("$LASTNAME",user.getLastName());
		variables.put("$USERAFFILIATION",user.getOrganization());
		for (Map.Entry<String, String> variable : variables.entrySet()) {
			out = repl(out,variable.getKey(),variable.getValue());
		}
		if(proposal.getProject() != null) {
				out = ProjectServiceImpl.parseProjectStrings(out,proposal.getProject(), this.env.getProperty("app.url"));
		}
		return out;
	}

	@Override
	public String getCurrentStatus(final ProjectProposal proposal, final Locale locale) {
		if(!proposal.getFinished()) {
			if (proposal.getProject() == null) {
				return getMessage("franchise.types.proposals.status1", locale);
			}
			if (proposal.getPos().equals(0)) {
				return getMessage("franchise.types.proposals.status4", locale);
			}
			if(proposal.getPos() > 0) {
				return getMessage("franchise.types.proposals.status5", locale) + proposal.getPos() + " : " + currentStep(proposal,locale);
			}
			if(proposal.getPos().equals(-1)) {
				return getMessage("franchise.types.proposals.status6", locale);
			}
		} else {
			return getMessage("franchise.types.proposals.status3", locale) + " <a href=\"" + env.getProperty("app.url") + "/content/" + proposal.getProject().getUrlidentifier()  + "\"> " + proposal.getProject().getName() + "</a>";
		}
		return getMessage("franchise.types.proposals.error", locale);
	}

	private String currentStep(ProjectProposal proposal, final Locale locale) {
		List<DBNavigationItem> items = wizardRepo.findByType(proposal.getType());
		if(items == null || items.size() < 0) {
			return getMessage("franchise.types.proposals.error", locale);
		}
		int cPos = proposal.getPos()-1;
		if(items.size() == cPos) {
			return getMessage("franchise.types.proposals.lastpage", locale);
		}
		return items.get(cPos).getName();
	}
}
