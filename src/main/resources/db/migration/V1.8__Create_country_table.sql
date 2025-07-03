create table country (
    id bigint unique generated always as identity,
    code varchar(2) not null unique,
    name text not null unique,
    created_by varchar not null default 'system',
    created_at timestamp not null default now(),
    updated_by varchar,
    updated_at timestamp
);

comment on table country is 'Country table based on ISO 3166-1 alpha-2';
comment on column country.id is 'Primary key';
comment on column country.code is 'Country code';
comment on column country.name is 'Country name';
comment on column country.created_by is 'User who created the record';
comment on column country.created_at is 'Record creation timestamp';
comment on column country.updated_by is 'User who last updated the record';
comment on column country.updated_at is 'Record last updated timestamp';
