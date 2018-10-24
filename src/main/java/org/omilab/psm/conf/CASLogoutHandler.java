// File:         CASLogoutHandler.java
// Created:      21.05.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.conf;

import org.omilab.psm.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class CASLogoutHandler implements LogoutHandler {

	private static final Logger logger = LoggerFactory.getLogger(CASLogoutHandler.class);

	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		if(authentication != null) {
			logger.debug("Logging out: " + authentication.getPrincipal().toString());
		}
		try {
			String url;
			HttpSession session = request.getSession(false);
			if(session != null) {
				url = (String) request.getSession().getAttribute("prelogout");
				if(url != null) {
					request.getSession().removeAttribute("prelogout");
					response.sendRedirect(url);
				}
			}
		} catch(IOException e) {
			logger.error("IO Error during logout");
			logger.info("Logout Error", e);
		}

	}
}