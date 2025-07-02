create table user_role (
    user_id bigint not null,
    role_id bigint not null,
    assigned_at timestamp not null default now(),
    created_by varchar not null default 'system',
    created_at timestamp not null default now(),
    updated_by varchar,
    updated_at timestamp,
    primary key (user_id, role_id),
    constraint fk_user_role_user_login foreign key (user_id) references user_login(id),
    constraint fk_user_role_role foreign key (role_id) references role(id)
);

comment on table user_role is 'Join table for users and roles';
comment on column user_role.user_id is 'Foreign key to user_login table';
comment on column user_role.role_id is 'Foreign key to role table';
comment on column user_role.assigned_at is 'When the role was assigned to the user';
comment on column user_role.created_by is 'User who created the record';
comment on column user_role.created_at is 'Record creation timestamp';
comment on column user_role.updated_by is 'User who last updated the record';
comment on column user_role.updated_at is 'Record last updated timestamp';
