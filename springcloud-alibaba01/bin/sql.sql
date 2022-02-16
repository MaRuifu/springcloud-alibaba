DROP DATABASE IF EXISTS `shop`;
CREATE DATABASE  `shop` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
USE `shop`;
CREATE TABLE `shop_order` (
                              `oid` int NOT NULL AUTO_INCREMENT COMMENT '主键',
                              `username` varchar(255) DEFAULT NULL COMMENT '用户名',
                              `uid` int DEFAULT NULL COMMENT '用户id',
                              `pid` int DEFAULT NULL COMMENT '商品ID',
                              `pname` varchar(255) DEFAULT NULL COMMENT '商品名称',
                              `pprice` decimal(10,2) DEFAULT NULL COMMENT '商品价格',
                              `number` int DEFAULT NULL COMMENT '数量',
                              PRIMARY KEY (`oid`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `shop_product` (
                                `pid` int NOT NULL AUTO_INCREMENT COMMENT '主键',
                                `pname` varchar(255) DEFAULT NULL COMMENT '商品名称',
                                `pprice` decimal(10,2) DEFAULT NULL COMMENT '商品价格',
                                `stock` int DEFAULT NULL COMMENT '库存',
                                PRIMARY KEY (`pid`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `shop_user` (
                             `uid` int NOT NULL AUTO_INCREMENT COMMENT '主键',
                             `username` varchar(255) DEFAULT NULL COMMENT '用户名',
                             `password` varchar(255) DEFAULT NULL COMMENT '密码',
                             `telephone` varchar(255) DEFAULT NULL COMMENT '手机号',
                             PRIMARY KEY (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;