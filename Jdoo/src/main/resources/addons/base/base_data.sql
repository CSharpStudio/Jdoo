-------------------------------------------------------------------------
-- Pure SQL
-------------------------------------------------------------------------

CREATE TABLE ir_actions (
  id varchar(36),
  primary key(id)
);
CREATE TABLE ir_act_window (primary key(id)) INHERITS (ir_actions);
CREATE TABLE ir_act_report_xml (primary key(id)) INHERITS (ir_actions);
CREATE TABLE ir_act_url (primary key(id)) INHERITS (ir_actions);
CREATE TABLE ir_act_server (primary key(id)) INHERITS (ir_actions);
CREATE TABLE ir_act_client (primary key(id)) INHERITS (ir_actions);

CREATE TABLE res_users (
    id varchar(36) NOT NULL,
    active boolean default True,
    login varchar(64) NOT NULL UNIQUE,
    password varchar default null,
    -- No FK references below, will be added later by ORM
    -- (when the destination rows exist)
    company_id varchar(36), -- references res_company,
    partner_id varchar(36), -- references res_partner,
    create_date timestamp without time zone,
    primary key(id)
);

CREATE TABLE res_groups (
    id varchar(36) NOT NULL,
    name varchar NOT NULL,
    primary key(id)
);

CREATE TABLE ir_module_category (
    id varchar(36) NOT NULL,
    create_uid varchar(36), -- references res_users on delete set null,
    create_date timestamp without time zone,
    write_date timestamp without time zone,
    write_uid varchar(36), -- references res_users on delete set null,
    parent_id varchar(36) REFERENCES ir_module_category ON DELETE SET NULL,
    name character varying(128) NOT NULL,
    primary key(id)
);

CREATE TABLE ir_module_module (
    id varchar(36) NOT NULL,
    create_uid varchar(36), -- references res_users on delete set null,
    create_date timestamp without time zone,
    write_date timestamp without time zone,
    write_uid varchar(36), -- references res_users on delete set null,
    website character varying(256),
    summary character varying(256),
    name character varying(128) NOT NULL,
    author character varying,
    icon varchar,
    state character varying(16),
    latest_version character varying(64),
    shortdesc character varying(256),
    category_id varchar(36) REFERENCES ir_module_category ON DELETE SET NULL,
    description text,
    application boolean default False,
    demo boolean default False,
    web boolean DEFAULT FALSE,
    license character varying(32),
    sequence integer DEFAULT 100,
    auto_install boolean default False,
    to_buy boolean default False,
    primary key(id)
);
ALTER TABLE ir_module_module add constraint name_uniq unique (name);

CREATE TABLE ir_module_module_dependency (
    id varchar(36) NOT NULL,
    create_uid varchar(36), -- references res_users on delete set null,
    create_date timestamp without time zone,
    write_date timestamp without time zone,
    write_uid varchar(36), -- references res_users on delete set null,
    name character varying(128),
    module_id varchar(36) REFERENCES ir_module_module ON DELETE cascade,
    auto_install_required boolean DEFAULT true,
    primary key(id)
);

CREATE TABLE ir_model_data (
    id varchar(36) NOT NULL,
    create_uid varchar(36),
    create_date timestamp without time zone,
    write_date timestamp without time zone,
    write_uid varchar(36),
    noupdate boolean DEFAULT False,
    name varchar NOT NULL,
    date_init timestamp without time zone,
    date_update timestamp without time zone,
    module varchar NOT NULL,
    model varchar NOT NULL,
    res_id varchar(36),
    primary key(id)
);

CREATE TABLE res_currency (
    id varchar(36),
    name varchar NOT NULL,
    symbol varchar NOT NULL,
    primary key(id)
);

CREATE TABLE res_company (
    id varchar(36),
    name varchar NOT NULL,
    partner_id varchar(36),
    currency_id varchar(36),
    sequence integer,
    create_date timestamp without time zone,
    primary key(id)
);

CREATE TABLE res_partner (
    id varchar(36),
    name varchar,
    company_id varchar(36),
    create_date timestamp without time zone,
    primary key(id)
);


---------------------------------
-- Default data
---------------------------------
insert into res_currency (id, name, symbol) VALUES ('1', 'EUR', '€');
insert into ir_model_data (id, name, module, model, noupdate, res_id) VALUES ('1', 'EUR', 'base', 'res.currency', true, '1');

insert into res_company (id, name, partner_id, currency_id, create_date) VALUES ('1', 'My Company', '1', '1', now() at time zone 'UTC');
insert into ir_model_data (id, name, module, model, noupdate, res_id) VALUES ('2', 'main_company', 'base', 'res.company', true, '1');

insert into res_partner (id, name, company_id, create_date) VALUES ('1', 'My Company', '1', now() at time zone 'UTC');
insert into ir_model_data (id, name, module, model, noupdate, res_id) VALUES ('3', 'main_partner', 'base', 'res.partner', true, '1');

insert into res_users (id, login, password, active, partner_id, company_id, create_date) VALUES ('1', '__system__', NULL, false, '1', '1', now() at time zone 'UTC');
insert into ir_model_data (id, name, module, model, noupdate, res_id) VALUES ('4', 'user_root', 'base', 'res.users', true, '1');

insert into res_groups (id, name) VALUES ('1', 'Employee');
insert into ir_model_data (id, name, module, model, noupdate, res_id) VALUES ('5', 'group_user', 'base', 'res.groups', true, '1');
