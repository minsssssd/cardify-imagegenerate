package com.cardify.imagegenerator.service;

import com.cardify.imagegenerator.dto.BackgroundRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class ImageFinderService implements ImageService { 

    private final String s3BaseUrl;

    public ImageFinderService(@Value("${s3.bucket.url}") String s3BaseUrl) {
        this.s3BaseUrl = s3BaseUrl;
    }

    // Q1~Q4의 data-value를 파일명 태그로 변환하기 위한 맵
    private static final Map<String, String> SUBJECT_MAP = Map.of(
            "Marble Pattern", "marble",
            "Wood Grain", "wood",
            "Water Ripple", "water",
            "Silk Texture", "silk"
    );

    private static final Map<String, String> ARTSTYLE_MAP = Map.of(
            "organic flowing curves, elegant, smooth", "classic",
            "complex overlapping patterns, improvisational, dynamic", "jazz",
            "geometric shapes, clean lines, futuristic, precise", "electro",
            "minimalist, simple forms, soft texture, subtle", "lofi"
    );

    private static final Map<String, String> COLOR_MAP = Map.of(
            "#FFC2D1,#FFFFFF,#A0C49D", "spring",
            "#0077B6,#90E0EF,#F9C74F", "summer",
            "#A4907C,#5D503C,#E4D5B7", "autumn",
            "#03045E,#FFFFFF,#4B0082", "winter"
    );

    private static final Map<String, String> COMPLEXITY_MAP = Map.of(
            "minimalist, clean negative space, simple, relaxing", "minimal",
            "high detail, intricate patterns, luxurious, ornamental", "luxury",
            "dynamic, energetic, complex, dense patterns", "dynamic",
            "rough, natural textures, organic complexity, raw", "rough"
    );


    @Override
    public String generateImage(BackgroundRequestDto dto) {
        
        // 1. DTO의 값들을 파일명 태그로 변환합니다.
        //    (맵에 없는 값이 들어오면 기본값 "default" 사용)
        String subjectTag = SUBJECT_MAP.getOrDefault(dto.getSubject(), "marble");
        String artStyleTag = ARTSTYLE_MAP.getOrDefault(dto.getArtStyle(), "classic");
        String colorTag = COLOR_MAP.getOrDefault(dto.getColorTone(), "autumn");
        String complexityTag = COMPLEXITY_MAP.getOrDefault(dto.getComplexity(), "minimal");

        // 2. 태그를 조합하여 S3 파일 이름을 생성합니다. (예: "marble_classic_autumn_minimal")
        String baseFileName = String.join("_", subjectTag, artStyleTag, colorTag, complexityTag);

        // 3. 3개의 이미지 URL을 생성합니다.
        String url1 = s3BaseUrl + "/" + baseFileName + "_1.png";
        String url2 = s3BaseUrl + "/" + baseFileName + "_2.png";
        String url3 = s3BaseUrl + "/" + baseFileName + "_3.png";

        log.info("→ Finding images with tags: {}", baseFileName);

        // 4. 프론트엔드(script.js)가 기대하는 JSON 형식으로 응답을 위조합니다.
        String jsonResponse = "{\"images\": [\"" + url1 + "\", \"" + url2 + "\", \"" + url3 + "\"]}";
        
        return jsonResponse;
    }
}