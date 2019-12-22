-- POSTGRESQL

create table if not exists crawler_data
(
    id                 text                          not null
        constraint crawler_data_pk
            primary key,
    name               text                          not null,
    photos             text[]                        not null,
    bio                text,
    ts                 timestamp default now(),
    rating             integer   default 0           not null,
    distance           integer   default 0           not null,
    birthday           timestamp                     not null,
    content_hash       text                          not null,
    s_number           text,
    updated_at         timestamp default now()       not null,
    verdict            integer   default 0           not null,
    verdict_sync_at    timestamp,
    verdicted_at       timestamp,
    hidden             boolean   default false       not null,
    img_cached         boolean   default false       not null,
    avg_batch_rank     int       default 0           not null,
    avg_batch_rank_idx int       default 0           not null,
    teasers            jsonb     default '{}'::jsonb not null,
    enrich_required    boolean   default true        not null,
    autoliked          boolean   default false       not null,
    height             integer   default -1          not null,
    longest_bio        text      default ''          not null
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

create index if not exists crawler_data_enrich_required_index
    on crawler_data ((1)) where enrich_required;

create index if not exists crawler_data_height_index
    on crawler_data (height);

create index if not exists crawler_data_updated_at_index
    on crawler_data (updated_at);

create index if not exists crawler_data_verdicted_at_index
    on crawler_data (verdicted_at);

CREATE EXTENSION pg_trgm;

CREATE index trgm_idx_crawler_data_bio
    ON crawler_data
        USING gin (lower(bio) gin_trgm_ops);


CREATE VIEW tcrawler_top AS
SELECT * FROM crawler_data
WHERE hidden = FALSE AND verdict = 0 AND verdict_sync_at is null
ORDER BY rating desc, updated_at DESC, id ASC;


CREATE VIEW tcrawler_search AS
SELECT * FROM crawler_data
WHERE hidden = FALSE AND verdict = 0 AND verdict_sync_at is null
order by updated_at DESC, id ASC;


CREATE VIEW tcrawler_latest AS
select * from crawler_data
WHERE rating > 0 AND hidden = FALSE AND verdict = 0 AND verdict_sync_at is null
order by date_trunc('day', GREATEST(ts,updated_at)) DESC, rating DESC, length(bio) DESC, id ASC;


CREATE VIEW tcrawler_random AS
select * from crawler_data where hidden = FALSE and now() - updated_at <= interval '7 days' and height > 0 and height < 170 and verdict = 0 order by random();

CREATE VIEW tcrawler_today AS
select * from crawler_data where date_trunc('day', ts) = date_trunc('day',now()) and verdict = 0 order by rating desc;

CREATE VIEW tcrawler_possible_likes AS
select * from crawler_data where verdict = 0 AND verdict_sync_at is null AND now() - updated_at <= interval '7 days'  AND avg_batch_rank_idx  > 40 order by avg_batch_rank_idx DESC, id ASC;

CREATE VIEW tcrawler_autolike AS
select * from crawler_data where height between 150 and 170 and rating > 0 and verdict = 0 order by updated_at asc,id ASC;

CREATE VIEW tcrawler_autodislike AS
select * from crawler_data where length(bio) = 0 and avg_batch_rank_idx < 5 and verdict = 0 order by updated_at asc,id ASC;

CREATE VIEW tcrawler_near AS select * from crawler_data where date_trunc('day',now())=date_trunc('day', updated_at) and verdict = 0 and distance = 1 order by rating desc;