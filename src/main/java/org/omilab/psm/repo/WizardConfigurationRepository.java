// File:         WizardConfigurationRepository.java
// Created:      01.02.16
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2016 by OMiLAB.ORG
//

package org.omilab.psm.repo;

import org.omilab.psm.model.db.DBNavigationItem;
import org.omilab.psm.model.db.ProjectType;
import org.omilab.psm.model.db.ServiceDefinition;
import org.omilab.psm.model.db.WizardConfigurationEntry;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import javax.transaction.Transactional;
import java.util.List;

public interface WizardConfigurationRepository extends Repository<WizardConfigurationEntry, Long> {

	@Query("select db from DBNavigationItem db join db.wizard_config wiz join wiz.type t where t = ?1")
	List<DBNavigationItem> findByType(ProjectType type);

	@Query("select wce from WizardConfigurationEntry wce join wce.type t where t = ?1")
	List<WizardConfigurationEntry> findWceByType(ProjectType type);

	WizardConfigurationEntry save(WizardConfigurationEntry wce);

	WizardConfigurationEntry findById(Long id);

	@Query("select wce from WizardConfigurationEntry wce join wce.dbnavigationitem db join wce.type t where t.id = ?2 and db.id = ?1")
	WizardConfigurationEntry findByDBNIandType(Long dbni,Long type);

	@Query("select db from DBNavigationItem db join db.wizard_config wiz join wiz.type t where t.id = ?1")
	List<DBNavigationItem> findDBNIByTypeInWCE(Long id);

	@Query("select db from DBNavigationItem db join db.wizard_config wiz join wiz.type t join db.servicedefinition sd where sd = ?1 and t = ?2")
	List<DBNavigationItem> findBySD(ServiceDefinition sd, ProjectType pt);

	@Modifying
	@Transactional
	@Query("delete from WizardConfigurationEntry wce where wce = ?1")
	void delete(WizardConfigurationEntry wce);

}
