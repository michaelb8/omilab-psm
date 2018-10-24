// File:         MainController.java
// Created:      18.02.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.web;


import org.jsoup.Jsoup;
import org.omilab.psm.model.db.*;
import org.omilab.psm.model.db.projectoverlay.Event;
import org.omilab.psm.model.db.projectoverlay.MMProject;
import org.omilab.psm.model.db.projectoverlay.Placeholder;
import org.omilab.psm.repo.ProjectSearch;
import org.omilab.psm.service.MainNavigationManagementService;
import org.omilab.psm.service.ProjectService;
import org.omilab.psm.service.UserService;
import org.omilab.psm.service.role.RoleService;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/")
@SuppressWarnings("unused")
public class MainController {

	private static final Logger logger = LoggerFactory.getLogger(MainController.class);

	private static final int TILES_PER_PAGE = 8;

	private final Environment env;

	private final UserService users;

	private final ProjectService projects;

	private final RoleService roles;

	private final MainNavigationManagementService mainNavigation;

	private final ProjectSearch projSearch;

	@Autowired
	public MainController(final Environment env, final UserService users, final ProjectService projects, final RoleService roles, final MainNavigationManagementService mainNavigation,
						  final ProjectSearch projSearch) {
		this.env = env;
		this.users = users;
		this.projects = projects;
		this.roles = roles;
		this.mainNavigation = mainNavigation;
		this.projSearch = projSearch;
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "profile/changeView", method = RequestMethod.GET)
	public ModelAndView changeView(final HttpServletRequest request) {

		if(request.getSession().getAttribute("adminview") == null)
			request.getSession().setAttribute("adminview", false);

		if(request.getSession().getAttribute("adminview").equals(true))
			request.getSession().setAttribute("adminview", false);
		else
			request.getSession().setAttribute("adminview", true);

		return new ModelAndView("redirect:" + request.getHeader("referer"));
	}

	@SuppressWarnings("unused")
	@RequestMapping("web/{project}/**")
	public ModelAndView redirectToProject(final @PathVariable("project") String projecturl) {
		return new ModelAndView("redirect:/content/" + projecturl);
	}

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public ModelAndView login(HttpServletRequest request) {
		String referer = request.getHeader("Referer");
		if(referer != null) {
			request.getSession().setAttribute("prelogin", referer);
		}
		String loginurl = env.getProperty("cas.service.url") + "/login?service=" + env.getProperty("app.url") + "/j_spring_cas_security_check";
		return new ModelAndView("redirect:" + loginurl);
	}

	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public ModelAndView logout(HttpServletRequest request) {
		String referer = request.getHeader("Referer");
		if(referer != null) {
			request.getSession().setAttribute("prelogout", referer);
		}
		String logouturl = env.getProperty("cas.service.url") + "/logout?service=" + env.getProperty("app.url") + "/caslogout";
		return new ModelAndView("redirect:" + logouturl);
	}

	@RequestMapping(value = "/register", method = RequestMethod.GET)
	public ModelAndView register(HttpServletRequest request) {
		return new ModelAndView("redirect:" + env.getProperty("omilab.passwords") + "/public/NewUser");
	}

	@RequestMapping(value = "/editprofile", method = RequestMethod.GET)
	public ModelAndView editprofile(HttpServletRequest request) {
		return new ModelAndView("redirect:" + env.getProperty("omilab.passwords") + "/private/Login?username=" + users.getCurrentUser().getUsername());
	}

	@RequestMapping(value = "/forgottenpassword", method = RequestMethod.GET)
	public ModelAndView forgottenpassword(HttpServletRequest request) {
		return new ModelAndView("redirect:" + env.getProperty("omilab.passwords") + "/public/ForgottenPassword");
	}

	@RequestMapping(value = "/projectlogos/{project}", method = RequestMethod.GET)
	public ModelAndView redirectImage(final @PathVariable("project") String projecturl) {
		AbstractProject ap = projects.getProject(projecturl);
		if(ap instanceof MMProject)
			return new ModelAndView("redirect:" + ((MMProject) ap).getProjectlogo());
		return new ModelAndView("redirect:/404");
	}

	@SuppressWarnings("unused")
	@RequestMapping("exception")
	public ModelAndView generateException() {
		throw new RuntimeException("Test exception!");
	}


	private String buildCSS() {
		StringBuilder sb = new StringBuilder();
		for(AbstractProject project : projects.getAllProjects()) {
			sb.append(".tile_");
			sb.append(project.getId());
			sb.append(" .live-tile > .slide-front {\n" + "\tbackground-color: ");
			if(project.getProjecttype() != null) {
				if(project.getProjecttype().getBackgroundColor() == null)
					sb.append(project.getBackgroundColor());
				else
					sb.append(project.getProjecttype().getBackgroundColor());
			}
			sb.append("; \n}\n");
		}
		return sb.toString();
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "projectcolors.css")
	@ResponseBody
	public String generateProjectsCSS() {
		return buildCSS();
	}

	@SuppressWarnings("unused")
	@RequestMapping("404")
	public ModelAndView generate404() {
		final ModelAndView mav = new ModelAndView("404", "support", env.getProperty("omilab.support"));
		return mav;
	}

	@SuppressWarnings("unused")
		 @RequestMapping("403")
		 public ModelAndView generate403() {
		final ModelAndView mav = new ModelAndView("403", "support", env.getProperty("omilab.support"));
		return mav;
	}

	@SuppressWarnings("unused")
	@RequestMapping("")
	public ModelAndView temporaryMain() {
		String url = mainNavigation.getMenu().get(0).getLink();
		if(url != null)
			return new ModelAndView("redirect:/" + url);
		else
			return new ModelAndView("redirect:/settings/");
	}

	private Boolean checkIfEvent(List<AbstractProject> abs) {
		//should be replaced with more performant SQL query when time
		for(AbstractProject ab : abs) {
			if(!ab.getProjecttype().getOverlay().equals("org.omilab.psm.model.db.projectoverlay.Event")) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unused")
	@RequestMapping("search")
	public ModelAndView search(final @RequestParam(value = "q") String query) {
		ModelAndView mav = new ModelAndView("searchresults");
		List<AbstractProject> result = null;
		List<MainNavigationItemHTML> htmlLinks = null;
		List<Map> htmlPages = new ArrayList<>();
		try {
			result = projSearch.search(query);
			htmlLinks = projSearch.searchHTML(query);
			for(MainNavigationItemHTML item : htmlLinks){
				Map<String, String> map = new HashMap<>();
				map.put("link", env.getProperty("app.url") + "/" + item.getLink());
				map.put("name", item.getDisplayname());
				map.put("occurrence", getOccurrenceString(item.getHtml(), query));
				htmlPages.add(map);
			}
		} catch (Exception e) {
			result = null;
			logger.error("Failed to search for \" " + query + "\" Error: " + e.getMessage());
			logger.debug("Failed to search for \" " + query + "\" Error: ",e);
		}
		//mav.addObject("htmlLinks", htmlLinks);
		mav.addObject("htmlPages", htmlPages);
		mav.addObject("projects",result);
		return mav;
	}

	private String getOccurrenceString(String html, String query){
		String htmlLower = Jsoup.parse(html).text().toLowerCase();
		html = Jsoup.parse(html).text();
		String occurrence = "";
		String startString = "";
		String endString = "";
		int characterCount = 70;
		int start = 0;
		int end = 0;
		int startOfOccurrence = htmlLower.indexOf(query.toLowerCase());
		if(startOfOccurrence - characterCount >= 0){
			start = startOfOccurrence - characterCount;
			startString = "...";
		}
		if(startOfOccurrence + characterCount > html.length()){
			end = html.length();
		} else{
			end = startOfOccurrence + characterCount;
			endString = "...";
		}
		occurrence = startString + html.substring(start, end) + endString;
		return occurrence;
	}

	@PreAuthorize("isAuthenticated()")
	@RequestMapping(value = "/processNewProposal", method = RequestMethod.POST)
	public ModelAndView processNewProposal(final @RequestParam(value = "name") String projectname,
										   final @RequestParam(value = "abstract") String projectabstract,
										   final @RequestParam(value = "siteparam") String siteparam,
										   final @RequestParam(value = "projecttype") Long type){

		final String projectnameSafe = Encode.forJavaScript(Encode.forHtml(projectname));
		final String projectabstractSafe = Encode.forJavaScript(Encode.forHtml(projectabstract));
		projects.createProposal(projectnameSafe,projectabstractSafe,users.getCurrentUser().getUsername(),users.queryUserDetails().getEmailAddress(),type,(MainNavigationItemTypes)mainNavigation.getMNIForURL(siteparam));
		return new ModelAndView("redirect:/" + siteparam + "?action=success&param=explore");
	}

	@RequestMapping(value = "{site}/ajaxpropose")
	public ModelAndView generateProjectProposal(final @PathVariable("site") String url) {
		MainNavigationItem mni = mainNavigation.getMNIForURL(url);
		if( mni instanceof MainNavigationItemTypes) {
			ModelAndView mav =  new ModelAndView("projectproposal", "mni", mainNavigation.getMNIForURL(url));
			mav.addObject("siteurl",url);
			if(!users.getCurrentUser().getUsername().equals("anonymousUser")) {
				try {
					mav.addObject("user", users.queryUserDetails());
				} catch (Exception e) {
					logger.error("Failed to query LDAP server");
				}
			}
			return mav;
		}
		return null;
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "{site}")
	public ModelAndView generateProjectContent(final @PathVariable("site") String url) {
		MainNavigationItem mni = mainNavigation.getMNIForURL(url);
		if(mni == null) {
			return new ModelAndView("redirect:/404");
		}
		if(mni.getAuthentication() && (users.getCurrentUser().getUsername().equals("anonymousUser")))
			return new ModelAndView("redirect:/403");
		if(mni instanceof MainNavigationItemHTML) {
			final ModelAndView mav = new ModelAndView("staticpage", "payload", ((MainNavigationItemHTML)mni));
			mav.addObject("carousel", ((MainNavigationItemHTML)mni).getCarousel());
			return mav;
		}
		if( mni instanceof MainNavigationItemTypes) {
			ModelAndView mav =  new ModelAndView("projectoverviewtiles", "mni", mainNavigation.getMNIForURL(url));
			List<AbstractProject> page = mainNavigation.getProjectsByURL(url);
			if(checkIfEvent(page)) {
				page.clear();
				List<Event> futureEvents = new ArrayList<>(); // list for future events
				List<Event> pastEvents = new ArrayList<>(); // list for past events
				Date currentDate = new Date(); // current date
				for(Event event : mainNavigation.getProjectsByURLChrono(url)) {
					if(event.getStart().compareTo(currentDate) < 0){
						// start date of event is before currentDate -> add to past events
						pastEvents.add(event);
					} else{
						// event is in the future (or happening at the moment)
						futureEvents.add(event);
					}
				}
				int year = 0;
				// add future events first
				for(Event event : futureEvents){
					int eventYear = year;
					try{
						eventYear = Integer.parseInt(new SimpleDateFormat("yyyy").format(event.getStart()));
					} catch(Exception e){
						// not able to parse year of event
					}
					if(eventYear != year){
						// add placeholder with year for better overview
						page.add((AbstractProject) (new Placeholder("" + eventYear, "Abb" + eventYear, "url" + eventYear, null, "" + eventYear)));
						year = eventYear;
					}
					page.add((AbstractProject) event);
				}
				// and then the past events but in reverse order - events that are not long ago are on top
				for(int i = pastEvents.size()-1; i >= 0; i--){
					int eventYear = year;
					try{
						eventYear = Integer.parseInt(new SimpleDateFormat("yyyy").format(pastEvents.get(i).getStart()));
					} catch (Exception e){
						// not able to parse year
					}
					if(eventYear != year){
						page.add((AbstractProject) (new Placeholder("" + eventYear, "Abb" + eventYear, "url" + eventYear, null, "" + eventYear)));
						year = eventYear;
					}
					page.add((AbstractProject) pastEvents.get(i));
				}
			} else {
				Collections.sort(page, new Comparator<AbstractProject>() {
					@Override
					public int compare(final AbstractProject object1, final AbstractProject object2) {
						return object1.getAbbreviation().toLowerCase().compareTo(object2.getAbbreviation().toLowerCase());
					}
				});
			}
			mav.addObject("projects",page);
			mav.addObject("siteurl",url);
			return mav;
		}
		if( mni instanceof MainNavigationItemProject) {
			AbstractProject project = mainNavigation.getProjectByURL(url);
			return new ModelAndView("redirect:/content/" + project.getUrlidentifier());
		}
		return new ModelAndView("redirect:/404");
	}

	@SuppressWarnings("unused")
	@RequestMapping("nocontent")
	public ModelAndView noContent(final @RequestParam(value = "project") String projectURLIdentifier,
								  final @RequestParam(value = "type") String contentType) {
		ModelAndView mav = new ModelAndView("nocontent");
		if(projectURLIdentifier != null && contentType != null) {
			AbstractProject ap = projects.getProject(projectURLIdentifier);
			mav.addObject("pagetitle", ap.getName() + ": " + contentType + " not found");
			String noContent = contentType + " could not be found for the project <a href=\"" + env.getProperty("app.url") + "/content/" + projectURLIdentifier + "\">" + ap.getName() + "</a>.<br>";
			noContent += "For more information please write an email to the contact person.";
			mav.addObject("nocontent", noContent);
		}

		/*List<AbstractProject> result = null;
		List<MainNavigationItemHTML> htmlLinks = null;
		List<Map> htmlPages = new ArrayList<>();
		try {
			result = projSearch.search(query);
			htmlLinks = projSearch.searchHTML(query);
			for(MainNavigationItemHTML item : htmlLinks){
				Map<String, String> map = new HashMap<>();
				map.put("link", env.getProperty("app.url") + "/" + item.getLink());
				map.put("name", item.getDisplayname());
				map.put("occurrence", getOccurrenceString(item.getHtml(), query));
				htmlPages.add(map);
			}
		} catch (Exception e) {
			result = null;
			logger.error("Failed to search for \" " + query + "\" Error: " + e.getMessage());
			logger.debug("Failed to search for \" " + query + "\" Error: ",e);
		}
		//mav.addObject("htmlLinks", htmlLinks);
		mav.addObject("htmlPages", htmlPages);
		mav.addObject("projects",result);*/
		return mav;
	}

}
