create table person (
    id bigint unique generated always as identity,
    first_name varchar,
    last_name varchar not null,
    email varchar unique,
    id_number varchar,
    id_type_id bigint,
    gender_id bigint,
    title_id bigint,
    created_by varchar not null default 'system',
    created_at timestamp not null default now(),
    updated_by varchar,
    updated_at timestamp,
    constraint fk_person_id_type foreign key (id_type_id) references id_type(id),
    constraint fk_person_gender foreign key (gender_id) references gender(id),
    constraint fk_person_title foreign key (title_id) references title(id)
);

create index idx_person_email on person(email);
create index idx_person_last_name on person(last_name);
create index idx_person_id_type on person(id_type_id);
create index idx_person_gender on person(gender_id);
create index idx_person_title on person(title_id);

comment on table person is 'Personal details';
comment on column person.id is 'Primary key';
comment on column person.first_name is 'Person first name';
comment on column person.last_name is 'Person last name (required)';
comment on column person.email is 'Person email address (unique)';
comment on column person.id_number is 'Person identification number';
comment on column person.id_type_id is 'Foreign key to id_type table';
comment on column person.gender_id is 'Foreign key to gender table';
comment on column person.title_id is 'Foreign key to title table';
comment on column person.created_by is 'User who created the record';
comment on column person.created_at is 'Record creation timestamp';
comment on column person.updated_by is 'User who last updated the record';
comment on column person.updated_at is 'Record last updated timestamp';
