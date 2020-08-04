create table client
(
    id         integer      not null
        constraint user_pk
            primary key,
    first_name varchar(255) not null,
    last_name  varchar(255) not null
);

create table queue
(
    id         int not null GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1)
        constraint queue_pk
            primary key,
    name       varchar(64)                                                                 not null,
    longitude  double precision                                                            not null,
    latitude   double precision                                                            not null,
    admin      integer                                                                     not null
        constraint queue_client_id_fk
            references client,
    work_hours varchar(70)  default 'testHours'                                            not null,
    active     boolean   default true                                                      not null,
    invalid    boolean   default false                                                     not null,
    start_time timestamp default current_timestamp                                         not null,
    end_time   timestamp default '9999-12-31-23.59.59.999999'                              not null,
    last_place integer   default 0
);

create table client_queue
(
    client_id    integer not null
        constraint client_queue_client_id_fk
            references client,
    queue_id     integer not null
        constraint client_queue_queue_id_fk
            references queue,
    start_time   timestamp,
    end_time     timestamp,
    place        integer not null,
    complete     boolean default false,
    served_by_id integer,
    notification boolean default true,
    constraint client_queue_pk
        primary key (client_id, queue_id, place)
);


create table executor
(
    client_id         integer      not null,
    name              varchar(511) not null,
    queue_id          integer      not null
        constraint executor_queue_id_fk
            references queue,
    serve_client      boolean default false,
    invalid           boolean default false,
    active            boolean default true,
    serving_client_id integer default 0,
    waiting           boolean default false
);
