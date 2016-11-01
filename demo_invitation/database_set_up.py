#!/usr/bin/python

import MySQLdb as mdb

#mysql parameters
DB_HOST="134.221.121.203"
DB_USER="coco"
DB_PWD="cocorules!"
DB_NAME="CoCoINV"

db = mdb.connect(DB_HOST, DB_USER, DB_PWD, DB_NAME)
cursor = db.cursor()
cursor.execute('SET FOREIGN_KEY_CHECKS=0;')

#### Drop existing tables
cursor.execute('show tables;')
tables = cursor.fetchall();
for table in tables:
	sql=""" DROP TABLE IF EXISTS %s; """ % (table[0],)
        cursor.execute(sql);

################ domainns - create very first - as it has no dependencies on other tables
sql = """ CREATE TABLE `domains` (
        `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
        `portal_address` VARCHAR(45) NOT NULL,
        `email_domain` VARCHAR(45) NOT NULL,
        `bgp_ip` varchar(45) DEFAULT NULL,
        `as_num` int(11) DEFAULT NULL,
        `as_name` varchar(45) DEFAULT NULL,
        PRIMARY KEY (`id`))
        ENGINE = InnoDB
        DEFAULT CHARSET=latin1; """
cursor.execute(sql)
# `bgp_peer` VARCHAR(45) NOT NULL,

sql = """INSERT  INTO `domains` (portal_address, email_domain, bgp_ip, as_num, as_name)
    VALUES ('http://134.221.121.202:9090/CoCo-agent','email111','10.2.0.254',65020,'tno-north'),('http://134.221.121.201:9090/CoCo-agent','email222','10.3.0.254',65030,'tno-south');"""
try:
	# Execute the SQL command
        cursor.execute(sql)
        # Commit your changes in the database
        db.commit()
except:
        # Rollback in case there is any error
        db.rollback()

############### get Ids for domains
sql = """SELECT `id` FROM %s.domains WHERE  `as_name` LIKE 'tno-north';""" % DB_NAME
cursor.execute(sql)
id_north = cursor.fetchone()[0]

sql = """SELECT `id` FROM %s.domains WHERE  `as_name` LIKE 'tno-south';""" % DB_NAME
cursor.execute(sql)
id_south = cursor.fetchone()[0]

###############   switches - create first - otherwise sites cannot be created as they reference to
# the key present here (errno 150)

# Drop table if it already exist using execute() method.
sql = """CREATE TABLE `switches` (
             `id` int(11) NOT NULL,
             `name` varchar(45) NOT NULL,
             `mininetname` varchar(45) NOT NULL,
             `x` int(10) unsigned NOT NULL,
             `y` int(10) unsigned NOT NULL,
             `mpls_label` int(10) unsigned NOT NULL ,
             PRIMARY KEY (`id`,`name`),
             UNIQUE KEY `id_UNIQUE` (`id`),
             UNIQUE KEY `name_UNIQUE` (`name`)  )
             ENGINE=InnoDB
             DEFAULT CHARSET=latin1;"""
cursor.execute(sql)

###############   sites
sql ="""CREATE TABLE `sites` (  \
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
        `domain` int(10) unsigned NOT NULL,
            PRIMARY KEY (`id`),
            UNIQUE KEY `id_UNIQUE` (`id`),
            UNIQUE KEY `name_UNIQUE` (`name`),
            KEY `switch_idx` (`switch`),
        KEY `domain_fk_idx` (`domain`),
        CONSTRAINT `domain_fk` FOREIGN KEY (`domain`) REFERENCES `domains` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
            CONSTRAINT `switch_id` FOREIGN KEY (`switch`) REFERENCES `switches` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION)
            ENGINE=InnoDB
            DEFAULT CHARSET=latin1;"""
cursor.execute(sql)


############ links
sql = """CREATE TABLE `links` (
        `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
        `from` int(11) NOT NULL,
        `to` int(11) NOT NULL,
        PRIMARY KEY (`id`),
        UNIQUE KEY `id_UNIQUE` (`id`),
        KEY `from_idx` (`from`),
        KEY `to_idx` (`to`),
        CONSTRAINT `from` FOREIGN KEY (`from`) REFERENCES `switches` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
        CONSTRAINT `to` FOREIGN KEY (`to`) REFERENCES `switches` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION)
        ENGINE=InnoDB
        DEFAULT CHARSET=latin1;"""
cursor.execute(sql)

sql = """CREATE TABLE `extLinks` (
        `id` int(11) NOT NULL AUTO_INCREMENT,
        `switch` int(11) DEFAULT NULL,
        `domain` int(10) unsigned DEFAULT NULL,
        PRIMARY KEY (`id`),
        KEY `switch_idx` (`switch`),
        KEY `domain_idx` (`domain`),
        CONSTRAINT `domain_fk_ext` FOREIGN KEY (`domain`) REFERENCES `domains` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
        CONSTRAINT `switch_fk_ext` FOREIGN KEY (`switch`) REFERENCES `switches` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION)
        ENGINE=InnoDB
        DEFAULT CHARSET=latin1;"""
cursor.execute(sql)


################ users
sql = """CREATE TABLE `users` (
        `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
        `name` VARCHAR(45) NULL,
        `email` VARCHAR(45) NOT NULL,
        `domain` int(10) unsigned NOT NULL,
        `site` int(10) unsigned NOT NULL,
        `admin` TINYINT(1) NOT NULL,
        PRIMARY KEY (`id`),
        INDEX `pk_user_idx` (`id` ASC) ,
        INDEX `domainId_idx` (`domain` ASC),
        INDEX `fk_user_site1_idx` (`site` ASC),
        CONSTRAINT `domainId_users`    FOREIGN KEY (`domain`)    REFERENCES `domains` (`id`)    ON DELETE NO ACTION    ON UPDATE NO ACTION,
        CONSTRAINT `fk_user_site1`    FOREIGN KEY (`site`)    REFERENCES `sites` (`id`)    ON DELETE NO ACTION    ON UPDATE NO ACTION)
        ENGINE = InnoDB
        CHARSET=latin1;"""
cursor.execute(sql)

sql = """SELECT `id`, `name` FROM %s.sites ;""" % DB_NAME
cursor.execute(sql)

for site in cursor:
        #default TN domain
        id_temp = id_north
        if "tn" in site[1]:
            id_temp = id_north
        elif "ts" in site[1]:
            id_temp = id_south
        sql = """INSERT INTO `users` (name, email, domain, site, admin)
                    VALUES ('%s_admin','%s_admin@mail.com','%d','%d',1),
                            ('%s_user','%s_user@mail.com','%d','%d',0);""" \
                        % (site[1], site[1], id_temp, site[0], site[1], site[1], id_temp, site[0])
        try:
            # Execute the SQL command
            cursor.execute(sql)
            # Commit your changes in the database
            db.commit()
        except:
            # Rollback in case there is any error
            db.rollback()

################ vpns
sql = """CREATE TABLE `vpns` (
        `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
        `name` VARCHAR(45) NULL,
        `route_target` VARCHAR(45) NULL DEFAULT NULL,
        `domain` int(10) unsigned NOT NULL,
        `owner` int(10) unsigned NOT NULL,
        `pathProtection` VARCHAR(45) NULL DEFAULT NULL,
        `failoverType` VARCHAR(45) NULL DEFAULT NULL,
        `isPublic` TINYINT(1) NOT NULL,
        PRIMARY KEY (`id`),
        INDEX `domainId_idx1` (`domain` ASC),
        INDEX `fk_vpn_user1_idx1` (`owner` ASC),
        CONSTRAINT `domainId_vpns`    FOREIGN KEY (`domain`)    REFERENCES `domains` (`id`)    ON DELETE NO ACTION    ON UPDATE NO ACTION,
        CONSTRAINT `fk_vpn_user1`    FOREIGN KEY (`owner`)    REFERENCES `users` (`id`)    ON DELETE NO ACTION    ON UPDATE NO ACTION)
        ENGINE = InnoDB
        CHARSET=latin1;"""
cursor.execute(sql)

sql = """CREATE TABLE `subnets` (
        `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
        `subnet` VARCHAR(45) NOT NULL,
        `site` int(10) unsigned NOT NULL,
        PRIMARY KEY (`id`),
        INDEX `fk_subnets_site1_idx` (`site` ASC),
        CONSTRAINT `fk_subnets_site1`
        FOREIGN KEY (`site`)    REFERENCES `sites` (`id`)    ON DELETE NO ACTION    ON UPDATE NO ACTION)
        ENGINE = InnoDB
        DEFAULT CHARSET=latin1; """
cursor.execute(sql)


################ subnetUsers
sql = """CREATE TABLE `subnetUsers` (
        `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
        `user` int(10) unsigned NOT NULL,
        `subnet` int(10) unsigned NOT NULL,
        PRIMARY KEY (`id`),
        INDEX `adminId_idx` (`user` ASC),
        INDEX `fk_userSubnet_subnets1_idx` (`subnet` ASC),
        CONSTRAINT `userId1`    FOREIGN KEY (`user`)    REFERENCES `users` (`id`)    ON DELETE NO ACTION    ON UPDATE NO ACTION,
        CONSTRAINT `fk_userSubnet_subnets1`    FOREIGN KEY (`subnet`)    REFERENCES `subnets` (`id`)    ON DELETE NO ACTION    ON UPDATE NO ACTION)
        ENGINE = InnoDB
        CHARSET=latin1;"""
cursor.execute(sql)

sql = """SELECT `id`, `site` FROM %s.subnets ;""" % DB_NAME
cursor.execute(sql)

for subnet in cursor:
        sql="""SELECT `name` FROM %s.sites WHERE id = %d ;""" % (DB_NAME, subnet[1])
        cursor.execute(sql)
        site_name = cursor.fetchone()[0]
        sql="""SELECT `id` FROM %s.users WHERE  `name` LIKE '%s_user';""" % (DB_NAME, site_name)
        cursor.execute(sql)
        user_id = cursor.fetchone()[0]
        sql = """INSERT INTO `subnetUsers` VALUES (NULL, '%d','%d');""" % (user_id, subnet[0])
        try:
            # Execute the SQL command
            cursor.execute(sql)
            # Commit your changes in the database
            db.commit()
        except:
            # Rollback in case there is any error
            db.rollback()

################ vpnUsers
sql = """CREATE TABLE `vpnUsers` (
        `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
        `vpn` int(10) unsigned NOT NULL,
        `user` int(10) unsigned NOT NULL,
        PRIMARY KEY (`id`),
        INDEX `vpnId_idx` (`vpn` ASC),
        INDEX `userId_idx` (`user` ASC),
        CONSTRAINT `vpnId_users`
        FOREIGN KEY (`vpn`)    REFERENCES `vpns` (`id`)    ON DELETE NO ACTION     ON UPDATE NO ACTION,
        CONSTRAINT `userId`    FOREIGN KEY (`user`)    REFERENCES `users` (`id`)    ON DELETE NO ACTION     ON UPDATE NO ACTION)
        ENGINE = InnoDB
        CHARSET=latin1;"""
cursor.execute(sql)


################ vpnSubnet
sql = """CREATE TABLE `vpnSubnet` (
        `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
        `vpn` int(10) unsigned NOT NULL,
        `subnet` int(10) unsigned NOT NULL,
        PRIMARY KEY (`id`),
        INDEX `vpnId_idx` (`vpn` ASC),
        INDEX `fk_vpnToSite_subnets1_idx` (`subnet` ASC),
        CONSTRAINT `vpnId_subnet`    FOREIGN KEY (`vpn`)    REFERENCES `vpns` (`id`)    ON DELETE NO ACTION    ON UPDATE NO ACTION,
        CONSTRAINT `fk_vpnToSite_subnets1`  FOREIGN KEY (`subnet`)    REFERENCES `subnets` (`id`)    ON DELETE NO ACTION    ON UPDATE NO ACTION)
        ENGINE = InnoDB
        CHARSET=latin1;"""
cursor.execute(sql)


##database processing ends
cursor.execute('SET FOREIGN_KEY_CHECKS=1;')
db.close()

