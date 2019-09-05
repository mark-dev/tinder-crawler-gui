select count(*)
from crawler_data
where length(bio) > 100;
select count(*)
from crawler_data;

select *
from crawler_data
WHERE verdict = 0
  AND verdict_sync_at is null
order by date_trunc('day', GREATEST(ts, updated_at)) DESC, rating DESC, recs_duplicate_count DESC, length(bio) DESC


select *
from crawler_data
where id = '5be7279600e3380454a6e621'

select *
from crawler_data
order by recs_duplicate_count desc
limit 5;


select rating,
       name,
       id,
       recs_duplicate_count,
       distance,
       recs_duplicate_count * recs_duplicate_count * (CASE WHEN distance < 10 THEN 1 WHEN distance > 10 THEN 10 END)
from crawler_data
where verdict_sync_at is null
order by (recs_duplicate_count * recs_duplicate_count *
          (CASE WHEN distance =< 10 THEN 1 WHEN distance > 10 THEN 10 END)) DESC;


select rating, name, id, recs_duplicate_count
from crawler_data
where id = '5bbcb2cfd7a27e2d5aecc4fa'


select *
from crawler_data
where id = '5caf93a9f3f88e16005fab71'

select *
from crawler_data
order by rating desc
limit 1;

select * from crawler_data where rating > 0 and verdict_sync_at is null order by random() limit 5
update crawler_data
SET avg_batch_rank     = CASE
                             WHEN avg_batch_rank_idx = 0 THEN ? --СРазу текущее значение ставим
                             ELSE
                                     ((avg_batch_rank_idx / (avg_batch_rank_idx + 1)::float) * avg_batch_rank) + (? / (avg_batch_rank_idx + 1)::float)
                         END,
    avg_batch_rank_idx = avg_batch_rank_idx + 1
WHERE id = '5caf93a9f3f88e16005fab71';

update crawler_data set avg_batch_rank = 0, avg_batch_rank_idx = 0 WHERE id = '5caf93a9f3f88e16005fab71';

select  ((avg_batch_rank_idx / (avg_batch_rank_idx + 1)::float)) as new_rank,
       1/2::float as ddd,
       avg_batch_rank_idx,
       avg_batch_rank
FROM crawler_data where id = '5caf93a9f3f88e16005fab71'

select id,avg_batch_rank,avg_batch_rank_idx,updated_at
from crawler_data
where avg_batch_rank_idx  > 10 and date_trunc('day', updated_at) = date_trunc('day',now())
order by  avg_batch_rank ASC limit 100

select id,bio,avg_batch_rank_idx, avg_batch_rank
from crawler_data
where length(bio) > 0 and avg_batch_rank_idx > 2
order by avg_batch_rank_idx desc

select max(avg_batch_rank) FROM crawler_data;
select sum(recs_duplicate_count) from crawler_data;

select id,bio,rating, teasers from crawler_data where name like 'Liza'


select id,bio,rating, teasers from crawler_data
where teasers ->> 'school' like '%МИИТ%' OR
      teasers ->> 'school' like '%РУТ%' OR
      teasers ->> 'school' like '%МГУПС%' OR
      teasers ->> 'school' like '%Railway%'

select id,rating,teasers from crawler_data
where
      lower(teasers ->> 'job') like '%yandex%' OR
      lower(teasers ->> 'school') like '%yandex%' OR
      lower(teasers ->> 'position') like '%yandex%' OR lower(teasers ->> 'position') like '%яндекс%' or
      lower(teasers ->> 'jobPosition') like '%yandex%'  OR  lower(teasers ->> 'jobPosition') like '%яндекс%'


select id,bio,teasers,rating from crawler_data where lower(teasers::text) like '%yandex%'
-- lower(bio) like '%netflix%'

INSERT INTO crawler_data(id,avg_batch_rank,avg_batch_rank_idx) VALUES ('aaaa',0,avg_batch_rank_idx+1)


select * from crawler_data where distance = 1 order by date_trunc('day',updated_at) desc,rating desc

select id,bio,recs_duplicate_count,birthday
from crawler_data
where length(bio) > 0 and verdict = 0
order by recs_duplicate_count desc


select * from crawler_data
where verdict_sync_at is null AND avg_batch_rank_idx  > 10
order by avg_batch_rank ASC, id
LIMIT 5 OFFSET 0


select age(now() at time zone 'utc',updated_at) from crawler_data
where verdict = 0 AND verdict_sync_at is null AND avg_batch_rank_idx  > 10  and date_trunc('day', updated_at) = date_trunc('day',now())
order by avg_batch_rank ASC, id


select length(bio),count(*) from crawler_data group by length(bio) order by length(bio) asc

select id,bio,rating from crawler_data where length(bio) = 499 order by rating desc limit 50

select id,bio,height,rating from (
select crawler_data.*,
       (select coalesce(max(el),-1) as height  from (select (rm::int[][])[1] as el from regexp_matches(bio,'([0-9]{3})', 'g') as rm) as match_sq)
from crawler_data) as crd_with_height
where height > 0 and height <= 170
order by rating desc

select arr[3][1] from (
                       select ARRAY(select max from regexp_matches(
                                                   '123 abc 232 12434',
                                                   '([0-9]{3})', 'g')) as arr
                   ) tt


alter table crawler_data add column enrich_required boolean default true not null;
alter table crawler_data add column height integer default -1 not null;


select count(*) from crawler_data where height > 0 and height <= 175;
select count(*) from crawler_data where height > 0;


select * from crawler_data where lower(bio) like '%шахматы%'
select count(*) from crawler_data where lower(bio) like '%рост%' and height = -1

select id,updated_at,height,rating,bio from crawler_data where now() - updated_at <= interval '7 days'
and height between 150 and 170
order by rating desc

select height,count(*) from crawler_data group by height


select (select count(*) from crawler_data where height = -1 and length(bio) > 0), (select count(*) from crawler_data where height > 0)


select id,bio,height from crawler_data order by height desc limit 10;

select (case WHEN length('aabc') > length('bbb') THEN 'aabc' ELSE 'bbb' END);

SELECT * FROM crawler_data WHERE img_cached = false LIMIT ?



select count(*) from crawler_data where not img_cached


select char(11 ^ ascii('t'))

select * from crawler_data where height between 150 and 170 and verdict = 0 and rating > 0
select * from crawler_data where height between 150 and 170 and verdict = 0 and rating > 0

select * from crawler_data where date_trunc('day', ts) = date_trunc('day',now()) and verdict = 0 order by  rating desc limit ? OFFSET ?


select * from crawler_data where date_trunc('day',now())=date_trunc('day', updated_at) and verdict = 0 and distance = 1 order by rating desc
select count(*) from crawler_data where date_trunc('day',now())=date_trunc('day', updated_at) and verdict = 0 and distance = 1


select * from crawler_data where verdict = 3 and verdict_sync_at is not null order by verdict_sync_at desc limit 5