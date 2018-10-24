// File:         RestServices.java
// Created:      08.06.16
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
//

package org.omilab.psm.rest;

import org.omilab.psm.model.db.DBNavigationItem;
import org.omilab.psm.model.db.ProjectType;
import org.omilab.psm.model.db.ServiceDefinition;
import org.omilab.psm.repo.ProjectTypeRepository;
import org.omilab.psm.repo.ServiceDefinitionRepository;
import org.omilab.psm.repo.ServiceNavigationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import java.util.List;

@Component
@Path("/service")
public class RestService {

    private final ServiceNavigationRepository servicenaviRepo;
    private final ServiceDefinitionRepository serviceRepo;

    @Autowired
    public RestService(ServiceNavigationRepository servicenaviRepo, ServiceDefinitionRepository serviceRepo) {
        this.servicenaviRepo = servicenaviRepo;
        this.serviceRepo = serviceRepo;
    }

    @GET
    @Produces("application/json")
    @Consumes("application/json")
    public List<ServiceDefinition> listAllServices() {
        return serviceRepo.findAll();
    }

    @GET
    @Path("/{sid}/")
    @Produces("application/json")
    @Consumes("application/json")
    public ServiceDefinition listService(final @PathParam("sid") Long id) {
        return serviceRepo.findById(id);
    }

    @GET
    @Path("/{sid}/endpoint")
    @Produces("application/json")
    @Consumes("application/json")
    public List<DBNavigationItem> listAllEndpoint(final @PathParam("sid") Long id) {
        return servicenaviRepo.findNavigationItemsForService(serviceRepo.findById(id));
    }

    @GET
    @Path("/{sid}/endpoint/{eid}")
    @Produces("application/json")
    @Consumes("application/json")
    public DBNavigationItem listEndpoint(final @PathParam("sid") Long sid, final @PathParam("eid") Long eid) {
        List<DBNavigationItem> items = servicenaviRepo.findNavigationItemsForService(serviceRepo.findById(sid));
        for(DBNavigationItem item : items) {
            if(item.getId().equals(eid))
                return item;
        }
        return null;
    }

}
