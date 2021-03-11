DROP TABLE IF EXISTS groups;
CREATE TABLE groups (
  id int(11) NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  PRIMARY KEY (id)
);

INSERT INTO groups VALUES ('1', 'admin');
INSERT INTO groups VALUES ('2', 'azkaban');

DROP TABLE IF EXISTS roles;
CREATE TABLE roles (
  id int(11) NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  permission varchar(255) not null,
  PRIMARY KEY (id)
);

INSERT INTO roles VALUES ('1', 'admin','ADMIN');
INSERT INTO roles VALUES ('2', 'azkaban','ADMIN');

DROP TABLE IF EXISTS users;
CREATE TABLE users (
  id int(11) NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL UNIQUE,
  password varchar(255) NOT NULL,
  email varchar(255) DEFAULT NULL,
  agent_user varchar(255) ,  --
  roles varchar(255), --
  PRIMARY KEY (id)
);



INSERT INTO azkaban.users (id, name, password, email, agent_user, roles) VALUES (1, 'admin', 'adb6c24f16c876fd3b38624da52fc00e', 'admin@azkaban.com', 'etl', 'admin'); --admin1
INSERT INTO azkaban.users (id, name, password, email, agent_user, roles) VALUES (2, 'azkaban', '4d722373a54a7d0f630188421ea18fb5', 'admin@azkaban.com', 'etl', 'azkaban'); --azkaban

DROP TABLE IF EXISTS user_groups;
CREATE TABLE user_groups (
  id int(11) NOT NULL AUTO_INCREMENT,
  group_id int(11) DEFAULT NULL,
  user_id int(11) DEFAULT NULL,
  PRIMARY KEY (id)
);

INSERT INTO user_groups VALUES ('1', '1', '1');
INSERT INTO user_groups VALUES ('2', '2', '2');
INSERT INTO user_groups VALUES ('3', '1', '2');



DROP TABLE IF EXISTS `group_role`;
CREATE TABLE `group_role` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `group_id` int not NULL,
  `role_id` int DEFAULT NULL
  PRIMARY KEY (`id`)
)
