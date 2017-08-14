/*
 sequence generate moudle database
*/

-- ----------------------------
-- 建库
-- ----------------------------
DROP DATABASE IF EXISTS db_sequence_center;
CREATE DATABASE IF NOT EXISTS db_sequence_center DEFAULT CHARACTER SET UTF8 COLLATE UTF8_GENERAL_CI;
USE db_sequence_center;


-- ----------------------------
-- 建表
-- ----------------------------
CREATE TABLE `t_sequence` (
  `id`             int(11)     NOT NULL AUTO_INCREMENT COMMENT '主键自增ID',
  `seq_name`       varchar(16) NOT NULL                COMMENT '序列名',
  `next_value`     bigint(20)  NOT NULL DEFAULT '1'    COMMENT '下一个序列值',
  `max_value`      bigint(20)  DEFAULT NULL            COMMENT '最大值（为空或小于0时表示无限制）',
  `period_seconds` int(11)     DEFAULT NULL            COMMENT '重置周期（单位秒，为空或小于0时表示不重置）',
  `last_reset`     datetime    DEFAULT NULL            COMMENT '最近一次重置时间（为空时表示不重置）',
  `version`        int(11)     NOT NULL DEFAULT '0'    COMMENT '版本号',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_seqname` (`seq_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


-- ----------------------------
-- 用户与权限
-- ----------------------------
GRANT ALL PRIVILEGES ON db_sequence_center.* TO 'db_seq_center'@'%' IDENTIFIED BY 'db_seq_center';
FLUSH PRIVILEGES;


-- ----------------------------
-- 创建序列存储过程
-- ----------------------------
DROP PROCEDURE IF EXISTS `prcd_sequence_center`;
DELIMITER // 
CREATE DEFINER=`db_seq_center`@`%` PROCEDURE `prcd_sequence_center`(IN s_name VARCHAR(50), IN batch_size INT)
BEGIN

  SET @sv:=NULL, @ev:=NULL, @lr:=NULL;

  UPDATE t_sequence SET 
    next_value=
      CASE 
        WHEN (@ps:=period_seconds) IS NULL OR period_seconds < 1 OR max_value IS NULL OR max_value < 1 OR last_reset IS NULL 
          THEN @ev:=(@sv:=next_value)+batch_size
        WHEN last_reset > NOW() OR (DATE_ADD(last_reset,INTERVAL period_seconds SECOND) > NOW() AND next_value > max_value) 
          THEN (@ev:=@sv:=0)+next_value
        WHEN last_reset > NOW() OR (DATE_ADD(last_reset,INTERVAL period_seconds SECOND) > NOW() AND next_value = max_value) 
          THEN  @ev:=(@sv:=next_value)+1
        WHEN last_reset > NOW() OR (DATE_ADD(last_reset,INTERVAL period_seconds SECOND) > NOW() AND next_value < max_value AND (next_value+batch_size)>max_value) 
          THEN @ev:=((@sv:=next_value)+max_value-next_value+1) 
        WHEN last_reset > NOW() OR (DATE_ADD(last_reset,INTERVAL period_seconds SECOND) > NOW() AND next_value < max_value) 
          THEN @ev:=(@sv:=next_value)+batch_size
        WHEN DATE_ADD(last_reset,INTERVAL period_seconds SECOND) > NOW() AND next_value < max_value 
          THEN IF((next_value+batch_size)>=max_value, @ev:=(@sv:=next_value)+max_value-next_value+1, @ev:=(@sv:=next_value)+batch_size)
        WHEN 1+batch_size>=max_value 
          THEN @ev:=(@sv:=next_value)+max_value-next_value+1
        ELSE @ev:=(@sv:=1)+batch_size
      END,
    last_reset=
      CASE
        WHEN last_reset IS NULL OR period_seconds IS NULL OR period_seconds < 1 
          THEN @lr:=last_reset 
        WHEN DATE_ADD(last_reset,INTERVAL period_seconds SECOND) <= NOW() 
          THEN @lr:=(DATE_ADD(last_reset, INTERVAL FLOOR((UNIX_TIMESTAMP(NOW())-UNIX_TIMESTAMP(last_reset))/period_seconds)*period_seconds SECOND))
        ELSE @lr:=last_reset 
      END
  WHERE seq_name=s_name;
  
  SELECT @sv `start`, @ev-1 `end`, @lr startPeriod, DATE_ADD(@lr, INTERVAL @ps-1 SECOND) endPeriod;

END// 
DELIMITER ;


-- 测试数据：
    -- INSERT INTO `db_sequence_center`.`t_sequence` (`seq_name`, `next_value`, `max_value`, `period_seconds`, `last_reset`, `version`) VALUES ('seq_order', '2259001', NULL, NULL, NULL, '0');
    -- INSERT INTO `db_sequence_center`.`t_sequence` (`seq_name`, `next_value`, `max_value`, `period_seconds`, `last_reset`, `version`) VALUES ('seq_user', '10', '100', '86400', '2017-05-30 00:00:00', '0');

-- 调用方式：
    -- CALL prcd_sequence_center('seq_user', 9);
