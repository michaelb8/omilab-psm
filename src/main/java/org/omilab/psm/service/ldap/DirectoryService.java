// File:         DirectoryService.java
// Created:      23.04.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.service.ldap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;

import javax.naming.directory.SearchControls;
import java.util.List;

@Service
public final class DirectoryService {

	private final LdapTemplate ldapTemplate;

	@Autowired
	public DirectoryService(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	public List<LDAPUser> listAllUsers() {
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		List<LDAPUser> users = ldapTemplate.search("ou=people", "(objectClass=person)", new LDAPUserAttributesMapper());
		return users;
	}

	public LDAPUser lookupPerson(String username) {
		return (LDAPUser) ldapTemplate.lookup("cn=" + username + ",ou=people", new LDAPUserAttributesMapper());
	}

	public List<LDAPUser> searchPerson(String usernamepattern) {
		List<LDAPUser> users = ldapTemplate.search("ou=people", "(&(objectClass=person)(sn=" + usernamepattern + "*))", new LDAPUserAttributesMapper());
		return users;
	}

	public Boolean checkExistance(String search) {
		try {
			lookupPerson(search);
		} catch(Exception e) {
			return false;
		}
		return true;
	}


}