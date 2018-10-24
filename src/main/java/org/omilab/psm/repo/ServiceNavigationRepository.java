// File:         ServiceNavigationRepository.java
// Created:      19.02.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.repo;

import org.omilab.psm.model.db.DBNavigationItem;
import org.omilab.psm.model.db.AbstractProject;
import org.omilab.psm.model.db.ServiceDefinition;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import javax.transaction.Transactional;
import java.util.List;

public interface ServiceNavigationRepository extends Repository<DBNavigationItem, Long> {

	void delete(DBNavigationItem dbItem);

	@Modifying
	@Transactional
	@Query("delete from DBNavigationItem db where db.servicedefinition=?1")
	void deleteByServicedefinition(ServiceDefinition sd);

	@Query("select db from DBNavigationItem db order by db.seq ASC")
	List<DBNavigationItem> findAllBySeqAsc();

	@Query("select db from DBNavigationItem db where db.servicedefinition = ?1 and db.endpoint = ?2")
	DBNavigationItem findNavigationItemByEndpoint(ServiceDefinition sd, String endpoint);

	@Query("select db from DBNavigationItem db join db.project_navigation pn join pn.project p where p = ?1 order by db.seq ASC")
	List<DBNavigationItem> findNavigationItemsForProject(AbstractProject project);

	@Query("select db from DBNavigationItem db where db.servicedefinition = ?1 order by db.seq ASC")
	List<DBNavigationItem> findNavigationItemsForService(ServiceDefinition sd);

	@Query(value = "SELECT id FROM dbnavigation_item WHERE EXISTS ( SELECT items_id FROM psm.abstract_project INNER JOIN projecttype_navigation ON abstract_project.projecttype_id = projecttype_navigation.types_id WHERE abstract_project.id=(?1)) AND dbnavigation_item.servicedefinition_id=(?2);", nativeQuery = true)
	List<Long> checkActivedEndpointsForProjectAndService(Long projectid, Long serviceid);

	DBNavigationItem save(DBNavigationItem dbItem);

	DBNavigationItem findById(Long id);

	@Query("select max(db.seq) from DBNavigationItem db")
	Integer findHighest();

	@Modifying
	@Transactional
	@Query("update DBNavigationItem db set seq = ?1 where db.id = ?2")
	void updateOrderForItem(Integer seq, Long id);

}
