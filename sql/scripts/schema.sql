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

create index if not exists idx_crawler_data_bio_length
    ON crawler_data (length(bio));

create index if not exists crawler_data_updated_at_index
    on crawler_data (updated_at);

create index if not exists crawler_data_verdicted_at_index
    on crawler_data (verdicted_at);

CREATE EXTENSION pg_trgm;

CREATE index trgm_idx_crawler_data_bio
    ON crawler_data
        USING gin (lower(bio) gin_trgm_ops);

CREATE OR REPLACE VIEW public.tcrawler_no_verdict AS
SELECT * FROM crawler_data
WHERE crawler_data.hidden = false AND (crawler_data.verdict = 0 OR (crawler_data.verdict = 2 AND crawler_data.autoliked));


CREATE VIEW tcrawler_top AS
SELECT * FROM tcrawler_no_verdict
ORDER BY rating desc, updated_at DESC, id ASC;


CREATE VIEW tcrawler_search AS
SELECT * FROM tcrawler_no_verdict
order by updated_at DESC, id ASC;


CREATE VIEW tcrawler_latest AS
select * from tcrawler_no_verdict
WHERE rating > 0
order by date_trunc('day', GREATEST(ts,updated_at)) DESC, rating DESC, length(bio) DESC, id ASC;


CREATE VIEW tcrawler_random AS
select * from tcrawler_no_verdict
where now() - updated_at <= interval '7 days'
and height between 150 and 170
order by random();

CREATE VIEW tcrawler_today AS
select * from tcrawler_no_verdict
where date_trunc('day', ts) = date_trunc('day',now())
order by rating desc;

CREATE VIEW tcrawler_possible_likes AS
select * from tcrawler_no_verdict
where now() - updated_at <= interval '7 days'  AND avg_batch_rank_idx  > 40
 order by avg_batch_rank_idx DESC, id ASC;

CREATE VIEW tcrawler_autolike AS
select * from tcrawler_no_verdict
where height between 150 and 170 and rating > 0
order by updated_at asc,id ASC;

CREATE VIEW tcrawler_autodislike AS
select * from tcrawler_no_verdict
where length(bio) = 0 and avg_batch_rank_idx < 5
order by updated_at asc,id ASC;

CREATE VIEW tcrawler_near AS
select * from tcrawler_no_verdict where date_trunc('day',now())=date_trunc('day', updated_at)
and distance = 1
order by rating desc;