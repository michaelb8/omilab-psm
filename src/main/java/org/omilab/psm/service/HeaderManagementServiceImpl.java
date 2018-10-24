// File:         HeaderManagementImpl.java
// Created:      21.05.2015
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//


package org.omilab.psm.service;

import org.omilab.psm.model.db.HeaderConfiguration;
import org.omilab.psm.repo.HeaderRepository;
import org.omilab.psm.service.logging.LogMessage;
import org.omilab.psm.service.logging.LoggingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component("HeaderManagementService")
@Transactional
public final class HeaderManagementServiceImpl implements HeaderManagementService {

	private final HeaderRepository headerRepo;

	private final LoggingService logService;

	private final UserService users;

	@Autowired
	public HeaderManagementServiceImpl(HeaderRepository headerRepo, LoggingService logService, UserService users) {
		this.headerRepo = headerRepo;
		this.logService = logService;
		this.users = users;
	}

	private void checkInvalid() {
		// In order to produce valid html for bootstrap it is necessary to
		// have at least one visible element set as initial element or
		// there will be strange effects
		// This method makes sure this case handled and the initial
		// element is automagically set to the first visible one
		if((!headerRepo.existsActiveStarterHeader()) && headerRepo.findVisibleHeaders().size() > 0) {
			for(HeaderConfiguration header : headerRepo.findAll()) {
				header.disableStarter();
			}
			headerRepo.findVisibleHeaders().get(0).makeStarter();
		}
	}


	@Override
	public void addHTMLHeader(final String content, final String title) {
		HeaderConfiguration hc = headerRepo.save(new HeaderConfiguration(content, title, "HTML", false));
		logService.logMessage(new LogMessage(users.getCurrentUser().getUsername(), "create", "HeaderConfiguration", Long.toString(hc.getId())));
	}

	@Override
	public void addImageHeader(final String content, final String title) {
		HeaderConfiguration hc = headerRepo.save(new HeaderConfiguration(content, title, "IMAGE", false));
		logService.logMessage(new LogMessage(users.getCurrentUser().getUsername(), "create", "HeaderConfiguration", Long.toString(hc.getId())));
	}

	@Override
	public List<HeaderConfiguration> getHeader() {
		return headerRepo.findAll();
	}

	@Override
	public List<HeaderConfiguration> getVisibleHeader() {
		return headerRepo.findVisibleHeaders();
	}

	@Override
	public void makeStarter(final Long id) {
		for(HeaderConfiguration header : headerRepo.findAll()) {
			header.disableStarter();
		}
		headerRepo.findById(id).makeStarter();
		checkInvalid();
		logService.logMessage(new LogMessage(users.getCurrentUser().getUsername(), "update", "HeaderConfiguration", Long.toString(id)));
	}

	@Override
	public void changeOrder(final List<Long> items) {
		for(Long item : items) {
			headerRepo.updateOrder(items.indexOf(item), item);
		}
	}

	@Override
	public void toggleVisibility(final Long id) {
		final HeaderConfiguration header = headerRepo.findById(id);
		if(header.isVisible())
			header.makeInVisible();
		else
			header.makeVisible();
		checkInvalid();
		logService.logMessage(new LogMessage(users.getCurrentUser().getUsername(), "update", "HeaderConfiguration", Long.toString(id)));
	}

	@Override
	public void update(final String content, final Long id) {
		headerRepo.findById(id).setContent(content);
		logService.logMessage(new LogMessage(users.getCurrentUser().getUsername(), "update", "HeaderConfiguration", Long.toString(id)));
	}

	@Override
	public void delete(final Long id) {
		headerRepo.deleteById(id);
		checkInvalid();
		logService.logMessage(new LogMessage(users.getCurrentUser().getUsername(), "delete", "HeaderConfiguration", Long.toString(id)));
	}
}
