// File:         KeywordRepository.java
// Created:      19.02.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.repo;

import org.omilab.psm.model.db.AbstractProject;
import org.omilab.psm.model.db.Keyword;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface KeywordRepository extends Repository<Keyword, Long> {

	List<Keyword> findAll();

	Keyword findByContent(String content);

	@Query("select k from Keyword k join k.projects p where p = ?1 order by k.content asc")
	List<Keyword> findByProject(AbstractProject project);

	@Query("select k from Keyword k join k.projects p where p = ?1 and k.content = ?2")
	Keyword findByProjectAndContent(AbstractProject project, String content);

	Keyword save(Keyword keyword);
}
