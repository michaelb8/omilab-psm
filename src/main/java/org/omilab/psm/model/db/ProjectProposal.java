// File:         ProjectProposal.java
// Created:      04.01.16
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2016 by OMiLAB.ORG
//

package org.omilab.psm.model.db;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.UUID;

@Entity
public class ProjectProposal {

	@Id
	@GeneratedValue
	private Long id;

	@Size(min = 0, max = 140)
	@Column(nullable = false)
	private String name;

	@Size(min = 0, max = 500)
	@Column(nullable = false)
	private String projectabstract;

	@Column
	private String userid;

	@Column
	private String email;

	@Column
	private String proposalID;

	@Column
	private Boolean inCreation;

	@Column
	private Integer pos;

	@OneToOne
	private AbstractProject project;

	@Column
	private String userStarted;

	@Column
	private String userStopped;

	@Column
	private Date dateStarted;

	@Column
	private Date dateFinished;

	@Column
	private Date dateAccepted;

	@Column
	private String userAccepted;

	@Column
	private Boolean finished;

	@ManyToOne
	@JoinColumn
	private ProjectType type;

	@Column
	private Boolean acceptedStatus;

	public ProjectProposal() {
	}

	public ProjectProposal(final String name, final String projectabstract, final String userid, final String email, final ProjectType type) {
		this.name = name;
		this.projectabstract = projectabstract;
		this.userid = userid;
		this.email = email;
		this.proposalID = UUID.randomUUID().toString();
		this.type = type;
		this.finished = false;
		this.inCreation = false;
	}

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getProjectabstract() {
		return projectabstract;
	}

	public void setProjectabstract(final String projectabstract) {
		this.projectabstract = projectabstract;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(final String userid) {
		this.userid = userid;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(final String email) {
		this.email = email;
	}

	public String getProposalID() {
		return proposalID;
	}

	public void setProposalID(final String proposalID) {
		this.proposalID = proposalID;
	}

	public ProjectType getType() {
		return type;
	}

	public void setType(final ProjectType type) {
		this.type = type;
	}

	public Boolean getAcceptedStatus() {
		return acceptedStatus;
	}

	public void setAcceptedStatus(final Boolean acceptedStatus) {
		this.acceptedStatus = acceptedStatus;
	}

	public Boolean getInCreation() {
		return inCreation;
	}

	public void setInCreation(final Boolean inCreation) {
		this.inCreation = inCreation;
	}

	public Boolean getFinished() {
		return finished;
	}

	public void setFinished(final Boolean finished) {
		this.finished = finished;
	}

	public Integer getPos() {
		return pos;
	}

	public String getUserStarted() {
		return userStarted;
	}

	public void setUserStarted(final String userStarted) {
		this.userStarted = userStarted;
	}

	public String getUserStopped() {
		return userStopped;
	}

	public void setUserStopped(final String userStopped) {
		this.userStopped = userStopped;
	}

	public void setPos(final Integer pos) {
		this.pos = pos;


	}

	public AbstractProject getProject() {
		return project;
	}

	public void setProject(final AbstractProject project) {
		this.project = project;
	}

	public Date getDateStarted() {
		if(dateStarted == null)
			return null;
		else
			return (Date)dateStarted.clone();
	}

	public void setDateStarted(final Date dateStarted) {
		this.dateStarted = (Date)dateStarted.clone();
	}

	public Date getDateFinished() {
		if( dateFinished == null)
			return null;
		else
			return (Date)dateFinished.clone();
	}

	public void setDateFinished(final Date dateFinished) {
		this.dateFinished = (Date)dateFinished.clone();
	}

	public String getUserAccepted() {
		return userAccepted;
	}

	public void setUserAccepted(final String userAccepted) {
		this.userAccepted = userAccepted;
	}

	public Date getDateAccepted() {
		if( dateAccepted == null)
			return null;
		else
			return (Date)dateAccepted.clone();
	}

	public void setDateAccepted(Date dateAccepted) {
		this.dateAccepted = (Date)dateAccepted.clone();
	}
}
