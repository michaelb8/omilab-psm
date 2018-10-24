// File:         GlobalConfigurationService.java
// Created:      14.01.16
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2016 by OMiLAB.ORG
//

package org.omilab.psm.service;

public interface GlobalConfigurationService {

	public void setKeyValue(String key, String value);

	public String getValue(String key);

	public void removeValue(String key);

}
