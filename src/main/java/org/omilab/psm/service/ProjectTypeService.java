// File:         ProjectTypeService.java
// Created:      29.05.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.service;

import org.omilab.psm.model.db.DBNavigationItem;
import org.omilab.psm.model.db.AbstractProject;
import org.omilab.psm.model.db.ProjectProposal;
import org.omilab.psm.model.db.ProjectType;

import java.util.List;

public interface ProjectTypeService {

	public void createType(String name, String description, String overlay);

	public void updateTileBackground(String tilebackground, Long id);

	public void updateTileForeground(String tileforeground, Long id);

	public List<ProjectType> getTypes();

	public ProjectType getById(Long id);

	public void refreshProjectsByType(ProjectType type);

	public void changeProjects(List<Long> updatedItems, Long id);

	public void changeServices(List<Long> updatedItems, Long id);

	public void changeWizardConfig(List<Long> updatedItems, Long id);

	public List<DBNavigationItem> getWServicesForPT(Long id);

	public List<AbstractProject> getProjectsForPT(Long id);

	public List<DBNavigationItem> getServicesForPT(Long id);

	public List<String> getAllowedEndpoints(ProjectType projectType);

	public void removeDBNavigationItem(Long id );

	public void setColor(String hex, Long id);

	public void unsetColor(Long id);

	public void toggleFullHeader(Long id);

	public void toggleNavigationBar(Long id);

	public void setWRoleServiceStatus(Long id, Boolean status);

	public void setWRepoStatus(Long id, Boolean status);

	public void activateWCE(Long type, Long dbni);

	public void setInstantiationString(Long type, String dbni, String value);

	public List<String> getAvailableOverlays();

	public void setOverlay(Long id, String overlay);

	public void removeType(ProjectType type);

	public List<ProjectProposal> getUnfinishedProposals(ProjectType type);

}
