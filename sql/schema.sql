-- POSTGRESQL

create table if not exists crawler_data
(
    id                   text                    not null
        constraint crawler_data_pk
            primary key,
    name                 text                    not null,
    photos               text[]                  not null,
    bio                  text,
    ts                   timestamp default now(),
    rating               integer   default 0     not null,
    distance             integer   default 0           not null,
    birthday             timestamp                     not null,
    content_hash         text                          not null,
    s_number             text,
    recs_duplicate_count integer   default 0           not null,
    updated_at           timestamp default now()       not null,
    verdict              integer   default 0           not null,
    verdict_sync_at      timestamp,
    hidden               boolean   default false       not null,
    avg_batch_rank       int       default 0           not null,
    avg_batch_rank_idx   int       default 0           not null,
    teasers              jsonb     default '{}'::jsonb not null
);

create unique index if not exists crawler_data_id_uindex
    on crawler_data (id);

create index if not exists crawler_data_rating_index
    on crawler_data (rating);

create index if not exists crawler_data_ts_index
    on crawler_data (ts);

create index if not exists crawler_data_avg_batch_rank_index
    on crawler_data (avg_batch_rank);

create index crawler_data_avg_batch_rank_idx_index
    on crawler_data (avg_batch_rank_idx desc);

create index if not exists crawler_data_recs_duplicate_count_index
    on crawler_data (recs_duplicate_count desc);

CREATE EXTENSION pg_trgm;

CREATE index trgm_idx_crawler_data_bio
    ON crawler_data
        USING gin (lower(bio) gin_trgm_ops);