// File:         OmilabPermissionEvaluator.java
// Created:      15.05.2015
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.conf;

import org.omilab.psm.model.db.AbstractProject;
import org.omilab.psm.model.db.MainNavigationItem;
import org.omilab.psm.model.db.MainNavigationItemProject;
import org.omilab.psm.repo.AbstractProjectRepository;
import org.omilab.psm.repo.GenericProjectRepository;
import org.omilab.psm.service.role.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Component
public class OmilabPermissionEvaluator implements PermissionEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(OmilabPermissionEvaluator.class);

	private final RoleService roles;
	private final GenericProjectRepository projectRepo;

	@Autowired
	public OmilabPermissionEvaluator(final RoleService roles, final GenericProjectRepository projectRepo) {
		this.roles = roles;
		this.projectRepo = projectRepo;
	}

	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		logger.debug("--------------------------------------------------");
		logger.debug("Started permission evaluation ...");

		final String username;

		if(permission instanceof String && targetDomainObject instanceof String) {
			if(permission.equals("visitor") ) {
				if(authentication == null || authentication instanceof AnonymousAuthenticationToken) {
					List<MainNavigationItemProject> items = projectRepo.findByUrlidentifier((String)targetDomainObject).getNavigation();
					Boolean authenticationflag = true;
					for(MainNavigationItem item : items) {
						if(item.getAuthentication()) {
							authenticationflag = false;
						}
					}
					return authenticationflag;
				} else return true;
			}
		}

		// receive username of currently logged in user or
		// disable further permission checking if user is not
		// logged in (authentication == null)
		if(authentication instanceof CasAuthenticationToken) {
			username = ((UserDetails) authentication.getPrincipal()).getUsername();
			logger.debug("Found user: " + username);
		} else {
			logger.debug("Disable permission evaluation, as user is NOT logged in");
			return false;
		}

		if(roles.isFranchiseAdmin(username)) {
			logger.debug(username + " is franchise administrator. Disabled further permission processing and granted all rights!");
			return true;
		} else {
			// if a franchise administrator setting is accessed, but the user does not have the
			// according rights
			if(targetDomainObject instanceof String && targetDomainObject.equals("franchise"))
				return false;
		}


		// used in thymeleaf template
		if(targetDomainObject instanceof AbstractProject && permission instanceof String) {

			logger.debug("Evaluating project: " + ((AbstractProject) targetDomainObject).getUrlidentifier());

			List<String> role = roles.getRoles(((AbstractProject) targetDomainObject).getUrlidentifier(), username);
			logger.debug("Roles of user: " + role.toString());

			if(permission.equals("projectadmin") && role.contains("PROJECT_OWNER")) {
				logger.debug("User is project administrator! Authorizing ...");
				return true;
			}

		}

		// used for controller methods
		if(permission instanceof String && targetDomainObject instanceof String) {
			logger.debug("Evaluating project: " + ((String) targetDomainObject));

			List<String> role = roles.getRoles((String) targetDomainObject, username);
			logger.debug("Roles of user: " + role.toString());

			if(permission.equals("projectadmin") && role.contains("PROJECT_OWNER")) {
				logger.debug("User is project administrator! Authorizing ...");
				return true;
			}
		}
		logger.info("No suitable authorization found for: " + username);
		logger.debug("End of permission evaluation ...");
		logger.debug("--------------------------------------------------");
		return false;
	}


	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
								 Object permission) {
		throw new UnsupportedOperationException("ID based permission evaluation currently not supported.");
	}
}