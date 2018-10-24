-- MySQL dump 10.16  Distrib 10.1.14-MariaDB, for Linux (x86_64)
--
-- Host: localhost    Database: psm
-- ------------------------------------------------------
-- Server version	10.1.14-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `abstract_project`
--

DROP TABLE IF EXISTS `abstract_project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `abstract_project` (
  `dtype` varchar(31) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `abbreviation` varchar(8) NOT NULL,
  `background_color` varchar(255) DEFAULT NULL,
  `created` datetime DEFAULT NULL,
  `in_config` bit(1) DEFAULT NULL,
  `name` varchar(140) NOT NULL,
  `uniqueid` varchar(255) DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `urlidentifier` varchar(10) NOT NULL,
  `affiliation` varchar(100) DEFAULT NULL,
  `projectlogo` varchar(255) DEFAULT NULL,
  `projectmodel` varchar(255) DEFAULT NULL,
  `shortdescription` varchar(250) DEFAULT NULL,
  `end` datetime DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `start` datetime DEFAULT NULL,
  `projecttype_id` bigint(20) DEFAULT NULL,
  `proposal_id` bigint(20) DEFAULT NULL,
  `link` longtext,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ie0b8lv5gn6r2mn1yt3f6n99g` (`abbreviation`),
  UNIQUE KEY `UK_ca4afmr1xkyqq2xb3tsl8adxc` (`name`),
  UNIQUE KEY `UK_i782e6ubwu9415xxaw17rpjst` (`urlidentifier`),
  KEY `FK_7v4jxc7oc0jlxpeidxaeqbgjt` (`projecttype_id`),
  KEY `FK_5hxskbp3juwfhtv1qfi0e0ulc` (`proposal_id`),
  CONSTRAINT `FK_5hxskbp3juwfhtv1qfi0e0ulc` FOREIGN KEY (`proposal_id`) REFERENCES `project_proposal` (`id`),
  CONSTRAINT `FK_7v4jxc7oc0jlxpeidxaeqbgjt` FOREIGN KEY (`projecttype_id`) REFERENCES `project_type` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `abstract_project`
--

LOCK TABLES `abstract_project` WRITE;
/*!40000 ALTER TABLE `abstract_project` DISABLE KEYS */;
INSERT INTO `abstract_project` VALUES ('mmproject',1,'EP','#646058',NULL,'\0','Example Project','60e6e46c-f72d-4a14-ba45-6537f5982112','2016-07-16 10:21:13','ep','Example affiliation','http://omi-gf03.dke.univie.ac.at/content/lab.jpg','http://omi-gf03.dke.univie.ac.at/content/lab.jpg','An example project to show the capabilities of the system',NULL,NULL,NULL,1,NULL,NULL);
/*!40000 ALTER TABLE `abstract_project` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dbnavigation_item`
--

DROP TABLE IF EXISTS `dbnavigation_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dbnavigation_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created` datetime DEFAULT NULL,
  `endpoint` varchar(255) NOT NULL,
  `mandatory` bit(1) NOT NULL,
  `name` varchar(255) NOT NULL,
  `seq` int(11) NOT NULL,
  `updated` datetime DEFAULT NULL,
  `servicedefinition_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_m5q9oyj72qkkipyibhevlc8lm` (`endpoint`),
  UNIQUE KEY `UK_regmwr5gcp3q7gh37whs1q1t6` (`name`),
  KEY `FK_qj5scun1jy2sikf2ptytlnc9b` (`servicedefinition_id`),
  CONSTRAINT `FK_qj5scun1jy2sikf2ptytlnc9b` FOREIGN KEY (`servicedefinition_id`) REFERENCES `service_definition` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dbnavigation_item`
--

LOCK TABLES `dbnavigation_item` WRITE;
/*!40000 ALTER TABLE `dbnavigation_item` DISABLE KEYS */;
INSERT INTO `dbnavigation_item` VALUES (1,'2016-02-27 17:12:03','member','\0','Members',4,NULL,1),(2,'2016-02-27 17:12:21','contact','\0','Contact',5,NULL,1),(3,'2016-02-27 17:12:39','home','','Home',0,NULL,3),(4,'2016-02-27 17:12:57','details','\0','Details',1,NULL,3),(5,'2016-02-27 17:13:10','dl','\0','Tool Download',2,NULL,4),(6,'2016-02-27 17:13:20','downloads','\0','Downloads',3,NULL,5);
/*!40000 ALTER TABLE `dbnavigation_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `global_configuration`
--

DROP TABLE IF EXISTS `global_configuration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `global_configuration` (
  `id` varchar(255) NOT NULL,
  `value` mediumtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `global_configuration`
--

LOCK TABLES `global_configuration` WRITE;
/*!40000 ALTER TABLE `global_configuration` DISABLE KEYS */;
INSERT INTO `global_configuration` VALUES ('footer','<div id=\"footercontent\" style=\"margin-bottom:0%;margin-left:8%;\">\r\n    <div class=\"panel-primary\" style=\"margin-top:0%;\">\r\n        <div class=\"panel-body container\" style=\"padding-bottom: 0;\"><img src=\"http://omi-gf03.dke.univie.ac.at/content/lab.jpg\" style=\"float:left;\">\r\n            <div style=\"float:left;margin-left:10%;\"><b>Open Models Laboratory</b>\r\n\r\n                <p>Währinger Straße 29<br>\r\n                    1090 Vienna</p>\r\n            </div>\r\n\r\n            <div style=\"float:left;margin-left:10%;\"><b>Coordinator</b>\r\n\r\n                <p>Prof. Dr. Dimitris Karagiannis</p>\r\n            </div>\r\n\r\n            <div style=\"float:left;margin-left:10%;\"><b style=\"cursor: pointer;\"> <a style=\"color:black;\" href=\"/psm/legal\">Legal information</a></b>\r\n\r\n            </div>\r\n        </div>\r\n    </div>\r\n</div>\r\n\r\n<div id=\"footercontentmobile\">\r\n    <p style=\"text-align:center;margin-top:1%;\"><a href=\"/psm/legal\">Legal Information</a></p>\r\n    <div class=\"social-container\">\r\n        <div class=\"links\">\r\n            <a href=\"#\" data-type=\"twitter\" th:attr=\"data-url=${#httpServletRequest.getRequestURL()}\" data-description=\"Visit OMiLAB!\" data-via=\"omilab\" class=\"prettySocial fa fa-twitter\" data-media=\"http://omi-gf03.dke.univie.ac.at/content/lab.png\"></a>\r\n\r\n            <a href=\"#\" data-type=\"facebook\" th:attr=\"data-url=${#httpServletRequest.getRequestURL()}\" data-title=\"OMiLAB\" data-description=\"Visit OMiLAB!\" class=\"prettySocial fa fa-facebook\" data-media=\"http://omi-gf03.dke.univie.ac.at/content/lab.png\"></a>\r\n            <!--  <a href=\"#\" data-type=\"facebook\" data-url=\"http://www.omilab.org\" data-title=\"OMiLAB\" data-description=\"Visit OMiLAB!\" data-media=\"http://sonnyt.com/assets/imgs/prettySocial.png\" class=\"prettySocial fa fa-facebook\"></a> -->\r\n\r\n            <a href=\"#\" data-type=\"googleplus\" th:attr=\"data-url=${#httpServletRequest.getRequestURL()}\" data-description=\"Visit OMiLAB!\" class=\"prettySocial fa fa-google-plus\" data-media=\"http://omi-gf03.dke.univie.ac.at/content/lab.png\"></a>\r\n\r\n            <a href=\"#\" data-type=\"linkedin\" th:attr=\"data-url=${#httpServletRequest.getRequestURL()}\" data-title=\"OMiLAB\" data-description=\"Visit OMiLAB!\" class=\"prettySocial fa fa-linkedin\" data-media=\"http://omi-gf03.dke.univie.ac.at/content/lab.png\"></a>\r\n        </div>\r\n    </div>\r\n</div>');
/*!40000 ALTER TABLE `global_configuration` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `header_configuration`
--

DROP TABLE IF EXISTS `header_configuration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `header_configuration` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content` text,
  `created` datetime DEFAULT NULL,
  `seq` int(11) NOT NULL,
  `starter` bit(1) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `visible` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `header_configuration`
--

LOCK TABLES `header_configuration` WRITE;
/*!40000 ALTER TABLE `header_configuration` DISABLE KEYS */;
/*!40000 ALTER TABLE `header_configuration` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `keyword`
--

DROP TABLE IF EXISTS `keyword`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `keyword` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content` varchar(255) DEFAULT NULL,
  `created` datetime DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_c1max2wochg8i03o26ntd1irg` (`content`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `keyword`
--

LOCK TABLES `keyword` WRITE;
/*!40000 ALTER TABLE `keyword` DISABLE KEYS */;
INSERT INTO `keyword` VALUES (1,'','2016-02-27 16:48:55',NULL),(2,'Testproject','2016-07-15 13:56:04',NULL);
/*!40000 ALTER TABLE `keyword` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `main_navigation_item`
--

DROP TABLE IF EXISTS `main_navigation_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `main_navigation_item` (
  `dtype` varchar(31) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `authentication` bit(1) NOT NULL,
  `created` datetime DEFAULT NULL,
  `displayname` varchar(255) NOT NULL,
  `link` varchar(255) DEFAULT NULL,
  `seq` int(11) NOT NULL,
  `updated` datetime DEFAULT NULL,
  `visible` bit(1) DEFAULT NULL,
  `caption` varchar(255) DEFAULT NULL,
  `newproject` bit(1) DEFAULT NULL,
  `newprojectlabel` varchar(255) DEFAULT NULL,
  `carousel` bit(1) DEFAULT NULL,
  `html` longtext,
  `project_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_s3jec9u0wslh2kvq254p3kmc4` (`link`),
  KEY `FK_lff43e65392xt736y8ddqkkql` (`project_id`),
  CONSTRAINT `FK_lff43e65392xt736y8ddqkkql` FOREIGN KEY (`project_id`) REFERENCES `abstract_project` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `main_navigation_item`
--

LOCK TABLES `main_navigation_item` WRITE;
/*!40000 ALTER TABLE `main_navigation_item` DISABLE KEYS */;
INSERT INTO `main_navigation_item` VALUES ('HTML',1,'\0',NULL,'Home','home',0,'2016-07-15 13:59:28','',NULL,NULL,NULL,NULL,'<div class=\"panel panel-default\">\r\n  <div class=\"panel-body\">\r\n   <h1>Welcome to your new OMiLAB PSM installation!</h1>\r\n  </div>\r\n</div>\r\n',NULL),('TYPES',2,'\0','2016-02-27 16:44:56','Projects','projects',0,NULL,'','Projects','\0','New project!',NULL,NULL,NULL);
/*!40000 ALTER TABLE `main_navigation_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `main_navigation_item_types`
--

DROP TABLE IF EXISTS `main_navigation_item_types`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `main_navigation_item_types` (
  `page_id` bigint(20) NOT NULL,
  `types_id` bigint(20) NOT NULL,
  KEY `FK_38mcdh1ni9f94c5qege1djfdl` (`types_id`),
  KEY `FK_talsy8410ew4c1d35hiex7gks` (`page_id`),
  CONSTRAINT `FK_38mcdh1ni9f94c5qege1djfdl` FOREIGN KEY (`types_id`) REFERENCES `project_type` (`id`),
  CONSTRAINT `FK_talsy8410ew4c1d35hiex7gks` FOREIGN KEY (`page_id`) REFERENCES `main_navigation_item` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `main_navigation_item_types`
--

LOCK TABLES `main_navigation_item_types` WRITE;
/*!40000 ALTER TABLE `main_navigation_item_types` DISABLE KEYS */;
INSERT INTO `main_navigation_item_types` VALUES (2,1);
/*!40000 ALTER TABLE `main_navigation_item_types` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_keyword`
--

DROP TABLE IF EXISTS `project_keyword`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `project_keyword` (
  `projects_id` bigint(20) NOT NULL,
  `keywords_id` bigint(20) NOT NULL,
  PRIMARY KEY (`projects_id`,`keywords_id`),
  KEY `FK_25hqr0sgm9d1pwptdgxc2dw8` (`keywords_id`),
  CONSTRAINT `FK_25hqr0sgm9d1pwptdgxc2dw8` FOREIGN KEY (`keywords_id`) REFERENCES `keyword` (`id`),
  CONSTRAINT `FK_3jpd6ghq5jfb4r5tw0aefav6j` FOREIGN KEY (`projects_id`) REFERENCES `abstract_project` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_keyword`
--

LOCK TABLES `project_keyword` WRITE;
/*!40000 ALTER TABLE `project_keyword` DISABLE KEYS */;
INSERT INTO `project_keyword` VALUES (1,2);
/*!40000 ALTER TABLE `project_keyword` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_navigation`
--

DROP TABLE IF EXISTS `project_navigation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `project_navigation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created` datetime DEFAULT NULL,
  `dbnavigationitem_id` bigint(20) DEFAULT NULL,
  `project_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_ht8ncr2tdm5arnga4f4adjvbl` (`dbnavigationitem_id`),
  KEY `FK_9od0rvc4uqqhmqajkwnukphgr` (`project_id`),
  CONSTRAINT `FK_9od0rvc4uqqhmqajkwnukphgr` FOREIGN KEY (`project_id`) REFERENCES `abstract_project` (`id`),
  CONSTRAINT `FK_ht8ncr2tdm5arnga4f4adjvbl` FOREIGN KEY (`dbnavigationitem_id`) REFERENCES `dbnavigation_item` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_navigation`
--

LOCK TABLES `project_navigation` WRITE;
/*!40000 ALTER TABLE `project_navigation` DISABLE KEYS */;
/*!40000 ALTER TABLE `project_navigation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_proposal`
--

DROP TABLE IF EXISTS `project_proposal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `project_proposal` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `accepted_status` bit(1) DEFAULT NULL,
  `date_accepted` datetime DEFAULT NULL,
  `date_finished` datetime DEFAULT NULL,
  `date_started` datetime DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `finished` bit(1) DEFAULT NULL,
  `in_creation` bit(1) DEFAULT NULL,
  `name` varchar(140) NOT NULL,
  `pos` int(11) DEFAULT NULL,
  `projectabstract` longtext NOT NULL,
  `proposalid` varchar(255) DEFAULT NULL,
  `user_accepted` varchar(255) DEFAULT NULL,
  `user_started` varchar(255) DEFAULT NULL,
  `user_stopped` varchar(255) DEFAULT NULL,
  `userid` varchar(255) DEFAULT NULL,
  `project_id` bigint(20) DEFAULT NULL,
  `type_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_abnnmek4o8qccm2lh3f1g7qxp` (`project_id`),
  KEY `FK_91tl09021wkn89ka5bd6mc5it` (`type_id`),
  CONSTRAINT `FK_91tl09021wkn89ka5bd6mc5it` FOREIGN KEY (`type_id`) REFERENCES `project_type` (`id`),
  CONSTRAINT `FK_abnnmek4o8qccm2lh3f1g7qxp` FOREIGN KEY (`project_id`) REFERENCES `abstract_project` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_proposal`
--

LOCK TABLES `project_proposal` WRITE;
/*!40000 ALTER TABLE `project_proposal` DISABLE KEYS */;
/*!40000 ALTER TABLE `project_proposal` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_type`
--

DROP TABLE IF EXISTS `project_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `project_type` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `background_color` varchar(255) DEFAULT NULL,
  `background_tile` longtext,
  `created` datetime DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `foreground_tile` longtext,
  `fullpheader` bit(1) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `navigation_bar` bit(1) DEFAULT NULL,
  `overlay` varchar(255) DEFAULT NULL,
  `reducedpheader` bit(1) DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `w_repo_status` bit(1) DEFAULT NULL,
  `w_repo_string` text,
  `w_role_status` bit(1) DEFAULT NULL,
  `w_role_string` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_type`
--

LOCK TABLES `project_type` WRITE;
/*!40000 ALTER TABLE `project_type` DISABLE KEYS */;
INSERT INTO `project_type` VALUES (1,NULL,'<div onclick=\"location.href=\'content/$PROJECTURLIDENTIFIER\';\" class=\"tilebackelementup\">\r\n    <img src=\"images/book.png\" title=\"Book\" class=\"valign tilebacklogo\" alt=\"Method\" />\r\n        <div class=\"valign tilebacktext\" > <b>Method</b> </div>\r\n</div>\r\n<div style=\"clear: both;\"></div>\r\n<div onclick=\"location.href=\'content/$PROJECTURLIDENTIFIER/download?view=download\';\" class=\"tilebackelementdown\">\r\n    <img src=\"images/tool.png\" title=\"Tool\" class=\"valign tilebacklogo\" alt=\"Tool\" />\r\n    <div class=\"valign tilebacktext\"> <b>Tool</b> </div>\r\n</div>','2016-02-27 16:21:06','This is a default project type. ','<div class=\"tilefrontelement\">\r\n     <img style=\"width:35% !important;\" src=\" $projectlogo \" class=\"full tilefrontlogo\" />\r\n     <div class=\"tilefronttext\">\r\n        <b style=\"color : $TEXTCOLOR ;\"> $name </b>\r\n        <br />\r\n        <span style=\"color : $TEXTCOLOR ;\"> $shortdescription </span>\r\n     </div>\r\n</div> ','','Default','','','\0','2016-02-27 16:47:52','\0',NULL,'',NULL);
/*!40000 ALTER TABLE `project_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `projecttype_navigation`
--

DROP TABLE IF EXISTS `projecttype_navigation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `projecttype_navigation` (
  `types_id` bigint(20) NOT NULL,
  `items_id` bigint(20) NOT NULL,
  KEY `FK_2j7pvhe8jr4wpuoxju6b5fojy` (`items_id`),
  KEY `FK_gmi0pma3w71whsuyeamadmopp` (`types_id`),
  CONSTRAINT `FK_2j7pvhe8jr4wpuoxju6b5fojy` FOREIGN KEY (`items_id`) REFERENCES `dbnavigation_item` (`id`),
  CONSTRAINT `FK_gmi0pma3w71whsuyeamadmopp` FOREIGN KEY (`types_id`) REFERENCES `project_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `projecttype_navigation`
--

LOCK TABLES `projecttype_navigation` WRITE;
/*!40000 ALTER TABLE `projecttype_navigation` DISABLE KEYS */;
INSERT INTO `projecttype_navigation` VALUES (1,1),(1,2),(1,3),(1,4),(1,5),(1,6);
/*!40000 ALTER TABLE `projecttype_navigation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_definition`
--

DROP TABLE IF EXISTS `service_definition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `service_definition` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created` datetime DEFAULT NULL,
  `description` longtext,
  `developer` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `special` varchar(255) DEFAULT NULL,
  `url` varchar(255) NOT NULL,
  `urlidentifier` varchar(255) NOT NULL,
  `visible` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ae6f64heyg7wxhbw0hqeuridm` (`name`),
  UNIQUE KEY `UK_o7agseel4usd9u73y107m9ahi` (`special`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_definition`
--

LOCK TABLES `service_definition` WRITE;
/*!40000 ALTER TABLE `service_definition` DISABLE KEYS */;
INSERT INTO `service_definition` VALUES (1,'2015-10-27 14:27:35','The RoleService manages the roles in the context of your project. If you unsubscribe it, you might loose access to your project!','OMiLAB Austria (support@omilab.org)','RoleService','role','http://localhost:8080/role/','role',''),(2,'2015-10-27 14:27:35','The FileManager is used to store static information like images, modelling toolkits or pdf documents and is used by a couple of service to delegate data storage. If you unsubscribe it, all your data is lost.','OMiLAB Austria (support@omilab.org)','FileManager','repo','http://localhost:8080/repo/ipsm/','',''),(3,'2015-10-27 14:27:35','The TextService manages static text based on a generic scheme and offers you the possibility to add formated text to your project.','OMiLAB Austria (support@omilab.org)','TextService',NULL,'http://localhost:8080/textservice/','info',''),(4,'2015-10-27 14:27:35','The ordinary Downloadservice offers the functionality to publish modelling toolkits and ADOxx libraries, along with instructions on how to use them.','OMiLAB Austria (support@omilab.org)','ToolDownloadService',NULL,'http://localhost:8080/downloadservice/','download',''),(5,'2015-10-27 14:27:35','This special DownloadService allows it to make list of diverse downloadable content, also specifying the permissions for each entry.','OMiLAB Austria (support@omilab.org)','SpecialDownloadService',NULL,'http://localhost:8080/downloadlist/','downloadlist','');
/*!40000 ALTER TABLE `service_definition` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_instance`
--

DROP TABLE IF EXISTS `service_instance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `service_instance` (
  `instanceidlocal` bigint(20) NOT NULL AUTO_INCREMENT,
  `created` datetime DEFAULT NULL,
  `instanceidremote` bigint(20) DEFAULT NULL,
  `project_id` bigint(20) DEFAULT NULL,
  `servicedefinition_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`instanceidlocal`),
  KEY `FK_hq0056mtgv4wy6mbtmmw6mgox` (`project_id`),
  KEY `FK_miojq50ycr6ytbfgmihdhhxgs` (`servicedefinition_id`),
  CONSTRAINT `FK_hq0056mtgv4wy6mbtmmw6mgox` FOREIGN KEY (`project_id`) REFERENCES `abstract_project` (`id`),
  CONSTRAINT `FK_miojq50ycr6ytbfgmihdhhxgs` FOREIGN KEY (`servicedefinition_id`) REFERENCES `service_definition` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_instance`
--

LOCK TABLES `service_instance` WRITE;
/*!40000 ALTER TABLE `service_instance` DISABLE KEYS */;
/*!40000 ALTER TABLE `service_instance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wizard_configuration_entry`
--

DROP TABLE IF EXISTS `wizard_configuration_entry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `wizard_configuration_entry` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `instantiation` text,
  `seq` int(11) DEFAULT NULL,
  `dbnavigationitem_id` bigint(20) DEFAULT NULL,
  `type_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_r3iatg8hrfqhv0t2is4qcqf6v` (`dbnavigationitem_id`),
  KEY `FK_oim69iegics2hi6clrvbh2dnd` (`type_id`),
  CONSTRAINT `FK_oim69iegics2hi6clrvbh2dnd` FOREIGN KEY (`type_id`) REFERENCES `project_type` (`id`),
  CONSTRAINT `FK_r3iatg8hrfqhv0t2is4qcqf6v` FOREIGN KEY (`dbnavigationitem_id`) REFERENCES `dbnavigation_item` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wizard_configuration_entry`
--

LOCK TABLES `wizard_configuration_entry` WRITE;
/*!40000 ALTER TABLE `wizard_configuration_entry` DISABLE KEYS */;
/*!40000 ALTER TABLE `wizard_configuration_entry` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-07-16 12:20:01
