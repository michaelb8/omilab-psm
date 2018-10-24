// File:         MainNavigationTypesRepository.java
// Created:      13.08.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.repo;

import org.omilab.psm.model.db.*;
import org.omilab.psm.model.db.projectoverlay.Event;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface MainNavigationTypesRepository extends MainNavigationRepository<MainNavigationItemTypes> {

	@Query("select pt from ProjectType pt join pt.page p where p = ?1")
	List<ProjectType> findProjectTypesForNavigation(MainNavigationItemTypes types);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM main_navigation_item_types WHERE page_id=(?1)", nativeQuery = true)
	void prepareDeleteById(Long id);

	@Query("select p from AbstractProject p join p.projecttype pt join pt.page mni where mni = ?1 and p.inConfig = false")
	List<AbstractProject> findProjectsByMNI(MainNavigationItemTypes types);

	@Query("select p from Event p join p.projecttype pt join pt.page mni where mni = ?1 and p.inConfig = false order by p.start asc")
	List<Event> findProjectsByMNIChronologically(MainNavigationItemTypes types);
}
