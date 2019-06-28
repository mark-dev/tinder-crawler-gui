package ru.gotinder.crawler.persistence.util;

import com.djm.tinder.user.Photo;
import com.djm.tinder.user.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import java.sql.Array;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

@AllArgsConstructor
public class CrawlerDataPreparedStatementSetter implements BatchPreparedStatementSetter {
    private ArrayList<User> data;

    private static final ObjectMapper JSON = new ObjectMapper();


    @Override
    public void setValues(PreparedStatement ps, int i) throws SQLException {
        //TODO: manually convert to CrawlerDataDTO before save

        User user = data.get(i);
        ps.setString(1, user.getId());
        ps.setString(2, user.getName());

        Object[] objects = user.getPhotos().stream().map(Photo::getUrl).toArray();
        Array photos = ps.getConnection().createArrayOf("text", objects);
        ps.setArray(3, photos);
        ps.setString(4, user.getBio());
        ps.setInt(5, -1); //рейтинг
        ps.setInt(6, Math.toIntExact(user.getDistance()));
        ps.setDate(7, new Date(user.getBirthDate().getTime()));
        ps.setString(8, user.getContentHash());
        ps.setString(9, user.getsNumber());
        ps.setInt(10, i + 1); //Чтобы отличить значение "0" по умолчанию(не заполненно), от реально значения - первого элемента в массиве (тоже 0)
        ps.setString(11, safeToJson(prepareTeaserMapToSave(user.getTeasers())));


    }

    @Override
    public int getBatchSize() {
        return data.size();
    }

    private String safeToJson(Object o) {
        try {
            return JSON.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private Map<String, String> prepareTeaserMapToSave(Map<String, String> teasers) {
        //Никакой полезной нагрузки инстаграм не несет, можно тут отклонировать объект, чтобы не "портить" исходный
        //Но нам он все равно не нужен, так что просто так.
        teasers.remove("instagram");
        return teasers;
    }
}
