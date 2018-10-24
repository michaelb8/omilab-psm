// File:         WizardController.java
// Created:      01.02.16
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2016 by OMiLAB.ORG
//

package org.omilab.psm.web;

import org.omilab.psm.model.db.*;
import org.omilab.psm.model.wrapper.GenericServiceContent;
import org.omilab.psm.repo.GenericProjectRepository;
import org.omilab.psm.service.ProjectService;
import org.omilab.psm.service.RequestorService;
import org.omilab.psm.service.ServiceManagementService;
import org.omilab.psm.service.WizardManagement;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/wizard")
@Transactional
@PreAuthorize("isAuthenticated()")
@Scope("session")
public class WizardController {

	private static final Logger logger = LoggerFactory.getLogger(WizardController.class);

	private final WizardManagement wizardMgmt;
	private final ProjectService projects;
	private final RequestorService requestService;
	private final ServiceManagementService serviceMgmt;
	private final Environment env;
	private final GenericProjectRepository projRepo;

	String sessionUUID;

	@Autowired
	public WizardController(final WizardManagement wizardMgmt, final ProjectService projects, final RequestorService requestService,
							final ServiceManagementService serviceMgmt, final Environment env, final GenericProjectRepository projRepo) {
		this.wizardMgmt = wizardMgmt;
		this.projects = projects;
		this.requestService = requestService;
		this.serviceMgmt = serviceMgmt;
		this.env = env;
		this.projRepo = projRepo;
	}

	@SuppressWarnings("unused")
	@RequestMapping("/start")
	public ModelAndView startWizard(final @RequestParam(value = "proposaluuid",defaultValue = "missing") String uuid,
									final HttpServletRequest request) {
		ProjectProposal proposal = projects.getProposalbyUUID(uuid);
		final ModelAndView mav = new ModelAndView("wizard/start", "proposal", proposal);
		mav.addObject("id",uuid);
		if( proposal == null || proposal.getAcceptedStatus() == null || proposal.getFinished() || !proposal.getAcceptedStatus()) {
			mav.addObject("failed","true");
			return mav;
		}
		if(proposal.getPos() != null)
			return new ModelAndView("redirect:/wizard/doNext?history=true&proposaluuid=" + uuid);
		if(proposal.getProject() != null)
			return new ModelAndView("redirect:/wizard/doNext?history=true&proposaluuid=" + uuid);


		mav.addObject("wizardnavigation",wizardMgmt.getStepsForProposal(proposal,1,request.getLocale()));
		mav.addObject("servicenames",wizardMgmt.getServiceNames(proposal));
		mav.addObject("serviceendpoints",wizardMgmt.getServiceEndpointNames(proposal));
		return mav;
	}

	@RequestMapping("/project")
	public ModelAndView createRealProject(final @RequestParam(value = "proposaluuid",defaultValue = "missing") String uuid,
										  final @RequestParam(value = "current", defaultValue = "step1") String step,
									      final @RequestParam(value = "error", defaultValue = "false") Boolean error,
										  final HttpServletRequest request) {
		ProjectProposal proposal = projects.getProposalbyUUID(uuid);
		final ModelAndView mav = new ModelAndView("wizard/createproject", "proposal", proposal);
		mav.addObject("id", uuid);
		if (proposal == null || proposal.getAcceptedStatus() == null || proposal.getFinished() || !proposal.getAcceptedStatus()) {
			mav.addObject("failed", "true");
			return mav;
		}
		//proposal.setPos(-1);
		if (proposal.getPos() != null)
			return new ModelAndView("redirect:/wizard/doNext?history=true&proposaluuid=" + uuid);

		//do not allow the creation of multiple projects with one key
		if (proposal.getProject() != null && (!step.equals("step2"))) {
			return new ModelAndView("redirect:/wizard/role?proposaluuid=" + uuid);
		}

		final List<String> franchiseKeywords = new ArrayList<>();
		for(Keyword keyword : projects.getAllKeywords()) {
			franchiseKeywords.add(keyword.getContent());
		}
		mav.addObject("franchisekeywords", franchiseKeywords);
		if(step.equals("step2")) {
			mav.addObject("project",proposal.getProject());
		}


		mav.addObject("wizardnavigation",wizardMgmt.getStepsForProposal(proposal,2,request.getLocale()));
		return mav;
	}

	@RequestMapping("/editproject")
	public ModelAndView editProject(final @RequestParam(value = "proposaluuid",defaultValue = "missing") String uuid,
									final @RequestParam(value = "error", defaultValue = "false") Boolean error,
										  final HttpServletRequest request) {
		ProjectProposal proposal = projects.getProposalbyUUID(uuid);
		//proposal.setPos(-1);
		final ModelAndView mav = new ModelAndView("wizard/editproject", "proposal", proposal);
		mav.addObject("id",uuid);
		if( proposal == null || proposal.getAcceptedStatus() == null || proposal.getFinished() || !proposal.getAcceptedStatus()) {
			mav.addObject("failed","true");
			return mav;
		}
		if(proposal.getPos() == null || proposal.getProject() == null)
			return new ModelAndView("redirect:/wizard/doNext?history=true&proposaluuid=" + uuid);
		mav.addObject("project",proposal.getProject());



		final List<String> franchiseKeywords = new ArrayList<>();
		for(Keyword keyword : projects.getAllKeywords()) {
			franchiseKeywords.add(keyword.getContent());
		}
		final StringBuilder projectKeywords = new StringBuilder();
		final List<Keyword> keywords = projects.getKeywordByProject(proposal.getProject());
		for(Keyword keyword : keywords) {
			projectKeywords.append(keyword.getContent());
			projectKeywords.append(",");
		}
		mav.addObject("franchisekeywords", franchiseKeywords);
		mav.addObject("keywords", projectKeywords.toString());

		mav.addObject("wizardnavigation",wizardMgmt.getStepsForProposal(proposal,2,request.getLocale()));
		return mav;
	}


	@SuppressWarnings("unused")
	@RequestMapping(value = "/processNewProject", method = RequestMethod.POST)
	public ModelAndView processNewProject(final @RequestParam(value = "name") String name,
											final @RequestParam(value = "abbreviation") String abbreviation,
											final @RequestParam(value = "urlidentifier") String urlidentifier,
											final @RequestParam(value = "tags") String tags,
											final @RequestParam(value = "proposaluuid") String uuid,
										    HttpServletRequest request,
											final RedirectAttributes redirect) {
		ProjectProposal proposal = projects.getProposalbyUUID(uuid);
		if( proposal == null || proposal.getAcceptedStatus() == null || proposal.getFinished() || !proposal.getAcceptedStatus()) {
			new ModelAndView("redirect:/wizard/project?proposaluuid=" + uuid);
		}
		//do not allow the creation of multiple projects with one key
		if(proposal != null && proposal.getProject() != null) {
			return new ModelAndView("redirect:/wizard/doNext?proposaluuid=" + uuid);
		}

		request.getSession().setAttribute("wizname",name);
		request.getSession().setAttribute("wizabbreviation",abbreviation);
		request.getSession().setAttribute("wizurlidentifier",urlidentifier);
		request.getSession().setAttribute("wizkeywords",tags);

		if(projRepo.findByName(name) != null) {
			return new ModelAndView("redirect:/wizard/project?current=step1&exists=name&error=true&proposaluuid=" + uuid);
		}
		if(projRepo.findByAbbreviation(abbreviation) != null) {
			return new ModelAndView("redirect:/wizard/project?current=step1&exists=abbr&error=true&proposaluuid=" + uuid);
		}
		if(projRepo.findByUrlidentifier(urlidentifier) != null) {
			return new ModelAndView("redirect:/wizard/project?current=step1&exists=url&error=true&proposaluuid=" + uuid);
		}

		AbstractProject p = projects.createProject(name,abbreviation,urlidentifier,proposal);
		if( p != null && proposal != null) {
			proposal.setProject(p);
			p.setProjecttype(proposal.getType());
		} else logger.error("Inconsistent internal state! Panic! Create a new proposal and check type configuration...");
		List<String> itemsString = Arrays.asList(tags.split("\\s*,\\s*"));
		projects.updateKeywords(p, itemsString);
		try {
			if(proposal != null && proposal.getType() != null && proposal.getType().getwRepoStatus())
				serviceMgmt.instantiateSpecialService(proposal.getProject(),"repo");
		} catch (IOException e) {
			logger.debug("Failed to instantiate repository for: " + proposal.getName() + ". Please check connection to FileManager.");
		}
		createRoleRepo(proposal);
		return new ModelAndView("redirect:/wizard/project?current=step2&proposaluuid=" + uuid);
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processEditAdditional", method 	= RequestMethod.POST)
	public ModelAndView processAdditionalProjectModification(@RequestParam Map<String,String> allRequestParams,
										  final @RequestParam(value = "proposaluuid") String uuid,
										  final HttpServletRequest request,
										  final RedirectAttributes redirect) {
		ProjectProposal proposal = projects.getProposalbyUUID(uuid);
		if( proposal == null || proposal.getAcceptedStatus() == null || proposal.getFinished() || !proposal.getAcceptedStatus()) {
			new ModelAndView("redirect:/wizard/project?proposaluuid=" + uuid);
		}
		for (Map.Entry<String,String> entry : allRequestParams.entrySet()) {
			request.getSession().setAttribute("wiz" + entry.getKey(),entry.getValue());
		}

		try {
			if(proposal != null && proposal.getProject() != null )
				projects.changeProjectAttributesWizard(allRequestParams,proposal.getProject());
			else
				logger.error("Inconsistent internal state! Panic! Create a new proposal and check type configuration...");
		} catch (TransactionSystemException e) {
			return new ModelAndView("redirect:/wizard/project?current=step2&ferror=true&proposaluuid=" + uuid);
		}

		return new ModelAndView("redirect:/wizard/doNext?proposaluuid=" + uuid);
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processEdit", method 	= RequestMethod.POST)
	public ModelAndView processProjectModification(@RequestParam Map<String,String> allRequestParams,
															 final @RequestParam(value = "proposaluuid") String uuid,
												    		 HttpServletRequest request,
															 final RedirectAttributes redirect) {
		ProjectProposal proposal = projects.getProposalbyUUID(uuid);
		if( proposal == null || proposal.getAcceptedStatus() == null || proposal.getFinished() || !proposal.getAcceptedStatus()) {
			new ModelAndView("redirect:/wizard/project?proposaluuid=" + uuid);
		}

		for (Map.Entry<String,String> entry : allRequestParams.entrySet()) {
			request.getSession().setAttribute("wiz" + entry.getKey(),entry.getValue());
		}

		if(projRepo.findByName(allRequestParams.get("name")) != null && (!proposal.getProject().getName().equals(allRequestParams.get("name")))) {
			return new ModelAndView("redirect:/wizard/editproject?exists=name&error=true&proposaluuid=" + uuid);
		}
		if(projRepo.findByAbbreviation(allRequestParams.get("abbreviation")) != null && (!proposal.getProject().getAbbreviation().equals(allRequestParams.get("abbreviation")))) {
			return new ModelAndView("redirect:/wizard/editproject?exists=abbr&error=true&proposaluuid=" + uuid);
		}
		if(projRepo.findByUrlidentifier(allRequestParams.get("urlidentifier")) != null && (!proposal.getProject().getUrlidentifier().equals(allRequestParams.get("urlidentifier")))) {
			return new ModelAndView("redirect:/wizard/editproject?exists=url&error=true&proposaluuid=" + uuid);
		}

		try {
			if( proposal != null && proposal.getProject() != null )
				projects.changeProjectAttributesWizard(allRequestParams,proposal.getProject());
			else
				logger.error("Inconsistent internal state! Panic! Create a new proposal and check type configuration...");
		} catch (TransactionSystemException e) {
			return new ModelAndView("redirect:/wizard/project?current=step2&ferror=true&proposaluuid=" + uuid);
		}

		return new ModelAndView("redirect:/wizard/doNext?proposaluuid=" + uuid);
	}


	@RequestMapping("/role")
	public ModelAndView doRoles(final @RequestParam(value = "proposaluuid",defaultValue = "missing") String uuid,
							   final @RequestParam Map<String, String> allRequestParams,
							   final HttpServletRequest request) {
		ProjectProposal proposal = projects.getProposalbyUUID(uuid);
		final ModelAndView mav = new ModelAndView("wizard/role", "proposal", proposal);
		mav.addObject("id",uuid);
		if( proposal == null || proposal.getAcceptedStatus() == null) {
			mav.addObject("failed","true");
			return mav;
		}
		if(proposal.getFinished() || !proposal.getAcceptedStatus()){
			mav.addObject("failed","true");
			return mav;
		}
		if(proposal.getProject() == null) {
			return new ModelAndView("redirect:/wizard/project?proposaluuid=" + uuid);
		}
		if(0 != proposal.getPos())
			return new ModelAndView("redirect:/wizard/doNext?history=true&proposaluuid=" + uuid);

		final int step = 0;

		List<DBNavigationItem> steps = wizardMgmt.getEndpointsForProposal(proposal);
		allRequestParams.remove("view");
		allRequestParams.remove("proposaluuid");

		final GenericServiceContent gsc = requestService.processAdminRequest(serviceMgmt.getServiceInstanceOfSpecialService(proposal.getProject(),"role"),allRequestParams,"rolemanagement");
		mav.addObject("wrapper",gsc);
		mav.addObject("wizardnavigation",wizardMgmt.getStepsForProposal(proposal,3,request.getLocale()));
		final int nextstep = step+1;
		mav.addObject("nextstep", "step" + nextstep);
		if(step+1 == steps.size())
			mav.addObject("finished",true);
		else
			mav.addObject("finished",false);
		return mav;
	}

	@RequestMapping("/{stepnumber}")
	public ModelAndView doStepRedirect(final @RequestParam(value = "proposaluuid",defaultValue = "missing") String uuid,
							   final @PathVariable("stepnumber") String url,
							   final @RequestParam Map<String, String> allRequestParams) {
		return new ModelAndView("redirect:/wizard/" + url + "/admin?proposaluuid=" + uuid);
	}

	@RequestMapping("/doNext")
	public ModelAndView doNext(final @RequestParam(value = "proposaluuid",defaultValue = "missing") String uuid,
							   final HttpServletRequest request,
							   final @RequestParam(value = "history",defaultValue = "false") Boolean hist) {
		ProjectProposal proposal = projects.getProposalbyUUID(uuid);
		if(proposal.getProject() == null) {
			wizardMgmt.startWizard(proposal);
			return new ModelAndView("redirect:/wizard/project?current=step1&proposaluuid=" + uuid);
		}
		Enumeration keys = request.getSession().getAttributeNames();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			if (key.contains("wiz"))
				request.getSession().removeAttribute(key);
		}
	/*	if(proposal.getProject() != null && (proposal.getProject().getProjectlogo() == null || proposal.getProject().getProjectmodel() == null)) {
			proposal.setPos(-1);
			createRoleRepo(proposal);
			return new ModelAndView("redirect:/wizard/editproject?proposaluuid=" + uuid);
		} */
		if(proposal.getPos() != null && proposal.getPos().equals(-1)) {
			if(proposal.getType().getwRoleStatus()) {
				proposal.setPos(0);
				return new ModelAndView("redirect:/wizard/role?proposaluuid=" + uuid);
			}
			else {
				proposal.setPos(null);
			}
		}
		if(proposal.getProject() != null && proposal.getPos() == null) {
			proposal.setPos(0);
			createRoleRepo(proposal);

			if(proposal.getType().getwRoleStatus()) {
				return new ModelAndView("redirect:/wizard/role?proposaluuid=" + uuid);
			}
			else {
				return new ModelAndView("redirect:/wizard/doNext?proposaluuid=" + uuid);
			}
		}
		if(!hist) {
			proposal.setPos(proposal.getPos() + 1);
		}
		if(proposal.getPos() == 0 && hist) {
			return new ModelAndView("redirect:/wizard/role?proposaluuid=" + uuid);
		}
		List<DBNavigationItem> steps = wizardMgmt.getEndpointsForProposal(proposal);
		if((steps.size()+1) == (proposal.getPos())) {
			return new ModelAndView("redirect:/wizard/finish?proposaluuid=" + uuid);
		}
		return new ModelAndView("redirect:/wizard/step" + proposal.getPos() + "?proposaluuid=" + uuid);
	}

	private void createRoleRepo(ProjectProposal proposal) {
		try {
			if(serviceMgmt.getServiceInstanceOfSpecialService(proposal.getProject(),"role") == null && proposal.getType().getwRoleStatus()) {
				serviceMgmt.instantiateSpecialService(proposal.getProject(), "role");
				if (proposal.getType().getwRoleString() != null) {
					requestService.processInitiationRequest(serviceMgmt.getServiceInstanceOfSpecialService(proposal.getProject(), "role"), wizardMgmt.parseVariables(proposal, proposal.getType().getwRoleString()), "rolemanagement");
				}
			}
			if(proposal.getType().getwRepoStatus() && serviceMgmt.getServiceInstanceOfSpecialService(proposal.getProject(),"repo") == null) {
				serviceMgmt.instantiateSpecialService(proposal.getProject(), "repo");
				if (proposal.getType().getwRepoString() != null) {
					requestService.processInitiationRequest(serviceMgmt.getServiceInstanceOfSpecialService(proposal.getProject(), "repo"), wizardMgmt.parseVariables(proposal, proposal.getType().getwRepoString()), "repo");
				}
			}
		} catch(Exception e) {
			logger.error("Error in configuring RoleService/Repostory: " + e.getMessage());
			logger.debug("Error in configuring RoleService/Repostory: ", e);
		}
	}

	@RequestMapping("/doPrevious")
	public ModelAndView doPrevious(final @RequestParam(value = "proposaluuid",defaultValue = "missing") String uuid,
							   final @RequestParam(value = "history",defaultValue = "false") Boolean hist) {
		ProjectProposal proposal = projects.getProposalbyUUID(uuid);
		if(proposal.getPos() > 0)
			proposal.setPos(proposal.getPos()-1);
		else {
			proposal.setPos(-1);
			return new ModelAndView("redirect:/wizard/editproject?proposaluuid=" + uuid);
		}
		if(proposal.getPos().equals(0) && proposal.getType().getwRoleStatus())
			return new ModelAndView("redirect:/wizard/role?proposaluuid=" + uuid);
		if(proposal.getPos().equals(0) && (!proposal.getType().getwRoleStatus())) {
			return new ModelAndView("redirect:/wizard/editproject?proposaluuid=" + uuid);
		}

		return new ModelAndView("redirect:/wizard/step" + proposal.getPos() + "?proposaluuid=" + uuid);
	}


	@RequestMapping(value = "/{stepnumber}/admin", method = {RequestMethod.GET, RequestMethod.POST})
	public ModelAndView doStep(@RequestParam(value = "proposaluuid",defaultValue = "missing") String uuid,
							   final @PathVariable("stepnumber") String url,
							   final @RequestParam Map<String, String> allRequestParams,
							   final HttpServletRequest request) {
		if(!uuid.equals("missing"))
			sessionUUID = uuid;
		else
			uuid = sessionUUID;
		ProjectProposal proposal = projects.getProposalbyUUID(uuid);
		final ModelAndView mav = new ModelAndView("wizard/step", "proposal", proposal);
		mav.addObject("id",uuid);
		if( proposal == null || proposal.getAcceptedStatus() == null) {
			mav.addObject("failed","true");
			return mav;
		}
		if(proposal.getFinished() || !proposal.getAcceptedStatus()){
			mav.addObject("failed","true");
			return mav;
		}
		final int currentStep = Integer.parseInt(url.replace("step", "")) - 1;
		if(proposal.getProject() == null) {
			return new ModelAndView("redirect:/wizard/doNext?history=true&proposaluuid=" + uuid);
		}
		if(proposal.getPos() == null)
			return new ModelAndView("redirect:/wizard/doNext?history=true&proposaluuid=" + uuid);
		if((currentStep+1) != proposal.getPos())
			return new ModelAndView("redirect:/wizard/doNext?history=true&proposaluuid=" + uuid);

		List<DBNavigationItem> steps = wizardMgmt.getEndpointsForProposal(proposal);

		allRequestParams.remove("view");
		allRequestParams.remove("proposaluuid");
		wizardMgmt.instantiateWhenNecessary(proposal,steps.get(currentStep).getServicedefinition());

		if(!serviceMgmt.checkEnabled(proposal.getProject(),steps.get(currentStep).getServicedefinition(),steps.get(currentStep).getEndpoint()))
			serviceMgmt.activateNavigation(proposal.getProject(),steps.get(currentStep));

		final GenericServiceContent gsc = requestService.processAdminRequest(serviceMgmt.getServiceInstance(proposal.getProject(),steps.get(currentStep).getServicedefinition()), allRequestParams,steps.get(currentStep).getEndpoint());
		mav.addObject("wrapper",gsc);
		mav.addObject("wizardnavigation",wizardMgmt.getStepsForProposal(proposal,currentStep+4,request.getLocale()));
		return mav;
	}

	@SuppressWarnings("unused")
	@RequestMapping("/finish")
	public ModelAndView finishWizard(final @RequestParam(value = "proposaluuid",defaultValue = "missing") String uuid) {
		ProjectProposal proposal = projects.getProposalbyUUID(uuid);
		final ModelAndView mav = new ModelAndView("wizard/finish", "proposal", proposal);
		mav.addObject("id",uuid);
		if( proposal == null || proposal.getAcceptedStatus() == null || proposal.getFinished() || !proposal.getAcceptedStatus()) {
			mav.addObject("failed","true");
			return mav;
		}

		mav.addObject("projectlink", env.getProperty("app.url") + "/content/" + proposal.getProject().getUrlidentifier());
		mav.addObject("servicenames",wizardMgmt.getServiceNames(proposal));
		mav.addObject("serviceendpoints",wizardMgmt.getServiceEndpointNames(proposal));
		return mav;
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processFinish")
	public ModelAndView processFinishProject(final @RequestParam(value = "proposaluuid") String uuid,
										  final RedirectAttributes redirect) {
		final ProjectProposal proposal = projects.getProposalbyUUID(uuid);
		if( proposal == null || proposal.getAcceptedStatus() == null || proposal.getFinished() || !proposal.getAcceptedStatus()) {
			new ModelAndView("redirect:/wizard/project?proposaluuid=" + uuid);
		}

		this.sessionUUID = null;

		wizardMgmt.stopWizard(proposal);

		if(proposal != null && proposal.getProject() != null)
			return new ModelAndView("redirect:" + env.getProperty("app.url") + "/content/" + proposal.getProject().getUrlidentifier());
		return new ModelAndView("redirect:/");

	}

}
