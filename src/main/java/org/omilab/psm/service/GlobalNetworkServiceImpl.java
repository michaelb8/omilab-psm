// File:         GlobalNetworkServiceImpl.java
// Created:      05.08.16
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2016 by OMiLAB.ORG
//


package org.omilab.psm.service;

import org.omilab.psm.model.wrapper.GenericRequest;
import org.omilab.psm.model.wrapper.GenericServiceContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component("GlobalNetworkService")
@Transactional
public class GlobalNetworkServiceImpl implements GlobalNetworkService {

	private static final Logger logger = LoggerFactory.getLogger(GlobalNetworkServiceImpl.class);
    private final GlobalConfigurationService gconf;
	private final Environment env;
    private final static String NETWORK_REPOSITORY_REG ="http://vienna-omilab.dke.univie.ac.at/globalnetworkservice/gn/register";
    private final static String NETWORK_REPOSITORY_SYNC ="http://vienna-omilab.dke.univie.ac.at/globalnetworkservice/gn/sync";

    @Autowired
    public GlobalNetworkServiceImpl(GlobalConfigurationService gcs, Environment env) {
        this.gconf = gcs;
		this.env = env;
    }

    @Override
    public boolean check() {
        return Boolean.parseBoolean(gconf.getValue("gn_regstatus"));
    }

    @Override
    public Boolean register() {
        Map<String,String> action = new HashMap<>();
        action.put("register","true");
        final GenericRequest gr = new GenericRequest("registration",null,action);
        final Client client = ClientBuilder.newClient();
        final WebTarget target = client.target(NETWORK_REPOSITORY_REG);
		final GenericServiceContent gsc = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(gr, MediaType.APPLICATION_JSON), GenericServiceContent.class);
		UUID uniqueid;
		try {
			uniqueid = UUID.fromString(gsc.getContent());
		} catch(Exception e) {
			logger.error("Error registering the instance!");
			return false;
		}
		gconf.setKeyValue("gn_regstatus","true");
		gconf.setKeyValue("gn_uuid",uniqueid.toString());
		return true;
    }

	@Override
	public void remove() {
		Map<String,String> confirm = new HashMap<>();
		logger.debug("Received signal to unregister!");
		confirm.put("confremove", "true");
		confirm.put("uuid", gconf.getValue("gn_uuid"));
		if(gconf.getValue("gn_ips") != null)
			gconf.removeValue("gn_ips");
		if(gconf.getValue("gn_psmversion") != null)
			gconf.removeValue("gn_psmversion");
		if(gconf.getValue("gn_regstatus") != null)
			gconf.removeValue("gn_regstatus");
		if(gconf.getValue("gn_uuid") != null)
			gconf.removeValue("gn_uuid");
		if(gconf.getValue("gn_lastcontact") != null)
			gconf.removeValue("gn_lastcontact");

		final GenericRequest grc;
		final WebTarget targetc;
		try {
			grc = new GenericRequest("synchronization",null,confirm);
			final Client clientc = ClientBuilder.newClient();
			targetc = clientc.target(NETWORK_REPOSITORY_SYNC);
			targetc.request(MediaType.APPLICATION_JSON).post(Entity.entity(grc, MediaType.APPLICATION_JSON), GenericServiceContent.class);
		} catch(Exception e) {
			logger.error("Failed to unregister at OMiLAB HQ, removing only local attributes. Please report this incident to support@omilab.org");
		}
	}

	public void printMapV2 (Map <?, ?> map) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("{");
		for (Map.Entry<?,?> entry : map.entrySet()) {
			if (sb.length()>1) {
				sb.append(", ");
			}
			sb.append(entry.getKey()).append("=").append(entry.getValue());
		}
		sb.append("}");
		System.out.println(sb);
	}

	/*
         * See: http://stackoverflow.com/questions/6701948/efficient-way-to-compare-version-strings-in-java
         */
	public static int versionCompare(String str1, String str2) {
		String[] vals1 = str1.split("\\.");
		String[] vals2 = str2.split("\\.");
		int i = 0;
		while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
			i++;
		}
		if (i < vals1.length && i < vals2.length) {
			int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
			return Integer.signum(diff);
		}
		return Integer.signum(vals1.length - vals2.length);
	}

	@Override
	public boolean uptodate() {
		if(!gconf.getValue("gn_regstatus").equals("true"))
			return false;
		if(gconf.getValue("gn_regstatus") == null || gconf.getValue("gn_psmversion").equals(""))
			return false;
		final String local = env.getProperty("omilab.version").replace("-SNAPSHOT","").replace("-RELEASE", "");
		final String remote = gconf.getValue("gn_psmversion").replace("v","");
		if(versionCompare(local,remote) < 0)
			return true;
		return false;
	}

	@Override
	@Scheduled(fixedRate = 5000000)
	public void sync() {
		if(gconf.getValue("gn_regstatus").equals("true")) {
			logger.debug("Registration with Global Network detected!");
			Map<String,String> action = new HashMap<>();
			action.put("uuid",gconf.getValue("gn_uuid"));
			action.put("psmversion",env.getProperty("omilab.version"));
			action.put("psmbuild",env.getProperty("omilab.buildTimestamp"));
			action.put("psmcommit",env.getProperty("omilab.commit"));
			action.put("apiurl",env.getProperty("app.url") + "/rest/");
			final GenericRequest gr = new GenericRequest("synchronization",null,action);
			final Client client = ClientBuilder.newClient();
			final WebTarget target = client.target(NETWORK_REPOSITORY_SYNC);
			final GenericServiceContent gsc = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(gr, MediaType.APPLICATION_JSON), GenericServiceContent.class);
			logger.debug("Received message from OMiLAB HQ!");
			gconf.setKeyValue("gn_psmversion",gsc.getSubmenu().get("psmversion"));
			gconf.setKeyValue("gn_ips",gsc.getSubmenu().get("ips"));
			final Date curr = new Date();
			gconf.setKeyValue("gn_lastcontact",curr.toString());
			if(gsc.getSubmenu().get("unregister").equals("true")) {
				remove();
			}

		} else
			logger.debug("Not calling OMiLAB Global Network, as no registration is present");
	}
}
