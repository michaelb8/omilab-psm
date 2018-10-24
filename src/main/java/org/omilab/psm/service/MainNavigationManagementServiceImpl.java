// File:         MainNavigationManagementServiceImpl.java
// Created:      13.08.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.service;

import org.omilab.psm.model.db.*;
import org.omilab.psm.model.db.projectoverlay.Event;
import org.omilab.psm.repo.*;
import org.omilab.psm.service.logging.LogMessage;
import org.omilab.psm.service.logging.LoggingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component("MainNavigationManagementService")
@Transactional
public final class MainNavigationManagementServiceImpl implements MainNavigationManagementService {

	private final MainNavigationGenericRepository mainNavigationRepo;
	private final MainNavigationHTMLRepository mainNavigationHTMLRepo;
	private final MainNavigationTypesRepository mainNavigationTypesRepo;
	private final ProjectTypeRepository projectTypeRepo;
	private final GenericProjectRepository projectRepo;
	private final MainNavigationProjectRepository mainNavigationProjectRepo;
	private final UserService userService;
	private final LoggingService logService;

	@Autowired
	public MainNavigationManagementServiceImpl(final MainNavigationGenericRepository mainNavigationRepo, final MainNavigationHTMLRepository mainNavigationHTMLRepo,
											   final MainNavigationTypesRepository mainNavigationTypesRepo, final ProjectTypeRepository projectTypeRepo, final GenericProjectRepository projectRepo,
											   final MainNavigationProjectRepository mainNavigationProjectRepo, final UserService userService, final LoggingService logService) {
		this.mainNavigationRepo = mainNavigationRepo;
		this.mainNavigationHTMLRepo = mainNavigationHTMLRepo;
		this.mainNavigationTypesRepo = mainNavigationTypesRepo;
		this.projectTypeRepo = projectTypeRepo;
		this.projectRepo = projectRepo;
		this.mainNavigationProjectRepo = mainNavigationProjectRepo;
		this.logService = logService;
		this.userService = userService;
	}

	@Override
	public List<MainNavigationItem> getMenu() {
		return mainNavigationRepo.findAll();
	}

	@Override
	public MainNavigationItemHTML getHTMLForURL(final String url) {
		MainNavigationItemHTML content = mainNavigationHTMLRepo.findByLink(url);
		if(content != null)
			return content;
		return null;
	}

	@Override
	public MainNavigationItemHTML getHTMLForID(final Long id) {
		MainNavigationItemHTML content = mainNavigationHTMLRepo.findById(id);
		if(content != null)
			return content;
		return null;
	}

	@Override
	public MainNavigationItem getMNIForID(Long id) {
		MainNavigationItem content = mainNavigationRepo.findById(id);
		if(content != null)
			return content;
		return null;
	}

	@Override
	public List<AbstractProject> getProjectsByURL(final String url) {
		return mainNavigationTypesRepo.findProjectsByMNI(mainNavigationTypesRepo.findByLink(url));
	}

	@Override
	public List<Event> getProjectsByURLChrono(String url) {
		return mainNavigationTypesRepo.findProjectsByMNIChronologically(mainNavigationTypesRepo.findByLink(url));
	}

	@Override
	public AbstractProject getProjectByURL(final String url) {
		final MainNavigationItemProject project = mainNavigationProjectRepo.findByLink(url);
		if( project != null)
			return project.getProject();
		return null;
	}

	@Override
	public synchronized void delete(final Long id) {
		if( mainNavigationRepo.findById(id) instanceof MainNavigationItemTypes) {
			mainNavigationTypesRepo.prepareDeleteById(id);
		}
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "delete", mainNavigationRepo.findById(id).getClass().getSimpleName(), Long.toString(id)));
		mainNavigationRepo.deleteById(id);
	}

	@Override
	public void toggleVisiblity(final Long id) {
		final MainNavigationItem mni = mainNavigationRepo.findById(id);
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "update", mni.getClass().getSimpleName(), Long.toString(id)));
		if(mni.isVisible())
			mni.makeInVisible();
		else
			mni.makeVisible();
	}


	@Override
	public void toggleNewProject(final Long id) {
		final MainNavigationItemTypes mni = mainNavigationTypesRepo.findById(id);
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "update", mni.getClass().getSimpleName(), Long.toString(id)));
		if(mni.getTypes().size() == 0) {
			mni.setNewproject(false);
			return;
		}
		if(mni.getNewproject() == null)
			mni.setNewproject(false);
		if(mni.getNewproject())
			mni.setNewproject(false);
		else {
			mni.setNewproject(true);
		}

	}

	@Override
	public void changeOrder(final List<Long> items) {
		for(Long item : items) {
			mainNavigationRepo.updateOrder(items.indexOf(item), item);
		}
	}

	@Override
	public void createLink(final String name, final String url) {
		MainNavigationItem mni = mainNavigationRepo.save(new MainNavigationItemLink(name, url));
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "create", mni.getClass().getSimpleName(), Long.toString(mni.getId())));
	}

	@Override
	public void createHTML(final String name, final String url) {
		MainNavigationItem mni = mainNavigationRepo.save(new MainNavigationItemHTML(name,url));
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "create", mni.getClass().getSimpleName(), Long.toString(mni.getId())));

	}

	@Override
	public void createTypes(final String name, final String url, final List<Long> types, final String caption, final String newprojectlabel) {
		List<ProjectType> projt = new ArrayList<>();
		for(Long type : types) {
			projt.add(projectTypeRepo.findById(type));
		}
		MainNavigationItem mni = mainNavigationTypesRepo.save(new MainNavigationItemTypes(name,url,projt,caption,newprojectlabel));
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "create", mni.getClass().getSimpleName(), Long.toString(mni.getId())));
	}

	@Override
	public void createProject(final String name, final String url, final Long id) {
		MainNavigationItem mni = mainNavigationRepo.save(new MainNavigationItemProject(name,url,projectRepo.findById(id)));
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "create", mni.getClass().getSimpleName(), Long.toString(mni.getId())));
	}

	@Override
	public void updateLink(final Long id, final String name, final String url) {
		MainNavigationItem mni = mainNavigationRepo.findById(id);
		if(mni instanceof MainNavigationItemLink) {
			((MainNavigationItemLink) mni).setDisplayname(name);
			((MainNavigationItemLink) mni).setLink(url);
			logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "update", mni.getClass().getSimpleName(), Long.toString(mni.getId())));
		} else throw new IllegalStateException("Internal error! Wrong object!");
	}

	@Override
	public void updateHTML(final Long id, final String name, final String url, final String html) {
		MainNavigationItem mni = mainNavigationRepo.findById(id);
		if(mni instanceof MainNavigationItemHTML) {
			((MainNavigationItemHTML) mni).setDisplayname(name);
			((MainNavigationItemHTML) mni).setLink(url);
			((MainNavigationItemHTML) mni).setHtml(html);
			logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "update", mni.getClass().getSimpleName(), Long.toString(mni.getId())));
		} else throw new IllegalStateException("Internal error! Wrong object!");
	}

	@Override
	public void updateTypes(final Long id, final String name, final String url, final List<Long> types,
							final String caption, final String label) {
		MainNavigationItem mni = mainNavigationRepo.findById(id);
		if(mni instanceof MainNavigationItemTypes) {
			((MainNavigationItemTypes) mni).setDisplayname(name);
			((MainNavigationItemTypes) mni).setLink(url);
			((MainNavigationItemTypes) mni).setCaption(caption);
			((MainNavigationItemTypes) mni).setNewprojectlabel(label);
			List<ProjectType> projt = new ArrayList<>();
			for(Long type : types) {
				projt.add(projectTypeRepo.findById(type));
			}
			((MainNavigationItemTypes) mni).setTypes(projt);
			logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "update", mni.getClass().getSimpleName(), Long.toString(mni.getId())));
		} else throw new IllegalStateException("Internal error! Wrong object!");
	}

	@Override
	public void updateProject(final Long id, final String name, final String url, final Long projid) {
		MainNavigationItem mni = mainNavigationRepo.findById(id);
		if(mni instanceof MainNavigationItemProject) {
			((MainNavigationItemProject) mni).setDisplayname(name);
			((MainNavigationItemProject) mni).setLink(url);
			((MainNavigationItemProject) mni).setProject(projectRepo.findById(projid));
			logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "update", mni.getClass().getSimpleName(), Long.toString(mni.getId())));
		} else throw new IllegalStateException("Internal error! Wrong object!");
	}

	@Override
	public List<ProjectType> getProjectTypeUsage(final Long id) {
		MainNavigationItem mni = mainNavigationRepo.findById(id);
		if( mni instanceof MainNavigationItemTypes) {
			return mainNavigationTypesRepo.findProjectTypesForNavigation((MainNavigationItemTypes)mni);
		}
		return null;
	}

	@Override
	public List<AbstractProject> getProjectUsage(final Long id) {
		MainNavigationItem mni = mainNavigationRepo.findById(id);
		if( mni instanceof MainNavigationItemProject) {
			List<AbstractProject> proj = mainNavigationProjectRepo.findProjectForNavigation((MainNavigationItemProject)mni);
			return proj;
		}
		return null;
	}

	@Override
	public void toggleCarousel(final Long id) {
		final MainNavigationItem mni = mainNavigationRepo.findById(id);
		if( mni instanceof MainNavigationItemHTML) {
			if(((MainNavigationItemHTML) mni).getCarousel() == null) {
				((MainNavigationItemHTML) mni).setCarousel(false);
			}
			if(((MainNavigationItemHTML) mni).getCarousel())
				((MainNavigationItemHTML) mni).setCarousel(false);
			else
				((MainNavigationItemHTML) mni).setCarousel(true);
		}
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "create", mni.getClass().getSimpleName(), Long.toString(mni.getId())));
	}

	@Override
	public MainNavigationItem getMNIForURL(final String url) {
		return mainNavigationRepo.findByLink(url);
	}
}
