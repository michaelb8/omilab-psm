// File:         RoleServiceImpl.java
// Created:      12.05.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.service.role;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.omilab.psm.model.db.AbstractProject;
import org.omilab.psm.repo.AbstractProjectRepository;
import org.omilab.psm.repo.GenericProjectRepository;
import org.omilab.psm.repo.ServiceInstanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component("RoleService")
public final class RoleServiceImpl implements RoleService {

	private static final Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);

	private final Environment env;

	private final List<String> franchisadmins;

	private final ServiceInstanceRepository instances;

	private final GenericProjectRepository projects;

	private final Cache<String, Map<String, List<String>>> rolecache;

	private final Boolean cacheEnabled;

	@Autowired
	public RoleServiceImpl(final Environment env, final ServiceInstanceRepository instances,
						   final GenericProjectRepository projects) {
		this.env = env;
		this.instances = instances;
		this.projects = projects;
		franchisadmins = new ArrayList(Arrays.asList(env.getProperty("omilab.admin").split(",")));
		this.cacheEnabled = Boolean.getBoolean(env.getProperty("omilab.role.cache"));
		if(cacheEnabled) {
			rolecache = CacheBuilder.newBuilder().maximumSize(400).expireAfterWrite(Long.parseLong(env.getProperty("omilab.role.cachtime")), TimeUnit.MINUTES).build();
		} else
			rolecache = null;

	}

	private List<Role> queryService(final AbstractProject project, final String username) {

		List<Role> roles = null;

		WebTarget target = null;
		try {
			final Client client = ClientBuilder.newClient();
			target = client.target(env.getProperty("omilab.role") + "/rest/role/instance/" + instances.findSpecialServiceInstanceIdForProject(project, "role").getInstanceidremote() + "/user/" + username);
			roles = target.request().get(new GenericType<List<Role>>() {
			});
		} catch(Exception e) {
			logger.error("Could NOT reach role service (incl query url) at: " + env.getProperty("omilab.role") + " ! Please check config!");
			if(target != null)
				logger.debug("Query role service with the following url: " + target.toString());
		}
		if(isFranchiseAdmin(username)) {
			if(roles == null)
				roles = new ArrayList<>();
			roles.add(new Role("PROJECT_OWNER"));
		}

		return roles;
	}

	@Override
	public List<String> getRoles(final String projecturl, final String username) {

		if(username.equals("anonymousUser"))
			return new ArrayList<String>();

		if(cacheEnabled) {
			// check if is already in cache
			Map<String, List<String>> entry = rolecache.getIfPresent(username);
			if(entry != null && entry.get(projecturl) != null)
				return entry.get(projecturl);
		}

		AbstractProject project = projects.findByUrlidentifier(projecturl);
		List<Role> roles = queryService(project, username);
		List<String> rolesString = new ArrayList<>();
		for(Role role : roles) {
			rolesString.add(role.getRole());
		}

		if(cacheEnabled) {
			// add entry to cache if lookup had to be performed
			putInCache(projecturl, username, rolesString);
		}

		return rolesString;
	}

	private void putInCache(final String projecturl, final String username, List<String> roles) {
		// if user had no roles within a project, put an empty list there, in order to remember
		// that no roles are given
		if(roles == null)
			roles = new ArrayList<>();
		Map<String, List<String>> entry = new HashMap<>();
		entry.put(projecturl, roles);
		rolecache.put(username, entry);
	}

	@Override
	public Boolean isFranchiseAdmin(final String username) {
		return franchisadmins.contains(username);
	}
}
