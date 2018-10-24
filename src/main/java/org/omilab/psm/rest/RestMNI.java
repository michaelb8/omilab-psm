// File:         RestMNI.java
// Created:      14.02.16
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
//

package org.omilab.psm.rest;

import org.omilab.psm.model.db.*;
import org.omilab.psm.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import java.util.ArrayList;
import java.util.List;

@Component
@Path("/mni")
public class RestMNI {

	private final MainNavigationGenericRepository mniRepo;
	private final MainNavigationTypesRepository typesRepo;
	private final MainNavigationHTMLRepository htmlRepo;
	private final MainNavigationProjectRepository projRepo;

	@Autowired
	public RestMNI(final MainNavigationGenericRepository mniRepo, final MainNavigationTypesRepository typesRepo,
				   final MainNavigationHTMLRepository htmlRepo, final MainNavigationProjectRepository projRepo) {
		this.mniRepo = mniRepo;
		this.typesRepo = typesRepo;
		this.htmlRepo = htmlRepo;
		this.projRepo = projRepo;
	}


	@GET
	@Produces("application/json")
	@Consumes("application/json")
	public List<MainNavigationItem> listAllMNIs() {
		return mniRepo.findAll();
	}

	@GET
	@Path("/{id}/")
	@Produces("application/json")
	@Consumes("application/json")
	public MainNavigationItem listMNI(final @PathParam("id") Long id) {
		MainNavigationItem mni = mniRepo.findById(id);
		if(mni instanceof MainNavigationItemTypes)
			return typesRepo.findById(id);
		if(mni instanceof MainNavigationItemHTML)
			return htmlRepo.findById(id);
		if(mni instanceof MainNavigationItemProject)
			return projRepo.findById(id);
		return mni;
	}


}
