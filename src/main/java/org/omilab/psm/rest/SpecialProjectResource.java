// File:         SpecialProjectResource.java
// Created:      29.07.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.rest;

import org.omilab.psm.repo.AbstractProjectRepository;
import org.omilab.psm.repo.GenericProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.ArrayList;
import java.util.List;

@Component
@Path("/special")
public class SpecialProjectResource {


	private final GenericProjectRepository projects;

	@Autowired
	public SpecialProjectResource(final GenericProjectRepository projects) {
		this.projects = projects;
	}

	@Path("/UUID")
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	public List<String> receiveProjectListByUUID(final List<String> ids) {
		List<String> tempList = new ArrayList<>();
		for(String id : ids)
			tempList.add(projects.findByUniqueID(id).getAbbreviation());
		return tempList;
	}

	@Path("/URLIDENT")
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	public List<String> receiveProjectListByURLIdentifier(final List<String> urls) {
		List<String> tempList = new ArrayList<>();
		for(String url : urls)
			tempList.add(projects.findByAbbreviation(url).getUniqueID());
		return tempList;
	}


}
