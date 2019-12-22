package ru.gotinder.crawler.persistence.util;

//TODO: Уже начинает попахивать спринг датой. А не простым приложением с 1 запросом.
public interface SQLHelper {
    String INSERT_CRAWLER_DATA = "INSERT INTO crawler_data(id,name,photos,bio,longest_bio,rating,distance,birthday,content_hash,s_number,avg_batch_rank,teasers,avg_batch_rank_idx) " +
            " VALUES (?,?,?,?,?,?,?,?,?,?,?,?::jsonb,1) ON CONFLICT(id) DO UPDATE " +
            "SET avg_batch_rank  = CASE " +
            //https://stackoverflow.com/questions/11074665/calculate-cumulative-average-mean
            "                      WHEN crawler_data.avg_batch_rank_idx = 0 THEN EXCLUDED.avg_batch_rank" +
            "                      ELSE ((crawler_data.avg_batch_rank_idx / (crawler_data.avg_batch_rank_idx + 1)::float) * crawler_data.avg_batch_rank) + (EXCLUDED.avg_batch_rank / (crawler_data.avg_batch_rank_idx + 1)::float) END," +
            "avg_batch_rank_idx = crawler_data.avg_batch_rank_idx + 1, img_cached = false, teasers = (EXCLUDED.teasers)::jsonb, bio = EXCLUDED.bio, longest_bio = (CASE WHEN length(EXCLUDED.bio) > length(crawler_data.longest_bio) THEN EXCLUDED.bio ELSE crawler_data.longest_bio END), enrich_required = true, rating = EXCLUDED.rating, birthday = EXCLUDED.birthday,name = EXCLUDED.name, content_hash = EXCLUDED.content_hash, s_number = EXCLUDED.s_number, photos = EXCLUDED.photos, distance = EXCLUDED.distance,updated_at = now()";
    String ENRICH_DATA = "UPDATE crawler_data SET rating = ?, height = ?, enrich_required = false WHERE id = ?";
    String SET_HIDDEN = "UPDATE crawler_data SET hidden = true WHERE id = ?";

    String DROP_ENRICH_FLAG = "UPDATE crawler_data SET enrich_required = true";

    String SET_VERDICT = "UPDATE crawler_data SET verdict = ?, verdicted_at = now(), autoliked = ?  WHERE id = ?";
    String SET_VERDICT_SYNC_TIME = "UPDATE crawler_data SET verdict_sync_at = now() WHERE id = ?";

    String LOAD_FOR_IMAGE_CACHE = "SELECT * FROM crawler_data WHERE img_cached = false LIMIT ?";

    String COUNT_ENRICH_REQUIRED = "select count(*) from crawler_data where enrich_required;";
    String LOAD_ENRICH_REQUIRED = "SELECT * FROM crawler_data WHERE enrich_required LIMIT ?";

    String TOP_BY_RATING = "SELECT * FROM tcrawler_top LIMIT ? OFFSET ?";
    String COUNT_TOP_BY_RATING = "SELECT count(*) FROM tcrawler_top";

    String SEARCH = "SELECT * FROM tcrawler_search WHERE (lower(bio) like '%'|| lower(?) || '%') LIMIT ? OFFSET ?";
    String COUNT_SEARCH = "SELECT count(*) FROM tcrawler_search WHERE (lower(bio) like '%'|| lower(?) || '%')";

    String LOAD_LATEST = "SELECT * FROM tcrawler_latest LIMIT ? OFFSET ?";
    String COUNT_LATEST = "SELECT count(*) FROM tcrawler_latest";

    String LOAD_VERDICTED_BUT_NOT_SYNCED = "SELECT * FROM crawler_data WHERE hidden = FALSE AND verdict > 0 AND verdict <= ? AND verdict_sync_at is null ORDER BY verdict DESC, verdicted_at DESC, id ASC LIMIT ? OFFSET ?";
    String COUNT_VERDICTED_BUT_NOT_SYNCED = "SELECT count(*) FROM crawler_data WHERE hidden = FALSE AND verdict <> 0 AND verdict_sync_at is null";

    String LOAD_RANDOM = "select * from tcrawler_random limit ?";

    String LOAD_TODAYS = "SELECT * from tcrawler_today LIMIT ? OFFSET ?";
    String COUNT_TODAYS = "SELECT count(*) FROM tcrawler_today";

    //TODO: Условие в запросе(case) от настроек профиля тиндер должно зависеть
    //TODO: Т.е. предполагаем что если нам дают людей с дистанцией больше чем у нас в настройках, возможно это лайк.
    //TODO: Ну и соответственно учитываем насколько "настойчиво" делает это тиндер.
//    String POSSIBLE_LIKES = "select * from crawler_data where verdict_sync_at is null order by (recs_duplicate_count*recs_duplicate_count*(CASE WHEN distance <= 10 THEN 1 WHEN distance > 10 THEN 10 END)) DESC LIMIT ? OFFSET ?";
    String POSSIBLE_LIKES = "select * from tcrawler_possible_likes LIMIT ? OFFSET ?";
    String COUNT_POSSIBLE_LIKES = "select count(*) from tcrawler_possible_likes";
//    String COUNT_POSSIBLE_LIKES = "select count(*) from crawler_data where verdict_sync_at is null";

    String LOAD_AUTO_SUPERLIKE_TARGETS = "select * from crawler_data where verdict = 3 and verdict_sync_at is null order by verdicted_at desc limit ?";
    //Лайкаем тех, кто подходит по росту и имеет не нулевой рейтинг
    String LOAD_AUTOLIKE_CANDIDATES = "select * from tcrawler_autolike LIMIT ? OFFSET ?";
    //Дизлайкаем тех, кто с пустым описанием, и кто показывался меньше 5 раз
    String LOAD_DISLIKE_CANDIDATES = "SELECT * FROM tcrawler_autodislike LIMIT ? OFFSET ?";

    String LOAD_NEAR = "select * from tcrawler_near LIMIT ? OFFSET ?";
    String COUNT_NEAR = "select count(*) from tcrawler_near";

}
