/*
Navicat MySQL Data Transfer

Source Server         : dzfp-dev
Source Server Version : 50621
Source Host           : 192.168.1.200:3306
Source Database       : dzfp-dev

Target Server Type    : MYSQL
Target Server Version : 50621
File Encoding         : 65001

Date: 2016-10-26 14:33:31
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for t_wxkb
-- ----------------------------
DROP TABLE IF EXISTS `t_wxkb`;
CREATE TABLE `t_wxkb` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `access_token` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `scsj` datetime DEFAULT NULL,
  `expires_in` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
