// File:         RequestorService.java
// Created:      14.02.2015
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.service;

import org.omilab.psm.model.db.ServiceInstance;
import org.omilab.psm.model.wrapper.GenericServiceContent;

import java.util.Map;

public interface RequestorService {

	public GenericServiceContent processAdminRequest(ServiceInstance si, Map<String, String> params, String endpoint);

	public GenericServiceContent processViewRequest(ServiceInstance si, Map<String, String> params, String endpoint);

	public void processInitiationRequest(ServiceInstance si, String jsonContent, String endpoint);

	public String processAJAXRequest(ServiceInstance si,String payload,String relative);

}
