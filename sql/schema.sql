create table if not exists crawler_data
(
	id text not null
		constraint crawler_data_pk
			primary key,
	name text not null,
	photos text[] not null,
	bio text,
	ts timestamp default now(),
	rating integer default 0 not null,
	distance integer default 0 not null,
	birthday timestamp not null,
	content_hash text not null,
	s_number text,
	updated_at timestamp default now() not null,
	verdict integer default 0 not null,
	verdict_sync_at timestamp
);

create unique index if not exists crawler_data_id_uindex
	on crawler_data (id);

create index if not exists crawler_data_rating_index
	on crawler_data (rating);

create index if not exists crawler_data_ts_index
	on crawler_data (ts);

CREATE EXTENSION pg_trgm;

CREATE index trgm_idx_crawler_data_bio
	ON crawler_data
		USING gin (lower(bio) gin_trgm_ops);