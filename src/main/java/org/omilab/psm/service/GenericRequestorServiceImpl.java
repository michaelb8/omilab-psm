// File:         GenericRequesterServiceImpl.java
// Created:      02.03.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.service;


import org.omilab.psm.model.db.ServiceInstance;
import org.omilab.psm.model.wrapper.GenericRequest;
import org.omilab.psm.model.wrapper.GenericServiceContent;
import org.omilab.psm.service.role.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("GenericRequestorService")
@Transactional
@SuppressWarnings("unused")
public final class GenericRequestorServiceImpl implements RequestorService {

	private static final Logger logger = LoggerFactory.getLogger(GenericRequestorServiceImpl.class);

	private static GenericServiceContent serviceUnavailable = new GenericServiceContent("<p style=\"margin-top:4%;font-size:20px;color:white;\"> <b> Service is currently not available! </b> </p>", new HashMap<String, String>());

	private final UserService users;

	private final Environment env;

	private final ProjectTypeService projectTypeService;

	private final RoleService roles;

	@Autowired
	public GenericRequestorServiceImpl(UserService users, ProjectTypeService projectTypeService, Environment env,
									   RoleService roles) {
		this.users = users;
		this.env = env;
		this.projectTypeService = projectTypeService;
		this.roles = roles;
	}

	@Override
	public GenericServiceContent processAdminRequest(final ServiceInstance si, final Map<String, String> params,
													 final String endpoint) {
		if(si == null) {
			logger.debug("No valid service instance found! Aborting request! ");
			return serviceUnavailable;
		}
		if(!si.getServicedefinition().getVisible()) {
			logger.info("Disabled service " + si.getServicedefinition().getName() + " has been called!");
			return serviceUnavailable;
		}
		if((!projectTypeService.getAllowedEndpoints(si.getProject().getProjecttype()).contains(endpoint)) && (!endpoint.equals("rolemanagement")))
			return serviceUnavailable;
		final String queryUrl = si.getServicedefinition().getUrl() + "admin/" + si.getInstanceidremote() + "/" + endpoint;
		final GenericRequest gr = new GenericRequest(users.getCurrentUser().getUsername(), roles.getRoles(si.getProject().getUrlidentifier(), users.getCurrentUser().getUsername()), params);
		final GenericServiceContent gsc = processRequest(queryUrl, gr);
		if(gsc.getSubmenu() == null)
			gsc.setSubmenu(new HashMap<String,String>());
		return gsc;
	}

	@Override
	public void processInitiationRequest(final ServiceInstance si, final String jsonContent, final String endpoint) {
		if(si == null) {
			logger.debug("No valid service instance found! Aborting request! ");
			return;
		}
		if(!si.getServicedefinition().getVisible()) {
			logger.info("Disabled service " + si.getServicedefinition().getName() + " has been called!");
			return;
		}
		if((!projectTypeService.getAllowedEndpoints(si.getProject().getProjecttype()).contains(endpoint)) && (!endpoint.equals("rolemanagement"))) {
			return;
		}
		final String queryUrl = si.getServicedefinition().getUrl() + "admin/" + si.getInstanceidremote() + "/" + endpoint;
		sendRequest(queryUrl, jsonContent);
	}

	@Override
	public GenericServiceContent processViewRequest(final ServiceInstance si, final Map<String, String> params,
													final String endpoint) {
		if(si == null) {
			logger.debug("No valid service instance found! Aborting request!");
			return serviceUnavailable;
		}
		if(!si.getServicedefinition().getVisible()) {
			logger.info("Disabled service " + si.getServicedefinition().getName() + " has been called!");
			return serviceUnavailable;
		}
		if(!projectTypeService.getAllowedEndpoints(si.getProject().getProjecttype()).contains(endpoint))
			return serviceUnavailable;

		final String queryUrl = si.getServicedefinition().getUrl() + "view/" + si.getInstanceidremote() + "/" + endpoint;
		final GenericRequest gr = new GenericRequest(users.getCurrentUser().getUsername(), roles.getRoles(si.getProject().getUrlidentifier(), users.getCurrentUser().getUsername()), params);
		final GenericServiceContent gsc = processRequest(queryUrl, gr);
		if(gsc.getSubmenu() == null)
			gsc.setSubmenu(new HashMap<String,String>());
		return gsc;
	}

	public GenericServiceContent processRequest(final String url, final GenericRequest gr) {

		long startTime = 0;
		if(Boolean.parseBoolean(env.getProperty("omilab.debug.performance")))
			startTime = System.currentTimeMillis();


		logger.debug("Attempting request to: " + url);
		logger.debug("Request payload is: " + gr.toString());

		try {
			final Client client = ClientBuilder.newClient();
			final WebTarget target = client.target(url);
			final GenericServiceContent gsc = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(gr, MediaType.APPLICATION_JSON), GenericServiceContent.class);
			if(gsc != null)
				logger.debug("Received response: " + gsc.toString());
			if(Boolean.parseBoolean(env.getProperty("omilab.debug.performance")) && gsc != null)
				gsc.setResponseTime(System.currentTimeMillis() - startTime);
			return gsc;
		} catch(Exception e) {
			logger.error("Error processing service request: " + e.getMessage());
			logger.debug("Error processing service request: ", e);
		}
		return serviceUnavailable;
	}

	@Override
	public String processAJAXRequest(ServiceInstance si, String payload, String relative) {
		StringBuffer response = null;
		try {
			String url = si.getServicedefinition().getUrl() + "view/" + si.getInstanceidremote() + "/" + relative;
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			//add reuqest header
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", "PSM AJAX Requests");
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

			String urlParameters = payload;

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();

			BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream(),StandardCharsets.UTF_8));
			String inputLine;
			response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(response != null) {
			return response.toString();
		} else
			return "failure";

	}

	public void sendRequest(final String url, final String content) {
		try {
			final Client client = ClientBuilder.newClient();
			final WebTarget target = client.target(url);
			target.request(MediaType.APPLICATION_JSON).post(Entity.json(content), String.class);
		} catch(Exception e) {
			logger.error("Error processing service request: " + e.getMessage());
			logger.debug("Error processing service request: ", e);
		}
	}

}
