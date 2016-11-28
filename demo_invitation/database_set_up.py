#!/usr/bin/python

import MySQLdb as mdb
import sys

#mysql parameters
DB_HOST_TN="134.221.121.202"
DB_HOST_TS="134.221.121.201"

DB_USER="coco"
DB_PWD="cocorules!"
DB_NAME="CoCoINV"

def database_set_up(db_host):

    db = mdb.connect(db_host, DB_USER, DB_PWD, DB_NAME)
    cursor = db.cursor()
    cursor.execute('SET FOREIGN_KEY_CHECKS=0;')

    #### Drop existing tables
    cursor.execute('show tables;')
    tables = cursor.fetchall();
    for table in tables:
            sql=""" DROP TABLE IF EXISTS %s; """ % (table[0],)
            cursor.execute(sql);
    ##########
    #DOMAINS
    ##########
    ################ domainns - create very first - as it has no dependencies on other tables
    sql = """ CREATE TABLE `domains` (
            `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
            `portal_address` VARCHAR(100) NOT NULL,
            `email_domain` VARCHAR(45) NOT NULL,
            `bgp_ip` varchar(45) DEFAULT NULL,
            `as_num` int(11) DEFAULT NULL,
            `as_name` varchar(45) DEFAULT NULL,
            PRIMARY KEY (`id`))
            ENGINE = InnoDB
            DEFAULT CHARSET=latin1; """
    cursor.execute(sql)
    # `bgp_peer` VARCHAR(45) NOT NULL,

    ################
    #SWITCHES
    ###############
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

    ###############
    #SITES
    ###############
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

    ############
    #LINKS
    ###########
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


    ###########
    #EXT LINKS
    ##########
    sql = """CREATE TABLE `extLinks` (
            `id` int(11) NOT NULL  AUTO_INCREMENT,
            `switch` int(11) ,
            `domain` int(10) unsigned,
            PRIMARY KEY (`id`),
            KEY `switch_idx` (`switch`),
            KEY `domain_idx` (`domain`),
            CONSTRAINT `domain_fk_ext` FOREIGN KEY (`domain`) REFERENCES `domains` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
            CONSTRAINT `switch_fk_ext` FOREIGN KEY (`switch`) REFERENCES `switches` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION)
            ENGINE=InnoDB
            DEFAULT CHARSET=latin1;"""
    cursor.execute(sql)

    ############
    #users
    ############
    sql = """CREATE TABLE `users` (
            `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
            `name` VARCHAR(45) NULL,
            `email` VARCHAR(45) NOT NULL,
            `domain` int(10) unsigned NOT NULL,
            `admin` TINYINT(1) NOT NULL,
            PRIMARY KEY (`id`),
            INDEX `pk_user_idx` (`id` ASC) ,
            INDEX `domainId_idx` (`domain` ASC),
            CONSTRAINT `domainId_users`    FOREIGN KEY (`domain`)    REFERENCES `domains` (`id`)    ON DELETE NO ACTION    ON UPDATE NO ACTION)
            ENGINE = InnoDB
            CHARSET=latin1;"""
    cursor.execute(sql)

    ################
    #VPNs
    ################
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


    #############
    #SUBNETS
    ############
    sql = """CREATE TABLE `subnets` (
            `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
            `subnet` VARCHAR(45) NOT NULL,
            `site` int(10) unsigned NOT NULL,
            PRIMARY KEY (`id`),
            UNIQUE KEY `subnet_UNIQUE` (`subnet`),
            INDEX `fk_subnets_site1_idx` (`site` ASC),
            CONSTRAINT `fk_subnets_site1`
            FOREIGN KEY (`site`)    REFERENCES `sites` (`id`)    ON DELETE NO ACTION    ON UPDATE NO ACTION)
            ENGINE = InnoDB
            DEFAULT CHARSET=latin1; """
    cursor.execute(sql)
    ################
    #subnetUsers
    ################
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

    ################
    #VPN USERS
    ###############
    # for now we are not using the vpnUsers table as each subnet is allocated to a VPN and a USER
    # sql = """CREATE TABLE `vpnUsers` (
    #         `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
    #         `vpn` int(10) unsigned NOT NULL,
    #         `user` int(10) unsigned NOT NULL,
    #         PRIMARY KEY (`id`),
    #         INDEX `vpnId_idx` (`vpn` ASC),
    #         INDEX `userId_idx` (`user` ASC),
    #         CONSTRAINT `vpnId_users` FOREIGN KEY (`vpn`)    REFERENCES `vpns` (`id`)    ON DELETE NO ACTION     ON UPDATE NO ACTION,
    #         CONSTRAINT `userId`    FOREIGN KEY (`user`)    REFERENCES `users` (`id`)    ON DELETE NO ACTION     ON UPDATE NO ACTION)
    #         ENGINE = InnoDB
    #         CHARSET=latin1;"""
    # cursor.execute(sql)


    ################
    #vpnSubnet
    ###############
    sql = """CREATE TABLE `vpnSubnet` (
            `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
            `vpn` int(10) unsigned NOT NULL,
            `subnet` int(10) unsigned NOT NULL,
            `user` INT(10) UNSIGNED UNSIGNED NULL DEFAULT NULL,
            PRIMARY KEY (`id`),
            INDEX `vpnId_idx` (`vpn` ASC),
            INDEX `fk_vpnToSite_subnets1_idx` (`subnet` ASC),
            CONSTRAINT `vpnId_subnet`    FOREIGN KEY (`vpn`)    REFERENCES `vpns` (`id`)    ON DELETE NO ACTION    ON UPDATE NO ACTION,
            CONSTRAINT `fk_vpnToSite_subnets1`  FOREIGN KEY (`subnet`)    REFERENCES `subnets` (`id`)    ON DELETE NO ACTION    ON UPDATE NO ACTION,
            CONSTRAINT `fk_user_vpnsubnet`  FOREIGN KEY (`user`)    REFERENCES `users` (`id`)    ON DELETE NO ACTION    ON UPDATE NO ACTION)
            ENGINE = InnoDB
            CHARSET=latin1;"""
    cursor.execute(sql)

    #################
    #BGP
    ################
    sql = """CREATE TABLE `bgps` (
            `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
            `hash` varchar(45) DEFAULT NULL,
            `nonce` varchar(45) DEFAULT NULL,
            `target` varchar(45) DEFAULT NULL,
            `localDomain` int(11) unsigned NOT NULL,
            `remoteDomain` int(11) unsigned NOT NULL,
            `vpn` int(11) unsigned DEFAULT NULL,
            `subnet` int(11) unsigned DEFAULT NULL,
            `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
            `announce` varchar(45) DEFAULT NULL,
            PRIMARY KEY (id),
            INDEX `bgps_fk_vpn_idx` (`vpn` ASC),
            INDEX `bgps_fk_local_idx` (`localDomain` ASC),
            INDEX `bgps_fk_remote_idx` (`remoteDomain` ASC),
            INDEX `bgps_fk_subnet_idx` (`subnet` ASC),
            CONSTRAINT `bgps_fk_vpn_idx`    FOREIGN KEY (`vpn`)    REFERENCES `vpns` (`id`)    ON DELETE NO ACTION    ON UPDATE NO ACTION,
            CONSTRAINT `bgps_fk_local_idx`    FOREIGN KEY (`localDomain`)    REFERENCES `domains` (`id`)    ON DELETE NO ACTION    ON UPDATE NO ACTION,
            CONSTRAINT `bgps_fk_remote_idx`    FOREIGN KEY (`remoteDomain`)    REFERENCES `domains` (`id`)    ON DELETE NO ACTION    ON UPDATE NO ACTION,
            CONSTRAINT `bgps_fk_subnet_idx`  FOREIGN KEY (`subnet`)    REFERENCES `subnets` (`id`)    ON DELETE NO ACTION    ON UPDATE NO ACTION)
            ENGINE = InnoDB
            AUTO_INCREMENT=5
            DEFAULT CHARSET=latin1;"""
    cursor.execute(sql)

        ##database processing ends
    cursor.execute('SET FOREIGN_KEY_CHECKS=1;')
    db.close()



def database_insert_data(domain, db_host, mode):

    db = mdb.connect(db_host, DB_USER, DB_PWD, DB_NAME)
    cursor = db.cursor()
    cursor.execute('SET FOREIGN_KEY_CHECKS=0;')



    ##########
    #DOMAINS
    ##########
    ################ domainns - create very first - as it has no dependencies on other tables


    sql = """INSERT  INTO `domains` (portal_address, email_domain, bgp_ip, as_num, as_name)
        VALUES 	('http://134.221.121.202:9090/CoCo-agent/static/index.html','email202','10.2.0.254',65020,'tno-north'),
                ('http://134.221.121.201:9090/CoCo-agent/static/index.html','email201','10.3.0.254',65030,'tno-south');"""
    try:
        # Execute the SQL command
            cursor.execute(sql)
            # Commit your changes in the database
            db.commit()
    except mdb.Error, e:
            print "MySQL Error [%d]: %s %s" % (e.args[0], e.args[1], sql)
            # Rollback in case there is any error
            db.rollback()


    ############### get Ids for domains
    sql = """SELECT `id` FROM %s.domains WHERE  `as_name` LIKE 'tno-north';""" % DB_NAME
    cursor.execute(sql)
    id_north = cursor.fetchone()[0]

    sql = """SELECT `id` FROM %s.domains WHERE  `as_name` LIKE 'tno-south';""" % DB_NAME
    cursor.execute(sql)
    id_south = cursor.fetchone()[0]


    ################
    #SWITCHES
    ###############
    if mode != 'full':
        if domain == 'tn':
            sql = """INSERT INTO `switches` (id, name, mininetname, x, y, mpls_label)
                                    VALUES ('1', 'TN-PE1', 'tn-pe1' , '0', '0', '1' ),
                            ('2', 'TN-PE2', 'tn-pe2' , '0', '0', '1' ),
                            ('3', 'TN-PC1', 'tn-pc1' , '0', '0', '1' );"""
        else:
                sql = """INSERT INTO `switches` (id, name, mininetname, x, y, mpls_label)
                                    VALUES ('1', 'TS-PE1', 'ts-pe1' , '0', '0', '2' );"""

        try:
            # Execute the SQL command
                cursor.execute(sql)
                # Commit your changes in the database
                db.commit()
        except mdb.Error, e:
                print "MySQL Error [%d]: %s %s" % (e.args[0], e.args[1], sql)
                # Rollback in case there is any error
                db.rollback()


    ###############
    #SITES
    ###############


    ############
    #LINKS
    ###########
    if mode != 'full':
            if  domain == 'tn':
                    sql = """INSERT INTO `links` (`from`, `to`)
                                     VALUES ((SELECT `id` FROM `switches` WHERE  `name` = 'TN-PC1'),(SELECT `id` FROM `switches` WHERE  `name` = 'TN-PE1')),
                                            ((SELECT `id` FROM `switches` WHERE  `name` = 'TN-PC1'),(SELECT `id` FROM `switches` WHERE  `name` = 'TN-PE2'));"""
                    try:
                            # Execute the SQL command
                            cursor.execute(sql)
                            # Commit your changes in the database
                            db.commit()
                    except mdb.Error, e:
                            print "MySQL Error [%d]: %s %s" % (e.args[0], e.args[1], sql)
                            # Rollback in case there is any error
                            db.rollback()


    ###########
    #EXT LINKS
    ##########
    if mode != 'full':
        if  domain == 'tn':
            sql = """INSERT INTO `extLinks` (`switch`, `domain`)
                             VALUES ((SELECT `id` FROM `switches` WHERE  `name` = 'TN-PE2'), '1');"""
        else:
            sql = """INSERT INTO `extLinks` (`switch`, `domain`)
                                 VALUES ((SELECT `id` FROM `switches` WHERE  `name` = 'TS-PE1'), '2');"""
        try:
            # Execute the SQL command
                cursor.execute(sql)
                # Commit your changes in the database
                db.commit()
        except mdb.Error, e:
                print "MySQL Error [%d]: %s %s" % (e.args[0], e.args[1], sql)
                # Rollback in case there is any error
                db.rollback()
    ############
    #users
    ############

    #Random data
    sql = """INSERT INTO `users` (name, email, domain, admin)
            VALUES 	('tn_admin','tn_admin@mail.com','%d',1),
                    ('bill_tn','bill_tn@mail.com','%d',0),
                    ('mark_tn','mark_tn@mail.com','%d',0),
                    ('tn_user','tn_user@mail.com','%d',0),
                    ('ts_admin','ts_admin@mail.com','%d',1),
                    ('ts_user','ts_user@mail.com','%d',0),
                    ('mike_ts','mike_ts@mail.com','%d',0),
                    ('harry_ts','harry_ts@mail.com','%d',0);""" %(id_north, id_north,id_north, id_north, id_south, id_south, id_south, id_south)
    try:
        # Execute the SQL command
        cursor.execute(sql)
        # Commit your changes in the database
        db.commit()
    except mdb.Error, e:
            print "MySQL Error [%d]: %s %s" % (e.args[0], e.args[1], sql)
            # Rollback in case there is any error
            db.rollback()



    #data prepared for tests
    sql = """INSERT INTO `users` (name, email, domain, admin) VALUES ('simon', 'simongunkel@googlemail.com', '1', '0');"""
    try:
        # Execute the SQL command
            cursor.execute(sql)
            # Commit your changes in the database
            db.commit()
    except mdb.Error, e:
            print "MySQL Error [%d]: %s %s" % (e.args[0], e.args[1], sql)
            # Rollback in case there is any error
            db.rollback()



    ################
    #VPNs
    ################

    #############
    #SUBNETS
    ############

    ################
    #subnetUsers
    ################


    ################
    #VPN USERS
    ###############


    ################
    #vpnSubnet
    ###############



    ##database processing ends
    cursor.execute('SET FOREIGN_KEY_CHECKS=1;')
    db.close()

def main(domain_arg=None, mode="all"):
    #by default tn domain
    domain = 'tn'
    db_host = 'localhost'
    if domain_arg:
        domain = domain_arg
    if domain == 'ts':
                db_host = DB_HOST_TS
    else:
                db_host = DB_HOST_TN

    database_set_up(db_host)
    database_insert_data(domain, db_host, mode)

if __name__ == '__main__':
    #by default tn domain
    domain = 'tn'
    mode = 'full'
    if len(sys.argv) < 3:
        print "No domain or mode provided; TN/full used by default"
    else:
        domain = sys.argv[1]
        mode = sys.argv[2]
    main(domain,mode)
