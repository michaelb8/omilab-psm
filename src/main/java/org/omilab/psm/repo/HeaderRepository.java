// File:         HeaderRepository.java
// Created:      21.05.2015
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//


package org.omilab.psm.repo;

import org.omilab.psm.model.db.HeaderConfiguration;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import javax.transaction.Transactional;
import java.util.List;

public interface HeaderRepository extends Repository<HeaderConfiguration, Long> {

	@Query("select hc from HeaderConfiguration hc order by hc.seq ASC")
	List<HeaderConfiguration> findAll();

	HeaderConfiguration save(HeaderConfiguration header);

	@Query("select hc from HeaderConfiguration hc where hc.id = ?1")
	HeaderConfiguration findById(Long id);

	@Query("select hc from HeaderConfiguration hc where hc.seq = ?1")
	HeaderConfiguration findByOrder(int seq);


	@Query("SELECT CASE WHEN COUNT(hc) > 0 THEN 'true' ELSE 'false' END FROM HeaderConfiguration hc WHERE hc.visible = 1 AND hc.starter = 1")
	Boolean existsActiveStarterHeader();

	@Query("select hc from HeaderConfiguration hc where hc.visible = true order by hc.seq ASC")
	List<HeaderConfiguration> findVisibleHeaders();

	@Modifying
	@Transactional
	@Query("update HeaderConfiguration hc set seq = ?1 where hc.id = ?2")
	void updateOrder(Integer seq, Long id);

	@Modifying
	@Transactional
	@Query("delete from HeaderConfiguration hc where hc.id=?1")
	void deleteById(Long id);


}
