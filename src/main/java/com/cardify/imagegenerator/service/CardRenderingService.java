package com.cardify.imagegenerator.service;

import com.cardify.imagegenerator.dto.BackgroundRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;

@Service
@Slf4j
public class CardRenderingService {

    public String renderCard(String bgUrl, BackgroundRequestDto info) {
        try {
            // 1. S3 배경 이미지 다운로드
            URL url = new URL(bgUrl);
            BufferedImage image = ImageIO.read(url);

            // 2. 그래픽 객체 생성 
            Graphics2D g = image.createGraphics();
            
            // 텍스트 품질 향상 (안티앨리어싱)
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // 3. 폰트 로드 (resources/fonts/NotoSansKR-Bold.ttf)
            InputStream fontStream = new ClassPathResource("fonts/NotoSansKR-Bold.ttf").getInputStream();
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            
            // 4. 스타일별 설정 (프론트엔드 로직을 자바로 이식)
            int width = image.getWidth();
            int x = 50;
            int align = 0; // 0:Left, 1:Center, 2:Right

            if ("airy-minimal".equals(info.getTextStyle())) {
                x = width / 2;
                align = 1;
            } else if ("bold-impact".equals(info.getTextStyle())) {
                x = width - 50;
                align = 2;
            }

            // 5. 텍스트 그리기
            // 이름 그리기 (가장 크게)
            g.setFont(customFont.deriveFont(Font.BOLD, 52f));
            g.setColor(Color.WHITE);
            drawAlignedString(g, info.getName(), x, 100, align);

            // 상세 정보 그리기 (직책, 회사, 전화, 이메일 순서)
            g.setFont(customFont.deriveFont(Font.PLAIN, 24f)); // 폰트 크기
            int y = 160; // 시작 Y 좌표

            // 화면에 표시할 순서대로 배열에 넣으세요
            String[] infos = {
                info.getPosition(), // 1. 직책 (예: CEO)
                info.getCompany(),  // 2. 회사 (예: Cardify)
                info.getPhone(),    // 3. 전화번호
                info.getEmail()     // 4. 이메일
            };
            
            for (String text : infos) {
                // 값이 있는 경우에만 그림
                if (text != null && !text.isEmpty()) {
                    drawAlignedString(g, text, x, y, align);
                    y += 40; // 줄 간격 40px
                }
            }

            g.dispose(); 

            // 6. 완성된 이미지를 Base64 문자열로 변환
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);

        } catch (Exception e) {
            log.error("이미지 합성 중 오류 발생", e);
            return bgUrl; // 실패하면 원본 URL이라도 반환
        }
    }

    // 정렬을 지원하는 글쓰기 헬퍼 메소드
    private void drawAlignedString(Graphics2D g, String text, int x, int y, int align) {
        FontMetrics metrics = g.getFontMetrics();
        int textWidth = metrics.stringWidth(text);
        
        int finalX = x;
        if (align == 1) { // Center
            finalX = x - (textWidth / 2);
        } else if (align == 2) { // Right
            finalX = x - textWidth;
        }
        
        // 그림자 효과 (검은색)
        g.setColor(new Color(0, 0, 0, 150));
        g.drawString(text, finalX + 2, y + 2);
        
        // 본문 (흰색)
        g.setColor(Color.WHITE);
        g.drawString(text, finalX, y);
    }
}