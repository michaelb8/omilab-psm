// File:         CacheFilter.java
// Created:      26.02.2016
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2016 by OMiLAB.ORG
//


package org.omilab.psm.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@SuppressWarnings("unused")
public class CacheFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(CacheFilter.class);


	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
						 FilterChain chain) throws IOException, ServletException {

		final HttpServletRequest request = (HttpServletRequest) req;
		final HttpServletResponse response = (HttpServletResponse) res;
		final PathMatcher pathMatcher = new AntPathMatcher();

		final String path = request.getRequestURI().substring(request.getContextPath().length());

		if(pathMatcher.match("/content/*/settings", path)) {
			response.setHeader("Cache-Control","no-cache");
			logger.debug("Found project owner content : " + path);
			chain.doFilter(req, res);
		} else if(pathMatcher.match("/content/*/*/admin", path) || pathMatcher.match("/content/*/role", path)) {
			response.setHeader("Cache-Control","no-cache");
			logger.debug("Found project owner content : " + path);
			chain.doFilter(req, res);
		} else if(pathMatcher.match("/wizard/**", path)) {
			response.setHeader("Cache-Control","no-cache");
			logger.debug("Found wizard content : " + path);
			chain.doFilter(req, res);
		} else if( pathMatcher.match("/settings/**", path)) {
			response.setHeader("Cache-Control","no-cache");
			logger.debug("Found franchise administration content: " + path);
			chain.doFilter(req, res);
		} else if( pathMatcher.match("/content/*/*", path)) {
			response.setHeader("Cache-Control","no-cache");
			logger.debug("Found regular project content: " + path);
			chain.doFilter(req, res);
		} else if( pathMatcher.match("/js/**", path) || pathMatcher.match("/css/**", path) || pathMatcher.match("/fonts/**", path) || pathMatcher.match("/images/**", path) || pathMatcher.match("/favicon.ico", path)) {
			response.setHeader("Cache-Control","max-age=1209600");
			logger.debug("Found static content: " + path);
			chain.doFilter(req, res);
		} else if( pathMatcher.match("/search/**", path)) {
			response.setHeader("Cache-Control","no-cache");
			logger.debug("Found search content: " + path);
			chain.doFilter(req, res);
		} else if( pathMatcher.match("/j_spring_cas_security_check", path) || pathMatcher.match("/login", path) || pathMatcher.match("/logout", path) || pathMatcher.match("/editprofile", path) || pathMatcher.match("/register", path) || pathMatcher.match("/forgottenpassword", path) ) {
			response.setHeader("Cache-Control","no-cache");
			logger.debug("Found internal thing: " + path);
			chain.doFilter(req, res);
		} else if( pathMatcher.match("/projectcolors.css", path)) {
			response.setHeader("Cache-Control","max-age=60");
			logger.debug("Found projectcolors: " + path);
			chain.doFilter(req, res);
		} else if( pathMatcher.match("/ajaxTiles", path)) {
			response.setHeader("Cache-Control","max-age=20");
			logger.debug("Found ajaxTiles: " + path);
			chain.doFilter(req, res);
		} else {
			response.setHeader("Cache-Control","max-age=2");
			logger.debug("Matched nothing" + path);
			chain.doFilter(req, res);
		}
	}

	@Override
	public void destroy() {
	}

}