package com.market.analytics.service;

import com.market.analytics.db.DataHistoryRepository;
import com.market.analytics.domain.AssetData;
import com.market.analytics.domain.AssetDataParams;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michel Barros
 */
@Slf4j
@Service
public class DataHistoryServiceImpl implements DataHistoryService {

    private static final String DATA_SEPARATOR = ",";
    private static final String BASE_CURRENCY_DATA = ".US";

    private final DataHistoryRepository dataHistoryRepository;

    public DataHistoryServiceImpl(DataHistoryRepository dataHistoryRepository) {
        this.dataHistoryRepository = dataHistoryRepository;
    }

    @Override
    public List<AssetData> getAssetDataHistory(AssetDataParams pagination) {
        return this.dataHistoryRepository.getAssetData(pagination);
    }

    @Override
    public String uploadAssetDataHistory(MultipartFile file) throws IOException {
        List<AssetData> data = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
        br.readLine();
        buildBufferedReaderLines(br, data);
        this.dataHistoryRepository.saveAssetDataHistory(data);

        return file.getOriginalFilename();
    }

    @Override
    public String uploadMultipleAssetsDataHistory(List<MultipartFile> files)  {
        files.forEach(file -> {
            List<AssetData> data = new ArrayList<>();
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
                br.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            buildBufferedReaderLines(br, data);
            this.dataHistoryRepository.saveAssetDataHistory(data);
        });
        return "ok";
    }

    private void buildBufferedReaderLines(BufferedReader br, List<AssetData> data) {
        br.lines()
                .forEach(line -> {
                    List<String> dataFields = List.of(line.split(DATA_SEPARATOR));
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    data.add(
                            new AssetData(
                                    dataFields.get(0).replace(BASE_CURRENCY_DATA, ""),
                                    dataFields.get(1),
                                    dataFields.get(2),
                                    OffsetDateTime.parse(dataFields.get(3), dateTimeFormatter),
                                    Double.parseDouble(dataFields.get(4)),
                                    Double.parseDouble(dataFields.get(5)),
                                    Double.parseDouble(dataFields.get(6)),
                                    Double.parseDouble(dataFields.get(7)),
                                    Double.parseDouble(dataFields.get(8)),
                                    Double.parseDouble(dataFields.get(9)),
                                    Boolean.parseBoolean(dataFields.get(10)),
                                    NumberUtils.DOUBLE_ZERO,
                                    NumberUtils.DOUBLE_ZERO,
                                    NumberUtils.DOUBLE_ZERO,
                                    NumberUtils.DOUBLE_ZERO,
                                    NumberUtils.DOUBLE_ZERO,
                                    NumberUtils.DOUBLE_ZERO
                            )
                    );
                });
    }

}
