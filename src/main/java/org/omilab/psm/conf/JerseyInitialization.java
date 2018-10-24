// File:         JerseyConfig.java
// Created:      15.07.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.conf;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class JerseyInitialization extends ResourceConfig {
	public JerseyInitialization(){
		register(JacksonFeature.class);
		packages("org.omilab.psm.rest");
	}
}