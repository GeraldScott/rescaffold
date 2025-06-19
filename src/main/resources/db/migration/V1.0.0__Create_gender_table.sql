create table gender (
    id uuid primary key default gen_random_uuid(),
    code varchar(1) not null unique,
    description text not null unique,
    is_active boolean not null default true,
    created_at timestamp not null default now(),
    updated_at timestamp,
    created_by varchar not null default 'system',
    updated_by varchar
);

comment on table gender is 'Gender information for demographic analysis';
comment on column gender.id is 'Primary key';
comment on column gender.code is 'Gender code';
comment on column gender.description is 'Gender description';
comment on column gender.is_active is 'Indicator to manage logical deletion';
comment on column gender.created_at is 'Record creation timestamp';
comment on column gender.updated_at is 'Record last updated timestamp';
comment on column gender.created_by is 'User who created the record';
comment on column gender.updated_by is 'User who last updated the record';

INSERT INTO gender (code, description) VALUES
    ('F', 'Female'),
    ('M', 'Male'),
    ('O', 'Other'),
    ('U', 'Unknown');