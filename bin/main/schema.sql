-- Database: millipore_db
-- ------------------------------------------------------
USE `millipore_db`;

/*Table structure for table `ams_user_sync` */

CREATE TABLE IF NOT EXISTS `ams_user_sync` (
  `ams_user_sync_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `client_id` varchar(50) NOT NULL,
  `last_synced_on` timestamp  NOT NULL,
  PRIMARY KEY (`ams_user_sync_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1