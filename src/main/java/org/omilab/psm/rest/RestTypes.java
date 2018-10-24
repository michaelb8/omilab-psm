// File:         RestTypes.java
// Created:      14.02.16
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
//

package org.omilab.psm.rest;

import org.omilab.psm.model.db.*;
import org.omilab.psm.model.db.projectoverlay.Event;
import org.omilab.psm.model.db.projectoverlay.MMProject;
import org.omilab.psm.repo.*;
import org.omilab.psm.repo.projectoverlays.EventRepository;
import org.omilab.psm.repo.projectoverlays.MMProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import java.util.List;
import java.util.Set;

@Component
@Path("/projecttype")
public class RestTypes {

	private final ProjectTypeRepository typesRepo;
	private final GenericProjectRepository projectRepo;
	private final EventRepository eventRepo;
	private final MMProjectRepository mmprojRepo;
	private final ServiceInstanceRepository instanceRepo;

	@Autowired
	public RestTypes(final ProjectTypeRepository typesRepo, final GenericProjectRepository projectRepo,
					 final EventRepository eventRepo, final MMProjectRepository mmprojRepo,
					 final ServiceInstanceRepository instanceRepo) {
		this.typesRepo = typesRepo;
		this.projectRepo = projectRepo;
		this.eventRepo = eventRepo;
		this.mmprojRepo = mmprojRepo;
		this.instanceRepo = instanceRepo;
	}

	@GET
	@Produces("application/json")
	@Consumes("application/json")
	public List<ProjectType> listAllPTs() {
		return typesRepo.findAll();
	}

	@GET
	@Path("/{id}/")
	@Produces("application/json")
	@Consumes("application/json")
	public ProjectType listPT(final @PathParam("id") Long id) {
		return typesRepo.findById(id);
	}

	@GET
	@Path("/{id}/project")
	@Produces("application/json")
	@Consumes("application/json")
	public List<AbstractProject> listAllProjects(final @PathParam("id") Long id) {
		return typesRepo.findById(id).getProjects();
	}

	@GET
	@Path("/{sid}/endpoint")
	@Produces("application/json")
	@Consumes("application/json")
	public List<DBNavigationItem> listAllEndpoint(final @PathParam("sid") Long id) {
		return typesRepo.findDBNavigationItems(id);
	}

	@GET
	@Path("/{sid}/endpoint/{eid}")
	@Produces("application/json")
	@Consumes("application/json")
	public DBNavigationItem listEndpoint(final @PathParam("sid") Long sid, final @PathParam("eid") Long eid) {
		List<DBNavigationItem> items = typesRepo.findDBNavigationItems(sid);
		for(DBNavigationItem item : items) {
			if(item.getId().equals(eid))
				return item;
		}
		return null;
	}

	private Boolean checkProjectPresence(ProjectType type, AbstractProject project) {
		Boolean presence = false;
		for(AbstractProject ap : type.getProjects() ) {
			if(ap.getId().equals(project.getId()))
				presence = true;
				return presence;
		}
		return presence;
	}

	@GET
	@Path("/{id}/project/{pid}")
	@Produces("application/json")
	@Consumes("application/json")
	public AbstractProject listProject(final @PathParam("pid") Long pid, final @PathParam("id") Long id) {
		AbstractProject ab = projectRepo.findById(pid);
		if(ab == null) {
			return null;
		}
		if(!checkProjectPresence(typesRepo.findById(id),ab))
			return null;
		if(ab instanceof MMProject)
			return mmprojRepo.findById(pid);
		if(ab instanceof Event)
			return eventRepo.findById(pid);
		return ab;
	}

	@GET
	@Path("/{id}/project/{sid}/instance")
	@Produces("application/json")
	@Consumes("application/json")
	public List<ServiceInstance> listAllInstance(final @PathParam("sid") Long id) {
		return instanceRepo.findByProject(projectRepo.findById(id));
	}


}
