// File:         ApplicationStartup.java
// Created:      18.05.2015
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//


package org.omilab.psm.conf;

import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebSecurityExpressionHandler;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Map;

@Component
public class ApplicationStartup implements ApplicationListener<ContextRefreshedEvent> {

	private static final Logger logger = LoggerFactory.getLogger(ApplicationStartup.class);

	@Autowired
	private ApplicationContext ctx;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public void onApplicationEvent(final ContextRefreshedEvent event) {
		/*
			Thymeleaf only supports one ExpressionHandler and simply uses the first one.
			In order to make the 'hasPermission' tag work within the Spring Security
			Thymeleaf dialect it is necessary to set the custom PermissionEvaluator for
			the FIRST WebSecurityExpressionHandler.

			*** DO NOT FORGET TO CHANGE WebSecurityExpressionHandler when transitioning ***
			***	to Spring Security 4 (to the new SecurityExpressionHandler)             ***

			See also: https://github.com/thymeleaf/thymeleaf-extras-springsecurity/issues/17
		 */
		final Map<String, WebSecurityExpressionHandler> expressionHandlers = ctx.getBeansOfType(WebSecurityExpressionHandler.class);

		//if(expressionHandlers.values().toArray()[0] != null)
		//	((DefaultWebSecurityExpressionHandler) expressionHandlers.values().toArray()[0]).setPermissionEvaluator(new ProjectPermissionEvaluator());
		OmilabPermissionEvaluator permissionEvaluator = (OmilabPermissionEvaluator) ctx.getBean("omilabPermissionEvaluator");
		((DefaultWebSecurityExpressionHandler) expressionHandlers.get("webSecurityExpressionHandler")).setPermissionEvaluator(permissionEvaluator);

		try {
			FullTextEntityManager fullTextEntityManager =
					Search.getFullTextEntityManager(entityManager);
			fullTextEntityManager.createIndexer().startAndWait();
		}
		catch (InterruptedException e) {
			logger.error(
					"An error occurred trying to build the serach index: " +
							e.toString());
		}
	}

}