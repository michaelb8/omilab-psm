// File:         GlobalConfigurationServiceImpl.java
// Created:      14.01.16
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2016 by OMiLAB.ORG
//

package org.omilab.psm.service;

import org.omilab.psm.model.db.GlobalConfiguration;
import org.omilab.psm.repo.GlobalConfigurationRepository;
import org.omilab.psm.service.logging.LogMessage;
import org.omilab.psm.service.logging.LoggingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Component("GlobalConfigurationService")
@Transactional
public class GlobalConfigurationServiceImpl implements GlobalConfigurationService {

	private final GlobalConfigurationRepository configurationRepo;
	private final LoggingService logService;
	private final UserService userService;

	@Autowired
	public GlobalConfigurationServiceImpl(final GlobalConfigurationRepository configurationRepo, final LoggingService logService,
										  final UserService userService) {
		this.configurationRepo = configurationRepo;
		this.logService = logService;
		this.userService = userService;
	}

	@Override
	public void setKeyValue(final String key, final String value) {
		if(configurationRepo.findById(key) != null)
			configurationRepo.findById(key).setValue(value);
		else
			configurationRepo.save(new GlobalConfiguration(key,value));
		logService.logMessage(new LogMessage(userService.getCurrentUser().getUsername(), "update", "GlobalConfiguration", key));
	}

	@Override
	public String getValue(final String key) {
		GlobalConfiguration result = configurationRepo.findById(key);

		if(key.equals("wizard_accepted_template") && result == null) {
			return "Dear $FIRSTNAME $LASTNAME,\n\nYour project proposal has been accepted! You may proceed with the creation procedure at: $LINK \n\nBest regards,\nThe OMiLAB Team";
		}
		if(key.equals("wizard_rejected_template") && result == null) {
			return "Dear $FIRSTNAME $LASTNAME,\n\nYour project proposal has been rejected! \n\nBest regards,\nThe OMiLAB Team";
		}
		if(key.equals("wizard_notification_new") && result == null) {
			return "david.goetzinger@univie.ac.at,simon.doppler@univie.ac.at";
		}
		if(key.equals("wizard_notification_finished") && result == null) {
			return "david.goetzinger@univie.ac.at,simon.doppler@univie.ac.at";
		}
		if(key.equals("footer") && result == null) {
			return "<br> <p style=\"text-align:center;\">Footer is unconfigured!</p>";
		}
		if(key.equals("gn_regstatus") && result == null) {
			return "false";
		}
		if(key.equals("gn_psmversion") && result == null) {
			return "";
		}
		if(key.equals("gn_lastcontact") && result == null) {
			return new Date(0).toString();
		}
		if(key.equals("gn_ips") && result == null) {
			return "127.0.0.1,127.0.1.1";
		}

		return result.getValue();
	}

	@Override
	public void removeValue(final String key) {
		configurationRepo.delete(configurationRepo.findById(key));
	}
}
