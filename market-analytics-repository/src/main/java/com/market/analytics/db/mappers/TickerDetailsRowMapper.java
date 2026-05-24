package com.market.analytics.db.mappers;

import com.market.analytics.domain.Ticker;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Michel Barros
 */
@Component
public class TickerDetailsRowMapper implements RowMapper<Ticker> {

    @Override
    public Ticker mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Ticker(
                hasColumn(rs, "id") ? rs.getInt("id") : 0,
                hasColumn(rs, "ticker") ? rs.getString("ticker") : null,
                hasColumn(rs, "name") ? rs.getString("name") : null,
                hasColumn(rs, "market") ? rs.getString("market") : null,
                hasColumn(rs, "locale") ? rs.getString("locale") : null,
                hasColumn(rs, "primaryExchange") ? rs.getString("primaryExchange") : null,
                hasColumn(rs, "type") ? rs.getString("type") : null,
                hasColumn(rs, "active") ? rs.getBoolean("active") : null,
                hasColumn(rs, "currencyName") ? rs.getString("currencyName") : null,
                hasColumn(rs, "cik") ? rs.getInt("cik") : 0,
                hasColumn(rs, "compositeFigi") ? rs.getString("compositeFigi") : null,
                hasColumn(rs, "shareClassFigi") ? rs.getString("shareClassFigi") : null,
                hasColumn(rs, "marketCap") ? rs.getBigDecimal("marketCap") : null,
                hasColumn(rs, "description") ? rs.getString("description") : null,
                hasColumn(rs, "sicCode") ? rs.getString("sicCode") : null,
                hasColumn(rs, "sicDescription") ? rs.getString("sicDescription") : null,
                hasColumn(rs, "tickerRoot") ? rs.getString("tickerRoot") : null,
                hasColumn(rs, "homepageUrl") ? rs.getString("homepageUrl") : null,
                hasColumn(rs, "totalEmployees") ? rs.getInt("totalEmployees") : 0,
                hasColumn(rs, "listDate") ? rs.getDate("listDate") : null,
                hasColumn(rs, "logoUrl") ? rs.getString("logoUrl") : null,
                hasColumn(rs, "iconUrl") ? rs.getString("iconUrl") : null,
                hasColumn(rs, "shareClassSharesOutstanding") ? rs.getLong("shareClassSharesOutstanding") : null,
                hasColumn(rs, "weightedSharesOutstanding") ? rs.getLong("weightedSharesOutstanding") : null,
                hasColumn(rs, "roundLot") ? rs.getInt("roundLot") : 0
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
