package com.cardify.imagegenerator.controller;

import com.cardify.imagegenerator.dto.BackgroundRequestDto;
import com.cardify.imagegenerator.service.CardRenderingService;
import com.cardify.imagegenerator.service.ImageFinderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/image")
@CrossOrigin(origins = "*")
public class ImageController {

    private final ImageFinderService imageFinderService;
    private final CardRenderingService cardRenderingService;

    public ImageController(ImageFinderService imageFinderService, CardRenderingService cardRenderingService) {
        this.imageFinderService = imageFinderService;
        this.cardRenderingService = cardRenderingService;
    }

    @PostMapping("/generate")
    public ResponseEntity<List<String>> generateImage(@RequestBody BackgroundRequestDto requestDto) {
        // 1. 배경 이미지 URL 3개 찾기
        String jsonUrls = imageFinderService.generateImage(requestDto);
        List<String> urls = extractUrlsFromJson(jsonUrls);

        // 2. 각 URL에 대해 합성 수행하여 리스트에 담기
        List<String> base64Images = new ArrayList<>();
        for (String url : urls) {
            String base64 = cardRenderingService.renderCard(url, requestDto);
            // 따옴표 없이 순수한 문자열 그대로 리스트에 추가
            base64Images.add(base64);
        }

        // 3. List<String> 자체를 반환 (자동으로 JSON Array [ ... ] 형태로 변환됨)
        return ResponseEntity.ok(base64Images);
    }

    // URL 추출 헬퍼 (기존 유지)
    private List<String> extractUrlsFromJson(String json) {
        List<String> urls = new ArrayList<>();
        String content = json.substring(json.indexOf("[") + 1, json.lastIndexOf("]"));
        
        for (String url : content.split(",")) {
            String cleanUrl = url.replace("\"", "").trim();
            if (!cleanUrl.isEmpty()) {
                urls.add(cleanUrl);
            }
        }
        return urls;
    }
}