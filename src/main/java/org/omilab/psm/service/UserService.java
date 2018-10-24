// File:         UserService.java
// Created:      18.02.2015
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.service;

import org.omilab.psm.model.wrapper.User;
import org.omilab.psm.model.wrapper.UserLoginInformation;
import org.omilab.psm.service.ldap.LDAPUser;
import org.omilab.psm.service.ldap.LDAPUser;

import java.util.HashMap;
import java.util.Map;

public interface UserService {

	public static final Map<String,UserLoginInformation> activeUsers = new HashMap<>();

	public LDAPUser querySpecificUser(String commonName);

	public LDAPUser queryUserDetails();

	public User getCurrentUser();
}
