create table client
(
    id         bigint       not null
        constraint user_pk
            primary key,
    first_name varchar(255) not null,
    last_name  varchar(255) not null
);

create table queue
(
    id         bigserial                                             not null
        constraint queue_pk
            primary key,
    name       varchar(64)                                           not null,
    longitude  double precision                                      not null,
    latitude   double precision                                      not null,
    admin      bigint                                                not null
        constraint queue_client_id_fk
            references client,
    work_hours varchar(70) default 'needTOChange'::character varying not null,
    active     boolean     default true                              not null,
    invalid    boolean     default false                             not null,
    start_time timestamp without time zone                           not null,
    end_time   timestamp without time zone   default 'infinity'::timestamp without time zone,
    last_place bigint      default 0
    geom geometry                                                    not null
);

create table client_queue
(
    client_id    bigint not null
        constraint client_queue_client_id_fk
            references client,
    queue_id     bigint not null
        constraint client_queue_queue_id_fk
            references queue,
    start_time   timestamp,
    end_time     timestamp,
    place        bigint not null,
    complete     boolean default false,
    served_by_id bigint,
    notification boolean default true,
    constraint client_queue_pk
        primary key (client_id, queue_id, place)
);

create table executor
(
    client_id         bigint       not null,
    name              varchar(511) not null,
    queue_id          bigint       not null
        constraint executor_queue_id_fk
            references queue,
    serve_client      boolean default false,
    invalid           boolean default false,
    active            boolean default false,
    serving_client_id bigint  default 0,
    waiting           boolean default false
);




