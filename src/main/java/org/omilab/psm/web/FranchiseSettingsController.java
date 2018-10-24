// File:         MainController.java
// Created:      14.02.2015
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.web;

import org.hibernate.exception.ConstraintViolationException;
import org.omilab.psm.model.db.*;
import org.omilab.psm.model.wrapper.PageWrapper;
import org.omilab.psm.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.env.Environment;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


@Controller
@PreAuthorize("isAuthenticated() and hasPermission('franchise', 'franchiseadmin')")
@RequestMapping("/settings")
@SuppressWarnings("unused")
public class FranchiseSettingsController {

	private static final Logger logger = LoggerFactory.getLogger(FranchiseSettingsController.class);

	private static final String DEFAULT_WIZARD_JSON = "{\n" +
			"  \"params\": {\n" +
			"    \"action\": \"example\"\n" +
			"  },\n" +
			"  \"roles\": [\"PROJECT_OWNER\"\n" +
			"  ],\n" +
			"  \"username\": \"$USERNAME\"\n" +
			"}";

	private static final int SERVICE_ELEMENTS_PER_PAGE = 5;

	private final ServiceDefinitionService services;

	private final ServiceManagementService serviceMgmt;

	private final UserService users;

	private final HeaderManagementService headers;

	private final ProjectService projects;

	private final ProjectTypeService projectTypes;

	private final MainNavigationManagementService navigationService;

	private final Environment env;

	private final GlobalConfigurationService globalconf;

	private final WizardManagement wizardMgmt;

	private final GlobalNetworkService globalnetService;

	private final ApplicationContext ctx;

	@Autowired
	public FranchiseSettingsController(ServiceDefinitionService services, ServiceManagementService serviceMgmt,
									   UserService users, HeaderManagementService headers, Environment env,
									   ApplicationContext ctx, ProjectTypeService projectTypes,
									   ProjectService projects, MainNavigationManagementService navigationService,
									   GlobalConfigurationService globalconf, WizardManagement wizardMgmt,
									   GlobalNetworkService globalnetService) {
		this.services = services;
		this.serviceMgmt = serviceMgmt;
		this.users = users;
		this.headers = headers;
		this.env = env;
		this.ctx = ctx;
		this.projectTypes = projectTypes;
		this.projects = projects;
		this.navigationService = navigationService;
		this.globalconf = globalconf;
		this.wizardMgmt = wizardMgmt;
		this.globalnetService = globalnetService;
	}

	private String getMessage(final String key, final Locale locale) {
		final MessageSource messageSource = (MessageSource) ctx.getBean("messageSource");
		try {
			return messageSource.getMessage(key, null, locale);
		} catch(NoSuchMessageException e) {
			return messageSource.getMessage(key, null, Locale.ENGLISH);
		}
	}

	@SuppressWarnings("unused")
	@RequestMapping("")
	public ModelAndView generateSettings(final @RequestParam(value = "page", defaultValue = "1") int pageNumber,
										 final @RequestParam(value= "delete", defaultValue = "0") String typeid,
										 final @RequestParam(value= "showOpen", defaultValue = "0") String stypeid,
										 final HttpServletRequest request) {

		final PageRequest pagerequest = new PageRequest(pageNumber - 1, SERVICE_ELEMENTS_PER_PAGE);
		final Page<ServiceDefinition> page = services.getServiceDefinitionPage(pagerequest);

		final ModelAndView mav = new ModelAndView("franchisesettings/main", "services", page);
		mav.addObject("pages", new PageWrapper<>(page));
		mav.addObject("menupoints", services.getAllNavigationItems());
		mav.addObject("headers", headers.getHeader());
		mav.addObject("projects", projects.getAllProjects());
		mav.addObject("sessions",UserService.activeUsers);
		mav.addObject("projecttypes",projectTypes.getTypes());
		mav.addObject("overlay", projectTypes.getAvailableOverlays());
		mav.addObject("navigationmenu", navigationService.getMenu());
		mav.addObject("wizard_accepted_template",globalconf.getValue("wizard_accepted_template"));
		mav.addObject("wizard_rejected_template",globalconf.getValue("wizard_rejected_template"));
		mav.addObject("wizard_notification_new",globalconf.getValue("wizard_notification_new"));
		mav.addObject("wizard_notification_finished",globalconf.getValue("wizard_notification_finished"));
		if( !(typeid.equals("undefined")) &&  (!typeid.equals("0"))) {
			mav.addObject("uerrorMessage", getMessage("franchise.types.removal.error2", request.getLocale()));
			mav.addObject("proposals",projectTypes.getUnfinishedProposals(projectTypes.getById(Long.parseLong(typeid))));
		}
		if((!(stypeid.equals("undefined")) &&  (!stypeid.equals("0")))) {
			mav.addObject("findOpen", "true");
			mav.addObject("proposals",projectTypes.getUnfinishedProposals(projectTypes.getById(Long.parseLong(stypeid))));
		}

		final Runtime runtime = Runtime.getRuntime();
		final long usemem = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
		final long freemem = runtime.freeMemory() / (1024 * 1024);
		final long totalmem = runtime.totalMemory() / (1024 * 1024);
		final long maxmem = runtime.maxMemory() / (1024 * 1024);

		mav.addObject("javaversion", System.getProperty("java.version"));
		mav.addObject("javahome", System.getProperty("java.home"));
		mav.addObject("javavendor", System.getProperty("java.vendor"));
		mav.addObject("osarch", System.getProperty("os.arch"));
		mav.addObject("osversion", System.getProperty("os.version"));
		mav.addObject("osname", System.getProperty("os.name"));
		mav.addObject("psmhome", System.getProperty("user.dir"));
		mav.addObject("psmuser", System.getProperty("user.name"));
		mav.addObject("usedmem", usemem);
		mav.addObject("freemem", freemem);
		mav.addObject("totalmem", totalmem);
		mav.addObject("maxmem", maxmem);
		mav.addObject("appname", env.getProperty("omilab.name"));
		mav.addObject("appversion", env.getProperty("omilab.version"));
		mav.addObject("appbuild", env.getProperty("omilab.buildTimestamp"));
		mav.addObject("commit", env.getProperty("omilab.commit"));

		return mav;
	}


	@SuppressWarnings("unused")
	@RequestMapping(value = "/ajaxProjectUsage", method = RequestMethod.GET)
	public ModelAndView generateUsage(final @RequestParam(value = "id") Long id) {

		final List<AbstractProject> projects = serviceMgmt.getServiceUsage(services.getServiceDefinition(id));
		final List<String> projectnames = new ArrayList<>();
		for(AbstractProject project : projects) {
			projectnames.add(project.getName());
		}

		return new ModelAndView("projectusage", "projects", projectnames);
	}



	@SuppressWarnings("unused")
	@RequestMapping(value = "/ajaxPTProjects", method = RequestMethod.GET)
	public ModelAndView generateProjectTypeProjects(final @RequestParam(value = "id") Long id) {
		ModelAndView mav = new ModelAndView("franchisesettings/ajaxProjectTypesProjectList", "projects", projects.getAllProjects());
		mav.addObject("projectusage",projectTypes.getProjectsForPT(id));
		mav.addObject("projecttype",projectTypes.getById(id));
		return mav;
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/ajaxPT", method = RequestMethod.GET)
	public ModelAndView generateProjectTypes(final @RequestParam(value = "id") Long id) {
		ModelAndView mav = new ModelAndView("franchisesettings/ajaxProjectTypes", "types", projectTypes.getTypes());
		mav.addObject("typesusage",navigationService.getProjectTypeUsage(id));
		return mav;
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/ajaxEndpoints", method = RequestMethod.GET)
	public ModelAndView generateEndpointOverview(final @RequestParam(value = "id") Long id) {

		List<DBNavigationItem> items = serviceMgmt.getAllMenus(services.getServiceDefinition(id));
		return new ModelAndView("franchisesettings/ajaxEndpoints", "endpoints", items);
	}


	@SuppressWarnings("unused")
	@RequestMapping(value = "/ajaxEndpoint", method = RequestMethod.GET)
	public ModelAndView generateEndpointEdit(final @RequestParam(value = "id") Long id) {

		DBNavigationItem item = serviceMgmt.getMenu(id);

		return new ModelAndView("franchisesettings/ajaxEndpointEdit", "endpoint", item);
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/ajaxProj", method = RequestMethod.GET)
	public ModelAndView generateProjects(final @RequestParam(value = "id") Long id) {
		ModelAndView mav = new ModelAndView("franchisesettings/ajaxProjects", "projects", projects.getAllProjects());
		mav.addObject("projusage",navigationService.getProjectUsage(id));
		return mav;
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/ajaxPTServices", method = RequestMethod.GET)
	public ModelAndView generateProjectTypeServices(final @RequestParam(value = "id") Long id) {

		ModelAndView mav = new ModelAndView("franchisesettings/ajaxProjectTypesServiceList", "services", services.getAllNavigationItems());
		mav.addObject("serviceusage",projectTypes.getServicesForPT(id));
		mav.addObject("projecttype",projectTypes.getById(id));
		return mav;
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/ajaxWConfEndpoints", method = RequestMethod.GET)
	public ModelAndView generateWizardConfEndpoint(final @RequestParam(value = "id") Long id) {

		ModelAndView mav = new ModelAndView("franchisesettings/ajaxWConfEndpoint", "services", services.getAllNavigationItems());
		mav.addObject("serviceusage",projectTypes.getServicesForPT(id));
		mav.addObject("wusage",projectTypes.getWServicesForPT(id));
		mav.addObject("projecttype",projectTypes.getById(id));
		return mav;
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/ajaxPTOverlay", method = RequestMethod.GET)
	public ModelAndView generateOverlayConf(final @RequestParam(value = "id") Long id) {

		ModelAndView mav = new ModelAndView("franchisesettings/ajaxPTOverlay", "overlay", projectTypes.getAvailableOverlays());
		mav.addObject("projecttype",projectTypes.getById(id));
		mav.addObject("coverlay",projectTypes.getById(id).getOverlay());
		return mav;
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/ajaxWConfInstantiation", method = RequestMethod.GET)
	public ModelAndView generateWizardConfInstantiation(@RequestParam(value = "id") String id,
														@RequestParam(value = "dbni") String dbni) {
		WizardConfigurationEntry wce;
		ProjectType pt = projectTypes.getById(Long.parseLong(id));
		ModelAndView mav = new ModelAndView("franchisesettings/ajaxWConfInstantiation", "projecttype", pt);
		mav.addObject("dbni",dbni.replace("C",""));
		String jsonContent;
		if( (!dbni.equals("Crole")) && (!dbni.equals("Crepo"))) {
			wce = wizardMgmt.getByDBNIAndType(Long.parseLong(dbni.replace("C","")),Long.parseLong(id));
			if(wce == null) {
				projectTypes.activateWCE(Long.parseLong(id),Long.parseLong(dbni.replace("C","")));
				wce = wizardMgmt.getByDBNIAndType(Long.parseLong(dbni.replace("C","")),Long.parseLong(id));
			}
			mav.addObject("wce",wce);
			jsonContent = wce.getInstantiation();
		} else {
			if(dbni.equals("Crole"))
				jsonContent = pt.getwRoleString();
			else
				jsonContent = pt.getwRepoString();
		}
		if(jsonContent == null)
			jsonContent  = DEFAULT_WIZARD_JSON;
		mav.addObject("jsoncontent",jsonContent);
		return mav;
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/ajaxProposals", method = RequestMethod.GET)
	public ModelAndView generateProposalOverview(final @RequestParam(value = "id") Long id) {
		List<ProjectProposal> items = projects.getProposalsForProjectType(projectTypes.getById(id));
		return new ModelAndView("franchisesettings/ajaxProposals", "proposals", items);
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/ajaxProposal", method = RequestMethod.GET)
	public ModelAndView generateProposal(final @RequestParam(value = "id") Long id,
										 final HttpServletRequest request) {
		ProjectProposal proposal = projects.getProposal(id);
		ModelAndView mav = new ModelAndView("franchisesettings/ajaxProposalEdit", "proposal", proposal);
		mav.addObject("user",users.querySpecificUser(proposal.getUserid()));
		mav.addObject("currenstatus",wizardMgmt.getCurrentStatus(proposal,request.getLocale()));
		return mav;
	}


	@SuppressWarnings("unused")
	@RequestMapping(value = "/processWizardGlobal", method = RequestMethod.POST)
	public ModelAndView processWizardGlobal(final @RequestParam(value = "templateaccepted") String acceptedtemp,
											final @RequestParam(value = "templaterejected") String rejectedtemp,
											final @RequestParam(value = "notificationnew") String newnot,
											final @RequestParam(value = "notificationfinish") String finishnot,
											final RedirectAttributes redirect) {

		globalconf.setKeyValue("wizard_accepted_template",acceptedtemp);
		globalconf.setKeyValue("wizard_rejected_template",rejectedtemp);
		globalconf.setKeyValue("wizard_notification_new",newnot);
		globalconf.setKeyValue("wizard_notification_finished",finishnot);

		return new ModelAndView("redirect:/settings?view=projecttypes");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processFooterAdaption", method = RequestMethod.POST)
	public ModelAndView processFooterAdaption(final @RequestParam(value = "footer") String footer,
											  final RedirectAttributes redirect) {
		globalconf.setKeyValue("footer",footer);
		return new ModelAndView("redirect:/settings?view=navigation");
	}


	@SuppressWarnings("unused")
	@RequestMapping(value = "/processOStatus", method = RequestMethod.POST)
	public ModelAndView processOverlay(final @RequestParam(value = "enabled") String enabled,
											final @RequestParam(value = "id") Long id,
											final RedirectAttributes redirect) {
		projectTypes.setOverlay(id,enabled);
		return new ModelAndView("redirect:/settings?view=projecttypes");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processNetworkRegistration", method = RequestMethod.POST)
	public ModelAndView processNetworkRegistration(final @RequestParam(value = "register") String reg,
											final RedirectAttributes redirect) {
		if(reg.equals("now"))
			try {
				globalnetService.register();
			} catch(Exception e) {
				logger.error("Could not contact OMiLAB HQ... " + e.getMessage());
				return new ModelAndView("redirect:/settings?view=franchises&cerror=true");
			}
		return new ModelAndView("redirect:/settings?view=franchises");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processNetworkSync", method = RequestMethod.POST)
	public ModelAndView processNetworkSync(final @RequestParam(value = "sync") String reg,
												   final RedirectAttributes redirect) {
		if(reg.equals("now"))
			try {
				globalnetService.sync();
			} catch(Exception e) {
				logger.error("Could not contact OMiLAB HQ... " + e.getMessage());
				return new ModelAndView("redirect:/settings?view=franchises&cerror=true");
			}
		return new ModelAndView("redirect:/settings?view=franchises");
	}
	@SuppressWarnings("unused")
	@RequestMapping(value = "/processNetworkRemoval", method = RequestMethod.POST)
	public ModelAndView processNetworkRemoval(final @RequestParam(value = "remove") String reg,
										   final RedirectAttributes redirect) {
		if(reg.equals("now"))
				globalnetService.remove();

		return new ModelAndView("redirect:/settings?view=franchises");
	}



	@SuppressWarnings("unused")
	@RequestMapping(value = "/processProposalDecision", method = RequestMethod.POST)
	public ModelAndView processProposalDecision(final @RequestParam(value = "id") Long id,
												final @RequestParam(value = "action") String action,
												final RedirectAttributes redirect) {

		if(action.equals("deny"))
			projects.deny(projects.getProposal(id));
		if(action.equals("approve"))
			projects.approve(projects.getProposal(id));

		return new ModelAndView("redirect:/settings?view=projecttypes");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processNavigationVisible", method = RequestMethod.POST)
	public ModelAndView processNavigationVisible(final @RequestParam(value = "id") Long id,
											 final RedirectAttributes redirect) {
		navigationService.toggleVisiblity(id);
		return new ModelAndView("redirect:/settings?view=navigation");
	}


	@SuppressWarnings("unused")
	@RequestMapping(value = "/processNavigationWizard", method = RequestMethod.POST)
	public ModelAndView processNavigationWizard(final @RequestParam(value = "id") Long id,
												final HttpServletRequest request,
												final RedirectAttributes redirect) {
		MainNavigationItem item = navigationService.getMNIForID(id);
		MainNavigationItemTypes itemT = (MainNavigationItemTypes)item;
		if(itemT.getTypes().size() < 1) {
			redirect.addFlashAttribute("errorMessage", getMessage("franchise.navigation.error.notypes", request.getLocale()));
			return new ModelAndView("redirect:/settings?view=navigation");
		}
		navigationService.toggleNewProject(id);
		return new ModelAndView("redirect:/settings?view=navigation");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processNavigationCarousel", method = RequestMethod.POST)
	public ModelAndView processNavigationCarousel(final @RequestParam(value = "id") Long id,
												 final RedirectAttributes redirect) {
		navigationService.toggleCarousel(id);
		return new ModelAndView("redirect:/settings?view=navigation");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processNavigationRemoval", method = RequestMethod.POST)
	public ModelAndView processNavigationRemoval(final @RequestParam(value = "id") Long id,
											 final RedirectAttributes redirect) {
		navigationService.delete(id);
		return new ModelAndView("redirect:/settings?view=navigation");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processNavigationOrder", method = RequestMethod.POST)
	public ModelAndView processNavigationOrder(final @RequestParam(value = "order") String order,
										       final RedirectAttributes redirect) {
		if(order.equals(""))
			return new ModelAndView("redirect:/settings?view=navigation");

		final List<String> itemsString = Arrays.asList(order.split("\\s*,\\s*"));
		final List<Long> itemsLong = new ArrayList<>();
		for(String item : itemsString) {
			itemsLong.add(Long.parseLong(item));
		}
		navigationService.changeOrder(itemsLong);
		return new ModelAndView("redirect:/settings?view=navigation");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processMNILinkCreation", method = RequestMethod.POST)
	public ModelAndView processNavigationLinkCreation(final @RequestParam(value = "name") String name,
											  final @RequestParam(value = "url") String url,
											  final HttpServletRequest request,
											  final RedirectAttributes redirect) {

		if(navigationService.getMNIForURL(url) != null) {
			redirect.addFlashAttribute("errorMessage", getMessage("franchise.navigation.error.exists", request.getLocale()));
			return new ModelAndView("redirect:/settings?view=navigation");
		}
		navigationService.createLink(name,url);
		return new ModelAndView("redirect:/settings?view=navigation");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processMNIHTMLCreation", method = RequestMethod.POST)
	public ModelAndView processNavigationHTMLCreation(final @RequestParam(value = "name") String name,
													  final @RequestParam(value = "url") String url,
													  final HttpServletRequest request,
													  final RedirectAttributes redirect) {

		if(navigationService.getMNIForURL(url) != null) {
			redirect.addFlashAttribute("errorMessage", getMessage("franchise.navigation.error.exists", request.getLocale()));
			return new ModelAndView("redirect:/settings?view=navigation");
		}
		navigationService.createHTML(name,url);
		return new ModelAndView("redirect:/settings?view=navigation");
	}


	@SuppressWarnings("unused")
	@RequestMapping(value = "/processMNIPTCreation", method = RequestMethod.POST)
	public ModelAndView processNavigationPTCreation(final @RequestParam(value = "name") String name,
													final @RequestParam(value = "url") String url,
													final @RequestParam(value = "caption") String caption,
													final @RequestParam(value = "label") String label,
													final @RequestParam(value= "enabled") String enabled,
													final HttpServletRequest request,
													final RedirectAttributes redirect) {
		if(navigationService.getMNIForURL(url) != null) {
			redirect.addFlashAttribute("errorMessage", getMessage("franchise.navigation.error.exists", request.getLocale()));
			return new ModelAndView("redirect:/settings?view=navigation");
		}
		final List<String> itemsString = Arrays.asList(enabled.split("\\s*,\\s*"));
		final List<Long> itemsLong = new ArrayList<>();
		if(!enabled.equals("")) {
			for(String item : itemsString) {
				itemsLong.add(Long.parseLong(item));
			}
		}
		navigationService.createTypes(name,url,itemsLong,caption,label);

		return new ModelAndView("redirect:/settings?view=navigation");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processMNIProjCreation", method = RequestMethod.POST)
	public ModelAndView processNavigationProjectCreation(final @RequestParam(value = "name") String name,
													  final @RequestParam(value = "url") String url,
													  final @RequestParam(value = "id") Long id,
														 final HttpServletRequest request,
													  final RedirectAttributes redirect) {
		if(navigationService.getMNIForURL(url) != null) {
			redirect.addFlashAttribute("errorMessage", getMessage("franchise.navigation.error.exists", request.getLocale()));
			return new ModelAndView("redirect:/settings?view=navigation");
		}
		navigationService.createProject(name,url,id);
		return new ModelAndView("redirect:/settings?view=navigation");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processMNILinkAdaption", method = RequestMethod.POST)
	public ModelAndView processNavigationLinkAdaption(final @RequestParam(value = "name") String name,
														 final @RequestParam(value = "url") String url,
														 final @RequestParam(value = "id") Long id,
													     final HttpServletRequest request,
														 final RedirectAttributes redirect) {
		if(navigationService.getMNIForURL(url) != null && (!navigationService.getMNIForID(id).getLink().equals(url))) {
			redirect.addFlashAttribute("errorMessage", getMessage("franchise.navigation.error.exists", request.getLocale()));
			return new ModelAndView("redirect:/settings?view=navigation");
		}
		navigationService.updateLink(id,name,url);
		return new ModelAndView("redirect:/settings?view=navigation");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processMNIHTMLAdaption", method = RequestMethod.POST)
	public ModelAndView processNavigationHTMLAdaption(final @RequestParam(value = "name") String name,
													  final @RequestParam(value = "url") String url,
													  final @RequestParam(value = "html") String html,
													  final @RequestParam(value = "id") Long id,
													  final HttpServletRequest request,
													  final RedirectAttributes redirect) {
		if(navigationService.getMNIForURL(url) != null && (!navigationService.getMNIForID(id).getLink().equals(url))) {
			redirect.addFlashAttribute("errorMessage", getMessage("franchise.navigation.error.exists", request.getLocale()));
			return new ModelAndView("redirect:/settings?view=navigation");
		}
		navigationService.updateHTML(id, name, url, html);
		return new ModelAndView("redirect:/settings?view=navigation");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processMNIPTAdaption", method = RequestMethod.POST)
	public ModelAndView processNavigationPTAdaption(final @RequestParam(value = "name") String name,
													  final @RequestParam(value = "url") String url,
													  final @RequestParam(value = "caption") String caption,
													  final @RequestParam(value = "label") String label,
													  final @RequestParam(value = "enabled") String enabled,
													  final @RequestParam(value = "id") Long id,
													  final HttpServletRequest request,
													  final RedirectAttributes redirect) {
		if(navigationService.getMNIForURL(url) != null && (!navigationService.getMNIForID(id).getLink().equals(url))) {
			redirect.addFlashAttribute("errorMessage", getMessage("franchise.navigation.error.exists", request.getLocale()));
			return new ModelAndView("redirect:/settings?view=navigation");
		}
		final List<String> itemsString = Arrays.asList(enabled.split("\\s*,\\s*"));
		final List<Long> itemsLong = new ArrayList<>();
		if(!enabled.equals("")) {
			for(String item : itemsString) {
				itemsLong.add(Long.parseLong(item));
			}
		}
		navigationService.updateTypes(id,name,url,itemsLong,caption,label);
		return new ModelAndView("redirect:/settings?view=navigation");
	}




	@SuppressWarnings("unused")
	@RequestMapping(value = "/processMNIProjAdaption", method = RequestMethod.POST)
	public ModelAndView processNavigationProjAdaption(final @RequestParam(value = "name") String name,
													  final @RequestParam(value = "url") String url,
													  final @RequestParam(value = "id") Long id,
													  final @RequestParam(value = "projid") Long projid,
													  final HttpServletRequest request,
													  final RedirectAttributes redirect) {
		if(navigationService.getMNIForURL(url) != null && (!navigationService.getMNIForID(id).getLink().equals(url))) {
			redirect.addFlashAttribute("errorMessage", getMessage("franchise.navigation.error.exists", request.getLocale()));
			return new ModelAndView("redirect:/settings?view=navigation");
		}
		navigationService.updateProject(id,name,url,projid);
		return new ModelAndView("redirect:/settings?view=navigation");
	}




	@SuppressWarnings("unused")
	@RequestMapping("/editpage")
	public ModelAndView generatePageEdit(final @RequestParam(value = "id") Long id) {
		return new ModelAndView("franchisesettings/editstatic","htmlelement", navigationService.getHTMLForID(id));
	}


	@SuppressWarnings("unused")
	@RequestMapping(value = "/processHeaderOrder", method = RequestMethod.POST)
	public ModelAndView processHeaderOrder(final @RequestParam(value = "order") String order,
										   final RedirectAttributes redirect) {
		if(order.equals(""))
			return new ModelAndView("redirect:/settings?view=carousel");

		final List<String> itemsString = Arrays.asList(order.split("\\s*,\\s*"));
		final List<Long> itemsLong = new ArrayList<>();
		for(String item : itemsString) {
			itemsLong.add(Long.parseLong(item));
		}
		headers.changeOrder(itemsLong);
		return new ModelAndView("redirect:/settings?view=carousel");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processHeaderVisible", method = RequestMethod.POST)
	public ModelAndView processHeaderVisible(final @RequestParam(value = "id") Long id,
											 final RedirectAttributes redirect) {
		headers.toggleVisibility(id);
		return new ModelAndView("redirect:/settings?view=carousel");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processHeaderAdaption", method = RequestMethod.POST)
	public ModelAndView processHeaderAdaption(final @RequestParam(value = "id") Long id,
											  @RequestParam(value = "content") String content,
											  final RedirectAttributes redirect) {
		if(content.startsWith(",")) {
			content = content.substring(1);
		}
		headers.update(content, id);
		return new ModelAndView("redirect:/settings?view=carousel");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processHeaderRemoval", method = RequestMethod.POST)
	public ModelAndView processHeaderRemoval(final @RequestParam(value = "id") Long id,
											 final RedirectAttributes redirect) {
		headers.delete(id);
		return new ModelAndView("redirect:/settings?view=carousel");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processHeaderStarter", method = RequestMethod.POST)
	public ModelAndView processHeaderStarter(final @RequestParam(value = "id") Long id,
											 final RedirectAttributes redirect) {
		headers.makeStarter(id);
		return new ModelAndView("redirect:/settings?view=carousel");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processHeaderCreation", method = RequestMethod.POST)
	public ModelAndView processHeaderCreation(final @RequestParam(value = "type") String type,
												   final @RequestParam(value = "content") String content,
												   final @RequestParam(value = "title") String title,
												   final RedirectAttributes redirect) {
		if(type.equals("HTML"))
			headers.addHTMLHeader(content, title);
		if(type.equals("IMAGE"))
			headers.addImageHeader(content, title);
		return new ModelAndView("redirect:/settings?view=carousel");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processEndpointCreation", method = RequestMethod.POST)
	public ModelAndView processEndpointCreation(final @RequestParam(value = "name") String name,
												 final @RequestParam(value= "endpoint") String endpoint,
												 final @RequestParam(value="mandatory") Boolean mandatory,
												 final @RequestParam(value="id") Long id,
												 final RedirectAttributes redirect) {

		serviceMgmt.createEndpoint(name,endpoint,mandatory,id);
		return new ModelAndView("redirect:/settings?view=service");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processEndpointAdaption", method = RequestMethod.POST)
	public ModelAndView processEndpointAdaption(final @RequestParam(value = "name") String name,
												final @RequestParam(value= "endpoint") String endpoint,
												final @RequestParam(value="mandatory") Boolean mandatory,
												final @RequestParam(value="id") Long id,
												final RedirectAttributes redirect) {

		if(name.equals("delete_this_item") && endpoint.equals("delete_this_item"))
			serviceMgmt.deleteEndpoint(id);
		else
			serviceMgmt.updateEndpoint(name,endpoint,mandatory,id);
		return new ModelAndView("redirect:/settings?view=service");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processProjectTypeCreation", method = RequestMethod.POST)
	public ModelAndView processProjectTypeCreation(final @RequestParam(value = "name") String name,
												   final @RequestParam(value = "description") String description,
												   final @RequestParam(value = "overlay") String overlay,
											  final RedirectAttributes redirect) {
		projectTypes.createType(name,description,overlay);
		return new ModelAndView("redirect:/settings?view=projecttypes");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processProjectTypeRemoval", method = RequestMethod.POST)
	public ModelAndView processProjectTypeRemoval(final @RequestParam(value = "id") Long id,
											  final RedirectAttributes redirect, final HttpServletRequest request) {

		final ProjectType pt = projectTypes.getById(id);
		if(projectTypes.getUnfinishedProposals(pt).size() > 0) {
			final StringBuilder sb = new StringBuilder();
			sb.append("<br>");
			for(ProjectProposal pp : projectTypes.getUnfinishedProposals(pt)) {
				sb.append("<a href=\"../wizard/start?proposaluuid=" + pp.getProposalID() + "\">" + pp.getName() + "</a> ");
				sb.append(",");
			}
			return new ModelAndView("redirect:/settings?view=projecttypes&delete=" + pt.getId());
		}
		if(pt.getProjects().size() > 0) {
			redirect.addFlashAttribute("errorMessage", getMessage("franchise.types.removal.error1", request.getLocale()));
			return new ModelAndView("redirect:/settings?view=projecttypes");
		}
		if(Integer.parseInt(projects.getNumberOfUnreadProposals(pt)) > 0) {
			redirect.addFlashAttribute("errorMessage", getMessage("franchise.types.removal.error3", request.getLocale()));
			return new ModelAndView("redirect:/settings?view=projecttypes");
		}
		projectTypes.removeType(pt);
		redirect.addFlashAttribute("successMessage", getMessage("franchise.types.removal.success", request.getLocale()));
		return new ModelAndView("redirect:/settings?view=projecttypes");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processProjectTypeBackAdaption", method = RequestMethod.POST)
	public ModelAndView processProjectTypeTileBackgroundAdaption( final @RequestParam(value = "tilebackground") String tilebackground,
													final @RequestParam(value = "id") Long id,
												    final RedirectAttributes redirect) {
		projectTypes.updateTileBackground(tilebackground,id);
		return new ModelAndView("redirect:/settings?view=projecttypes");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processProjectTypeFrontAdaption", method = RequestMethod.POST)
	public ModelAndView processProjectTypeTileForegroundAdaption( final @RequestParam(value = "tileforeground") String tileforeground,
													final @RequestParam(value = "id") Long id,
													final RedirectAttributes redirect) {
		projectTypes.updateTileForeground(tileforeground,id);
		return new ModelAndView("redirect:/settings?view=projecttypes");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processProjectTypeFull", method = RequestMethod.POST)
	public ModelAndView processProjectTypeFull(final @RequestParam(value = "id") Long id,
											   final RedirectAttributes redirect) {
		projectTypes.toggleFullHeader(id);
		return new ModelAndView("redirect:/settings?view=projecttypes");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processProjectTypeNavbar", method = RequestMethod.POST)
	public ModelAndView processProjectTypeNavbar(final @RequestParam(value = "id") Long id,
												  final RedirectAttributes redirect) {
		projectTypes.toggleNavigationBar(id);
		return new ModelAndView("redirect:/settings?view=projecttypes");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processProjectTypeProjects", method = RequestMethod.POST)
	public ModelAndView processProjectTypeProjects(final @RequestParam(value = "id") Long id,
												   final @RequestParam(value= "enabled") String enabled,
											  final RedirectAttributes redirect) {

		final List<String> itemsString = Arrays.asList(enabled.split("\\s*,\\s*"));
		final List<Long> itemsLong = new ArrayList<>();

		if(!enabled.equals("")) {
			for(String item : itemsString) {
				itemsLong.add(Long.parseLong(item));
			}
		}

		projectTypes.changeProjects(itemsLong,id);
		return new ModelAndView("redirect:/settings?view=projecttypes");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processProjectTypeServices", method = RequestMethod.POST)
	public ModelAndView processProjectTypeServices(final @RequestParam(value = "id") Long id,
												   final @RequestParam(value= "enabled") String enabled,
												   final RedirectAttributes redirect) {

		final List<String> itemsString = Arrays.asList(enabled.split("\\s*,\\s*"));
		final List<Long> itemsLong = new ArrayList<>();

		if(!enabled.equals("")) {
			for(String item : itemsString) {
				itemsLong.add(Long.parseLong(item));
			}
		}

		projectTypes.changeServices(itemsLong, id);

		return new ModelAndView("redirect:/settings?view=projecttypes");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processWServices", method = RequestMethod.POST)
	public ModelAndView processWServices(final @RequestParam(value = "id") Long id,
												   final @RequestParam(value= "enabled") String enabled,
												   final RedirectAttributes redirect) {

		final List<String> itemsString = Arrays.asList(enabled.split("\\s*,\\s*"));
		final List<Long> itemsLong = new ArrayList<>();

		if(!enabled.equals("")) {
			for(String item : itemsString) {
				if( (!item.equals("role")) && (!item.equals("repo")))
					itemsLong.add(Long.parseLong(item));
			}
		}
		if(itemsString.contains("role"))
				projectTypes.setWRoleServiceStatus(id,true);
		else
				projectTypes.setWRoleServiceStatus(id, false);
		if(itemsString.contains("repo")) {
			projectTypes.setWRepoStatus(id, true);
			projectTypes.setWRoleServiceStatus(id,true);
		}
		else
			projectTypes.setWRepoStatus(id,false);
		projectTypes.changeWizardConfig(itemsLong,id);

		return new ModelAndView("redirect:/settings?view=projecttypes");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processWInstantiation", method = RequestMethod.POST)
	public ModelAndView processWInstantiation(final @RequestParam(value = "typeid") Long typeid,
											  final @RequestParam(value = "dbid") String dbid,
											  final @RequestParam(value = "json") String json,
											  final RedirectAttributes redirect) {

		projectTypes.setInstantiationString(typeid,dbid,json);
		return new ModelAndView("redirect:/settings?view=projecttypes");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processProjectTypeColorAdaption", method = RequestMethod.POST)
	public ModelAndView processProjectTypeColorAdaption(final @RequestParam(value = "id") Long id,
												   final @RequestParam(value= "color") String color,
												   final RedirectAttributes redirect) {

		if(color.equals("unset"))
			projectTypes.unsetColor(id);
		else
			projectTypes.setColor(color,id);

		return new ModelAndView("redirect:/settings?view=projecttypes");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processServiceCreation", method = RequestMethod.POST)
	public ModelAndView processServiceCreation(final @RequestParam("fileup") MultipartFile file,
											   final RedirectAttributes redirect,
											   final HttpServletRequest request) {
		if(!file.isEmpty()) {
			try {
				services.createServiceDefinition(file);
			} catch(Exception e) {
				//logger.error("Error processing new service definition: " + e.getMessage());
				logger.error("Error processing new service definition: ",e);
				redirect.addFlashAttribute("errorMessage", getMessage("franchise.service.failure.servicedefinition", request.getLocale()));
			}
		}

		return new ModelAndView("redirect:/settings?view=service&page=1");
	}


	@SuppressWarnings("unused")
	@RequestMapping(value = "/processServiceRemoval", method = RequestMethod.POST)
	public ModelAndView processServiceRemoval(final @RequestParam(value = "id") Long id,
											  final @RequestParam(value = "force", defaultValue = "false") Boolean force,
											  final RedirectAttributes redirect,
											  final HttpServletRequest request) throws IOException {

		final String name = services.getServiceDefinition(id).getName();
		final ServiceDefinition sd = services.getServiceDefinition(id);

		if(serviceMgmt.deleteServiceRecursivley(sd,force))
			redirect.addFlashAttribute("successMessage", getMessage("franchise.service.success.deleteservice", request.getLocale()).replace("$SERVICENAME", name));
		else
			redirect.addFlashAttribute("errorMessage", getMessage("franchise.service.failure.deleteservice", request.getLocale()).replace("$SERVICENAME", name));

		return new ModelAndView("redirect:/settings?view=service&page=1");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processServiceDisable", method = RequestMethod.POST)
	public ModelAndView processDisable(final @RequestParam(value = "id") Long id, final RedirectAttributes redirect,
									   final HttpServletRequest request) {

		services.disableServiceDefinition(id);
		redirect.addFlashAttribute("successMessage", getMessage("franchise.service.success.disableservice", request.getLocale()).replace("$SERVICENAME", services.getServiceDefinition(id).getName()));
		return new ModelAndView("redirect:/settings?view=service&page=1");
	}


	@SuppressWarnings("unused")
	@RequestMapping(value = "/processServiceEnable", method = RequestMethod.POST)
	public ModelAndView processEnable(final @RequestParam(value = "id") Long id, final RedirectAttributes redirect,
									  final HttpServletRequest request) {

		services.enableServiceDefinition(id);
		redirect.addFlashAttribute("successMessage", getMessage("franchise.service.success.enablesservice", request.getLocale()).replace("$SERVICENAME", services.getServiceDefinition(id).getName()));
		return new ModelAndView("redirect:/settings?view=service&page=1");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processOrderChange", method = RequestMethod.POST)
	public ModelAndView processMenuOrderChange(final @RequestParam(value = "order") String order,
											   final RedirectAttributes redirect, final HttpServletRequest request) {


		final List<String> itemsString = Arrays.asList(order.split("\\s*,\\s*"));
		final List<Long> itemsLong = new ArrayList<>();
		for(String item : itemsString) {
			itemsLong.add(Long.parseLong(item));
		}
		services.saveNavigationItemOrder(itemsLong);
		redirect.addFlashAttribute("successMessage", getMessage("franchise.service.success.changeorder", request.getLocale()));
		return new ModelAndView("redirect:/settings?view=order");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processProjectRemoval", method = RequestMethod.POST)
	public ModelAndView processProjectRemoval(final @RequestParam(value = "projectid") Long id,
											  final @RequestParam(value = "force", defaultValue = "false") Boolean force,
											  final RedirectAttributes redirect, final HttpServletRequest request) {

		final AbstractProject p = projects.getProject(id);
		final String name = p.getName();
		try {
			if(!serviceMgmt.unsubscribeAllServices(p,force)) {
				redirect.addFlashAttribute("errorMessage", getMessage("franchise.project.unsubscribeerror", request.getLocale()));
				return new ModelAndView("redirect:/content/" + p.getUrlidentifier() +"/settings?view=config");
			}
		} catch (IOException e) {
			logger.error("Removal project failed with: " + e.getMessage());
			logger.debug("Removal project failed with: ",e);
		}
		if(!projects.removeProject(p)) {
			redirect.addFlashAttribute("errorMessage", getMessage("franchise.project.removerror", request.getLocale()) + env.getProperty("omilab.support"));
			return new ModelAndView("redirect:/content/" + p.getUrlidentifier() +"/settings?view=config");
		}
		redirect.addFlashAttribute("successMessage", getMessage("franchise.project.removalsuccess", request.getLocale()) + " " + name);
		return new ModelAndView("redirect:/settings?view=projecttypes");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processUrlChange", method = RequestMethod.POST)
	public ModelAndView processUrlChange(final @RequestParam(value = "projectid") Long id,
										 final @RequestParam(value = "urlidentifier") String url,
											  final RedirectAttributes redirect, final HttpServletRequest request) {

		AbstractProject p = projects.getProject(id);
		if(projects.getProject(url) != null) {
			redirect.addFlashAttribute("errorMessage", getMessage("franchise.project.urlerror", request.getLocale()));
			return new ModelAndView("redirect:/content/" + p.getUrlidentifier() +"/settings?view=config");
		}
		projects.changeUrlIdentifier(p,url);
		return new ModelAndView("redirect:/content/" + p.getUrlidentifier() +"/settings?view=config");
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/processProjectVisibilityChange", method = RequestMethod.POST)
	public ModelAndView processProjectVisibilityChange(final @RequestParam(value = "projectid") Long id,
										 final RedirectAttributes redirect, final HttpServletRequest request) {

		projects.toggleInConfig(projects.getProject(id));
		return new ModelAndView("redirect:/content/" + projects.getProject(id).getUrlidentifier() +"/settings?view=config");
	}



	@SuppressWarnings("unused")
	@ExceptionHandler(IOException.class)
	public RedirectView handleIOException(final HttpServletRequest request, final Exception e) {

		String redirect = "/settings?view=service&page=1";
		RedirectView rv = new RedirectView(redirect);
		FlashMap outputFlashMap = RequestContextUtils.getOutputFlashMap(request);
		outputFlashMap.put("errorMessage", getMessage("franchise.service.ioexception", request.getLocale()).replace("$SUPPORT", env.getProperty("omilab.support")));
		logger.error("Most likely some error happened while parsing the ServiceDefinition file: ", e);
		return rv;
	}

	@SuppressWarnings("unused")
	@ExceptionHandler(RuntimeException.class)
	public RedirectView handleContraintViolationException(final HttpServletRequest request,
														  final Exception e) throws Exception {

		if(e.getCause() instanceof ConstraintViolationException) {
			String redirect = "/settings?view=service&page=1";
			RedirectView rv = new RedirectView(redirect);
			FlashMap outputFlashMap = RequestContextUtils.getOutputFlashMap(request);
			if(((ConstraintViolationException) e.getCause()).getErrorCode() == 1062) {
				outputFlashMap.put("errorMessage", getMessage("franchise.service.duplicatedexception", request.getLocale()).replace("$SUPPORT", env.getProperty("omilab.support")));
			} else {
				Integer code = ((ConstraintViolationException) e.getCause()).getErrorCode();
				outputFlashMap.put("errorMessage", getMessage("franchise.service.sqlexception", request.getLocale()).replace("$SUPPORT", env.getProperty("omilab.support")).replace("$ERRORCODE", code.toString()));
				logger.error("SQL Error: " + ((ConstraintViolationException) e.getCause()).getErrorCode() + " happened. HttpRequest: " + request.toString(), e);
			}
			return rv;

		} else {
			throw e;
		}
	}

}
