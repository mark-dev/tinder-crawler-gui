update crawler_data
set rating = -1;

select count(*)
from crawler_data
where rating = -1;


select rating, count(*)
from crawler_data
WHERE bio <> ''
group by rating
order by rating desc;

-- Top by rating
select id,rating,bio,photos
from crawler_data
order by rating desc, length(bio) DESC;

--Latest data
select *
from crawler_data
order by date_trunc('day', ts) DESC,ts DESC,rating desc, length(bio) DESC;