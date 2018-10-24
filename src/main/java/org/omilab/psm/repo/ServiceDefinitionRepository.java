// File:         ServiceRepository.java
// Created:      15.02.2015
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.repo;

import org.omilab.psm.model.db.AbstractProject;
import org.omilab.psm.model.db.ServiceDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import javax.transaction.Transactional;
import java.util.List;

public interface ServiceDefinitionRepository extends Repository<ServiceDefinition, Long> {

	@Modifying
	@Transactional
	@Query("delete from ServiceDefinition s where s = ?1")
	void delete(ServiceDefinition sd);

	Page<ServiceDefinition> findAll(Pageable pageable);

	List<ServiceDefinition> findAll();

	ServiceDefinition findById(Long id);

	@Query("select s from ServiceDefinition s join s.serviceinstance si where si.project = ?1")
	List<ServiceDefinition> findByProject(AbstractProject project);

	ServiceDefinition findByUrlidentifier(String urlidentifier);

	ServiceDefinition findBySpecial(String special);

	@Query("select s from ServiceDefinition s where s.visible = true")
	Page<ServiceDefinition> findByVisibleTrue(Pageable pageable);

	ServiceDefinition save(ServiceDefinition sd);

}
