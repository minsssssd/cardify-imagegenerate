package com.cardify.imagegenerator.dto;

import lombok.Data;

// 배경 이미지 생성 요청에만 사용할 DTO
@Data
public class BackgroundRequestDto {
    private String subject; // Q1 (예: "나무의 나이테")
    private String artStyle;  // Q2 (예: "complex overlapping patterns...")
    private String colorTone; // Q3 (예: "#A4907C...")
    private String complexity; // Q4 (예: "high detail, intricate patterns...")

    // 텍스트 합성 및 사용자 정보 필드 
    private String textStyle; // (예: "modern-classic")
    
    private String name;      // 
    private String title;     // (예: "CEO") - 기존 phone 자리에 들어갈 정보 등 자유롭게 활용
    private String email;
    private String sns;
}