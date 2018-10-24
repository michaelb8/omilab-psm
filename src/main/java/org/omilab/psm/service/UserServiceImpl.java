// File:         UserServiceImpl.java
// Created:      18.02.2015
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.service;

import org.omilab.psm.model.wrapper.User;
//import org.omilab.psm.service.ldap.DirectoryService;
//import org.omilab.psm.service.ldap.LDAPUser;
import org.omilab.psm.service.ldap.DirectoryService;
import org.omilab.psm.service.ldap.LDAPUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("UserService")
@Transactional
@SuppressWarnings("unused")
public final class UserServiceImpl implements UserService {

	private final DirectoryService ds;

	@Autowired
	public UserServiceImpl(final DirectoryService ds) {
		this.ds = ds;
	}

	public LDAPUser queryUserDetails() {
		return ds.lookupPerson(getCurrentLogin());
	}

	@Override
	public LDAPUser querySpecificUser(final String commonName) {
		return ds.lookupPerson(commonName);
	}

	@Override
	public User getCurrentUser() {
		return new User(getCurrentLogin(), null, null, null, null);
	}

	private static String getCurrentLogin() {
		SecurityContext securityContext = SecurityContextHolder.getContext();
		Authentication authentication = securityContext.getAuthentication();
		UserDetails springSecurityUser = null;
		String userName = null;

		if(authentication != null) {
			if(authentication.getPrincipal() instanceof UserDetails) {
				springSecurityUser = (UserDetails) authentication.getPrincipal();
				userName = springSecurityUser.getUsername();
			} else if(authentication.getPrincipal() instanceof String) {
				userName = (String) authentication.getPrincipal();
			}
		}
		return userName;
	}
}
