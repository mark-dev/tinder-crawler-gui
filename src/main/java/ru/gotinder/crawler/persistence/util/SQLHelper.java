package ru.gotinder.crawler.persistence.util;

//TODO: Уже начинает попахивать спринг датой. А не простым приложением с 1 запросом.
public interface SQLHelper {
    String INSERT_CRAWLER_DATA = "INSERT INTO crawler_data(id,name,photos,bio,rating,distance,birthday,content_hash,s_number,avg_batch_rank,avg_batch_rank_idx) " +
            " VALUES (?,?,?,?,?,?,?,?,?,?,1) ON CONFLICT(id) DO UPDATE " +
            "SET avg_batch_rank  = CASE " +
            //https://stackoverflow.com/questions/11074665/calculate-cumulative-average-mean
            "                      WHEN crawler_data.avg_batch_rank_idx = 0 THEN EXCLUDED.avg_batch_rank" +
            "                      ELSE ((crawler_data.avg_batch_rank_idx / (crawler_data.avg_batch_rank_idx + 1)::float) * crawler_data.avg_batch_rank) + (EXCLUDED.avg_batch_rank / (crawler_data.avg_batch_rank_idx + 1)::float) END," +
            "avg_batch_rank_idx = crawler_data.avg_batch_rank_idx + 1, bio = EXCLUDED.bio,recs_duplicate_count = crawler_data.recs_duplicate_count + 1, rating = EXCLUDED.rating, birthday = EXCLUDED.birthday,name = EXCLUDED.name, content_hash = EXCLUDED.content_hash, s_number = EXCLUDED.s_number, photos = EXCLUDED.photos, distance = EXCLUDED.distance,updated_at = now()";
    String UPDATE_RATING = "UPDATE crawler_data SET rating = ? WHERE id = ?";
    String SET_HIDDEN = "UPDATE crawler_data SET hidden = true WHERE id = ?";

    String DROP_RATING = "UPDATE crawler_data SET rating = -1";

    String SET_VERDICT = "UPDATE crawler_data SET verdict = ? WHERE id = ?";
    String SET_VERDICT_SYNC_TIME = "UPDATE crawler_data SET verdict_sync_at = now() WHERE id = ?";


    String COUNT_UNRATED = "select count(*) from crawler_data where rating = -1;";
    String LOAD_UNRATED = "SELECT * FROM crawler_data WHERE rating = -1 LIMIT 500";

    String TOP_BY_RATING = "SELECT * FROM crawler_data WHERE hidden = FALSE AND verdict = 0 AND (length(?) = 0 OR lower(bio) like '%'|| lower(?) || '%') AND verdict_sync_at is null ORDER BY rating desc, updated_at DESC, id ASC LIMIT ? OFFSET ?";
    String COUNT_TOP_BY_RATING = "SELECT count(*) FROM crawler_data WHERE hidden = FALSE AND verdict = 0 AND (length(?) = 0 OR bio like '%'|| lower(?) || '%') AND verdict_sync_at is null";

    String COUNT_LATEST = "SELECT count(*) from crawler_data WHERE rating > 0 AND hidden = FALSE AND verdict = 0 AND verdict_sync_at is null";
    String LOAD_LATEST = "select * from crawler_data WHERE rating > 0 AND hidden = FALSE AND verdict = 0 AND verdict_sync_at is null " +
            "order by date_trunc('day', GREATEST(ts,updated_at)) DESC, rating DESC, length(bio) DESC, id ASC " +
            "LIMIT ? OFFSET ?";

    String LOAD_VERDICTED_BUT_NOT_SYNCED = "SELECT * FROM crawler_data WHERE hidden = FALSE AND verdict <> 0 AND verdict_sync_at is null ORDER BY verdict DESC, id ASC LIMIT ? OFFSET ?";
    String COUNT_VERDICTED_BUT_NOT_SYNCED = "SELECT count(*) FROM crawler_data WHERE hidden = FALSE AND verdict <> 0 AND verdict_sync_at is null";

    String LOAD_RANDOM = "select * from crawler_data where  hidden = FALSE AND rating > 0 and verdict_sync_at is null order by random() limit ?";

    //TODO: Условие в запросе(case) от настроек профиля тиндер должно зависеть
    //TODO: Т.е. предполагаем что если нам дают людей с дистанцией больше чем у нас в настройках, возможно это лайк.
    //TODO: Ну и соответственно учитываем насколько "настойчиво" делает это тиндер.
//    String POSSIBLE_LIKES = "select * from crawler_data where verdict_sync_at is null order by (recs_duplicate_count*recs_duplicate_count*(CASE WHEN distance <= 10 THEN 1 WHEN distance > 10 THEN 10 END)) DESC LIMIT ? OFFSET ?";
    String POSSIBLE_LIKES = "select * from crawler_data where verdict = 0 AND verdict_sync_at is null AND avg_batch_rank_idx  > 10 order by avg_batch_rank ASC, id ASC LIMIT ? OFFSET ?";
    String COUNT_POSSIBLE_LIKES = "select count(*) from crawler_data where verdict = 0 AND verdict_sync_at is null AND avg_batch_rank_idx  > 10";
//    String COUNT_POSSIBLE_LIKES = "select count(*) from crawler_data where verdict_sync_at is null";
}
