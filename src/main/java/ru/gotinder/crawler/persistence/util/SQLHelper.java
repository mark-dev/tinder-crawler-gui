package ru.gotinder.crawler.persistence.util;

public interface SQLHelper {
    String INSERT_CRAWLER_DATA = "INSERT INTO crawler_data(id,name,photos,bio,rating,distance,birthday,content_hash,s_number) " +
            " VALUES (?,?,?,?,?,?,?,?,?) ON CONFLICT(id) DO UPDATE SET bio = EXCLUDED.bio,rating = EXCLUDED.rating, birthday = EXCLUDED.birthday,name = EXCLUDED.name, content_hash = EXCLUDED.content_hash, s_number = EXCLUDED.s_number, photos = EXCLUDED.photos, distance = EXCLUDED.distance,updated_at = now()";
    String UPDATE_RATING = "UPDATE crawler_data SET rating = ? WHERE id = ?";

    String SET_VERDICT = "UPDATE crawler_data SET verdict = ? WHERE id = ?";
    String SET_VERDICT_SYNC_TIME = "UPDATE crawler_data SET verdict_sync_at = now() WHERE id = ?";


    String COUNT_UNRATED = "select count(*) from crawler_data where rating = -1;";
    String LOAD_UNRATED = "SELECT * FROM crawler_data WHERE rating = -1 LIMIT 500";

    String LOAD_BY_RATING = "SELECT * FROM crawler_data WHERE verdict = 0 AND verdict_sync_at is null ORDER BY rating desc, length(bio) DESC LIMIT ? OFFSET ?";
    String COUNT_BY_RATING = "SELECT count(*) FROM crawler_data WHERE verdict = 0 AND verdict_sync_at is null";

    String COUNT_LATEST = "SELECT count(*) from crawler_data WHERE verdict = 0 AND verdict_sync_at is null";
    String LOAD_LATEST = "select * from crawler_data " +
            "WHERE verdict = 0 AND verdict_sync_at is null " +
            "order by date_trunc('day', ts) DESC, rating DESC, ts DESC, length(bio) DESC " +
            "LIMIT ? OFFSET ?";

    String LOAD_VERDICTED_BUT_NOT_SYNCED = "SELECT * FROM crawler_data WHERE verdict <> 0 AND verdict_sync_at is null ORDER BY verdict DESC LIMIT ? OFFSET ?";
    String COUNT_VERDICTED_BUT_NOT_SYNCED = "SELECT count(*) FROM crawler_data WHERE verdict <> 0 AND verdict_sync_at is null";
}
