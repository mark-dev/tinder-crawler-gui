package ru.gotinder.crawler.persistence;

import com.djm.tinder.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import ru.gotinder.crawler.persistence.dto.CrawlerDataDTO;
import ru.gotinder.crawler.persistence.dto.EnrichDataDTO;
import ru.gotinder.crawler.persistence.dto.VerdictEnum;
import ru.gotinder.crawler.persistence.util.CrawlerDataPreparedStatementSetter;
import ru.gotinder.crawler.persistence.util.SQLHelper;
import ru.gotinder.crawler.persistence.util.UpdateRatingPreparedStatementSetter;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.gotinder.crawler.persistence.util.SQLHelper.LOAD_FOR_IMAGE_CACHE;
import static ru.gotinder.crawler.persistence.util.SQLHelper.SET_VERDICT_SYNC_TIME;

//TODO: Spring Data + Specifications API
//TODO: Можно отрефакторить DAO, ввести новое понятие - срез данных(enum: LATEST,NEAR,FOR_SYNC,TOP etc), и возвращать в одном объекте сразу список данных и count
//TODO: Соответственно для каждого среза данных нужно будет сконфигурировать запросы на выгрузку и count
//TODO: Это уберет дублирование кода, в местах, откуда мы вызываем данные методы (да и тут тоже уберет - по сути везде одно и тоже, только запрос меняется)
@Service
public class CrawlerDAO {


    @Autowired
    private JdbcTemplate template;


    @Autowired
    private ObjectMapper om;

    private NamedParameterJdbcTemplate namedJdbcTemplate;

    @PostConstruct
    public void init() {
        namedJdbcTemplate = new NamedParameterJdbcTemplate(template.getDataSource());
    }

    private static final VerdictEnum[] VERDICT_ENUMS = VerdictEnum.values();

    private RowMapper<CrawlerDataDTO> rowMapper = (rs, rowNum) -> {
        String[] photos = (String[]) rs.getArray("photos").getArray();
        CrawlerDataDTO dto = new CrawlerDataDTO();
        dto.setId(rs.getString("id"));
        dto.setName(rs.getString("name"));
        dto.setPhoto(Stream.of(photos).collect(Collectors.toCollection(ArrayList::new)));
        dto.setBio(rs.getString("bio"));
        dto.setRating(rs.getInt("rating"));
        dto.setDistance(rs.getInt("distance"));
        dto.setBirthday(rs.getDate("birthday").toLocalDate());
        dto.setRecsCount(rs.getInt("avg_batch_rank_idx"));
        dto.setAvgBatchRank(rs.getInt("avg_batch_rank"));
        String teasersStr = rs.getString("teasers");
        Map<String, String> teasers;
        try {
            teasers = om.reader().forType(Map.class).readValue(teasersStr);
        } catch (IOException e) {
            teasers = Collections.emptyMap();
        }
        dto.setTeasers(teasers);
        dto.setTs(rs.getTimestamp("ts").toInstant());
        dto.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
        dto.setVerdict(VERDICT_ENUMS[rs.getInt("verdict")]);
        Timestamp verdictSyncTs = rs.getTimestamp("verdict_sync_at");
        dto.setVedictSync(!rs.wasNull());


        dto.setSNumber(rs.getString("s_number"));
        dto.setContentHash(rs.getString("content_hash"));

        return dto;
    };

    public void saveBatch(Collection<User> list) {
        ArrayList<User> asArrayList = null;
        if (list instanceof ArrayList) {
            asArrayList = (ArrayList<User>) list;
        } else {
            asArrayList = new ArrayList<>(list);

        }
        template.batchUpdate(SQLHelper.INSERT_CRAWLER_DATA, new CrawlerDataPreparedStatementSetter(asArrayList));
    }

    public List<CrawlerDataDTO> loadSuperLikeCandidates(int limit){
        return template.query(SQLHelper.LOAD_AUTO_SUPERLIKE_TARGETS, rowMapper, limit);
    }

    public List<CrawlerDataDTO> loadEnrichRequired(int limit) {
        return template.query(SQLHelper.LOAD_ENRICH_REQUIRED, rowMapper, limit);
    }

    public List<CrawlerDataDTO> topByRating(int page, int size) {
        return template.query(SQLHelper.TOP_BY_RATING, rowMapper, size, page * size);
    }

    public List<CrawlerDataDTO> search(String search, int page, int size) {
        return template.query(SQLHelper.SEARCH, rowMapper, search, size, page * size);
    }

    public List<CrawlerDataDTO> loadRandom(int size) {
        return template.query(SQLHelper.LOAD_RANDOM, rowMapper, size);
    }

    public List<CrawlerDataDTO> loadLatest(int page, int size) {
        return template.query(SQLHelper.LOAD_LATEST, rowMapper, size, page * size);
    }

    public List<CrawlerDataDTO> loadVerdictedButNotSynced(VerdictEnum verdictBoundInclusive, int page, int size) {
        return template.query(SQLHelper.LOAD_VERDICTED_BUT_NOT_SYNCED, rowMapper, verdictBoundInclusive.ordinal(), size, page * size);
    }

    public List<CrawlerDataDTO> loadPossibleLikes(int page, int size) {
        return template.query(SQLHelper.POSSIBLE_LIKES, rowMapper, size, page * size);
    }

    public List<CrawlerDataDTO> loadMissInImageCache(int limit) {
        return template.query(LOAD_FOR_IMAGE_CACHE, rowMapper, limit);
    }

    public List<CrawlerDataDTO> loadAutoLikeCandidates() {
        return loadByQuery(SQLHelper.LOAD_AUTOLIKE_CANDIDATES);
    }

    public List<CrawlerDataDTO> loadAutoDislikeCandidates() {
        return loadByQuery(SQLHelper.LOAD_DISLIKE_CANDIDATES);
    }

    public List<CrawlerDataDTO> loadTodays(int page, int size) {
        return loadByQuery(SQLHelper.LOAD_TODAYS, size, page * size);
    }

    public Optional<CrawlerDataDTO> byId(String id) {
        List<CrawlerDataDTO> res = template.query("SELECT * from crawler_data WHERE id = ?", rowMapper, id);
        if (res.isEmpty()) return Optional.empty();
        return Optional.of(res.get(0));
    }

    public List<CrawlerDataDTO> loadNear(int page, int size) {
        return loadByQuery(SQLHelper.LOAD_NEAR, size, page * size);
    }

    public Integer countTodays() {
        return template.queryForObject(SQLHelper.COUNT_TODAYS, Integer.class);
    }

    public Integer countNear() {
        return template.queryForObject(SQLHelper.COUNT_NEAR, Integer.class);
    }

    public Integer countSearch(String search) {
        return template.queryForObject(SQLHelper.COUNT_SEARCH, Integer.class, search);
    }

    public Integer countTopByRating() {
        return template.queryForObject(SQLHelper.COUNT_TOP_BY_RATING, Integer.class);
    }

    public Integer countLatest() {
        return template.queryForObject(SQLHelper.COUNT_LATEST, Integer.class);
    }

    public Integer countVerdicted() {
        return template.queryForObject(SQLHelper.COUNT_VERDICTED_BUT_NOT_SYNCED, Integer.class);
    }

    public Integer countPossibleLikes() {
        return template.queryForObject(SQLHelper.COUNT_POSSIBLE_LIKES, Integer.class);
    }

    public void enrichData(Map<String, EnrichDataDTO> ratingMap) {
        template.batchUpdate(SQLHelper.ENRICH_DATA, new UpdateRatingPreparedStatementSetter(ratingMap));
    }

    public void dropEnrichFlag() {
        template.update(SQLHelper.DROP_ENRICH_FLAG);
    }

    public Integer countEnrichRequired() {
        return template.queryForObject(SQLHelper.COUNT_ENRICH_REQUIRED, Integer.class);
    }

    public void setVerdict(String id, VerdictEnum v, boolean autoLiked) {
        template.update(SQLHelper.SET_VERDICT, v.ordinal(), autoLiked, id);
    }

    public void updateVerdictTimestamp(String id) {
        template.update(SET_VERDICT_SYNC_TIME, id);
    }


    public void updateImageCacheDownloadedFlag(List<String> userIds) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("ids", userIds);


        namedJdbcTemplate.update("UPDATE crawler_data SET img_cached = true WHERE id in (:ids)", parameters);
    }


    public void hide(String id) {
        template.update(SQLHelper.SET_HIDDEN, id);
    }

    private List<CrawlerDataDTO> loadByQuery(String query, Object... args) {
        return template.query(query, rowMapper, args);
    }
}
