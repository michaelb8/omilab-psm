// File:         CASAuthSuccessHandler.java
// Created:      18.05.2015
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//


package org.omilab.psm.conf;

import org.omilab.psm.model.wrapper.UserLoginInformation;
import org.omilab.psm.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class CASAuthSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	private static final Logger logger = LoggerFactory.getLogger(CASAuthSuccessHandler.class);

	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
										Authentication authentication) throws ServletException, IOException {
		String url;
		HttpSession session = request.getSession(false);
		if(authentication != null) {
			logger.debug("Authenticated: " + authentication.getPrincipal().toString());
			if(UserService.activeUsers.get(authentication.getName()) != null) {
				UserService.activeUsers.remove(authentication.getName());
			}
			String ip = "0.0.0.0";
			Enumeration headerNames = request.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				String key = (String) headerNames.nextElement();
				if(key.equals("x-real-ip")) {
					ip = request.getHeader(key);
				}
			}
			if(ip.equals("0.0.0.0"))
				ip = request.getRemoteAddr();
			UserService.activeUsers.put(authentication.getName(),new UserLoginInformation(ip));
		}
		if(session != null) {
			url = (String) request.getSession().getAttribute("prelogin");
			if(url != null) {
				request.getSession().removeAttribute("prelogin");
				response.sendRedirect(url);
			} else
				super.onAuthenticationSuccess(request, response, authentication);
		} else
			super.onAuthenticationSuccess(request, response, authentication);
	}

}
