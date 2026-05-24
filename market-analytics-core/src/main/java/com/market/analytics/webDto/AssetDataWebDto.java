package com.market.analytics.webDto;

import com.market.analytics.domain.AssetData;

import java.util.List;

/**
 * @author Michel Barros
 */
public record AssetDataWebDto(
        List<AssetData> history
        ) {
}
