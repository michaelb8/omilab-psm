// File:         ProjectTypeServiceImpl.java
// Created:      29.05.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.service;

import org.omilab.psm.model.db.*;
import org.omilab.psm.repo.*;
import org.omilab.psm.service.logging.LogMessage;
import org.omilab.psm.service.logging.LoggingService;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Component("ProjectTypeService")
@Transactional
public final class ProjectTypeServiceImpl implements ProjectTypeService {

	private static final Logger logger = LoggerFactory.getLogger(ProjectTypeServiceImpl.class);

	private final ProjectTypeRepository projectTypeRepo;

	private final ProjectService projects;

	private final ServiceDefinitionService services;

	private final ProjectNavigationRepository navigationRepo;

	private final ServiceNavigationRepository dbniRepo;

	private final WizardConfigurationRepository wceRepo;

	private final ProjectProposalRepository ppRepo;

	private final MainNavigationGenericRepository mniRepo;

	private final LoggingService logService;

	private final UserService userService;

	@Autowired
	public ProjectTypeServiceImpl(final ProjectTypeRepository projectTypeRepo, final ProjectService projects,
								  final ServiceDefinitionService services, final ProjectNavigationRepository navigationRepo,
								  final WizardConfigurationRepository wceRepo, final ServiceNavigationRepository dbniRepo,
								  final ProjectProposalRepository ppRepo, final MainNavigationGenericRepository mniRepo,
								  final LoggingService logService, final UserService userService) {
		this.projectTypeRepo = projectTypeRepo;
		this.projects = projects;
		this.services = services;
		this.navigationRepo = navigationRepo;
		this.wceRepo = wceRepo;
		this.dbniRepo = dbniRepo;
		this.ppRepo = ppRepo;
		this.mniRepo = mniRepo;
		this.logService = logService;
		this.userService = userService;
	}

	@Override
	public void createType(final String name, final String description, final String overlay) {
		projectTypeRepo.save(new ProjectType(name, description,overlay));
	}

	@Override
	public List<ProjectType> getTypes() {
		return projectTypeRepo.findAll();
	}

	@Override
	public ProjectType getById(final Long id) {
		return projectTypeRepo.findById(id);
	}

	@Override
	public void refreshProjectsByType(ProjectType type) {
		forceMandatory(type);
		removeUnallowed(type);
	}

	private synchronized void forceMandatory(final ProjectType type) {
		List<AbstractProject> projects = projectTypeRepo.findProjects(type.getId());
		for (AbstractProject project : projects) {
			// only add mandatory navigation menus for already subscribed services
			// if a service has no subscribtion, but the has a mandatory DBNavigation item
			// it is *NOT* added.
			Set<ServiceInstance> instances = project.getInstances();
			for (ServiceInstance instance : instances) {
				Set<DBNavigationItem> items = instance.getServiceid().getServicenavigationitems();
				for (DBNavigationItem item : items) {
					if(item.getMandatory()) {
						if(navigationRepo.existsByProjectAndDBNavigation(project, item)) {
						} else {
							if(type.getItems().contains(item)) {
								navigationRepo.save(new Project_Navigation(project, item));
							}
						}
					}
				}
			}
		}
	}


	private synchronized void removeUnallowed(ProjectType type) {
		List<AbstractProject> projects = projectTypeRepo.findProjects(type.getId());
		for (AbstractProject project : projects) {
			// safe to use here, as a project cannot have a DBNavigationItem,
			// that is not associated with a instance. So, if we look
			// through all instances, we will find all present DBNavigationItems
			Set<ServiceInstance> instances = project.getInstances();
			for (ServiceInstance instance : instances) {
				Set<DBNavigationItem> items = instance.getServiceid().getServicenavigationitems();
				for (DBNavigationItem item : items) {
					if (navigationRepo.existsByProjectAndDBNavigation(project, item)) {
						if(!type.getItems().contains(item)) {
							navigationRepo.delete(navigationRepo.findByProjectAndDBNavigationItem(project, item));
						}
					}

				}
			}
		}
	}

	@Async
	@Override
	public void changeProjects(final List<Long> updatedItems, final Long id) {
		final List<AbstractProject> projectList = projects.getAllProjects();
		final ProjectType projectType = projectTypeRepo.findById(id);
		for (AbstractProject project : projectList) {
			if (project.getProjecttype() == null) {
				if (updatedItems.contains(project.getId()))
					project.setProjecttype(projectTypeRepo.findById(id));
			} else {
				if (project.getProjecttype().getId().equals(id) && (!updatedItems.contains(project.getId())))
					project.setProjecttype(null);
			}
		}
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "update", projectType.getClass().getSimpleName(), Long.toString(id)));
		refreshProjectsByType(projectType);
	}


	@Async
	@Override
	public void changeServices(final List<Long> updatedItems, final Long id) {
		final List<DBNavigationItem> serviceList = services.getAllNavigationItems();
		final ProjectType projectType = projectTypeRepo.findById(id);
		for (DBNavigationItem item : serviceList) {
			if (updatedItems.contains(item.getId())) {
				if (!projectType.getItems().contains(item)) {
					projectType.addNavigationItem(item);
				}
			} else {
				if (projectType.getItems().contains(item)) {
					projectType.removeNavigationItem(item);
					wceRepo.delete(wceRepo.findByDBNIandType(item.getId(),id));
				}
			}
		}
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "update", projectType.getClass().getSimpleName(), Long.toString(id)));
		refreshProjectsByType(projectType);
	}

	@Override
	public void changeWizardConfig(final List<Long> updatedItems, final Long id) {
		ProjectType type = projectTypeRepo.findById(id);
		for (DBNavigationItem item : type.getItems()) {
			if (!updatedItems.contains(item.getId())) {
				if (wceRepo.findByDBNIandType(item.getId(), id) != null) {
					wceRepo.delete(wceRepo.findByDBNIandType(item.getId(), id));
				}
			}
		}
		for (Long newItem : updatedItems) {
			if (wceRepo.findByDBNIandType(newItem, id) == null) {
				wceRepo.save(new WizardConfigurationEntry(dbniRepo.findById(newItem), type));
			}
		}
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "update", type.getClass().getSimpleName(), Long.toString(id)));
		// reset position for all active proposals (= proposals currently in creation)
		for(ProjectProposal pp : ppRepo.findProposalsByType(projectTypeRepo.findById(id)))
			if(!pp.getFinished() && pp.getPos() != null && pp.getProject() != null)
				pp.setPos(0);

	}

	@Override
	public void updateTileBackground(final String tilebackground, final Long id) {
		ProjectType projectType = projectTypeRepo.findById(id);
		projectType.setBackgroundTile(tilebackground);
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "update", projectType.getClass().getSimpleName(), Long.toString(id)));
	}

	@Override
	public void updateTileForeground(final String tileforeground, final Long id) {
		ProjectType projectType = projectTypeRepo.findById(id);
		projectType.setForegroundTile(tileforeground);
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "update", projectType.getClass().getSimpleName(), Long.toString(id)));
	}


	@Override
	public List<AbstractProject> getProjectsForPT(final Long id) {
		return projectTypeRepo.findProjects(id);
	}

	@Override
	public List<DBNavigationItem> getServicesForPT(final Long id) {
		return projectTypeRepo.findDBNavigationItems(id);
	}

	@Override
	public List<DBNavigationItem> getWServicesForPT(final Long id) {
		return wceRepo.findDBNIByTypeInWCE(id);
	}

	@Override
	public List<String> getAllowedEndpoints(final ProjectType projectType) {
		return projectTypeRepo.findEndpoints(projectType.getId());
	}

	@Override
	public void setColor(final String hex, final Long id) {
		projectTypeRepo.findById(id).setBackgroundColor(hex);
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "update", "ProjectType", Long.toString(id)));
	}

	@Override
	public void unsetColor(final Long id) {
		projectTypeRepo.findById(id).setBackgroundColor(null);
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "update", "ProjectType", Long.toString(id)));
	}

	@Override
	public void setWRoleServiceStatus(final Long id, final Boolean status) {
		projectTypeRepo.findById(id).setwRoleStatus(status);
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "update", "ProjectType", Long.toString(id)));
	}

	@Override
	public void setWRepoStatus(final Long id, final Boolean status) {
		projectTypeRepo.findById(id).setwRepoStatus(status);
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "update", "ProjectType", Long.toString(id)));
	}

	@Override
	public void toggleNavigationBar(final Long id) {
		ProjectType pt = projectTypeRepo.findById(id);
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "update", pt.getClass().getSimpleName(), Long.toString(id)));
		if (pt.getNavigationBar() == null)
			pt.setNavigationBar(true);
		if (pt.getNavigationBar())
			pt.setNavigationBar(false);
		else
			pt.setNavigationBar(true);
	}

	@Override
	public void toggleFullHeader(final Long id) {
		ProjectType pt = projectTypeRepo.findById(id);
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "update", pt.getClass().getSimpleName(), Long.toString(id)));
		if (pt.getFullPHeader())
			pt.setFullPHeader(false);
		else {
			pt.setFullPHeader(true);
			if (pt.getReducedPHeader())
				pt.setReducedPHeader(false);
		}
	}

	@Override
	public void removeDBNavigationItem(final Long id) {
		projectTypeRepo.deleteByDBNavigationItem(id);
	}

	@Override
	public void activateWCE(final Long type, final Long dbni) {
		wceRepo.save(new WizardConfigurationEntry(dbniRepo.findById(dbni), projectTypeRepo.findById(type)));
	}

	@Override
	public void setInstantiationString(final Long type, final String dbni, final String value) {
		ProjectType pt = projectTypeRepo.findById(type);
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "update", pt.getClass().getSimpleName(), Long.toString(type)));
		if (dbni.equals("role")) {
			if (value.equals("null"))
				pt.setwRoleString(null);
			else
				pt.setwRoleString(value);
		}
		if (dbni.equals("repo")) {
			if (value.equals("null"))
				pt.setwRepoString(null);
			else
				pt.setwRepoString(value);
		}
		if ((!dbni.equals("role")) && (!dbni.equals("repo"))) {
			WizardConfigurationEntry wce = wceRepo.findByDBNIandType(Long.parseLong(dbni), type);
			if (value.equals("null"))
				wce.setInstantiation(null);
			else
				wce.setInstantiation(value);
		}
	}

	@Override
	public List<String> getAvailableOverlays() {
		List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
		classLoadersList.add(ClasspathHelper.contextClassLoader());
		classLoadersList.add(ClasspathHelper.staticClassLoader());

		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.setScanners(new SubTypesScanner(false /* don't exclude Object.class */), new ResourcesScanner())
				.setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
				.filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix("org.omilab.psm.model.db.projectoverlay"))));

		Set<Class<? extends AbstractProject>> classes = reflections.getSubTypesOf(AbstractProject.class);
		List<String> listing = new ArrayList<>();
		for (Class<?> clazz : classes) {
			listing.add(clazz.getName());
		}
		return listing;
	}

	@Override
	public void setOverlay(Long id, String overlay) {
		projectTypeRepo.findById(id).setOverlay(overlay);
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "update", "ProjectType", Long.toString(id)));
	}

	@Override
	public void removeType(ProjectType type) {
		final List<MainNavigationItemTypes> items = type.getPage();
		for(MainNavigationItemTypes item : items) {
			item.remove(type);
		}
		for(ProjectProposal pp : ppRepo.findProposalsByType(type)) {
			ppRepo.updateToNull(pp.getId());
		}
		final List<WizardConfigurationEntry> wces = wceRepo.findWceByType(type);
		for(WizardConfigurationEntry wce : wces) {
			wceRepo.delete(wce);
		}
		type.removeAllNavigationItem();
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "delete", type.getClass().getSimpleName(), Long.toString(type.getId())));
		projectTypeRepo.deleteById(type.getId());
	}

	@Override
	public List<ProjectProposal> getUnfinishedProposals(ProjectType type) {
		return ppRepo.findUnfinishedTypeProposals(type);
	}
}