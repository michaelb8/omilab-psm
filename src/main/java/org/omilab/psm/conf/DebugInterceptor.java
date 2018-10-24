// File:         DebugInterceptor.java
// Created:      24.04.2015
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//


package org.omilab.psm.conf;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DebugInterceptor extends HandlerInterceptorAdapter {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
							 Object handler) throws Exception {
		long startTime = System.currentTimeMillis();
		request.setAttribute("startTime", startTime);
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
						   ModelAndView mav) throws Exception {
		long diff = System.currentTimeMillis() - ((Long) request.getAttribute("startTime"));
		mav.addObject("generationtime", diff);
	}


}