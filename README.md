# sequence generate server

Others way:
DDL:
CREATE TABLE `uid_sequence` (  
  `id` bigint(20) unsigned NOT NULL auto_increment,  
  `stub` char(1) NOT NULL default '',  
  PRIMARY KEY  (`id`),  
  UNIQUE KEY `stub` (`stub`)  
) ENGINE=MyISAM;

DML:
REPLACE INTO uid_sequence (stub) VALUES ('test');  
SELECT LAST_INSERT_ID();
