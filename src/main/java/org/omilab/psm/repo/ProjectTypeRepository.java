// File:         ProjectTypeRepository.java
// Created:      29.05.2015
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//


package org.omilab.psm.repo;

import org.omilab.psm.model.db.DBNavigationItem;
import org.omilab.psm.model.db.AbstractProject;
import org.omilab.psm.model.db.ProjectType;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import javax.transaction.Transactional;
import java.util.List;

public interface ProjectTypeRepository extends Repository<ProjectType, Long> {

	List<ProjectType> findAll();

	ProjectType save(ProjectType pt);

	ProjectType findById(Long id);

	@Query("select p from AbstractProject p join p.projecttype pt where pt.id = ?1")
	List<AbstractProject> findProjects(Long id);

	@Query("select db from DBNavigationItem db join db.types t where t.id = ?1")
	List<DBNavigationItem> findDBNavigationItems(Long id);

	@Query("select db.endpoint from DBNavigationItem db join db.types t where t.id = ?1")
	List<String> findEndpoints(Long id);

	@Modifying
	@Transactional
	@Query(value = "DELETE projecttype_navigation FROM projecttype_navigation  WHERE projecttype_navigation.items_id = (?1);", nativeQuery = true)
	void deleteByDBNavigationItem(Long dbItem);

	@Modifying
	@Transactional
	@Query("delete from ProjectType pt where pt.id=?1")
	void deleteById(Long id);


}
