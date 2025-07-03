create table role (
    id bigint unique generated always as identity,
    name varchar(50) not null unique,
    description text not null unique,
    created_by varchar not null default 'system',
    created_at timestamp not null default now(),
    updated_by varchar,
    updated_at timestamp
);

comment on table role is 'Role for RBAC security';
comment on column role.id is 'Primary key';
comment on column role.name is 'Role name (ROLE_ADMIN, ROLE_USER, etc.)';
comment on column role.description is 'Role description';
comment on column role.created_by is 'User who created the record';
comment on column role.created_at is 'Record creation timestamp';
comment on column role.updated_by is 'User who last updated the record';
comment on column role.updated_at is 'Record last updated timestamp';
