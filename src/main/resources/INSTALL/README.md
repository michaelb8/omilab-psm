# Installation Guide

## Database setup

The OMiLAB Project Structure Manager requires MySQL database to work. It has been tested v5.5, but others should be compatible as well.
In order to create a database issue the following SQL command. ```CREATE DATABASE psm; ```
Furthermore a user is required to access the database. You may create a new one with the following command: ```CREATE USER 'newuser'@'localhost' IDENTIFIED BY 'password'; ```
Finally the user needs to be granted privileges on the newly created database. This may be achieved by the following SQL statement: ```GRANT ALL PRIVILEGES ON psm. * TO 'newuser'@'localhost'; ```
For the following configuration of the database you need the database name, user and password.


## Configuration of the application

A exemplary and well-commented application.properties should be present in /WEB-INF/classes/. Strong attention should be payed to filling in the database entries correctly, as well as the app.url (URL where the PSM will be reachable externally) and the URL of the CAS server.

Please do not modify the section which says "internal use only", or you will likely break something.

## Deployment on a servlet container

The application has been tested with Tomcat 7 and recent java versions (JRE7 and JRE8). For further information on how to install and configure Tomcat please visit: http://tomcat.apache.org/whichversion.html . A WAR can be obtained from http://jenkins.dke.univie.ac.at . The downloaded WAR still has to be configured.


# Developer Guide

## Setup of developer environment

A pom.xml should be present in root of the source directory. Different profiles are present in order to automate the deployment and creation of artifacts for different environments. The configuration of the developer environment is done automatically by importing the maven project. On how this import process works exacly, please refer to the documentation of your developer environment.

The code may be obtained from git@gitlab.dke.univie.ac.at:OMiLAB_Development/PSM.git (with a valid gitlab account, sufficient rights inside the internal network)

## Class responsibilities

The application is divided into several parts according to the MVC pattern. The controller can be found in the web-package and is responsible for preparing the view. The service layer can be found in the service-package. Besides of the business logic it contains the packages logging role, which enable communication with the ActivityLogging and Role-Service. The repo-package provides the JPA-Repository interfaces, which are used for the database access. The model-package is diveded into a db and wrapper package. Classes in the db package are persisted in the database. Classes in the wrapper package are used for wrapping db classes, when necessary, for modelling JSON messages or dynamic content. The conf-package includes classes which configure certain Spring components or serve as filter or interceptor.


### Controller

* **Franchise Settings Controller** provides the /settings endpoint. It is used to configure all settings regarding the franchise environment and cannot be accessed by ordinary project admins.

* **GlobalControllerExceptionHandler** is used catch and log all exceptions that are not handled anywhere else.

* **MainController** provides certain important portal functionality, that do not apply only in the project context.

* **ProjectController** is resonsible for providing all project-specific settings and display of project-specific content-


### Service Layer

* **UserService** provides an abstraction layer to acceess user data.
* **ServiceManagementService** is responsible for all actions regarding services. This especially includes instantiation and removal of services, as well as the generation of the navigation within a project.
* **ServiceDefinitionService** provides an abstraction layer to access and modify data regarding the service definitions.
* **ProjectTypeService** provides an abstraction layer to access and modify data regarding the project types.
* **ProjectService** provides an abstraction layer to access data and modify regarding the project details.  
* **HeaderManagementService** provides an abstraction layer to access data and modify regarding the global header.
* **GenericRequestorService** is the main component which proxies the requests coming to the PSM to the underlying services and delivery the accoriding content to the ProjectController.

### Model 

The following concepts are persisted to the database:

* **Project** represents an OMiLAB project, with all attributes.
* **Keyword** represents a Keyword that is connected to a Project (ManyToMany)
* **HeaderConfiguration** represents one of the header elements of the whole portal. There is no relation to another entity.
* **ServiceDefinition** represents a service in the portal. It contains all information in order to contact a service and several other useful attributes, like responsible developer. It is added by the franchise administrator and is the main part of the service configuration file. 
* **DBNavigationItem** is the other part of the service configuration file, provides the database concept for a menu point
and holds the mapping to the endpoints of the service. 
* **ServiceInstance** represents the instance of a service on database level. They are created in the context of a project, when clicked on 'Subscribe', and can only be be accessed by the instantiating project´, but has no influence on the project endpoints.
* **ProjectType** represents a group of projects and a group of service endpoints. Assigned projects can only access the accordingly assigned service endpoints. 

The following concepts are not persisted to the database:

* **GenericRequest** represents a PSM request to the service. 
* **GenericServiceContent** represents the response of the service, which is then later on displayed as project content
* **PageWrapper** is a object that is used for the page calculations.
* **User** is a class which provides an abstraction layer to access user information stored elsewhere
* **UINavigationItem** is derived from the DBNavigationItem and is used for the representation of the menu poin in the user interface


### Java Configuration

* **SecurityConfiguration** is necessary for configuring the integration with the CAS server and to define which pages need special protection or can be accessed by everybody. 
* **ProjectFilter** filters all requests to project sites. If it encounters a request to a non-existing project, it intercepts the request and directs it to an error page.
* **OmilabPermissionEvaluator** is resonsible for deciding if a user is allowed to access a resource or not. It hands the username (verified by CAS) and the current project to the Role Service and based on the response access is granted or not. 
* **DebugInterceptor** provides the page generation time in a information box on each site and the response time of the service.
* **CASLogoutHandler** is called after the user logs out and directs him based on the site he was previously.
* **CASLoginHandler** is called after the user logs in and directs him based on the site he was previously.
* **ApplicationStartup** injects the PermissionHandler into the first generated webSecurityExpressionHandler, as Thymeleaf can only access the first one. 

### Templates

In the templates folder all prepared HTML templates can be found. Usually they are filled by the controllers. The most important is probably layout.html, as most elements (header, footer) are imported from there. The files located in the static folder can be used to modify the according css and js files. 

The static strings are automatically translated to the given locale. You may define the strings in the Message_en.properties file (en may be replaced with the according abbreviation). 

A documentation regarding the Thymeleaf syntax can be found in: http://www.thymeleaf.org/doc/tutorials/2.1/usingthymeleaf.html


# Troubleshooting

Errors should be logged in catalina.out. If the log is not verbose enough please consider increasing the log level. In order to do so, add the following line to your application.properties ```logging.level.org.omilab.psm: DEBUG``` . In order to increase the log level of standard components you may use for example:

```logging.level.org.springframework: WARN
logging.level.org.hibernate: WARN ```


# Where to get more help

If problems arise which are not solveable with application logs, please email dgoetzinger@dke.univie.ac.at