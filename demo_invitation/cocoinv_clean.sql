-- MySQL dump 10.13  Distrib 5.5.52, for debian-linux-gnu (x86_64)
--
-- Host: 127.0.0.1    Database: CoCoINV
-- ------------------------------------------------------
-- Server version	5.5.52-0ubuntu0.14.04.1

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
-- Table structure for table `domainSite`
--

DROP TABLE IF EXISTS `domainSite`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `domainSite` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `domain` int(10) unsigned NOT NULL,
  `site` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `domainID_idx` (`domain`),
  KEY `siteId_idx` (`site`),
  CONSTRAINT `domainId_sites` FOREIGN KEY (`domain`) REFERENCES `domains` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `siteId` FOREIGN KEY (`site`) REFERENCES `sites` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `domainSite`
--

LOCK TABLES `domainSite` WRITE;
/*!40000 ALTER TABLE `domainSite` DISABLE KEYS */;
INSERT INTO `domainSite` VALUES (1,1,1),(2,1,2);
/*!40000 ALTER TABLE `domainSite` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `domains`
--

DROP TABLE IF EXISTS `domains`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `domains` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `portal_address` varchar(45) NOT NULL,
  `email_domain` varchar(45) NOT NULL,
  `bgp_ip` varchar(45) DEFAULT NULL,
  `as_num` int(11) DEFAULT NULL,
  `as_name` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `domains`
--

LOCK TABLES `domains` WRITE;
/*!40000 ALTER TABLE `domains` DISABLE KEYS */;
INSERT INTO `domains` VALUES (1,'www1','email1','10.2.0.254',65020,'tno-north'),(2,'www2','email2','10.3.0.254',65030,'tno-south');
/*!40000 ALTER TABLE `domains` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `extLinks`
--

DROP TABLE IF EXISTS `extLinks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `extLinks` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `switch` int(11) DEFAULT NULL,
  `domain` int(10) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `switch_idx` (`switch`),
  KEY `domain_idx` (`domain`),
  CONSTRAINT `domain_fk_ext` FOREIGN KEY (`domain`) REFERENCES `domains` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `switch_fk_ext` FOREIGN KEY (`switch`) REFERENCES `switches` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `extLinks`
--

LOCK TABLES `extLinks` WRITE;
/*!40000 ALTER TABLE `extLinks` DISABLE KEYS */;
INSERT INTO `extLinks` VALUES (1,3,2);
/*!40000 ALTER TABLE `extLinks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `links`
--

DROP TABLE IF EXISTS `links`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `links` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `from` int(11) NOT NULL,
  `to` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `from_idx` (`from`),
  KEY `to_idx` (`to`),
  CONSTRAINT `from` FOREIGN KEY (`from`) REFERENCES `switches` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `to` FOREIGN KEY (`to`) REFERENCES `switches` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `links`
--

LOCK TABLES `links` WRITE;
/*!40000 ALTER TABLE `links` DISABLE KEYS */;
INSERT INTO `links` VALUES (1,1,3),(2,2,1);
/*!40000 ALTER TABLE `links` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sites`
--

DROP TABLE IF EXISTS `sites`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sites` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `x` int(11) NOT NULL,
  `y` int(11) NOT NULL,
  `switch` int(11) NOT NULL,
  `remote_port` int(10) unsigned NOT NULL,
  `local_port` int(10) unsigned NOT NULL,
  `vlanid` int(10) unsigned NOT NULL,
  `ipv4prefix` varchar(45) NOT NULL,
  `mac_address` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`),
  KEY `switch_idx` (`switch`),
  CONSTRAINT `switch_id` FOREIGN KEY (`switch`) REFERENCES `switches` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sites`
--

LOCK TABLES `sites` WRITE;
/*!40000 ALTER TABLE `sites` DISABLE KEYS */;
INSERT INTO `sites` VALUES (1,'tn_ce1',0,0,2,2,1,0,'10.2.1.0/24','00:10:02:00:00:01'),(2,'tn_ce2',0,0,3,2,1,0,'10.2.2.0/24','00:10:02:00:00:02');
/*!40000 ALTER TABLE `sites` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subnetUsers`
--

DROP TABLE IF EXISTS `subnetUsers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `subnetUsers` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `user` int(10) unsigned NOT NULL,
  `subnet` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `adminId_idx` (`user`),
  KEY `fk_userSubnet_subnets1_idx` (`subnet`),
  CONSTRAINT `userId1` FOREIGN KEY (`user`) REFERENCES `users` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_userSubnet_subnets1` FOREIGN KEY (`subnet`) REFERENCES `subnets` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subnetUsers`
--

LOCK TABLES `subnetUsers` WRITE;
/*!40000 ALTER TABLE `subnetUsers` DISABLE KEYS */;
INSERT INTO `subnetUsers` VALUES (1,2,1),(2,4,2);
/*!40000 ALTER TABLE `subnetUsers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subnets`
--

DROP TABLE IF EXISTS `subnets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `subnets` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `subnet` varchar(45) NOT NULL,
  `site` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_subnets_site1_idx` (`site`),
  CONSTRAINT `fk_subnets_site1` FOREIGN KEY (`site`) REFERENCES `sites` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subnets`
--

LOCK TABLES `subnets` WRITE;
/*!40000 ALTER TABLE `subnets` DISABLE KEYS */;
INSERT INTO `subnets` VALUES (1,'10.2.1.0/24',1),(2,'10.2.2.0/24',2);
/*!40000 ALTER TABLE `subnets` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `switches`
--

DROP TABLE IF EXISTS `switches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `switches` (
  `id` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `mininetname` varchar(45) NOT NULL,
  `x` int(10) unsigned NOT NULL,
  `y` int(10) unsigned NOT NULL,
  `mpls_label` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`,`name`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `switches`
--

LOCK TABLES `switches` WRITE;
/*!40000 ALTER TABLE `switches` DISABLE KEYS */;
INSERT INTO `switches` VALUES (1,'openflow:34','tn_pc1',0,0,0),(2,'openflow:33','tn_pe1',0,0,0),(3,'openflow:35','tn_pe2',0,0,0);
/*!40000 ALTER TABLE `switches` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(45) DEFAULT NULL,
  `email` varchar(45) NOT NULL,
  `domain` int(10) unsigned NOT NULL,
  `site` int(10) unsigned NOT NULL,
  `admin` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `pk_user_idx` (`id`),
  KEY `domainId_idx` (`domain`),
  KEY `fk_user_site1_idx` (`site`),
  CONSTRAINT `domainId_users` FOREIGN KEY (`domain`) REFERENCES `domains` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_user_site1` FOREIGN KEY (`site`) REFERENCES `sites` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'tn_ce1_admin','tn_ce1_admin@mail.com',1,1,1),(2,'tn_ce1_user','tn_ce1_user@mail.com',1,1,0),(3,'tn_ce2_admin','tn_ce2_admin@mail.com',1,2,1),(4,'tn_ce2_user','tn_ce2_user@mail.com',1,2,0);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vpnSubnet`
--

DROP TABLE IF EXISTS `vpnSubnet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vpnSubnet` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `vpn` int(10) unsigned NOT NULL,
  `subnet` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `vpnId_idx` (`vpn`),
  KEY `fk_vpnToSite_subnets1_idx` (`subnet`),
  CONSTRAINT `vpnId_subnet` FOREIGN KEY (`vpn`) REFERENCES `vpns` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_vpnToSite_subnets1` FOREIGN KEY (`subnet`) REFERENCES `subnets` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vpnSubnet`
--

LOCK TABLES `vpnSubnet` WRITE;
/*!40000 ALTER TABLE `vpnSubnet` DISABLE KEYS */;
/*!40000 ALTER TABLE `vpnSubnet` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vpnUsers`
--

DROP TABLE IF EXISTS `vpnUsers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vpnUsers` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `vpn` int(10) unsigned NOT NULL,
  `user` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `vpnId_idx` (`vpn`),
  KEY `userId_idx` (`user`),
  CONSTRAINT `vpnId_users` FOREIGN KEY (`vpn`) REFERENCES `vpns` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `userId` FOREIGN KEY (`user`) REFERENCES `users` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vpnUsers`
--

LOCK TABLES `vpnUsers` WRITE;
/*!40000 ALTER TABLE `vpnUsers` DISABLE KEYS */;
/*!40000 ALTER TABLE `vpnUsers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vpns`
--

DROP TABLE IF EXISTS `vpns`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vpns` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(45) DEFAULT NULL,
  `route_target` varchar(45) NOT NULL,
  `domain` int(10) unsigned NOT NULL,
  `owner` int(10) unsigned NOT NULL,
  `pathProtection` varchar(45) DEFAULT NULL,
  `failoverType` varchar(45) DEFAULT NULL,
  `isPublic` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `domainId_idx1` (`domain`),
  KEY `fk_vpn_user1_idx1` (`owner`),
  CONSTRAINT `domainId_vpns` FOREIGN KEY (`domain`) REFERENCES `domains` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_vpn_user1` FOREIGN KEY (`owner`) REFERENCES `users` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vpns`
--

LOCK TABLES `vpns` WRITE;
/*!40000 ALTER TABLE `vpns` DISABLE KEYS */;
/*!40000 ALTER TABLE `vpns` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-09-22 14:25:40
