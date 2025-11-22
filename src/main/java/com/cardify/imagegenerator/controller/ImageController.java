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
    public ResponseEntity<String> generateImage(@RequestBody BackgroundRequestDto requestDto) {
        // 1. 배경 이미지 URL 3개 찾기 
        
        String jsonUrls = imageFinderService.generateImage(requestDto); 
        // 예: {"images": ["url1", "url2", "url3"]}
        
        // 간단 파싱 
        List<String> urls = extractUrlsFromJson(jsonUrls); 
        
        // 2. 각 URL에 대해 합성 수행
        List<String> base64Images = new ArrayList<>();
        for (String url : urls) {
            String base64 = cardRenderingService.renderCard(url, requestDto);
            base64Images.add("\"" + base64 + "\""); // JSON 포맷을 위해 따옴표 추가
        }

        // 3. 최종 JSON 생성
        String finalJson = "{\"images\": [" + String.join(",", base64Images) + "]}";
        
        return ResponseEntity.ok(finalJson);
    }

    // 간단한 URL 추출 헬퍼 
    private List<String> extractUrlsFromJson(String json) {
        List<String> urls = new ArrayList<>();
        String content = json.replace("{\"images\": [", "").replace("]}", "").replace("\"", "");
        for (String url : content.split(",")) {
            urls.add(url.trim());
        }
        return urls;
    }
}