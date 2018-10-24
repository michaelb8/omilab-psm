// File:         ServiceInstanceRepository.java
// Created:      19.02.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.repo;

import org.omilab.psm.model.db.AbstractProject;
import org.omilab.psm.model.db.ProjectType;
import org.omilab.psm.model.db.ServiceDefinition;
import org.omilab.psm.model.db.ServiceInstance;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import javax.transaction.Transactional;
import java.util.List;

public interface ServiceInstanceRepository extends Repository<ServiceInstance, Long> {

	List<ServiceInstance> findAll();

	ServiceInstance findByInstanceidlocal(Long id);

	@Modifying
	@Transactional
	@Query("delete from ServiceInstance s where s.project = ?1 and s.servicedefinition=?2")
	void deleteByProjectAndServicedefinition(AbstractProject project, ServiceDefinition sd);

	@Query("select s from ServiceInstance s where s.project = ?1")
	List<ServiceInstance> findByProject(AbstractProject project);

	@Query("select s from ServiceInstance s where s.project = ?1 and s.servicedefinition = ?2")
	ServiceInstance findByProjectAndServicedefinition(AbstractProject project, ServiceDefinition sd);

	@Query("select p from ServiceInstance si join si.project p where si.servicedefinition = ?1")
	List<AbstractProject> findProjectUsageByServiceDefinitionID(ServiceDefinition sd);

	@Query("select si from ServiceInstance si join si.project p join si.servicedefinition sd where sd.special = ?2 and p = ?1")
	ServiceInstance findSpecialServiceInstanceIdForProject(AbstractProject p, String special);

	ServiceInstance save(ServiceInstance si);

}

