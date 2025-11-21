package com.cardify.imagegenerator.service;

import com.cardify.imagegenerator.dto.BackgroundRequestDto; // Txt2ImgRequestDto 대신 import

public interface ImageService {
    /**
     * 사용자의 4가지 선택(DTO)을 기반으로,
     * 사전 생성된 이미지의 URL이 담긴 JSON을 반환한다.
     *
     * @param dto 사용자가 선택한 Q1~Q4의 값
     * @return S3 이미지 URL이 담긴 JSON 응답 문자열
     */
    String generateImage(BackgroundRequestDto dto); // 파라미터를 Txt2ImgRequestDto -> BackgroundRequestDto로 변경
}