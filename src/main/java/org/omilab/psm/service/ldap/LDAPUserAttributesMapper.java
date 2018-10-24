package org.omilab.psm.service.ldap;

import org.springframework.ldap.core.AttributesMapper;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

/**
 * manages attribute mapping from ldap directory
 *
 * @author gerald
 */
public final class LDAPUserAttributesMapper implements AttributesMapper {

	@Override
	public LDAPUser mapFromAttributes(Attributes attributes) throws NamingException {

		LDAPUser userObject = new LDAPUser();

		String cn = (String) attributes.get("cn").get();
		userObject.setCommonName(cn);

		if(attributes.get("mail") != null) {
			String email = attributes.get("mail").get().toString();
			userObject.setEmailAddress(email);
		}
		if(attributes.get("givenName") != null) {
			String firstName = attributes.get("givenName").get().toString();
			userObject.setFirstName(firstName);
		}
		if(attributes.get("sn") != null) {
			String lastName = attributes.get("sn").get().toString();
			userObject.setLastName(lastName);
		}

		if(attributes.get("title") != null) {
			String title = attributes.get("title").get().toString();
			userObject.setTitle(title);
		}

		if(attributes.get("o") != null) {
			String organizationName = attributes.get("o").get().toString();
			userObject.setOrganization(organizationName);
		}

		if(attributes.get("ou") != null) {
			String subOrganizationName = attributes.get("ou").get().toString();
			userObject.setSuborganization(subOrganizationName);
		}

		if(attributes.get("telephoneNumber") != null) {
			String phone = attributes.get("telephoneNumber").get().toString();
			userObject.setPhone(phone);
		}

		if(attributes.get("telephoneNumber") != null) {
			String phone = attributes.get("telephoneNumber").get().toString();
			userObject.setPhone(phone);
		}

		if(attributes.get("street") != null) {
			String street = attributes.get("street").get().toString();
			userObject.setStreet(street);
		}

		if(attributes.get("postalAddress") != null) {
			String postal = attributes.get("postalAddress").get().toString();
			userObject.setPostal(postal);
		}

		if(attributes.get("jpegPhoto") != null) {
			byte[] photo = (byte[])attributes.get("jpegPhoto").get();
			userObject.setJpegPhoto(photo);
		}

		return userObject;
	}
}