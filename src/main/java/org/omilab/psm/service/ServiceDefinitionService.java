// File:         ServiceDefinitionService.java
// Created:      15.02.2015
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.service;

import org.omilab.psm.model.db.DBNavigationItem;
import org.omilab.psm.model.db.ServiceDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ServiceDefinitionService {

	public void createServiceDefinition(MultipartFile file) throws IOException;

	public void disableServiceDefinition(Long id);

	public void enableServiceDefinition(Long id);

	public List<DBNavigationItem> getAllNavigationItems();

	public ServiceDefinition getServiceDefinition(String urlidentifier);

	public ServiceDefinition getServiceDefinition(Long id);

	public Page<ServiceDefinition> getServiceDefinitionPage(Pageable page);

	public Page<ServiceDefinition> getVisibleServiceDefinitionPage(Pageable page);

	public void saveNavigationItemOrder(List<Long> ids);

	public List<DBNavigationItem> getNavigationItemsByServiceDefinition(ServiceDefinition sd);

}
