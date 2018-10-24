// File:         ServiceManagementServiceImpl.java
// Created:      19.02.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.service;


import org.omilab.psm.model.db.*;
import org.omilab.psm.model.wrapper.GenericRequest;
import org.omilab.psm.model.wrapper.UINavigationItem;
import org.omilab.psm.repo.ProjectNavigationRepository;
import org.omilab.psm.repo.ServiceDefinitionRepository;
import org.omilab.psm.repo.ServiceInstanceRepository;
import org.omilab.psm.repo.ServiceNavigationRepository;
import org.omilab.psm.service.logging.LogMessage;
import org.omilab.psm.service.logging.LoggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

@Component("ServiceManagementService")
@Transactional
@SuppressWarnings("unused")
public final class ServiceManagementServiceImpl implements ServiceManagementService {

	private static final Logger logger = LoggerFactory.getLogger(ServiceManagementServiceImpl.class);

	private final ApplicationContext ctx;

	private final Environment env;

	private final ServiceInstanceRepository instanceRepo;

	private final LoggingService logService;

	private final ServiceNavigationRepository navigationRepo;

	private final ProjectNavigationRepository navigationprojectRepo;

	private final ProjectTypeService projectTypeService;

	private final ServiceDefinitionRepository serviceRepo;

	private final ProjectService projects;

	private final UserService userService;


	@Autowired
	public ServiceManagementServiceImpl(final ServiceInstanceRepository instanceRepo,
										final ServiceNavigationRepository navigationRepo,
										final ServiceDefinitionRepository serviceRepo,
										final ProjectNavigationRepository navigationprojectRepo, final LoggingService logService,
										final UserService userService, final ProjectTypeService projectTypeService, final Environment env,
										final ApplicationContext ctx, ProjectService projects) {
		this.instanceRepo = instanceRepo;
		this.navigationRepo = navigationRepo;
		this.serviceRepo = serviceRepo;
		this.navigationprojectRepo = navigationprojectRepo;
		this.logService = logService;
		this.userService = userService;
		this.projectTypeService = projectTypeService;
		this.env = env;
		this.ctx = ctx;
		this.projects = projects;
	}

	@Override
	public void changeNavigationStatus(final List<Long> updatedItems, final AbstractProject project) {
		final List<DBNavigationItem> dbItems = generateMenuSelection(project);
		for(DBNavigationItem dbitem : dbItems) {
			if((!dbitem.getMandatory()) && instanceRepo.findByProjectAndServicedefinition(project, dbitem.getServicedefinition()) != null) {
				if(updatedItems.contains(dbitem.getId())) {
					if(!navigationprojectRepo.existsByProjectAndDBNavigation(project, dbitem)) {
						navigationprojectRepo.save(new Project_Navigation(project, dbitem));
						logger.debug("Setting " + dbitem.getName() + " for " + project.getName() + " to active");
					}
				} else {
					navigationprojectRepo.deleteByProjectAndDBNavigationItem(project.getId(), dbitem.getId());
					logger.debug("Setting " + dbitem.getName() + " for " + project.getName() + " to inactive");
				}
			}
		}
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "update", project.getClass().getSimpleName(), Long.toString(project.getId())));
	}

	public void activateNavigation(final AbstractProject project, final DBNavigationItem item) {
		if(instanceRepo.findByProjectAndServicedefinition(project, item.getServicedefinition()) != null)
			navigationprojectRepo.save(new Project_Navigation(project, item));

	}

	@Override
	public Boolean checkEnabled(final AbstractProject project, final ServiceDefinition sd, final String endpoint) {
		return checkEnabled(project, navigationRepo.findNavigationItemByEndpoint(sd, endpoint));
	}

	@Override
	public Boolean checkServiceAvailability(final Long project, final Long servicedefinition) {
		if(serviceRepo.findById(servicedefinition).getSpecial() != null)
			return true;
		/* else if(navigationRepo.checkActivedEndpointsForProjectAndService(project, servicedefinition).size() > 0)
			return true;*/
		if(checkInstance(projects.getProject(project),serviceRepo.findById(servicedefinition)))
			return true;
		final ProjectType type = projects.getProject(project).getProjecttype();
		for(DBNavigationItem item : serviceRepo.findById(servicedefinition).getServicenavigationitems()) {
			if(type.getItems().contains(item))
				return true;
		}
		return false;
	}

	@Override
	public Boolean checkEndpointAvailability(final Long project, final Long endpoint) {
		final ProjectType type = projects.getProject(project).getProjecttype();
		final DBNavigationItem item = navigationRepo.findById(endpoint);
		return type.getItems().contains(item);
	}

	private Boolean checkInstance(final AbstractProject project, final ServiceDefinition sd) {
		if(instanceRepo.findByProjectAndServicedefinition(project, sd) != null)
			return true;
		return false;
	}

	@Async
	@Override
	public void createEndpoint(final String name, final String endpoint, final Boolean mandatory,
							   final Long servicedefinition) {

		ServiceDefinition sd = serviceRepo.findById(servicedefinition);
		Integer highest = navigationRepo.findHighest();
		if(highest == null)
			highest = 1;
		DBNavigationItem item = navigationRepo.save(new DBNavigationItem(name, endpoint, mandatory, highest+1, sd));
		for(ProjectType type : projectTypeService.getTypes()) {
			projectTypeService.refreshProjectsByType(type);
		}
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "create", item.getClass().getSimpleName(), Long.toString(item.getId())));
	}

	@Async
	@Override
	public void updateEndpoint(final String name, final String endpoint, final Boolean mandatory, final Long id) {
		final DBNavigationItem item = navigationRepo.findById(id);
		item.setName(name);
		item.setEndpoint(endpoint);
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "update", item.getClass().getSimpleName(), Long.toString(item.getId())));
		if(!item.getMandatory().equals(mandatory)) {
			item.setMandatory(mandatory);
			for (ProjectType type : projectTypeService.getTypes()) {
				projectTypeService.refreshProjectsByType(type);
			}
		}
	}

	@Override
	public void deleteEndpoint(final Long id) {
		navigationprojectRepo.deleteByDBNavigationItem(id);
		projectTypeService.removeDBNavigationItem(id);
		navigationRepo.delete(navigationRepo.findById(id));
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "create", "DBNavigationItem", Long.toString(id)));
	}

	@Override
	public Boolean deleteServiceRecursivley(final ServiceDefinition sd, final Boolean force) throws IOException {
		final List<AbstractProject> projects = getServiceUsage(sd);
		for(AbstractProject project : projects) {
			if(!unsubscribeService(project,sd,force)) {
				if(!force)
					return false;
			}
		}

		navigationprojectRepo.deleteByServiceDefinition(sd.getId());
		navigationRepo.deleteByServicedefinition(sd);
		serviceRepo.delete(sd);
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "delete", sd.getClass().getSimpleName(), Long.toString(sd.getId())));

		logger.debug("Sucessfully unsubscribed all projects from service" + sd.getName());

		return true;
	}

	@Override
	public List<DBNavigationItem> generateMenuSelection(final AbstractProject project) {
		final List<ServiceDefinition> sds = serviceRepo.findByProject(project);
		final List<DBNavigationItem> dbItems = new ArrayList<>();

		for(ServiceDefinition sd : sds) {
			for(DBNavigationItem item : navigationRepo.findNavigationItemsForService(sd)) {
				if(projectTypeService.getAllowedEndpoints(project.getProjecttype()).contains(item.getEndpoint()))
					dbItems.add(item);
			}
		}
		return dbItems;
	}

	@Override
	public List<UINavigationItem> generateNavigationMenu(final AbstractProject project, final ServiceDefinition sd,
														 final String endpoint, final Locale locale) {

		final List<DBNavigationItem> dbItems = navigationRepo.findNavigationItemsForProject(project);
		final List<UINavigationItem> uiItems = new ArrayList<>();

		Boolean active;
		for(DBNavigationItem item : dbItems) {
			active = (sd != null && item.getEndpoint().equals(endpoint) && item.getServicedefinition().getName().equals(sd.getName()));
			if(projectTypeService.getAllowedEndpoints(project.getProjecttype()).contains(item.getEndpoint()))
				uiItems.add(new UINavigationItem(item, active));
		}

		// special handling for role menu point
		active = (sd == null && endpoint.equals("roles"));
		uiItems.add(new UINavigationItem(getMessage("general.layout.roles", locale), "role", active, true, true, 0L));

		// special handling for settings menu point
		active = (sd == null && endpoint.equals("settings"));
		uiItems.add(new UINavigationItem(getMessage("general.layout.settings", locale), "settings", active, true, true, 0L));


		return uiItems;
	}

	@Override
	public List<DBNavigationItem> getAllEnabledMenus(final AbstractProject project) {
		return navigationRepo.findNavigationItemsForProject(project);
	}

	@Override
	public String getFirstEndpoint(final ServiceDefinition sd, final AbstractProject project) {
		final List<DBNavigationItem> dbItems = navigationRepo.findNavigationItemsForService(sd);
		for(DBNavigationItem dbItem : dbItems) {
			if(checkEnabled(project, dbItem))
				return dbItem.getEndpoint();
		}
		return "";
	}

	@Override
	public String getFirstService(final AbstractProject project) {
		final List<DBNavigationItem> dbItems = navigationRepo.findNavigationItemsForProject(project);
		if(!dbItems.isEmpty())
			return dbItems.get(0).getServicedefinition().getUrlidentifier();
		else
			return "settings";
	}

	@Override
	public List<ServiceDefinition> getServiceDefinitionByProject(final AbstractProject project) {
		final List<ServiceDefinition> sd = new ArrayList<>();
		for(ServiceInstance si : instanceRepo.findByProject(project)) {
			sd.add(si.getServicedefinition());
		}
		return sd;
	}

	@Override
	public ServiceInstance getServiceInstance(final AbstractProject project, final ServiceDefinition sd) {
		return this.instanceRepo.findByProjectAndServicedefinition(project, sd);
	}

	@Override
	public ServiceInstance getServiceInstanceOfSpecialService(final AbstractProject project, final String special) {
		return instanceRepo.findSpecialServiceInstanceIdForProject(project, special);
	}

	@Override
	public List<AbstractProject> getServiceUsage(final ServiceDefinition sd) {
		return instanceRepo.findProjectUsageByServiceDefinitionID(sd);
	}

	@Override
	public Boolean instantiateService(final AbstractProject project, final ServiceDefinition sd) throws IOException {

		final Long remoteID = createRemoteInstance(sd, project);

		if(remoteID == 0) {
			return false;
		}

		//force addition menu point, if it is mandatory and not already existing
		for(DBNavigationItem item : navigationRepo.findNavigationItemsForService(sd)) {
			if((!navigationprojectRepo.existsByProjectAndDBNavigation(project, item)) && item.getMandatory() && projectTypeService.getAllowedEndpoints(project.getProjecttype()).contains(item.getEndpoint()))
				navigationprojectRepo.save(new Project_Navigation(project, item));
		}

		final ServiceInstance si = new ServiceInstance(remoteID, project, sd);
		instanceRepo.save(si);

		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "instantiate", project.getClass().getSimpleName(), Long.toString(project.getId())));
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "instantiate", sd.getClass().getSimpleName(), Long.toString(sd.getId())));
		return true;
	}

	private synchronized long createRemoteInstance(final ServiceDefinition sd, final AbstractProject project) {
		try {
			final Map<String, String> params = new HashMap<>();
			params.put("mode", "create");

			final String url;
			if(Boolean.getBoolean(env.getProperty("omilab.url.psm.override")))
				url = env.getProperty("omilab.url.psm");
			else
				url = InetAddress.getLocalHost().getHostName();

			params.put("psmurl", url);
			if(project != null) {
				params.put("uniqueID", project.getUniqueID());
				params.put("projecturl", project.getUrlidentifier());
			}

			final GenericRequest gr = new GenericRequest(null, null, params);

			final Client client = ClientBuilder.newClient();
			final WebTarget target = client.target(sd.getUrl() + "instanceMgmt");
			logger.debug("Instantiating " + sd.getName() + " at " + target.toString() + " with params: " + gr.toString());
			final String id = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(gr, MediaType.APPLICATION_JSON), String.class);
			logger.debug("Instantiated with remote id: " + id);

			return Long.parseLong(id);
		} catch(Exception e) {
			logger.warn("Creation of remote instance failed with: " + e.getMessage());
			logger.error("Creation of remote instance failed with: ", e);
			return 0;
		}

	}

	@Override
	public synchronized Boolean instantiateSpecialService(final AbstractProject project, final String special) throws IOException {
		ServiceDefinition sd = serviceRepo.findBySpecial(special);
		if(sd != null) {
			instantiateService(project, serviceRepo.findBySpecial(special));
			logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "instantiate", project.getClass().getSimpleName(), Long.toString(project.getId())));
			logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "instantiate", sd.getClass().getSimpleName(), Long.toString(sd.getId())));
		}
		else
			logger.error("Could not find special service: " + special);
		return true;
	}

	@Override
	public Boolean unsubscribeService(final AbstractProject project, final ServiceDefinition service, final Boolean force) throws IOException {
		if(!deleteRemoteInstance(service, project)) {
			if(!force)
				return false;
		}
		instanceRepo.deleteByProjectAndServicedefinition(project, service);
		navigationprojectRepo.deleteByServiceDefinitionAndProject(service.getId(), project.getId());
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "unsubscribe", project.getClass().getSimpleName(), Long.toString(project.getId())));
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "unsubscribe", service.getClass().getSimpleName(), Long.toString(service.getId())));
		return true;
	}

	private boolean deleteRemoteInstance(final ServiceDefinition sd, final AbstractProject project) {
		try {
			final Map<String, String> params = new HashMap<>();
			params.put("mode", "delete");
			final Long instanceid = instanceRepo.findByProjectAndServicedefinition(project, sd).getInstanceidremote();
			params.put("instanceid", instanceid.toString());

			final GenericRequest gr = new GenericRequest(null, null, params);

			final Client client = ClientBuilder.newClient();
			final WebTarget target = client.target(sd.getUrl() + "instanceMgmt");
			final String response = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(gr, MediaType.APPLICATION_JSON), String.class);

			return Boolean.parseBoolean(response);
		} catch(Exception e) {
			logger.warn("Removal of remote instance failed with: " + e.getMessage());
			logger.error("Removal of remote instance failed with: ", e);
			return false;
		}
	}

	private String getMessage(final String key, final Locale locale) {
		final MessageSource messageSource = (MessageSource) ctx.getBean("messageSource");
		try {
			return messageSource.getMessage(key, null, locale);
		} catch(NoSuchMessageException e) {
			return messageSource.getMessage(key, null, Locale.ENGLISH);
		}
	}

	public Boolean checkEnabled(final AbstractProject project, final DBNavigationItem dbitem) {
		if(!navigationprojectRepo.existsByProjectAndDBNavigation(project, dbitem))
			return false;
		return true;
	}

	@Override
	public List<DBNavigationItem> getAllMenus(final ServiceDefinition sd) {
		return navigationRepo.findNavigationItemsForService(sd);
	}

	@Override
	public DBNavigationItem getMenu(final Long id) {
		return navigationRepo.findById(id);
	}


	@Override
	public Boolean unsubscribeAllServices(final AbstractProject project, final Boolean force) throws IOException {
		Boolean result = true;
		for(ServiceDefinition sd : serviceRepo.findByProject(project)) {
			logger.debug("Found Subscription to Service: " + sd.getName() + " Removing instance for " + project.getName());
			if(!unsubscribeService(project,sd,force)) {
				logger.error("Project removal failed!. Failed to unsubscribe \"" + sd.getName() + "\" for \"" + project.getName() + "\". Check if the service is available.");
				result = false;
			}
		}
		if(force)
			result = true;
		return result;
	}

}
