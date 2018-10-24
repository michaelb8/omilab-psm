package org.omilab.psm.service.ldap;

import java.io.Serializable;

public final class LDAPUser implements Serializable {

	private static final long serialVersionUID = 1L;

	private String title;

	private String commonName;

	private String firstName;

	private String lastName;

	private String emailAddress;

	private String organization;

	private String suborganization;

	private String phone;

	private String street;

	private String postal;

	private byte[] jpegPhoto;

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(final String commonName) {
		this.commonName = commonName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(final String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(final String lastName) {
		this.lastName = lastName;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(final String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(final String organization) {
		this.organization = organization;
	}

	public String getSuborganization() {
		return suborganization;
	}

	public void setSuborganization(final String suborganization) {
		this.suborganization = suborganization;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(final String phone) {
		this.phone = phone;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(final String street) {
		this.street = street;
	}

	public String getPostal() {
		return postal;
	}

	public void setPostal(final String postal) {
		this.postal = postal;
	}

	public byte[] getJpegPhoto() {
		if(jpegPhoto != null)
			return jpegPhoto.clone();
		return null;
	}

	public void setJpegPhoto(final byte[] jpegPhoto) {
		this.jpegPhoto = jpegPhoto.clone();
	}

	@Override
	public String toString() {
		return "LDAPUser{" +
				"commonName='" + commonName + '\'' +
				", firstName='" + firstName + '\'' +
				", lastName='" + lastName + '\'' +
				", emailAddress='" + emailAddress + '\'' +
				'}';
	}
}