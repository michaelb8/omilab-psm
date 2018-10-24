package org.omilab.psm.conf;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class CASUserDetailsService implements AuthenticationUserDetailsService<Authentication> {

	public CASUserDetailsService() {
		super();
	}

	@Override
	public UserDetails loadUserDetails(Authentication token) throws UsernameNotFoundException {
		String login = token.getPrincipal().toString();
		String lowercaseLogin = login.toLowerCase();

		List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();

		grantedAuthorities.add(new GrantedAuthority() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getAuthority() {
				return "ROLE_AUTHENTICATED";
			}
		});

		return new org.springframework.security.core.userdetails.User(lowercaseLogin, lowercaseLogin, grantedAuthorities);
	}
}
