// File:         MainNavigationRepository.java
// Created:      13.08.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.repo;

import org.omilab.psm.model.db.MainNavigationItem;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import javax.transaction.Transactional;
import java.util.List;

public interface MainNavigationRepository<T extends MainNavigationItem>  extends Repository<T, Long> {

	@Query("select n from #{#entityName} n order by n.seq ASC")
	List<T> findAll();

	@Query("select n from #{#entityName} n where n.id = ?1")
	T findById(Long id);

	@Query("select n from #{#entityName} n where n.link = ?1")
	T findByLink(String link);

	@Modifying
	@Transactional
	@Query("delete from #{#entityName} n where n.id=?1")
	void deleteById(Long id);

	@Modifying
	@Transactional
	@Query("update #{#entityName} n set seq = ?1 where n.id = ?2")
	void updateOrder(Integer seq, Long id);

	T save(T mni);
}
