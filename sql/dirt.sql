-- -----------------------------------------------------------------------------
-- Conventions used by this database:
--
-- tables and columns are named in camelCase
-- indices are named in the following convention:
--   px_<table>_<col>			  primary KEY (single column, sometimes on surrogate indices)
--   fx_<table>_<col1>_<col2>_...  foreign indices (constrain to other tables)
--   ux_<table>_<col1>_<col2>_...  unique indices (constraints on this table)
--   ix_<table>_<col1>_<col2>_...  alternative indices (faster operations)
-- additionally, indices will typically be defined in px, fx, ux, ix order
-- -----------------------------------------------------------------------------

USE `eve`;

-- -----------------------------------------------------------------------------

-- import sde
ALTER TABLE `invTypes`
	ADD KEY `ix_invTypes_marketGroupID` (`marketGroupID`);

-- -----------------------------------------------------------------------------

DROP TABLE IF EXISTS `alliance`;
DROP TABLE IF EXISTS `corporation`;
DROP TABLE IF EXISTS `character`;
DROP TABLE IF EXISTS `structure`;

CREATE TABLE `alliance` (
	`allianceId` INT,
	`allianceName` VARCHAR(64),
	`ticker` VARCHAR(8),
	`dateFounded` DATE,
	`executorCorp` INT,
	PRIMARY KEY (`allianceId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `corporation` (
	`corpId` INT,
	`corpName` VARCHAR(64),
	`ticker` VARCHAR(8),
	`allianceId` INT,
	`ceo` INT,
	`creator` INT,
	`creationDate` DATE,
	`memberCount` INT,
	`taxRate` FLOAT,
	`url` VARCHAR(255),
	PRIMARY KEY (`corpId`),
	KEY `ix_corporation_allianceId` (`allianceId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `character` (
	`charId` INT,
	`charName` VARCHAR(64),
	`corpId` INT,
	`allianceId` INT,
	`birthday` DATE,
	PRIMARY KEY (`charId`)
)  ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `structure` (
	`structId` BIGINT,
	`structName` VARCHAR(64),
	`corpId` INT,
	`systemId` INT NOT NULL,
	`typeId` INT NOT NULL,
	PRIMARY KEY (`structId`),
	KEY `ix_structure_systemId` (`systemId`),
	KEY `ix_structure_corpId` (`corpId`)
) ENGINE=InnoDb DEFAULT CHARSET=utf8;

-- -----------------------------------------------------------------------------

DROP TABLE IF EXISTS `property`;
DROP TABLE IF EXISTS `dirtListItem`;
DROP TABLE IF EXISTS `dirtList`;
DROP TABLE IF EXISTS `dirtApiAuth`;
DROP TABLE IF EXISTS `dirtUser`;
DROP TABLE IF EXISTS `fortchain`;

CREATE TABLE `property` (
	`id` INT AUTO_INCREMENT,
	`propertyName` VARCHAR(64) NULL,
	`propertyValue` VARCHAR(256) NULL,
	PRIMARY KEY (`id`),
	UNIQUE KEY `ux_property_propertyName` (`propertyName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `dirtUser` (
	`userId` INT AUTO_INCREMENT,
	`username` VARCHAR(64) NOT NULL,
	`name` VARCHAR(64),
	`hash` VARCHAR(255) NOT NULL,
	`dateCreated` TIMESTAMP NOT NULL,
	`lastLogin` TIMESTAMP NOT NULL,
	`admin` BOOLEAN NOT NULL DEFAULT FALSE,
	`disabled` BOOLEAN NOT NULL DEFAULT FALSE,
	PRIMARY KEY (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `dirtList` (
	`listId` INT AUTO_INCREMENT,
	`userId` INT NOT NULL,
	`name` VARCHAR(64),
	`public` BOOLEAN NOT NULL DEFAULT 0,
	PRIMARY KEY (`listId`),
	FOREIGN KEY (`userId`)
		REFERENCES `dirtUser`(`userId`)
		ON DELETE CASCADE,
	KEY `ix_dirtList_userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `dirtListItem` (
	`itemEntryId` INT AUTO_INCREMENT,
	`listId` INT NOT NULL,
	`typeId` INT NOT NULL,
	`quantity` INT NOT NULL DEFAULT 1 CHECK (`quantity` >= 0),
	PRIMARY KEY (`itemEntryId`),
	FOREIGN KEY (`listId`)
		REFERENCES `dirtList` (`listId`)
		ON DELETE CASCADE,
	FOREIGN KEY (`typeId`)
		REFERENCES `invTypes` (`typeID`),
	UNIQUE KEY (`listId` , `typeId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `dirtApiAuth` (
	`keyId` INT AUTO_INCREMENT,
	`userId` INT NOT NULL,
	`charId` INT NOT NULL,
	`charName` VARCHAR(255),
	`charHash` VARCHAR(255),
	`token` VARCHAR(255),
	`expires` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	`refresh` VARCHAR(255),
	PRIMARY KEY (`keyId`),
	FOREIGN KEY (`userId`)
		REFERENCES `dirtUser` (`userId`)
		ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `fortchain` (
	`systemId` int(11) NOT NULL,
	`superDocking` BOOLEAN DEFAULT FALSE,
	PRIMARY KEY (`systemId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- -----------------------------------------------------------------------------

DROP TABLE IF EXISTS `marketHistory`;
DROP TABLE IF EXISTS `marketOrder`;
DROP TABLE IF EXISTS `insurancePrice`;

CREATE TABLE `marketHistory` (
	`histEntryId` BIGINT AUTO_INCREMENT,
	`typeId` INT NOT NULL,
	`regionId` INT NOT NULL,
	`date` DATE NOT NULL,
	`highest` DECIMAL(19, 4),
	`average` DECIMAL(19, 4),
	`lowest` DECIMAL(19, 4),
	`volume` BIGINT,
	`orderCount` BIGINT,
	PRIMARY KEY (`histEntryId`),
	UNIQUE KEY `ux_marketHistory_typeId_regionId_date` (`typeId`,`regionId`,`date`),
	KEY `ix_marketHistory_typeId_regionId` (`typeId`, `regionId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `marketOrder` (
	`orderEntryId` BIGINT AUTO_INCREMENT,
	`issued` TIMESTAMP,
	`range` VARCHAR(45),
	`isBuyOrder` BOOLEAN,
	`duration` INT,
	`orderId` BIGINT NOT NULL,
	`volumeRemain` INT,
	`minVolume` INT,
	`typeId` INT NOT NULL,
	`volumeTotal` INT,
	`locationId` BIGINT NOT NULL,
	`price` DECIMAL(19,4),
	`regionId` INT NOT NULL,
	-- `visibility` INT, -- 0=private, 1=corp, 2=alliance, 3=public
	`retrieved` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (`orderEntryId`),
	UNIQUE KEY `ux_marketOrder_orderId_retrieved` (`orderId`, `retrieved`),
	KEY `ix_marketOrder_typeId_regionId` (`typeId`, `regionId`),
	KEY `ix_marketOrder_locationId` (`locationId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `insurancePrice` (
	`insuranceEntryId` BIGINT AUTO_INCREMENT,
	`typeId` INT NOT NULL,
	`name` VARCHAR(255) NOT NULL,
	`cost` DECIMAL(19, 4),
	`payout` DECIMAL(19, 4),
	PRIMARY KEY (`insuranceEntryId`),
	UNIQUE KEY (`typeId`, `name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- -----------------------------------------------------------------------------

DROP TABLE IF EXISTS `merIskVolume`;
DROP TABLE IF EXISTS `merMoneySupply`;
DROP TABLE IF EXISTS `merProdDestMine`;
DROP TABLE IF EXISTS `merRegStat`;
DROP TABLE IF EXISTS `merSinkFaucet`;

CREATE TABLE `merIskVolume` (
	`date` DATE NOT NULL,
	`iskVolume` BIGINT,
	PRIMARY KEY (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `merMoneySupply` (
	`date` DATE NOT NULL,
	`character` BIGINT,
	`corporation` BIGINT,
	`total` BIGINT,
	PRIMARY KEY (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `merProdDestMine` (
	`date` DATE NOT NULL,
	`produced` BIGINT,
	`destroyed` BIGINT,
	`mined` BIGINT,
	PRIMARY KEY (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `merRegStat` (
	`regStatEntryId` INT AUTO_INCREMENT,
	`date` DATE NOT NULL,
	`regionId` INT NOT NULL,
	`exports` BIGINT,
	`exportsm3` BIGINT,
	`imports` BIGINT,
	`importsm3` BIGINT,
	`netExports` BIGINT,
	`netImports` BIGINT,
	`produced` BIGINT,
	`destroyed` BIGINT,
	`mined` BIGINT,
	`traded` BIGINT,
	PRIMARY KEY (`regStatEntryId`),
	FOREIGN KEY (`regionId`)
		REFERENCES `mapRegions`(`regionID`),
	UNIQUE KEY `ux_merRegStat_regionId_date` (`regionId`, `date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `merSinkFaucet` (
	`sinkFaucetEntryId` INT AUTO_INCREMENT,
	`date` DATE NOT NULL,
	`keyText` VARCHAR(64) NOT NULL,
	`category` VARCHAR(64),
	`faucet` BIGINT,
	`sink` BIGINT,
	`sortValue` BIGINT,
	PRIMARY KEY (`sinkFaucetEntryId`),
	UNIQUE KEY `ux_merSinkFaucet_keyText_date` (`keyText` , `date`)
)  ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- -----------------------------------------------------------------------------

DROP VIEW IF EXISTS `vJitaBestSell`;
DROP VIEW IF EXISTS `vAmarrBestSell`;

CREATE VIEW vJitaBestSell AS SELECT typeId, MIN(price) AS best FROM marketOrder WHERE locationId=60003760 AND isBuyOrder=0 GROUP BY typeId, locationId;
CREATE VIEW vAmarrBestSell AS SELECT typeId, MIN(price) AS best FROM marketOrder WHERE locationId=60008494 AND isBuyOrder=0 GROUP BY typeId, locationId;

-- -----------------------------------------------------------------------------

DROP USER IF EXISTS 'dirt'@'localhost';
DROP USER IF EXISTS 'dirt'@'DOMAINNAME';
CREATE USER 'dirt'@'localhost' IDENTIFIED BY 'DIRTDBPW';
CREATE USER 'dirt'@'DOMAINNAME' IDENTIFIED BY 'DIRTDBPW';
GRANT ALL ON eve.* TO 'dirt'@'localhost','dirt'@'DOMAINNAME';
FLUSH PRIVILEGES;

-- -----------------------------------------------------------------------------

-- default admin
-- username: admin
-- password: dirtadmin
INSERT INTO `dirtUser` (
	`username`, `name`, `hash`, `dateCreated`, `lastLogin`, `admin`, `disabled`
) VALUES (
	'admin',
	'Admin',
	'$2y$10$dmQhr32z5mibYp5FLI8gNuNtpkhhX/7j.oyWcwRZ57a85jbgSxcdC',
	NOW(),
	NOW(),
	TRUE,
	FALSE
);

INSERT INTO `property` (`propertyName`,`propertyValue`) VALUES ("domain","DOMAINNAME");
INSERT INTO `property` (`propertyName`,`propertyValue`) VALUES ("adminemail","webmaster@DOMAINNAME");
INSERT INTO `property` (`propertyName`,`propertyValue`) VALUES ("scraperkeyid","1");
INSERT INTO `property` (`propertyName`,`propertyValue`) VALUES ("ssoclientid","APPCLIENTID");
INSERT INTO `property` (`propertyName`,`propertyValue`) VALUES ("ssosecretkey","APPSECRETKEY");
INSERT INTO `property` (`propertyName`,`propertyValue`) VALUES ("threads","1");
INSERT INTO `property` (`propertyName`,`propertyValue`) VALUES ("marketorders.regions","10000002,10000043,10000032,10000030,10000042");
INSERT INTO `property` (`propertyName`,`propertyValue`) VALUES ("marketorders.period","60");
INSERT INTO `property` (`propertyName`,`propertyValue`) VALUES ("markethistory.regions","10000002,10000043,10000032,10000030,10000042");
INSERT INTO `property` (`propertyName`,`propertyValue`) VALUES ("markethistory.period","1440");
INSERT INTO `property` (`propertyName`,`propertyValue`) VALUES ("publicstructures.period","1440");
INSERT INTO `property` (`propertyName`,`propertyValue`) VALUES ("insuranceprices.period","240");
INSERT INTO `property` (`propertyName`,`propertyValue`) VALUES ("characterdata.period","15");
INSERT INTO `property` (`propertyName`,`propertyValue`) VALUES ("characterdata.expires","60");
INSERT INTO `property` (`propertyName`,`propertyValue`) VALUES ("marketpublic","false");
INSERT INTO `property` (`propertyName`,`propertyValue`) VALUES ("maintenancemode","false");
INSERT INTO `property` (`propertyName`,`propertyValue`) VALUES ("useragent","DIRT/0.1 (webmaster@DOMAINNAME)");
INSERT INTO `property` (`propertyName`,`propertyValue`) VALUES ("ssoscope","esi-wallet.read_character_wallet.v1 esi-universe.read_structures.v1 esi-assets.read_assets.v1 esi-ui.open_window.v1 esi-markets.structure_markets.v1 esi-markets.read_character_orders.v1");
