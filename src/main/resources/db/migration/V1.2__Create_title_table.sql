create table title (
    id bigint unique generated always as identity,
    code varchar(5) not null unique,
    description text not null unique,
    is_active boolean not null default true,
    created_by varchar not null default 'system',
    created_at timestamp not null default now(),
    updated_by varchar,
    updated_at timestamp
);

comment on table title is 'Title or honorific for personal identification';
comment on column title.id is 'Primary key';
comment on column title.code is 'Title code';
comment on column title.description is 'Title description';
comment on column title.is_active is 'Indicator to manage logical deletion';
comment on column title.created_by is 'User who created the record';
comment on column title.created_at is 'Record creation timestamp';
comment on column title.updated_by is 'User who last updated the record';
comment on column title.updated_at is 'Record last updated timestamp';
