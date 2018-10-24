// File:         ProjectSearch.java
// Created:      26.02.16
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2016 by OMiLAB.ORG
//


package org.omilab.psm.repo;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.hunspell.Dictionary;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.util.Version;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.jsoup.Jsoup;
import org.omilab.psm.conf.OverlayUndefinedException;
import org.omilab.psm.model.db.AbstractProject;
import org.omilab.psm.model.db.MainNavigationItemHTML;
import org.omilab.psm.model.db.ProjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@Transactional
public class ProjectSearch {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ProjectTypeRepository ptRepo;

    @Autowired
    private MainNavigationHTMLRepository mainNavigationHTMLRepository;

    public List<MainNavigationItemHTML> searchHTML(String text){
        List<MainNavigationItemHTML> returnList = new ArrayList<>();

        for(MainNavigationItemHTML item : mainNavigationHTMLRepository.findAll()){
            if(Jsoup.parse(item.getHtml()).text().toLowerCase().contains(text.toLowerCase())){
                returnList.add(item);
            }
        }

        return returnList;
    }

    public List<AbstractProject> search(String text) {

        FullTextEntityManager fullTextEntityManager =
                org.hibernate.search.jpa.Search.
                        getFullTextEntityManager(entityManager);

        QueryBuilder queryBuilder =
                fullTextEntityManager.getSearchFactory()
                        .buildQueryBuilder().forEntity(AbstractProject.class).get();

        org.apache.lucene.search.Query queryABfuzzy =
            queryBuilder
                .keyword()
                .fuzzy()
                .withThreshold( .8f )
                .withPrefixLength( 3 )
                .onFields("abbreviation", "name", "urlidentifier")
                .matching(text)
                .createQuery();

        org.apache.lucene.search.Query queryABwildcard =
                queryBuilder
                        .keyword()
                        .wildcard()
                        .onFields("abbreviation", "name", "urlidentifier")
                        .matching(text)
                        .createQuery();

        BooleanQuery combined = new BooleanQuery();
        combined.add(queryABfuzzy, BooleanClause.Occur.SHOULD);
        combined.add(queryABwildcard, BooleanClause.Occur.SHOULD);


        Set<String> classNames = new HashSet<>();
        for(ProjectType pt : ptRepo.findAll()) {
            classNames.add(pt.getOverlay());
        }
       for(String name : classNames) {
            try {

                Class<?> clazz = Class.forName(name);
                QueryBuilder tempBuilder =
                        fullTextEntityManager.getSearchFactory()
                                .buildQueryBuilder().forEntity(clazz).get();

                Set<String> fieldNames = new HashSet<>();
                for(Field field  : clazz.getDeclaredFields()) {
                    if (field.isAnnotationPresent(org.hibernate.search.annotations.Field.class)) {
                        fieldNames.add(field.getName());
                    }
                }
                String[] searchFields = fieldNames.toArray(new String[fieldNames.size()]);

                org.apache.lucene.search.Query tempFuzzy =
                        tempBuilder
                                .keyword()
                                .fuzzy()
                                .withThreshold( .8f )
                                .withPrefixLength( 3 )
                                .onFields(searchFields)
                                .matching(text)
                                .createQuery();

                org.apache.lucene.search.Query tempWildcard =
                        tempBuilder
                                .keyword()
                                .wildcard()
                                .onFields(searchFields)
                                .matching(text)
                                .createQuery();

                combined.add(tempFuzzy, BooleanClause.Occur.SHOULD);
                combined.add(tempWildcard, BooleanClause.Occur.SHOULD);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new OverlayUndefinedException("Project Template has no overlay defined! Contact administrator!");
            }
        }

        org.hibernate.search.jpa.FullTextQuery jpaQuery =
                fullTextEntityManager.createFullTextQuery(combined, AbstractProject.class);

        @SuppressWarnings("unchecked")
        List<AbstractProject> results = jpaQuery.getResultList();

        return results;
    }

}
