package com.market.analytics.controller.web;

import com.market.analytics.domain.AssetDataParams;
import com.market.analytics.domain.AssetDailyPricing;
import com.market.analytics.domain.Temporality;
import com.market.analytics.service.DataHistoryService;
import com.market.analytics.service.StockDataService;
import com.market.analytics.webDto.AssetDataWebDto;
import com.market.analytics.webDto.TickerWebDto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/data")
public class DataController {

    private static final String TICKER_PARAM = "ticker";
    private static final String TEMPORALITY_PARAM = "temporality";
    private static final String PAGE_NUMBER_PARAM = "pageNumber";
    private static final String PAGE_SIZE_PARAM = "pageSize";

    private final DataHistoryService dataHistoryService;
    private final StockDataService stockDataService;

    public DataController(DataHistoryService dataHistoryService, StockDataService stockDataService) {
        this.dataHistoryService = dataHistoryService;
        this.stockDataService = stockDataService;
    }

    @GetMapping
    public HttpEntity<AssetDataWebDto> getAssetDataHistory(
            @RequestParam(value = TICKER_PARAM) String ticker,
            @RequestParam(value = TEMPORALITY_PARAM) Temporality temporality,
            @RequestParam(value = PAGE_NUMBER_PARAM) int pageNumber,
            @RequestParam(value = PAGE_SIZE_PARAM) int pageSize
    ) {
        var pagination = new AssetDataParams(ticker, temporality, pageNumber, pageSize);
        return ResponseEntity.ok(
                new AssetDataWebDto(this.dataHistoryService.getAssetDataHistory(pagination))
        );
    }

    @GetMapping("/tickers")
    public HttpEntity<TickerWebDto> getTickers(
            @RequestParam(value = TICKER_PARAM) String ticker
    ) {
        return ResponseEntity.ok(new TickerWebDto(this.stockDataService.getTickerInfo(ticker)));
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadAssetDataHistory(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return ResponseEntity.status(HttpStatus.OK)
                .body(this.dataHistoryService.uploadAssetDataHistory(file));
    }

    @PostMapping("/uploadMultipleAssets")
    public ResponseEntity<?> uploadMultipleAssetsDataHistory(
            @RequestParam("files") MultipartFile[] files
    ) throws IOException {
        List<MultipartFile> multipleAssets = new ArrayList<>();
        Arrays.asList(files).stream().forEach(file -> multipleAssets.add(file));
        return ResponseEntity.status(HttpStatus.OK)
                .body(this.dataHistoryService.uploadMultipleAssetsDataHistory(multipleAssets));
    }

    @GetMapping("stockDailyPricing")
    public HttpEntity<AssetDailyPricing> getStockDailyPricing() {
        return ResponseEntity.status(HttpStatus.OK).body(stockDataService.getStockDailyPricing());
    }

}
