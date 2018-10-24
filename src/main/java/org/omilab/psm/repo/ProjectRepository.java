// File:         ProjectRepository.java
// Created:      18.02.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.repo;


import org.omilab.psm.model.db.AbstractProject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import javax.transaction.Transactional;
import java.util.List;

public interface ProjectRepository extends Repository<AbstractProject, Long> {

	Page<AbstractProject> findAll(Pageable pageable);

	List<AbstractProject> findAll();

	AbstractProject findById(Long id);

	@Query("select p from AbstractProject p where p.uniqueID = ?1")
	AbstractProject findByUniqueID(String uuid);

	@Query("select p from AbstractProject p where p.urlidentifier = ?1")
	AbstractProject findByUrlidentifier(String urlidentifier);

	@Query("select p from AbstractProject p where p.abbreviation = ?1")
	AbstractProject findByAbbreviation(String abbreviation);

	AbstractProject save(AbstractProject project);


}
