// File:         PSMApplication.java
// Created:      13.02.2015
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm;

import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;
import org.omilab.psm.conf.DebugInterceptor;
import org.omilab.psm.conf.JerseyInitialization;
import org.omilab.psm.conf.RestAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.*;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;


import javax.servlet.http.HttpSessionListener;
import java.util.Locale;


@SpringBootApplication
@Configuration
@ComponentScan
@EnableJpaRepositories
@EnableAsync
@EnableScheduling
public class PSMApplication extends WebMvcConfigurerAdapter {

	@Autowired
	Environment env;

	public static void main(String[] args) throws Exception {
		new SpringApplicationBuilder(PSMApplication.class).run(args);
	}

	@Bean
	public MessageSource messageSource() {
		final ResourceBundleMessageSource messageSource;
		messageSource = new ResourceBundleMessageSource();
		messageSource.setDefaultEncoding("UTF-8");
		messageSource.setBasename("Messages");
		return messageSource;
	}

	@Bean
	public LocaleResolver localeResolver() {
		SessionLocaleResolver slr = new SessionLocaleResolver();
		slr.setDefaultLocale(Locale.ENGLISH);
		return slr;
	}

	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
		LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
		lci.setParamName("lang");
		return lci;
	}

	@Bean
	public DebugInterceptor debugInterceptor() {
		DebugInterceptor di = new DebugInterceptor();
		return di;
	}

	public void addInterceptors(InterceptorRegistry registry) {
		if(Boolean.parseBoolean(env.getProperty("omilab.debug.performance")))
			registry.addInterceptor(debugInterceptor());
		registry.addInterceptor(localeChangeInterceptor());
	}

	@Bean
	public ServletRegistrationBean jerseyServlet() {
		ServletRegistrationBean registration = new ServletRegistrationBean(new ServletContainer(), "/rest/*");
		registration.addInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS, JerseyInitialization.class.getName());
		return registration;
	}

	@Bean
	public EmbeddedServletContainerCustomizer containerCustomizer() {

		return new EmbeddedServletContainerCustomizer() {
			@Override
			public void customize(ConfigurableEmbeddedServletContainer container) {

				ErrorPage error403Page = new ErrorPage(HttpStatus.FORBIDDEN, "/403.html");
				ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/404.html");

				container.addErrorPages(error404Page, error403Page);
			}
		};
	}

}
