// File:         ProjectFilter.java
// Created:      24.04.2015
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//


package org.omilab.psm.conf;

import org.omilab.psm.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Component
@SuppressWarnings("unused")
public class ProjectFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(ProjectFilter.class);

	@Autowired
	private ProjectService projects;

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
						 FilterChain chain) throws IOException, ServletException {

		final HttpServletRequest request = (HttpServletRequest) req;
		final HttpServletResponse response = (HttpServletResponse) res;
		final PathMatcher pathMatcher = new AntPathMatcher();

		String path = request.getRequestURI().substring(request.getContextPath().length());

		if(pathMatcher.match("/content/*/**", path)) {
			final Map<String, String> params = pathMatcher.extractUriTemplateVariables("/content/{project}/**", path);
			if(projects.getProject(params.get("project")) == null) {
				logger.info("Nonexisting project has been requested: " + params.get("project"));
				response.sendRedirect(request.getContextPath() + "/404");
			} else
				chain.doFilter(req, res);
		} else {
			chain.doFilter(req, res);
		}
	}

	@Override
	public void destroy() {
	}

}