package ru.gotinder.crawler.persistence.util;

import com.djm.tinder.user.Photo;
import com.djm.tinder.user.User;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import java.sql.Array;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CrawlerDataPreparedStatementSetter implements BatchPreparedStatementSetter {
    private ArrayList<User> data;

    @Override
    public void setValues(PreparedStatement ps, int i) throws SQLException {
        //TODO: manually convert to CrawlerDataDTO before save

        User user = data.get(i);
        ps.setString(1, user.getId());
        ps.setString(2, user.getName());

        Object[] objects = user.getPhotos().stream().map(Photo::getUrl).collect(Collectors.toList()).toArray();
        Array photos = ps.getConnection().createArrayOf("text", objects);
        ps.setArray(3, photos);
        ps.setString(4, user.getBio());
        ps.setInt(5, -1);
        ps.setInt(6, Math.toIntExact(user.getDistance()));
        ps.setDate(7, new Date(user.getBirthDate().getTime()));
        ps.setString(8, user.getContentHash());
        ps.setString(9, user.getsNumber());


    }

    @Override
    public int getBatchSize() {
        return data.size();
    }
}
