// File:         MainNavigationHTMLRepository.java
// Created:      13.08.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.repo;

import org.omilab.psm.model.db.MainNavigationItemProject;
import org.omilab.psm.model.db.AbstractProject;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface MainNavigationProjectRepository extends MainNavigationRepository<MainNavigationItemProject> {

	@Query("select p from AbstractProject p join p.navigation mni where mni = ?1")
	List<AbstractProject> findProjectForNavigation(MainNavigationItemProject mni);
}