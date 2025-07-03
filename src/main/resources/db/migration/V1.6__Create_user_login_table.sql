create table user_login (
    id bigint unique generated always as identity,
    username varchar(50) not null unique,
    password_hash varchar(255) not null,
    person_id bigint,
    last_login timestamp,
    created_by varchar not null default 'system',
    created_at timestamp not null default now(),
    updated_by varchar,
    updated_at timestamp,
    constraint fk_user_person foreign key (person_id) references person(id)
);

comment on table user_login is 'User authentication records';
comment on column user_login.id is 'Primary key';
comment on column user_login.username is 'Unique username for login';
comment on column user_login.password_hash is 'BCrypt hashed password';
comment on column user_login.person_id is 'Foreign key to person table';
comment on column user_login.last_login is 'Timestamp of last successful login';
comment on column user_login.created_by is 'User who created the record';
comment on column user_login.created_at is 'Record creation timestamp';
comment on column user_login.updated_by is 'User who last updated the record';
comment on column user_login.updated_at is 'Record last updated timestamp';
