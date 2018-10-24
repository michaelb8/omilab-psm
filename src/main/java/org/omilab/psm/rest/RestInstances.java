// File:         RestInstances.java
// Created:      14.11.16
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.rest;

import org.omilab.psm.model.db.AbstractProject;
import org.omilab.psm.model.db.ServiceDefinition;
import org.omilab.psm.model.db.ServiceInstance;
import org.omilab.psm.repo.ServiceInstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import java.util.List;

@Component
@Path("/instance")
public class RestInstances {

	private final ServiceInstanceRepository instanceRepo;

	@Autowired
	public RestInstances(final ServiceInstanceRepository instanceRepo) {
		this.instanceRepo = instanceRepo;
	}

	@GET
	@Produces("application/json")
	@Consumes("application/json")
	public List<ServiceInstance> listAllPTs() {
		return instanceRepo.findAll();
	}

	@GET
	@Path("/{id}/")
	@Produces("application/json")
	@Consumes("application/json")
	public ServiceInstance listPT(final @PathParam("id") Long id) {
		return instanceRepo.findByInstanceidlocal(id);
	}

	@GET
	@Path("/{id}/project")
	@Produces("application/json")
	@Consumes("application/json")
	public AbstractProject listAllProjects(final @PathParam("id") Long id) {
		return instanceRepo.findByInstanceidlocal(id).getProject();
	}

	@GET
	@Path("/{id}/service")
	@Produces("application/json")
	@Consumes("application/json")
	public ServiceDefinition listAllServiceDefinitions(final @PathParam("id") Long id) {
		return instanceRepo.findByInstanceidlocal(id).getServicedefinition();
	}

}
