create table title (
    id bigint unique generated always as identity,
    code varchar(5) not null unique,
    description text not null unique
);

comment on table title is 'Title or honorific';
comment on column title.id is 'Primary key';
comment on column title.code is 'Title code';
comment on column title.description is 'Title description';

INSERT INTO title (code, description) VALUES
    ('MR', 'Mr.'),
    ('MS', 'Ms.'),
    ('DR', 'Dr.'),
    ('PROF', 'Prof.'),
    ('U', 'Unknown');