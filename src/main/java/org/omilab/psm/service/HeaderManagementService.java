// File:         HeaderManagement.java
// Created:      21.05.2015
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//


package org.omilab.psm.service;

import org.omilab.psm.model.db.HeaderConfiguration;

import java.util.List;

public interface HeaderManagementService {

	public void addHTMLHeader(String content, String title);

	public void addImageHeader(String content, String title);

	public List<HeaderConfiguration> getHeader();

	public List<HeaderConfiguration> getVisibleHeader();

	public void makeStarter(Long id);

	public void changeOrder(List<Long> items);

	public void toggleVisibility(Long id);

	public void update(String content, Long id);

	public void delete(Long id);

}
