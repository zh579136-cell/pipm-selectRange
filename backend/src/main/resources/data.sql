insert into sys_org(org_id, org_name, parent_org_id, org_level, org_pathid) values
('100000', '招商银行', null, 'BANK', '100000'),
('100001', '总行', '100000', 'HEAD_OFFICE', '100000/100001'),
('100101', '北京分行', '100001', 'BRANCH', '100000/100001/100101'),
('100111', '北京城区分行', '100101', 'BRANCH', '100000/100001/100101/100111'),
('100102', '上海分行', '100001', 'BRANCH', '100000/100001/100102'),
('100201', '北京朝阳支行', '100101', 'SUB_BRANCH', '100000/100001/100101/100201'),
('100202', '北京海淀支行', '100101', 'SUB_BRANCH', '100000/100001/100101/100202'),
('100211', '朝阳望京支行', '100201', 'SUB_BRANCH', '100000/100001/100101/100201/100211'),
('100301', '上海浦东支行', '100102', 'SUB_BRANCH', '100000/100001/100102/100301'),
('100401', '信息技术部', '100001', 'DEPT', '100000/100001/100401'),
('100402', '二组', '100401', 'TEAM', '100000/100001/100401/100402'),
('100501', '北京运营管理部', '100101', 'DEPT', '100000/100001/100101/100501'),
('100511', '城区运营管理部', '100111', 'DEPT', '100000/100001/100101/100111/100511'),
('100521', '朝阳支行营业部', '100201', 'DEPT', '100000/100001/100101/100201/100521'),
('100531', '望京支行营业部', '100211', 'DEPT', '100000/100001/100101/100201/100211/100531'),
('100601', '上海风险管理部', '100102', 'DEPT', '100000/100001/100102/100601');

insert into sys_user(user_id, user_name, login_name, org_id, org_level_for_scope, position_name) values
('U100', '张强', 'zhangqiang', '100402', 'HEAD_OFFICE', '信息技术部开发工程师'),
('U101', '李敏', 'limin', '100501', 'BRANCH', '分行运营经理'),
('U102', '王磊', 'wanglei', '100201', 'SUB_BRANCH', '支行柜员'),
('U103', '陈审', 'chenshen', '100521', 'SUB_BRANCH', '审计专员'),
('U104', '赵合', 'zhaohe', '100301', 'SUB_BRANCH', '支行负责人'),
('U105', '孙特', 'sunte', '100301', 'SUB_BRANCH', '特别授权专员'),
('U106', '周城', 'zhoucheng', '100511', 'BRANCH', '城区运营经理'),
('U107', '吴望', 'wuwang', '100531', 'SUB_BRANCH', '审计经理'),
('U108', '何部', 'hebu', '100501', 'BRANCH', '分行报表专员');

insert into sys_role(role_code, role_name) values
('REPORT_VIEWER', '报表查看员'),
('BRANCH_MANAGER', '分行管理岗'),
('AUDITOR', '审计岗');

insert into sys_user_role(user_id, role_code) values
('U100', 'REPORT_VIEWER'),
('U101', 'BRANCH_MANAGER'),
('U102', 'REPORT_VIEWER'),
('U103', 'AUDITOR'),
('U104', 'REPORT_VIEWER'),
('U104', 'BRANCH_MANAGER'),
('U105', 'REPORT_VIEWER'),
('U106', 'BRANCH_MANAGER'),
('U107', 'AUDITOR'),
('U108', 'REPORT_VIEWER');

insert into ds_page_config(page_code, page_name, enabled) values
('DEMO_REPORT_QUERY', '客户指标查询', true),
('LEGACY_REPORT_QUERY', '旧版报表查询', false);

insert into ds_default_rule(page_code, role_code, user_org_level, job_keyword, scope_type, enabled) values
('DEMO_REPORT_QUERY', 'REPORT_VIEWER', 'HEAD_OFFICE', null, 'FIRST_DEPT_AND_DESCENDANTS', true),
('DEMO_REPORT_QUERY', 'REPORT_VIEWER', 'BRANCH', null, 'CURRENT_ORG_AND_DESCENDANTS', true),
('DEMO_REPORT_QUERY', 'REPORT_VIEWER', 'SUB_BRANCH', null, 'CURRENT_ORG_ONLY', true),
('DEMO_REPORT_QUERY', 'BRANCH_MANAGER', 'BRANCH', null, 'OWNER_BANK_AND_DESCENDANTS', true),
('DEMO_REPORT_QUERY', 'BRANCH_MANAGER', 'SUB_BRANCH', null, 'CURRENT_ORG_AND_DESCENDANTS', true),
('DEMO_REPORT_QUERY', 'AUDITOR', 'SUB_BRANCH', '审计', 'OWNER_BANK_AND_DESCENDANTS', true);

insert into ds_user_override_rule(user_id, page_code, role_code, scope_type, custom_org_ids, enabled) values
('U105', 'DEMO_REPORT_QUERY', 'REPORT_VIEWER', null, '100000', true);

insert into biz_demo_report(customer_name, metric_name, metric_value, org_id, org_pathid) values
('总行科技项目A', '贷款余额', '1200万', '100001', '100000/100001'),
('北京分行普惠金融', '客户数', '3200', '100101', '100000/100001/100101'),
('上海分行公司金融', '客户数', '2800', '100102', '100000/100001/100102'),
('朝阳支行零售', '贷款余额', '450万', '100201', '100000/100001/100101/100201'),
('海淀支行财富管理', '客户数', '860', '100202', '100000/100001/100101/100202'),
('望京支行跨区业务', '客户数', '430', '100211', '100000/100001/100101/100201/100211'),
('浦东支行跨境业务', '贷款余额', '700万', '100301', '100000/100001/100102/100301'),
('信息技术部运维', '事件数', '42', '100401', '100000/100001/100401'),
('信息技术部二组', '迭代数', '9', '100402', '100000/100001/100401/100402'),
('北京运营管理部', '流程优化项', '17', '100501', '100000/100001/100101/100501'),
('城区运营管理部', '客户数', '530', '100511', '100000/100001/100101/100111/100511'),
('朝阳支行营业部', '客诉数', '3', '100521', '100000/100001/100101/100201/100521'),
('望京支行营业部', '审批数', '21', '100531', '100000/100001/100101/100201/100211/100531'),
('上海风险管理部', '预警数', '5', '100601', '100000/100001/100102/100601');
