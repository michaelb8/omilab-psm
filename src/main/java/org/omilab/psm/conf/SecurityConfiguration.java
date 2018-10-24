package org.omilab.psm.conf;


import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private Environment env;

	@Autowired
	private CasAuthenticationProvider authProvider;

	@Bean
	public ServiceProperties serviceProperties() {
		ServiceProperties sp = new ServiceProperties();
		sp.setSendRenew(false);
		sp.setService(env.getProperty("app.url") + "/j_spring_cas_security_check");
		return sp;
	}

	@SuppressWarnings("rawtypes")
	@Autowired
	private AuthenticationUserDetailsService customUserDetailsService() {
		return new CASUserDetailsService();
	}


	@Bean
	public CasAuthenticationProvider casAuthenticationProvider() {
		CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
		casAuthenticationProvider.setAuthenticationUserDetailsService(customUserDetailsService());
		casAuthenticationProvider.setServiceProperties(serviceProperties());
		casAuthenticationProvider.setTicketValidator(cas20ServiceTicketValidator());
		casAuthenticationProvider.setKey("an_id_for_this_auth_provider_only");
		return casAuthenticationProvider;
	}

	@Bean
	public Cas20ServiceTicketValidator cas20ServiceTicketValidator() {
		return new Cas20ServiceTicketValidator(env.getProperty("cas.service.url"));
	}

	@Bean
	public CasAuthenticationFilter casAuthenticationFilter() throws Exception {
		CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
		casAuthenticationFilter.setAuthenticationManager(authenticationManager());
		casAuthenticationFilter.setAuthenticationSuccessHandler(savedRequestAwareAuthenticationSuccessHandler());
		return casAuthenticationFilter;
	}

	@Bean
	public CasAuthenticationEntryPoint casAuthenticationEntryPoint() {
		CasAuthenticationEntryPoint ep = new CasAuthenticationEntryPoint();
		ep.setLoginUrl(env.getProperty("cas.service.url") + "/login");
		ep.setServiceProperties(serviceProperties());
		return ep;
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authProvider);
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/js/**").antMatchers("/fonts/**").antMatchers("/images/**").antMatchers("/css/**");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.exceptionHandling().
				authenticationEntryPoint(casAuthenticationEntryPoint()).and().addFilter(casAuthenticationFilter()).
				logout().logoutUrl("/caslogout").addLogoutHandler(logoutHandler()).logoutSuccessUrl("/").deleteCookies("JSESSIONID").permitAll().and().
				csrf().disable().headers().frameOptions().disable().authorizeRequests().antMatchers("/rest/**").permitAll().
				antMatchers("/login/**").authenticated().antMatchers("/settings/**").authenticated().
				antMatchers("/projects/*/settings").authenticated().antMatchers("/projects/*/role").authenticated().
				antMatchers("/projects/*/*/admin").authenticated().antMatchers("/wizard/**").authenticated().
				antMatchers("/**").permitAll();
	}


	@Bean
	public SavedRequestAwareAuthenticationSuccessHandler savedRequestAwareAuthenticationSuccessHandler() {
		CASAuthSuccessHandler auth = new CASAuthSuccessHandler();
		return auth;
	}

	@Bean
	public CASLogoutHandler logoutHandler() {
		CASLogoutHandler logout = new CASLogoutHandler();
		return logout;
	}


	@EnableGlobalMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
	private static class GlobalSecurityConfiguration extends GlobalMethodSecurityConfiguration {
	}
}
