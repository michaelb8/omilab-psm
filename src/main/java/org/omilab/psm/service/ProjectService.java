// File:         ProjectService.java
// Created:      18.02.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.service;

import org.omilab.psm.model.db.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.net.URL;
import java.util.List;
import java.util.Map;

public interface ProjectService {

	public List<AbstractProject> getAllProjects();

	public List<Keyword> getAllKeywords();

	public List<Keyword> getKeywordByProject(AbstractProject project);

	public AbstractProject getProject(String urlidentifier);

	public AbstractProject getProject(Long id);

	public Page<AbstractProject> getProjectOverviewPage(Pageable page);

	public AbstractProject createProject(String name, String abbreviation, String urlidentifier, ProjectProposal proposal);

	public void updateKeywords(AbstractProject project, List<String> keywords);

	public String getBackTileForProject(AbstractProject project);

	public String getFrontTileForProject(AbstractProject project);

	public List<AbstractProject> search(String query);

	public int getProjectTileStart();

	public ProjectProposal createProposal(String projectname, String projectabstract, String username, String email, Long type, MainNavigationItemTypes mni);

	public String getNumberOfUnreadProposals(ProjectType type);

	public List<ProjectProposal> getProposalsForProjectType(ProjectType type);

	public ProjectProposal getProposal(Long id);

	public ProjectProposal getProposalbyUUID(String UUID);

	public void approve(ProjectProposal proposal);

	public void deny(ProjectProposal proposal);

	public void changeProjectAttributesPO(Map<String,String> attributes, AbstractProject project);

	public void changeProjectAttributesWizard(Map<String,String> attributes, AbstractProject project);

	public Integer getOpenProposals();

	public ProjectProposal getFirstOpenProposal();

	public void changeUrlIdentifier(AbstractProject p, String url);

	public Boolean removeProject(AbstractProject p);

	public void toggleInConfig(AbstractProject p);
}
