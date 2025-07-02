create table id_type (
    id bigint unique generated always as identity,
    code varchar(5) not null unique,
    description text not null unique,
    created_by varchar not null default 'system',
    created_at timestamp not null default now(),
    updated_by varchar,
    updated_at timestamp
);

comment on table id_type is 'Type of ID document';
comment on column id_type.id is 'Primary key';
comment on column id_type.code is 'ID document type code';
comment on column id_type.description is 'Type of ID document';
comment on column id_type.created_by is 'User who created the record';
comment on column id_type.created_at is 'Record creation timestamp';
comment on column id_type.updated_by is 'User who last updated the record';
comment on column id_type.updated_at is 'Record last updated timestamp';
