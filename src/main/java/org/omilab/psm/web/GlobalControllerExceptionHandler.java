// File:         GlobalControllerExceptionHandler.java
// Created:      19.02.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@ControllerAdvice
@SuppressWarnings("unused")
public final class GlobalControllerExceptionHandler {

	private final Environment env;

	@Autowired
	public GlobalControllerExceptionHandler(Environment env) {
		this.env = env;
	}

	@ExceptionHandler(value = Exception.class)
	public ModelAndView defaultErrorHandler(HttpServletRequest request, Exception e) throws Exception {

		if(AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null)
			throw e;

		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date();

		ModelAndView mav = new ModelAndView();
		mav.addObject("message", e.getMessage());
		mav.addObject("url", request.getRequestURL());
		mav.addObject("timestamp", dateFormat.format(date));
		mav.setViewName("error");
		return mav;
	}
}
