# --- !Ups


create table users (
  id       uuid primary key not null,

  email    varchar(255) not null,
  password varchar(255) not null
);

create table roles (

  user_id uuid references users (id),

  name varchar(255) not null
);

create table customers (

  id uuid primary key not null ,

  name varchar(255) not null
);

create table contacts (

  id uuid primary key not null,

  name varchar(255) not null,

  customer_id uuid null references customers(id)
);

create table login_attempts (

  id uuid primary key not null,

  ip   varchar(255) not null,
  time timestamp    not null
);


create table blocked_ips (

  id uuid primary key not null ,

  ip varchar(255) not null
);

create table makeup_types (

  id uuid primary key not null,

  name varchar(255) not null

);

create table makeups (

  id      uuid primary key not null,
  type_id uuid references makeup_types(id),
  name        varchar(255) not null,
  description varchar(255)     null,
  rank        int              null
);


# --- !Downs

drop table users;
drop table roles;
drop table customers;
drop table contacts;
drop table login_attempts;
drop table blocked_ips;
drop table makeup_types;
drop table makeups;