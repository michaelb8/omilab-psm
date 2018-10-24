// File:         ProjectController.java
// Created:      15.02.2015
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.web;

import org.hibernate.exception.ConstraintViolationException;
import org.omilab.psm.model.db.Keyword;
import org.omilab.psm.model.db.AbstractProject;
import org.omilab.psm.model.db.ServiceDefinition;
import org.omilab.psm.model.db.ServiceInstance;
import org.omilab.psm.model.wrapper.GenericServiceContent;
import org.omilab.psm.model.wrapper.PageWrapper;
import org.omilab.psm.repo.GenericProjectRepository;
import org.omilab.psm.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/content")
@SuppressWarnings("unused")
public class ProjectController {

	private static final int PAGE_SIZE = 11;

	private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

	private final ProjectService projects;

	private final GenericProjectRepository projRepo;

	private final UserService users;

	private final ServiceDefinitionService services;

	private final ServiceManagementService serviceMgmt;

	private final RequestorService requestService;

	private final Environment env;

	private final ApplicationContext ctx;

	@Autowired
	public ProjectController(final ProjectService projects, final UserService users, final ServiceDefinitionService services,
							 final ServiceManagementService serviceMgmt, final RequestorService requestService, final Environment env,
							 final ApplicationContext ctx, final GenericProjectRepository projRepo) {
		this.projects = projects;
		this.users = users;
		this.services = services;
		this.serviceMgmt = serviceMgmt;
		this.requestService = requestService;
		this.ctx = ctx;
		this.env = env;
		this.projRepo = projRepo;
	}

	private String getMessage(final String key, final Locale locale) {
		final MessageSource messageSource = (MessageSource) ctx.getBean("messageSource");
		try {
			return messageSource.getMessage(key, null, locale);
		} catch(NoSuchMessageException e) {
			return messageSource.getMessage(key, null, Locale.ENGLISH);
		}
	}

	@PreAuthorize("isAuthenticated() and hasPermission(#projecturl, 'projectadmin')")
	@SuppressWarnings("unused")
	@RequestMapping(value = "{project}/settings", method = RequestMethod.GET)
	public ModelAndView generateProjectSettings(final @PathVariable("project") String projecturl,
												final @RequestParam(value = "page", defaultValue = "1") int pageNumber,
												final @RequestParam(value = "error", defaultValue = "false") Boolean error,
												final HttpServletRequest request) {

		if(!error) {
			Enumeration keys = request.getSession().getAttributeNames();
			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				if (key.contains("wiz"))
					request.getSession().removeAttribute(key);
			}
		}

		final AbstractProject project = projects.getProject(projecturl);

		final PageRequest pagerequest = new PageRequest(pageNumber - 1, PAGE_SIZE);
		final Page<ServiceDefinition> page = this.services.getVisibleServiceDefinitionPage(pagerequest);

		final List<String> franchiseKeywords = new ArrayList<>();
		for(Keyword keyword : projects.getAllKeywords()) {
			franchiseKeywords.add(keyword.getContent());
		}

		final StringBuilder projectKeywords = new StringBuilder();
		final List<Keyword> keywords = projects.getKeywordByProject(project);
		for(Keyword keyword : keywords) {
			projectKeywords.append(keyword.getContent());
			projectKeywords.append(",");
		}

		final ModelAndView mav = new ModelAndView("projectsettings", "services", page);
		mav.addObject("pages", new PageWrapper<>(page));
		mav.addObject("project", project);
		mav.addObject("subscribed", serviceMgmt.getServiceDefinitionByProject(project));
		mav.addObject("franchisekeywords", franchiseKeywords);
		mav.addObject("projectkeywords", projectKeywords.toString());
		mav.addObject("projectnavigation", serviceMgmt.generateNavigationMenu(project, null, "settings", request.getLocale()));
		mav.addObject("projectitems", serviceMgmt.generateMenuSelection(project));
		mav.addObject("projectitemsenabled", serviceMgmt.getAllEnabledMenus(project));
		mav.addObject("keywords", projects.getKeywordByProject(project));
		return mav;
	}

	@PreAuthorize("isAuthenticated() and hasPermission(#projecturl, 'projectadmin')")
	@SuppressWarnings("unused")
	@RequestMapping(value = "{project}/{service}/admin", method = {RequestMethod.GET, RequestMethod.POST})
	public ModelAndView generateServiceAdministration(final @PathVariable("project") String projecturl,
													  final @PathVariable("service") String serviceurl,
													  final @RequestParam Map<String, String> allRequestParams,
													  @RequestParam(value = "view", defaultValue = "") String endpoint,
													  final HttpServletRequest request) {

		final AbstractProject project = projects.getProject(projecturl);

		final ServiceDefinition sd = services.getServiceDefinition(serviceurl);
		if(sd == null) {
			return new ModelAndView("redirect:/content/" + projecturl);
		}
		if(endpoint.equals("")) {
			endpoint = serviceMgmt.getFirstEndpoint(sd, project);
		}

		final ServiceInstance si = serviceMgmt.getServiceInstance(project, sd);

		allRequestParams.remove("view");

		final GenericServiceContent gsc = requestService.processAdminRequest(si, allRequestParams, endpoint);

		final ModelAndView mav = new ModelAndView("projectserviceadministration", "wrapper", gsc);
		mav.addObject("project", project);
		mav.addObject("projectnavigation", serviceMgmt.generateNavigationMenu(project, sd, endpoint, request.getLocale()));
		mav.addObject("keywords", projects.getKeywordByProject(project));

		return mav;
	}

	@PreAuthorize("isAuthenticated() and hasPermission(#projecturl, 'projectadmin')")
	@SuppressWarnings("unused")
	@RequestMapping("{project}/role")
	public ModelAndView generateProjectRoleSettings(final @PathVariable("project") String projecturl,
													final @RequestParam Map<String, String> allRequestParams,
													final HttpServletRequest request) throws IOException {

		final AbstractProject project = projects.getProject(projecturl);

		if(serviceMgmt.getServiceInstanceOfSpecialService(project,"role") == null)
			serviceMgmt.instantiateSpecialService(project,"role");

		allRequestParams.remove("view");

		final GenericServiceContent gsc = requestService.processAdminRequest(serviceMgmt.getServiceInstanceOfSpecialService(project,"role"),allRequestParams,"rolemanagement");


		final ModelAndView mav = new ModelAndView("projectroles", "wrapper", gsc);
		mav.addObject("project", project);
		mav.addObject("projectnavigation", serviceMgmt.generateNavigationMenu(project, null, "roles", request.getLocale()));
		mav.addObject("keywords", projects.getKeywordByProject(project));
		return mav;
	}

	@SuppressWarnings("unused")
	@PreAuthorize("hasPermission(#projecturl, 'visitor')")
	@RequestMapping(value = "{project}/{service}", method = {RequestMethod.GET, RequestMethod.POST})
	public ModelAndView generateProjectContent(final @PathVariable("project") String projecturl,
											   final @PathVariable("service") String serviceurl,
											   @RequestParam(value = "view", defaultValue = "") String endpoint,
											   final @RequestParam Map<String, String> allRequestParams,
											   final HttpSession session, final HttpServletRequest request) {

		final AbstractProject project = projects.getProject(projecturl);

		final ServiceDefinition sd = services.getServiceDefinition(serviceurl);
		if(sd == null) {
			return new ModelAndView("redirect:/projects/" + projecturl);
		}

		if(endpoint.equals("")) {
			endpoint = serviceMgmt.getFirstEndpoint(sd, project);
		}

		if(!serviceMgmt.checkEnabled(project, sd, endpoint)) {
			return new ModelAndView("redirect:/content/" + projecturl);
		}

		final ServiceInstance si = serviceMgmt.getServiceInstance(project, sd);


		allRequestParams.remove("view");

		final GenericServiceContent gsc = requestService.processViewRequest(si, allRequestParams, endpoint);

		final ModelAndView mav = new ModelAndView("projectcontent", "wrapper", gsc);
		mav.addObject("project", project);
		mav.addObject("servicedefinition", sd);
		mav.addObject("projectnavigation", serviceMgmt.generateNavigationMenu(project, sd, endpoint, request.getLocale()));
		mav.addObject("keywords", projects.getKeywordByProject(project));
		return mav;
	}

	@SuppressWarnings("unused")
	@PreAuthorize("hasPermission(#projecturl, 'visitor')")
	@RequestMapping(value = "{project}/{service}/ajax/**", method = {RequestMethod.POST})
	@ResponseBody
	public String generateProjectContentAJAX(final @PathVariable("project") String projecturl,
											   final @PathVariable("service") String serviceurl,
											   @RequestBody String postPayload,
											   final HttpSession session, final HttpServletRequest request) {

		final AbstractProject project = projects.getProject(projecturl);

		final ServiceDefinition sd = services.getServiceDefinition(serviceurl);
		if(sd == null) {
			return null;
		}
		final String uri = request.getRequestURI();
		final String ruri = uri.substring(uri.indexOf("/ajax/")+1);
		final ServiceInstance si = serviceMgmt.getServiceInstance(project, sd);
		return requestService.processAJAXRequest(si,postPayload,ruri);
	}

	@SuppressWarnings("unused")
	@RequestMapping("{project}")
	public ModelAndView generateProjectContentForService(final @PathVariable("project") String projecturl) {
		final AbstractProject project = projects.getProject(projecturl);
		return new ModelAndView("redirect:/content/" + projecturl + "/" + serviceMgmt.getFirstService(project));
	}

	@PreAuthorize("isAuthenticated() and hasPermission(#projecturl, 'projectadmin')")
	@SuppressWarnings("unused")
	@RequestMapping(value = "{project}/settings/processServiceInstantiation", method = RequestMethod.POST)
	public ModelAndView processServiceInstantiation(final @PathVariable("project") String projecturl,
													final @RequestParam(value = "id") Long serviceid,
													final RedirectAttributes redirect,
													final HttpServletRequest request) throws IOException {

		final AbstractProject project = projects.getProject(projecturl);

		final ServiceDefinition sd = services.getServiceDefinition(serviceid);

		if(serviceMgmt.instantiateService(project, sd))
			redirect.addFlashAttribute("successMessage", getMessage("project.settings.success.instantiateservice", request.getLocale()));
		else
			redirect.addFlashAttribute("errorMessage", getMessage("project.settings.failure.instantiateservice", request.getLocale()).replace("$DEVELOPER", sd.getDeveloper()));

		return new ModelAndView("redirect:/content/" + projecturl + "/settings?view=service");
	}

	@PreAuthorize("isAuthenticated() and hasPermission(#projecturl, 'projectadmin')")
	@SuppressWarnings("unused")
	@RequestMapping(value = "{project}/settings/processServiceRemoval", method = RequestMethod.POST)
	public ModelAndView processServiceRemoval(final @PathVariable("project") String projecturl,
											  final @RequestParam(value = "id") Long serviceid,
											  final RedirectAttributes redirect,
											  final HttpServletRequest request) throws IOException {

		final AbstractProject project = projects.getProject(projecturl);

		final ServiceDefinition sd = services.getServiceDefinition(serviceid);

		if(serviceMgmt.unsubscribeService(project,sd,false))
			redirect.addFlashAttribute("successMessage", getMessage("project.settings.success.unsubscribeservice", request.getLocale()));
		else
			redirect.addFlashAttribute("errorMessage", getMessage("project.settings.failure.unsubscribeservice", request.getLocale()));

		return new ModelAndView("redirect:/content/" + projecturl + "/settings?view=service");
	}

	@PreAuthorize("isAuthenticated() and hasPermission(#projecturl, 'projectadmin')")
	@SuppressWarnings("unused")
	@RequestMapping(value = "{projecturl}/settings/processConfigAdaption", method = RequestMethod.POST)
	public String processConfigAdaption(final @PathVariable("projecturl") String projecturl,
										final RedirectAttributes redirect,
										@RequestParam Map<String,String> allRequestParams,
										final @ModelAttribute(value = "tags") String tags,
										final HttpServletRequest request) {

		AbstractProject project = projects.getProject(projecturl);

		for (Map.Entry<String,String> entry : allRequestParams.entrySet()) {
			request.getSession().setAttribute("wiz" + entry.getKey(),entry.getValue());
		}

		if(projRepo.findByName(allRequestParams.get("name")) != null && (!project.getName().toLowerCase().equals(allRequestParams.get("name").toLowerCase()))) {
			return ("redirect:/content/" + projecturl + "/settings?view=config&exists=name&error=true");
		}
		if(projRepo.findByAbbreviation(allRequestParams.get("abbreviation")) != null && (!project.getAbbreviation().toLowerCase().equals(allRequestParams.get("abbreviation").toLowerCase()))) {
			return ("redirect:/content/" + projecturl + "/settings?view=config&exists=abbr&error=true");
		}

		List<String> itemsString = Arrays.asList(tags.split("\\s*,\\s*"));
		projects.updateKeywords(project, itemsString);
		try {
			projects.changeProjectAttributesPO(allRequestParams,project);
		} catch (TransactionSystemException e) {
			return ("redirect:/content/" + projecturl + "/settings?view=config&ferror=true");
		}

		redirect.addFlashAttribute("successMessage", getMessage("project.settings.success.configchange", request.getLocale()));
		return ("redirect:/content/" + projecturl + "/settings?view=config");
	}

	@PreAuthorize("isAuthenticated() and hasPermission(#projecturl, 'projectadmin')")
	@SuppressWarnings("unused")
	@RequestMapping(value = "{project}/settings/processMenuStatus", method = RequestMethod.POST)
	public ModelAndView processStatusUpdate(final @PathVariable("project") String projecturl,
											final RedirectAttributes redirect,
											final @ModelAttribute(value = "enabled") String enabled,
											final HttpServletRequest request) {

		final AbstractProject project = projects.getProject(projecturl);

		final List<String> itemsString = Arrays.asList(enabled.split("\\s*,\\s*"));
		final List<Long> itemsLong = new ArrayList<>();

		if(!enabled.equals("")) {
			for(String item : itemsString) {
				itemsLong.add(Long.parseLong(item));
			}
		}

		serviceMgmt.changeNavigationStatus(itemsLong, project);

		redirect.addFlashAttribute("successMessage", getMessage("project.settings.success.configchange", request.getLocale()));
		return new ModelAndView("redirect:/content/" + projecturl + "/settings?view=menu");
	}

	@SuppressWarnings("unused")
	@ExceptionHandler(RuntimeException.class)
	public RedirectView handleContraintViolationException(final HttpServletRequest request,
														  final Exception e) throws Exception {
		if(e.getCause() instanceof ConstraintViolationException) {
			String redirect = "/content";
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
