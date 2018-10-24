// File:         ProjectProposalRepository.java
// Created:      04.01.16
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2016 by OMiLAB.ORG
//

package org.omilab.psm.repo;

import org.omilab.psm.model.db.ProjectProposal;
import org.omilab.psm.model.db.ProjectType;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import javax.transaction.Transactional;
import java.util.List;

public interface ProjectProposalRepository extends Repository<ProjectProposal, Long> {

	@Query("select p from ProjectProposal p join p.type pt where p.acceptedStatus = null and pt.id = ?1")
	List<ProjectProposal> findUnreadProposals(Long type);

	ProjectProposal save(ProjectProposal pp);

	ProjectProposal findById(Long id);

	ProjectProposal findByProposalID(String proposalID);

	@Query("select count(p) from ProjectProposal p where p.userid = ?1 and p.finished = false and p.acceptedStatus = true")
	Integer countProposals(String username);

	@Query("select p from ProjectProposal p where p.userid = ?1 and p.finished = false and p.acceptedStatus = true")
	List<ProjectProposal> findUnfinishedUserProposals(String username);

	@Query("select p from ProjectProposal p join p.type pt where pt = ?1 and p.finished = false and p.acceptedStatus = true")
	List<ProjectProposal> findUnfinishedTypeProposals(ProjectType type);

	@Query("select p from ProjectProposal p join p.type pt where pt = ?1")
	List<ProjectProposal> findProposalsByType(ProjectType pt);

	@Query("select p from ProjectProposal p join p.project ab where ab.id = ?1")
	ProjectProposal findProposalsByProjectId(Long id);

	@Modifying
	@Transactional
	@Query("delete from ProjectProposal p where p.id=?1")
	void deleteById(Long id);

	@Modifying
	@Transactional
	@Query("update ProjectProposal p set p.type = NULL where p.id = ?1")
	void updateToNull(Long id);

}
