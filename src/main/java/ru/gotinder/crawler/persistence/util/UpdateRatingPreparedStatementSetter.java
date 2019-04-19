package ru.gotinder.crawler.persistence.util;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

public class UpdateRatingPreparedStatementSetter implements BatchPreparedStatementSetter {
    private Map<String, Integer> data;

    private ArrayList<String> userIds;

    public UpdateRatingPreparedStatementSetter(Map<String, Integer> data) {
        this.data = data;
        this.userIds = new ArrayList<>(data.keySet());
    }

    @Override
    public void setValues(PreparedStatement ps, int i) throws SQLException {
        String userId = userIds.get(i);
        Integer rating = data.get(userId);

        ps.setInt(1, rating);
        ps.setString(2, userId);
    }

    @Override
    public int getBatchSize() {
        return data.size();
    }
}
