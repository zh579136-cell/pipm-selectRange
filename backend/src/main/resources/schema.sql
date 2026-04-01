drop table if exists biz_demo_report;
drop table if exists ds_user_override_rule;
drop table if exists ds_default_rule;
drop table if exists ds_page_config;
drop table if exists sys_user_role;
drop table if exists sys_role;
drop table if exists sys_user;
drop table if exists sys_org;

create table sys_org (
    org_id varchar(32) primary key,
    org_name varchar(128) not null,
    parent_org_id varchar(32),
    org_level varchar(32) not null,
    org_pathid varchar(255) not null
);

create table sys_user (
    user_id varchar(32) primary key,
    user_name varchar(64) not null,
    login_name varchar(64) not null,
    org_id varchar(32) not null,
    org_level_for_scope varchar(32) not null,
    position_name varchar(128) not null
);

create table sys_role (
    role_code varchar(64) primary key,
    role_name varchar(128) not null
);

create table sys_user_role (
    user_id varchar(32) not null,
    role_code varchar(64) not null
);

create table ds_page_config (
    page_code varchar(64) primary key,
    page_name varchar(128) not null,
    enabled boolean not null
);

create table ds_default_rule (
    id bigint auto_increment primary key,
    page_code varchar(64) not null,
    role_code varchar(64) not null,
    user_org_level varchar(32) not null,
    job_keyword varchar(128),
    scope_type varchar(64) not null,
    enabled boolean not null
);

create table ds_user_override_rule (
    id bigint auto_increment primary key,
    user_id varchar(32) not null,
    page_code varchar(64) not null,
    role_code varchar(64) not null,
    scope_type varchar(64) not null,
    enabled boolean not null
);

create table biz_demo_report (
    id bigint auto_increment primary key,
    customer_name varchar(128) not null,
    metric_name varchar(128) not null,
    metric_value varchar(128) not null,
    org_id varchar(32) not null,
    org_pathid varchar(255) not null
);

create index idx_org_pathid on sys_org(org_pathid);
create index idx_report_orgid on biz_demo_report(org_id);
create index idx_report_orgpathid on biz_demo_report(org_pathid);
