// File:         GlobalConfigurationRepository.java
// Created:      14.01.16
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2016 by OMiLAB.ORG
//

package org.omilab.psm.repo;

import org.omilab.psm.model.db.GlobalConfiguration;
import org.springframework.data.repository.Repository;

public interface GlobalConfigurationRepository extends Repository<GlobalConfiguration, Long> {

	GlobalConfiguration findById(String key);

	GlobalConfiguration save(GlobalConfiguration config);

	void delete(GlobalConfiguration config);

}
