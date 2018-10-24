// File:         ProjectNavigationRepository.java
// Created:      18.02.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//


package org.omilab.psm.repo;

import org.omilab.psm.model.db.AbstractProject;
import org.omilab.psm.model.db.DBNavigationItem;
import org.omilab.psm.model.db.Project_Navigation;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import javax.transaction.Transactional;

public interface ProjectNavigationRepository extends Repository<Project_Navigation, Long> {

	void delete(Project_Navigation item);

	@Modifying
	@Transactional
	@Query(value = "DELETE project_navigation FROM project_navigation INNER JOIN dbnavigation_item ON dbnavigation_item.id=project_navigation.dbnavigationitem_id WHERE dbnavigation_item.servicedefinition_id = (?1);", nativeQuery = true)
	void deleteByServiceDefinition(Long sd);

	@Modifying
	@Transactional
	@Query(value = "DELETE project_navigation FROM project_navigation  WHERE project_navigation.project_id = (?1) AND project_navigation.dbnavigationitem_id = (?2);", nativeQuery = true)
	void deleteByProjectAndDBNavigationItem(Long project, Long dbItem);

	@Modifying
	@Transactional
	@Query(value = "DELETE project_navigation FROM project_navigation  WHERE project_navigation.dbnavigationitem_id = (?1);", nativeQuery = true)
	void deleteByDBNavigationItem(Long dbItem);

	@Modifying
	@Transactional
	@Query(value = "DELETE project_navigation FROM project_navigation INNER JOIN dbnavigation_item ON dbnavigation_item.id=project_navigation.dbnavigationitem_id WHERE dbnavigation_item.servicedefinition_id = (?1) AND project_navigation.project_id = (?2);", nativeQuery = true)
	void deleteByServiceDefinitionAndProject(Long sd, Long project);

	@Query("select pn from Project_Navigation pn join pn.project p join pn.dbnavigationitem db where p = ?1 AND db = ?2")
	Project_Navigation findByProjectAndDBNavigationItem(AbstractProject project, DBNavigationItem dbItem);

	@Query("SELECT CASE WHEN COUNT(pn) > 0 THEN 'true' ELSE 'false' END FROM Project_Navigation pn WHERE pn.project = ?1 and pn.dbnavigationitem = ?2")
	Boolean existsByProjectAndDBNavigation(AbstractProject p, DBNavigationItem dbItem);

	Project_Navigation save(Project_Navigation pn);

}