package ru.gotinder.crawler.persistence;

import com.djm.tinder.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import ru.gotinder.crawler.persistence.dto.CrawlerDataDTO;
import ru.gotinder.crawler.persistence.dto.VerdictEnum;
import ru.gotinder.crawler.persistence.util.CrawlerDataPreparedStatementSetter;
import ru.gotinder.crawler.persistence.util.SQLHelper;
import ru.gotinder.crawler.persistence.util.UpdateRatingPreparedStatementSetter;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.gotinder.crawler.persistence.util.SQLHelper.SET_VERDICT_SYNC_TIME;

//TODO: Spring Data + Specifications API
@Service
public class CrawlerDAO {
    @Autowired
    private JdbcTemplate template;

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
        dto.setRecsDuplicateCount(rs.getInt("recs_duplicate_count"));

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

    public List<CrawlerDataDTO> loadUnrated() {
        return template.query(SQLHelper.LOAD_UNRATED, rowMapper);
    }

    public List<CrawlerDataDTO> topByRating(String search, int page, int size) {
        return template.query(SQLHelper.LOAD_BY_RATING, rowMapper, search, search, size, page * size);
    }

    public Integer countTopByRating(String search) {
        return template.queryForObject(SQLHelper.COUNT_BY_RATING, Integer.class, search, search);
    }

    public List<CrawlerDataDTO> loadLatest(int page, int size) {
        return template.query(SQLHelper.LOAD_LATEST, rowMapper, size, page * size);
    }

    public Integer countLatest() {
        return template.queryForObject(SQLHelper.COUNT_LATEST, Integer.class);
    }

    public List<CrawlerDataDTO> loadVerdictedButNotSynced(int page, int size) {
        return template.query(SQLHelper.LOAD_VERDICTED_BUT_NOT_SYNCED, rowMapper, size, page * size);
    }

    public Integer countVerdicted() {
        return template.queryForObject(SQLHelper.COUNT_VERDICTED_BUT_NOT_SYNCED, Integer.class);
    }

    public List<CrawlerDataDTO> loadRecsDuplicated(int page, int size) {
        return template.query(SQLHelper.LOAD_RECS_DUPLICATED, rowMapper, size, page * size);
    }

    public Integer countRecsDuplicated() {
        return template.queryForObject(SQLHelper.COUNT_RECS_DUPLICATED, Integer.class);
    }

    public void updateRating(Map<String, Integer> ratingMap) {
        template.batchUpdate(SQLHelper.UPDATE_RATING, new UpdateRatingPreparedStatementSetter(ratingMap));
    }

    public void dropRating() {
        template.update(SQLHelper.DROP_RATING);
    }

    public Integer countUnrated() {
        return template.queryForObject(SQLHelper.COUNT_UNRATED, Integer.class);
    }

    public void setVerdict(String id, VerdictEnum v) {
        template.update(SQLHelper.SET_VERDICT, v.ordinal(), id);
    }

    public void updateVerdictTimestamp(String id) {
        template.update(SET_VERDICT_SYNC_TIME, id);
    }


    public Optional<CrawlerDataDTO> byId(String id) {
        List<CrawlerDataDTO> res = template.query("SELECT * from crawler_data WHERE id = ?", rowMapper, id);
        if (res.isEmpty()) return Optional.empty();
        return Optional.of(res.get(0));
    }
}
