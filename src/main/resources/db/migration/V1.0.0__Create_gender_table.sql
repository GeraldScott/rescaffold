create table gender (
    id bigint unique generated always as identity,
    code varchar(1) not null unique,
    description varchar(255) not null unique
);

comment on table gender is 'Gender information for demographic analysis';
comment on column gender.id is 'Primary key';
comment on column gender.code is 'Gender code (single character)';
comment on column gender.description is 'Gender description';

INSERT INTO gender (code, description) VALUES
    ('F', 'Female'),
    ('M', 'Male'),
    ('O', 'Other'),
    ('U', 'Unknown');