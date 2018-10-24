// File:         ServiceManagementService.java
// Created:      19.02.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.service;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.omilab.psm.model.db.AbstractProject;
import org.omilab.psm.model.db.DBNavigationItem;
import org.omilab.psm.model.db.ServiceDefinition;
import org.omilab.psm.model.db.ServiceInstance;
import org.omilab.psm.model.wrapper.UINavigationItem;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public interface ServiceManagementService {

	public void changeNavigationStatus(List<Long> items, AbstractProject project);

	public void activateNavigation(AbstractProject project, DBNavigationItem item);

	public Boolean checkEnabled(AbstractProject project, ServiceDefinition sd, String endpoint);

	public Boolean checkServiceAvailability(Long project, Long servicedefinition);

	public Boolean checkEndpointAvailability(Long project, Long servicedefinition);

	public void createEndpoint(String name, String endpoint, Boolean mandatory, Long servicedefinition);

	public void updateEndpoint(String name, String endpoint, Boolean mandatory, Long id);

	public void deleteEndpoint(Long id);

	public Boolean deleteServiceRecursivley(ServiceDefinition sd, Boolean force) throws IOException;

	public List<DBNavigationItem> generateMenuSelection(AbstractProject project);

	public List<UINavigationItem> generateNavigationMenu(AbstractProject project, ServiceDefinition sd, String endpoint,
														 Locale locale);

	public List<DBNavigationItem> getAllEnabledMenus(AbstractProject project);

	public String getFirstEndpoint(ServiceDefinition sd, AbstractProject project);

	public String getFirstService(AbstractProject project);

	public List<ServiceDefinition> getServiceDefinitionByProject(AbstractProject project);

	public ServiceInstance getServiceInstance(AbstractProject project, ServiceDefinition sd);

	public ServiceInstance getServiceInstanceOfSpecialService(AbstractProject project, String special);

	public List<AbstractProject> getServiceUsage(ServiceDefinition sd);

	public Boolean instantiateService(AbstractProject project, ServiceDefinition sd) throws IOException;

	public Boolean instantiateSpecialService(AbstractProject project, String special) throws IOException;

	public Boolean unsubscribeService(AbstractProject project, ServiceDefinition sd, Boolean force) throws IOException;

	public List<DBNavigationItem> getAllMenus(ServiceDefinition sd);

	public DBNavigationItem getMenu(Long id);

	public Boolean unsubscribeAllServices(AbstractProject project, Boolean force) throws IOException;

}
