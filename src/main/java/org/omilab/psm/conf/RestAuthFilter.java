// File:         RestAuthFilter.java
// Created:      22.04.2015
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.conf;

import org.omilab.psm.service.GlobalConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component("RestAuthFilter")
public class RestAuthFilter implements Filter {


	private static final Logger logger = LoggerFactory.getLogger(RestAuthFilter.class);
	private final List<String> allowedIPs;
	private final Environment env;
	private final GlobalConfigurationService gconf;

	@Autowired
	public RestAuthFilter(Environment env, GlobalConfigurationService gconf) {
		this.env = env;
		this.gconf = gconf;
		this.allowedIPs = Arrays.asList(env.getProperty("omilab.rest.ips").split("[,]"));
		logger.debug("IPs allowed to use REST service: " + allowedIPs.toString());
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(final ServletRequest req, final ServletResponse res,
						 final FilterChain chain) throws ServletException, IOException {


		final HttpServletRequest request = (HttpServletRequest) req;
		final PathMatcher pathMatcher = new AntPathMatcher();

		String path = request.getRequestURI().substring(request.getContextPath().length());

		if(pathMatcher.match("/rest/**", path)) {

			logger.debug("Incoming request from: " + req.getRemoteAddr());

			List<String> tempgn;
			if(gconf.getValue("gn_regstatus") != null || gconf.getValue("gn_regstatus").equals("true")) {
				if(gconf.getValue("gn_ips") != null)
					tempgn = Arrays.asList(gconf.getValue("gn_ips").split("[,]"));
				else
					tempgn = new ArrayList<>();
			} else {
				tempgn = new ArrayList<>();
			}

			List<String> allAllowedIPs = new ArrayList<>();
			allAllowedIPs.addAll(allowedIPs);
			allAllowedIPs.addAll(tempgn);

			for(String ip : allAllowedIPs) {
				IpAddressMatcher matcher = new IpAddressMatcher(ip);
				if(matcher.matches(req.getRemoteAddr())) {
					chain.doFilter(req, res);
				} else
					((HttpServletResponse) res).setStatus(HttpServletResponse.SC_FORBIDDEN, "Not allowed to use REST API!");
			}

		} else  chain.doFilter(req, res);

	}

	@Override
	public void init(final FilterConfig config) throws ServletException {

	}
}

