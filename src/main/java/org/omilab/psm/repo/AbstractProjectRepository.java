// File:         ProjectRepository.java
// Created:      18.02.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2016 by OMiLAB.ORG
//

package org.omilab.psm.repo;


import org.omilab.psm.model.db.AbstractProject;
import org.omilab.psm.model.db.MainNavigationItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import javax.transaction.Transactional;
import java.util.List;

public interface AbstractProjectRepository<T extends AbstractProject>  extends Repository<T, Long> {

	Page<T> findAll(Pageable pageable);

	List<T> findAll();

	T findById(Long id);

	@Query("select p from #{#entityName} p where p.uniqueID = ?1")
	T findByUniqueID(String uuid);

	@Query("select p from #{#entityName} p where p.urlidentifier = ?1")
	T findByUrlidentifier(String urlidentifier);

	@Query("select p from #{#entityName} p where p.abbreviation = ?1")
	T findByAbbreviation(String abbreviation);

	@Query("select p from #{#entityName} p where p.name = ?1")
	T findByName(String name);

	T save(AbstractProject abstractProject);

	@Modifying
	@Transactional
	@Query("delete from #{#entityName} p where p.id=?1")
	void deleteById(Long id);

}
