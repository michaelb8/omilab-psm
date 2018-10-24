// File:         ServiceDefinitionServiceImpl.java
// Created:      15.02.2015
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.omilab.psm.model.db.DBNavigationItem;
import org.omilab.psm.model.db.ServiceDefinition;
import org.omilab.psm.repo.ServiceDefinitionRepository;
import org.omilab.psm.repo.ServiceNavigationRepository;
import org.omilab.psm.service.logging.LogMessage;
import org.omilab.psm.service.logging.LoggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@Component("ServiceDefinitionService")
@Transactional
@SuppressWarnings("unused")
public final class ServiceDefinitionServiceImpl implements ServiceDefinitionService {

	private static final Logger logger = LoggerFactory.getLogger(ServiceDefinitionServiceImpl.class);

	private final LoggingService logService;

	private final ServiceNavigationRepository navigationRepo;

	private final ServiceDefinitionRepository serviceRepo;

	private final UserService userService;

	@Autowired
	public ServiceDefinitionServiceImpl(ServiceDefinitionRepository serviceRepo,
										ServiceNavigationRepository navigationRepo, UserService userService,
										LoggingService logService) {
		this.serviceRepo = serviceRepo;
		this.navigationRepo = navigationRepo;
		this.userService = userService;
		this.logService = logService;
	}

	@Override
	public void createServiceDefinition(final MultipartFile file) throws IOException {

		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode rawSd, navItems;

		rawSd = mapper.readTree(file.getBytes());

		final ServiceDefinition sd = mapper.readValue(rawSd.traverse(), ServiceDefinition.class);
		addServiceDefinition(sd);
		logger.debug("Added ServiceDefinition with id: " + sd.getId() + " and title: " + sd.getName());

		navItems = rawSd.get("servicenavigationitems");
		for(int i = 0; i < navItems.size(); i++) {
			final DBNavigationItem dbitem = mapper.readValue(navItems.get(i).traverse(), DBNavigationItem.class);
			dbitem.setServicedefinition(sd);
			addServiceNavigationItem(dbitem);
			logger.debug("Added NavigationItem with id: " + dbitem.getId() + " and title: " + dbitem.getName());
		}
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "update", sd.getClass().getSimpleName(), Long.toString(sd.getId())));

	}

	@Override
	public void disableServiceDefinition(final Long id) {
		final ServiceDefinition sd = serviceRepo.findById(id);
		sd.makeInvisible();
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "disable", sd.getClass().getSimpleName(), Long.toString(id)));
		logger.debug("Disabled ServiceDefinition with id: " + sd.getId() + " and title: " + sd.getName());
	}

	@Override
	public void enableServiceDefinition(final Long id) {
		final ServiceDefinition sd = serviceRepo.findById(id);
		sd.makeVisible();
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "enable", sd.getClass().getSimpleName(), Long.toString(id)));
		logger.debug("Enabled ServiceDefinition with id: " + sd.getId() + " and title: " + sd.getName());
	}

	@Override
	public List<DBNavigationItem> getAllNavigationItems() {
		return navigationRepo.findAllBySeqAsc();
	}

	@Override
	public ServiceDefinition getServiceDefinition(final String urlidentifier) {
		return this.serviceRepo.findByUrlidentifier(urlidentifier);
	}

	@Override
	public ServiceDefinition getServiceDefinition(final Long id) {
		return serviceRepo.findById(id);
	}

	@Override
	public Page<ServiceDefinition> getServiceDefinitionPage(final Pageable pageable) {
		return this.serviceRepo.findAll(pageable);
	}

	@Override
	public Page<ServiceDefinition> getVisibleServiceDefinitionPage(final Pageable pageable) {
		return this.serviceRepo.findByVisibleTrue(pageable);
	}

	@Override
	public void saveNavigationItemOrder(final List<Long> items) {
		for(Long item : items) {
			navigationRepo.updateOrderForItem(items.indexOf(item), item);
		}
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "update", "Franchise", "0"));
	}

	private void addServiceNavigationItem(final DBNavigationItem item) {
		navigationRepo.save(item);
		int highest = navigationRepo.findHighest();
		item.setSeq(highest+1);
	}

	private void addServiceDefinition(final ServiceDefinition service) {
		final ServiceDefinition sd = serviceRepo.save(service);
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "create", sd.getClass().getSimpleName(), Long.toString(sd.getId())));
	}

	@Override
	public List<DBNavigationItem> getNavigationItemsByServiceDefinition(final ServiceDefinition sd) {
		return navigationRepo.findNavigationItemsForService(sd);
	}

}
