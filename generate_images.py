import requests
import json
import base64
import os

# --- 1. 설정: AI 서버 및 저장 경로 ---
AI_API_URL = "http://127.0.0.1:42032/sdapi/v1/txt2img"
OUTPUT_FOLDER = "generated_images"

# --- 2. 매핑: ImageFinderService.java와 동일한 규칙 ---
SUBJECTS = {
    "Marble Pattern": "marble",
    "Wood Grain": "wood",
    "Water Ripple": "water",
    "Silk Texture": "silk"
}
ARTSTYLES = {
    "organic flowing curves, elegant, smooth": "classic",
    "complex overlapping patterns, improvisational, dynamic": "jazz",
    "geometric shapes, clean lines, futuristic, precise": "electro",
    "minimalist, simple forms, soft texture, subtle": "lofi"
}
COLORS = {
    "#FFC2D1,#FFFFFF,#A0C49D": "spring",
    "#0077B6,#90E0EF,#F9C74F": "summer",
    "#A4907C,#5D503C,#E4D5B7": "autumn",
    "#03045E,#FFFFFF,#4B0082": "winter"
}
COMPLEXITIES = {
    "minimalist, clean negative space, simple, relaxing": "minimal",
    "high detail, intricate patterns, luxurious, ornamental": "luxury",
    "dynamic, energetic, complex, dense patterns": "dynamic",
    "rough, natural textures, organic complexity, raw": "rough"
}

# --- 3. PromptGenerator.java의 로직을 Python으로 구현 ---
def translate_color_to_phrase(palette_key):
    descriptions = {
        "#FFC2D1,#FFFFFF,#A0C49D": "in a warm and hopeful atmosphere, with a palette of soft pinks, clean whites, and fresh greens",
        "#0077B6,#90E0EF,#F9C74F": "in a vibrant and energetic atmosphere, with a palette of bright blues, vivid cyans, and pops of bright yellow",
        "#A4907C,#5D503C,#E4D5B7": "in a calm and mature atmosphere, with a palette of warm earthy tones, including taupe, rich umber, and soft stone colors",
        "#03045E,#FFFFFF,#4B0082": "in a quiet and mystical atmosphere, featuring a palette of deep blues, crisp whites, and rich purples"
    }
    description = descriptions.get(palette_key, "with a neutral color scheme")
    return f"({description}:1.2)"

NEGATIVE_PROMPT = "person, human, people, face, portrait, photo, man, woman, " + \
                  "text, letters, words, font, signature, logo, watermark, card, frame, border, " + \
                  "ugly, blurry, bad quality, paint, scene, landscape, building, " + \
                  "flower, flowers, floral, leaf, tree, sun, ocean, cloud, mountain, building"

# --- 4. 메인 실행 함수 ---
def main():
    total = len(SUBJECTS) * len(ARTSTYLES) * len(COLORS) * len(COMPLEXITIES)
    print(f"--- 이미지 생성 이어하기를 시작합니다 (총 {total}개 조합) ---")
    
    if not os.path.exists(OUTPUT_FOLDER):
        os.makedirs(OUTPUT_FOLDER)
        print(f"'{OUTPUT_FOLDER}' 폴더를 생성했습니다.")

    count = 1

    for subject_prompt, subject_tag in SUBJECTS.items():
        for style_prompt, style_tag in ARTSTYLES.items():
            for color_key, color_tag in COLORS.items():
                for complexity_prompt, complexity_tag in COMPLEXITIES.items():
                    
                    base_filename = f"{subject_tag}_{style_tag}_{color_tag}_{complexity_tag}"
                    
                    # ▼▼▼ [수정] 이 6줄의 코드가 추가되었습니다! (이어하기 기능) ▼▼▼
                    # 1. 파일명 생성 (ImageFinderService 규칙과 동일)
                    check_file_path = os.path.join(OUTPUT_FOLDER, f"{base_filename}_1.png")
                    
                    # 1.5 이미지가 이미 존재하는지 확인
                    if os.path.exists(check_file_path):
                        print(f"\n({count}/{total}) 이미 존재함: {base_filename} (SKIP)")
                        count += 1
                        continue # 이 조합은 건너뛰고 다음 루프로 이동
                    # ▲▲▲ 수정 끝 ▲▲▲

                    print(f"\n({count}/{total}) 조합 생성 중: {base_filename}")
                    
                    # 2. Positive 프롬프트 생성 (PromptGenerator 로직과 동일)
                    color_description = translate_color_to_phrase(color_key)
                    positive_prompt = (
                        f"masterpiece, best quality, professional business card background, "
                        f"an abstract seamless pattern of ((({subject_prompt}))), "
                        f"rendered in a style that is {style_prompt} and {complexity_prompt}, "
                        f"{color_description}"
                    )
                    
                    # 3. AI API에 보낼 JSON 페이로드 생성
                    payload = {
                        "prompt": positive_prompt,
                        "negative_prompt": NEGATIVE_PROMPT,
                        "n_iter": 3,
                        "width": 1024,
                        "height": 640,
                        "steps": 30,
                        "cfg_scale": 7.0,
                        "sampler_name": "Euler a"
                    }

                    try:
                        # 4. AI 서버에 POST 요청
                        response = requests.post(url=AI_API_URL, json=payload, timeout=300) 
                        response.raise_for_status() 
                        
                        r = response.json()
                        images_base64 = r['images']
                        
                        # 5. 응답받은 3개의 이미지를 Base64 디코딩 후 파일로 저장
                        for i, img_b64 in enumerate(images_base64):
                            file_path = os.path.join(OUTPUT_FOLDER, f"{base_filename}_{i+1}.png")
                            try:
                                if "," in img_b64:
                                    img_b64 = img_b64.split(',', 1)[1]
                                
                                img_data = base64.b64decode(img_b64)
                                with open(file_path, 'wb') as f:
                                    f.write(img_data)
                                print(f"  -> {file_path} 저장 완료")
                            except Exception as e:
                                print(f"  [오류] {base_filename}_{i+1}.png 저장 실패: {e}")

                    except requests.exceptions.RequestException as e:
                        print(f"  [치명적 오류] {base_filename} 조합 생성 실패 (AI 서버 연결 확인): {e}")

                    count += 1
    
    print("\n--- 모든 이미지 생성이 완료되었습니다! ---")

if __name__ == "__main__":
    main()