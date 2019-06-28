package ru.gotinder.crawler.persistence.util;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import ru.gotinder.crawler.persistence.dto.EnrichDataDTO;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

public class UpdateRatingPreparedStatementSetter implements BatchPreparedStatementSetter {
    private Map<String, EnrichDataDTO> data;

    private ArrayList<String> userIds;

    public UpdateRatingPreparedStatementSetter(Map<String, EnrichDataDTO> data) {
        this.data = data;
        this.userIds = new ArrayList<>(data.keySet());
    }

    @Override
    public void setValues(PreparedStatement ps, int i) throws SQLException {
        String userId = userIds.get(i);
        EnrichDataDTO dto = data.get(userId);

        ps.setInt(1, dto.getRating());
        ps.setInt(2, dto.getHeight());
        ps.setString(3, userId);
    }

    @Override
    public int getBatchSize() {
        return data.size();
    }
}
