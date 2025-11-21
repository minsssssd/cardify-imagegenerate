package com.cardify.imagegenerator.controller;

import com.cardify.imagegenerator.dto.BackgroundRequestDto;
import com.cardify.imagegenerator.service.ImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/image")
@CrossOrigin(origins = "*")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) { 
        this.imageService = imageService;
    }

     @PostMapping("/generate")
    public ResponseEntity<String> generateImage(@RequestBody BackgroundRequestDto requestDto) {
        // DTO를 ImageFinderService로 그대로 전달하고, 받은 JSON을 반환합니다.
        String jsonResponse = imageService.generateImage(requestDto);
        return ResponseEntity.ok(jsonResponse);
    }
}