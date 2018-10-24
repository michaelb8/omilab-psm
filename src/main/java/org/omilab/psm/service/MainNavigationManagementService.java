// File:         MainNavigationManagementService.java
// Created:      13.08.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//
package org.omilab.psm.service;

import org.omilab.psm.model.db.*;
import org.omilab.psm.model.db.projectoverlay.Event;

import java.util.List;

public interface MainNavigationManagementService {

	public List<MainNavigationItem> getMenu();

	public MainNavigationItem getMNIForURL(String url);

	public MainNavigationItemHTML getHTMLForURL(String url);

	public MainNavigationItem getMNIForID(Long id);

	public MainNavigationItemHTML getHTMLForID(Long id);

	public List<AbstractProject> getProjectsByURL(String url);

	public List<Event> getProjectsByURLChrono(String url);

	public AbstractProject getProjectByURL(String url);

	public void delete(Long id);

	public void toggleVisiblity(Long id);

	public void toggleNewProject(Long id);

	public void toggleCarousel(Long id);

	public void changeOrder(List<Long> items);

	public void createLink(String name, String url);

	public void createHTML(String name, String url);

	public void createTypes(String name, String url, List<Long> types, String caption, String nProjLabel);

	public void createProject(String name, String url, Long id);

	public void updateLink(Long id, String name, String url);

	public void updateHTML(Long id, String name, String url, String html);

	public void updateTypes(Long id, String name, String url, List<Long> types, String caption, String nProjLabel);

	public void updateProject(Long id, String name, String url, Long projid);

	public List<ProjectType> getProjectTypeUsage(Long id);

	public List<AbstractProject> getProjectUsage(Long id);

}
