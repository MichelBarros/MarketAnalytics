package com.market.analytics.batch.utils;

import com.market.analytics.domain.AdxResult;
import com.market.analytics.domain.AssetClass;
import com.market.analytics.domain.AssetData;
import com.market.analytics.exception.DataNotAppendedToStructuredFile;
import com.market.analytics.exception.DataStructuredFileNotCreated;
import com.market.analytics.exception.DataStructuredFileNotFound;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public final class ObjectStorageLoaderUtils {

    private static final String PATH_SEPARATOR = "\\\\";
    private static final String FILENAME_SEPARATOR = "\\.";
    public static final String DATA_STRUCTURED_EXTENSION = ".csv";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static void mapFilesPath(File sourceDirectory, List<String> files) {
        Arrays.stream(Objects.requireNonNull(sourceDirectory.listFiles()))
                .filter(File::exists)
                .filter(File::isDirectory)
                .filter(file -> Objects.requireNonNull(file.listFiles()).length != NumberUtils.INTEGER_ZERO)
                .forEach(dir -> {
                    Arrays.stream(Objects.requireNonNull(dir.listFiles()))
                            .filter(File::isFile)
                            .forEach(file -> files.add(file.getPath()));
                });
    }

    public static void createDataStructuredFile(List<String> files, String dataStoragePath) {
        files.forEach(file -> {
            AssetClass assetClass = AssetClass.getPrefixFromSuffix(getFileName(file));
            String filePath = dataStoragePath + File.separator + assetClass.market + File.separator + getAssetName(file) + assetClass.currency + DATA_STRUCTURED_EXTENSION;
            File newFile = new File(filePath);
            try {
                if (newFile.createNewFile()) {
                    log.debug("File Created: {}", filePath);
                    AssetCommonUtils.writeCsvHeader(newFile);
                }
            } catch (IOException e) {
                throw new DataStructuredFileNotCreated();
            }

        });
    }

    public static List<AssetData> appendDataToStructuredFile(List<String> files, String structuredFilePath, int mirroringChunkSize) {
        List<AssetData> response = new ArrayList<>();
        files.forEach(file -> {
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                bufferedReader.readLine();

                AssetClass assetClass = AssetClass.getPrefixFromSuffix(getFileName(file));
                String assetName = getAssetName(file);
                BufferedWriter bufferedWriter = new BufferedWriter(
                        new FileWriter(structuredFilePath + File.separator + assetClass.market + File.separator + assetName + assetClass.currency + DATA_STRUCTURED_EXTENSION, true)
                );

                long indexToSave = getLinesNumber(file) - mirroringChunkSize;
                log.info("Total lines: {}", indexToSave);
                AtomicInteger lineCounter = new AtomicInteger(0);
                RsiCalculator rsiCalculator = new RsiCalculator();
                AdxCalculator adxCalculator = new AdxCalculator();
                bufferedReader.lines().forEach(line -> {
                    try {
                        String lineToWrite = line.replace(assetClass.tickerSuffix, "")
                                .replace(assetName, replaceAsset(assetClass, assetName));
                        int index = lineCounter.incrementAndGet();
                        AssetData assetData = parseLineToAssetData(lineToWrite);
                        Double rsi = rsiCalculator.nextValue(assetData.close());
                        AdxResult adxResult = adxCalculator.nextValue(assetData.high(), assetData.low(), assetData.close());

                        AssetData enrichedData = assetData.toBuilder()
                                .rsi(rsi)
                                .diPos(adxResult.diPos())
                                .diNeg(adxResult.diNeg())
                                .adx(adxResult.adx())
                                .atr(adxResult.atr())
                                .volatility(adxResult.volatility())
                                .build();

                        bufferedWriter.write(enrichedData.toCsvWithIndicators());
                        bufferedWriter.newLine();
                        if (index > indexToSave) {
                            log.info("Registers to save in DDBB {}, line {}", index, lineToWrite);
                            response.add(enrichedData);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                bufferedReader.close();
                bufferedWriter.close();
            } catch (FileNotFoundException e) {
                throw new DataStructuredFileNotFound();
            } catch (IOException e) {
                throw new DataNotAppendedToStructuredFile();
            }
        });
        return response;
    }

    public static String getAssetName(String file) {
        String[] assetName = getFileName(file).split(FILENAME_SEPARATOR);
        return assetName[NumberUtils.INTEGER_ZERO].toUpperCase();
    }

    public static void deleteSourceFiles(List<String> files) {
        files.forEach(file -> {
            File fileToDelete = new File(file);
            if (fileToDelete.delete()) {
                log.info("File Deleted: {}", file);
            } else {
                log.info("File Couldn't Be Deleted: {}", file);
            }
        });

        files.clear();
    }

    private static String getFileName(String file) {
        String[] fileName = file.split(PATH_SEPARATOR);
        return fileName[fileName.length - 1];
    }

    private static long getLinesNumber(String filePath) throws IOException {
        return new BufferedReader(new FileReader(filePath)).lines().count() - 1;
    }

    private static String replaceAsset(AssetClass assetClass, String assetName) {
        return assetName + assetClass.currency + "," + assetClass.assetPrefix + assetName + assetClass.currency;
    }

    private static AssetData parseLineToAssetData(String line) {
        String[] columns = line.split(",");

        return AssetData.builder()
                .ticker(columns[0])
                .providerTicker(columns[1])
                .per(columns[2])
                .dateTime(buildDateTime(columns[3] + columns[4]))
                .open(Double.parseDouble(columns[5]))
                .high(Double.parseDouble(columns[6]))
                .low(Double.parseDouble(columns[7]))
                .close(Double.parseDouble(columns[8]))
                .vol(Double.parseDouble(columns[9]))
                .volumeWeighted(Double.parseDouble(columns[10]))
                .otc(Boolean.FALSE)
                .build();
    }

    private static OffsetDateTime buildDateTime(String date) {
        return LocalDateTime.parse(date, DATE_FORMATTER)
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
                .atStartOfDay(ZoneOffset.UTC)
                .toOffsetDateTime();
    }

}
