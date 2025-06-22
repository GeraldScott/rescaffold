create table gender (
    id bigint unique generated always as identity,
    code varchar(1) not null unique,
    description text not null unique,
    is_active boolean not null default true,
    created_by varchar not null default 'system',
    created_at timestamp not null default now(),
    updated_by varchar,
    updated_at timestamp
);

comment on table gender is 'Gender information for demographic analysis';
comment on column gender.id is 'Primary key';
comment on column gender.code is 'Gender code';
comment on column gender.description is 'Gender description';
comment on column gender.is_active is 'Indicator to manage logical deletion';
comment on column gender.created_by is 'User who created the record';
comment on column gender.created_at is 'Record creation timestamp';
comment on column gender.updated_by is 'User who last updated the record';
comment on column gender.updated_at is 'Record last updated timestamp';

INSERT INTO gender (code, description) VALUES
    ('F', 'Female'),
    ('M', 'Male'),
    ('O', 'Other'),
    ('U', 'Unknown');