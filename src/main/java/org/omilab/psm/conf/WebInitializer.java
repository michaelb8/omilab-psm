package org.omilab.psm.conf;

import org.omilab.psm.PSMApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

// necessary for working deployment as war on tomcat:
// http://stackoverflow.com/questions/27904594/spring-boot-war-deployed-to-tomcat
public class WebInitializer extends SpringBootServletInitializer {
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(PSMApplication.class);
	}
}
