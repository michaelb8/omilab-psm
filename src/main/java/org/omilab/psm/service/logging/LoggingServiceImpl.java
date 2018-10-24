// File:         LoggingServiceImpl.java
// Created:      13.04.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.service.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.*;
import javax.ws.rs.core.Response;


@Component("LoggingService")
public final class LoggingServiceImpl implements LoggingService {

	private static final Logger logger = LoggerFactory.getLogger(LoggingServiceImpl.class);

	@Autowired
	Environment env;

	@Override
	public void logMessage(final LogMessage log) {

		log.setSid(env.getProperty("omilab.sid"));

		final Client client = ClientBuilder.newClient();
		final WebTarget target = client.target(env.getProperty("omilab.activity")).path("log");

		target.request().async().post(Entity.json(log), new InvocationCallback<Response>() {
			@Override
			public void completed(Response response) {
				if(response.getStatus() == 403) {
					registerService(log);
				}
				response.close();
				client.close();
			}

			@Override
			public void failed(Throwable throwable) {
				logger.error("Error sending a log message to activity logging service at: " + env.getProperty("omilab.activity"));
				throwable.printStackTrace();
			}
		});

	}

	private synchronized void registerService(final LogMessage lm) {

		final Client client = ClientBuilder.newClient();
		final WebTarget target = client.target(env.getProperty("omilab.activity")).path("instanceMgmt");

		target.request().async().post(Entity.text(env.getProperty("omilab.sid")), new InvocationCallback<Response>() {
			@Override
			public void completed(Response response) {
				if(response.getStatus() == 200) {
					logger.info("Succesfully registered sid at activity logging service!");
					logMessage(lm);
				}
				response.close();
				client.close();
			}

			@Override
			public void failed(Throwable throwable) {
				logger.error("Failed to register sid: " + env.getProperty("omilab.sid") + "at activity logging service: " + env.getProperty("omilab.activity"));
				throwable.printStackTrace();
			}
		});
	}


}
