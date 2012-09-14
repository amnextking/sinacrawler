
CREATE DATABASE IF NOT EXISTS weibo;

use weibo;

DROP TABLE IF EXISTS sina_weibo_user;
CREATE TABLE sina_weibo_user (
user_id bigint(20) NOT NULL COMMENT '新浪微博用户ID',
update_time datetime NOT NULL COMMENT '更新时间',
is_updated tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否已更新',
is_crawled tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否已爬取',
crawl_failed tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否爬取失败',
user_source_type int(11) NOT NULL DEFAULT '0' COMMENT '用户来源类型：1：关键词搜索得到；2：标签搜索得到；3：官博粉丝；4：指定微博的评论转发用户；5：微群用户；6：粉丝；7：其他',
user_source_desc varchar(50) DEFAULT NULL COMMENT '用户来源描述',
is_selected tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否已选为种子节点',
send_time int(5) NOT NULL DEFAULT '0' COMMENT '发送次数',
PRIMARY KEY (user_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT = '新浪微博用户表';


