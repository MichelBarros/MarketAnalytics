package com.market.analytics.service;

import com.market.analytics.domain.AssetData;
import com.market.analytics.domain.AssetDataParams;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @author Michel Barros
 */
public interface DataHistoryService {

    List<AssetData> getAssetDataHistory(AssetDataParams pagination);

    String uploadAssetDataHistory(MultipartFile file) throws IOException;

    String uploadMultipleAssetsDataHistory(List<MultipartFile> files);

}
