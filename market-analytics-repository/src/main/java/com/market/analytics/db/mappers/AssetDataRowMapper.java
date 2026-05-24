package com.market.analytics.db.mappers;

import com.market.analytics.domain.AssetData;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

/**
 * @author Michel Barros
 */
@Component
public class AssetDataRowMapper implements RowMapper<AssetData> {

    @Override
    public AssetData mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new AssetData(
                hasColumn(rs, "ticker") ? rs.getString("ticker") : null,
                hasColumn(rs, "provider_ticker") ? rs.getString("provider_ticker") : null,
                hasColumn(rs, "period") ? rs.getString("period") : null,
                hasColumn(rs, "time") ? rs.getObject("time", OffsetDateTime.class) : null,
                hasColumn(rs, "open") ? rs.getDouble("open") : 0.0,
                hasColumn(rs, "high") ? rs.getDouble("high") : 0.0,
                hasColumn(rs, "low") ? rs.getDouble("low") : 0.0,
                hasColumn(rs, "close") ? rs.getDouble("close") : 0.0,
                hasColumn(rs, "volume") ? rs.getDouble("volume") : 0.0,
                hasColumn(rs, "volume_weighted") ? rs.getDouble("volume_weighted") : 0.0,
                hasColumn(rs, "otc") ? rs.getBoolean("otc") : null,
                hasColumn(rs, "rsi") ? rs.getDouble("rsi") : 0.0,
                hasColumn(rs, "dipos") ? rs.getDouble("dipos") : 0.0,
                hasColumn(rs, "dineg") ? rs.getDouble("dineg") : 0.0,
                hasColumn(rs, "adx") ? rs.getDouble("adx") : 0.0,
                hasColumn(rs, "atr") ? rs.getDouble("atr") : 0.0,
                hasColumn(rs, "volatility") ? rs.getDouble("volatility") : 0.0
        );
    }

    private boolean hasColumn(ResultSet rs, String column) {
        try {
            rs.findColumn(column);
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

}
