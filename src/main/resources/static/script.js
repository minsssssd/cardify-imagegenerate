document.addEventListener('DOMContentLoaded', () => {
    
    // ▼▼▼ [Step 1] 로그인한 사용자 정보 (가상 데이터) ▼▼▼
    // 나중에 리지님이 이 부분을 실제 DB 데이터로 교체하면 됩니다.
    const currentUser = {
        name: "홍길동",
        phone: "010-1234-5678",
        email: "hong@cardify.com",
        sns: "@hong_design"
    };

    // 사용자의 선택을 저장할 객체
    let userChoices = {
        subject: '',
        artStyle: '',
        colorTone: '',
        complexity: '',
        textStyle: '' // Q5 추가
    };

    // 모든 선택 버튼에 이벤트 리스너 등록
    const allButtons = document.querySelectorAll('button[data-step]');
    allButtons.forEach(btn => {
        btn.addEventListener('click', (e) => handleStepClick(e));
    });

    // ▼▼▼ [Step 2] 단계별 이동 로직 ▼▼▼
    function handleStepClick(event) {
        const btn = event.target.closest('button');
        const currentStep = parseInt(btn.dataset.step);
        const dataType = btn.dataset.type;
        
        // 1. 선택값 저장
        if (dataType === 'colorTone') {
            userChoices[dataType] = btn.dataset.palette;
        } else {
            userChoices[dataType] = btn.dataset.value;
        }

        // 2. 현재 단계 숨기기
        document.getElementById(`step-${currentStep}`).classList.remove('active');

        // 3. 다음 단계 보여주기 (5단계면 결과 생성)
        if (currentStep < 5) {
            document.getElementById(`step-${currentStep + 1}`).classList.add('active');
        } else {
            generateFinalCards(); 
        }
    }

    // ▼▼▼ [Step 3] 최종 명함 생성 로직 ▼▼▼
    async function generateFinalCards() {
        const resultSection = document.getElementById('result-section');
        const gallery = document.getElementById('final-gallery');
        const loadingMsg = document.getElementById('loading-msg');
        
        resultSection.classList.add('active'); // 결과 화면 표시
        gallery.innerHTML = ''; // 초기화
        loadingMsg.style.display = 'block';

        try {
            // 1. 백엔드에 배경 이미지 요청 (S3 URL 받아오기)
            const requestBody = {
                subject: userChoices.subject,
                artStyle: userChoices.artStyle,
                colorTone: userChoices.colorTone,
                complexity: userChoices.complexity
            };

            // AWS 서버 주소 (본인의 IP 확인 필요)
            const response = await fetch('/api/image/generate', { 
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(requestBody)
            });

            if (!response.ok) throw new Error('서버 통신 실패');
            const data = await response.json();

            loadingMsg.style.display = 'none'; // 로딩 숨기기

            // 2. 받아온 3개의 배경 이미지 각각에 대해 텍스트 합성 수행
            data.images.forEach(imageUrl => {
                createCardCanvas(imageUrl, userChoices.textStyle);
            });

        } catch (error) {
            console.error(error);
            loadingMsg.textContent = '이미지 생성 중 오류가 발생했습니다.';
        }
    }

    // ▼▼▼ [Step 4] 캔버스 합성 및 Base64 변환 함수 ▼▼▼
    function createCardCanvas(bgUrl, textStyleName) {
        const canvas = document.createElement('canvas');
        canvas.width = 1024; // 고해상도 명함
        canvas.height = 640;
        const ctx = canvas.getContext('2d');
        
        const img = new Image();
        img.crossOrigin = "Anonymous"; // ★ S3 이미지 사용 시 필수
        
        // S3 URL이 http로 시작하면 그대로, 아니면 Base64로 처리 (유연성 확보)
        img.src = bgUrl.startsWith('http') ? bgUrl : `data:image/png;base64,${bgUrl}`;

        img.onload = () => {
            // 1. 배경 그리기
            ctx.drawImage(img, 0, 0, canvas.width, canvas.height);

            // 2. 텍스트 스타일 적용 (Q5 선택값) 및 텍스트 그리기
            drawTextOnCanvas(ctx, textStyleName, canvas.width, canvas.height);

            // 3. 최종 결과물을 Base64 이미지 태그로 변환하여 화면에 표시
            const finalBase64 = canvas.toDataURL('image/png');
            
            const resultImg = document.createElement('img');
            resultImg.src = finalBase64; // ★ 리지님이 원하던 Base64 형식
            resultImg.alt = "완성된 명함";
            
            // 클릭 시 다운로드 기능
            resultImg.onclick = () => {
                const link = document.createElement('a');
                link.download = 'my_cardify_business_card.png';
                link.href = finalBase64;
                link.click();
            };
            
            document.getElementById('final-gallery').appendChild(resultImg);
        };
    }

    // 텍스트 그리기 헬퍼 함수
    function drawTextOnCanvas(ctx, styleName, w, h) {
        // 가독성 분석 (배경 밝기 확인)
        const avgBrightness = getAverageBrightness(ctx, w, h);
        const textColor = avgBrightness > 128 ? '#212121' : '#FFFFFF';
        const shadowColor = avgBrightness > 128 ? 'rgba(255,255,255,0.5)' : 'rgba(0,0,0,0.7)';

        ctx.fillStyle = textColor;
        ctx.shadowColor = shadowColor;
        ctx.shadowBlur = 4;
        ctx.shadowOffsetX = 2;
        ctx.shadowOffsetY = 2;

        // 스타일별 정렬 및 폰트 설정
        let align = 'left';
        let x = 50;
        
        if (styleName === 'modern-classic') {
            ctx.font = 'bold 52px serif';
            align = 'left';
        } else if (styleName === 'airy-minimal') {
            ctx.font = '300 48px sans-serif';
            align = 'center';
            x = w / 2;
        } else if (styleName === 'bold-impact') {
            ctx.font = '900 68px sans-serif';
            align = 'right';
            x = w - 50;
        } else { // casual
            ctx.font = 'normal 60px cursive';
            align = 'left';
        }
        
        ctx.textAlign = align;

        // 사용자 정보 그리기 (Mock Data 사용)
        // 이름
        ctx.fillText(currentUser.name, x, 100);
        
        // 상세 정보 (폰트 크기 줄임)
        ctx.font = ctx.font.replace(/\d+px/, '24px'); 
        let y = 160;
        
        // 직함, 연락처, 이메일, SNS 순서대로 그리기
        const infos = [currentUser.phone, currentUser.email, currentUser.sns];
        infos.forEach(info => {
            ctx.fillText(info, x, y);
            y += 40;
        });
    }

    // 배경 밝기 분석 함수 (텍스트 색상 결정을 위해)
    function getAverageBrightness(ctx, width, height) {
        try {
            // 중앙 부분 샘플링
            const imageData = ctx.getImageData(width/4, height/4, width/2, height/2).data;
            let total = 0;
            for(let i=0; i<imageData.length; i+=4) {
                total += (0.299*imageData[i] + 0.587*imageData[i+1] + 0.114*imageData[i+2]);
            }
            return total / (imageData.length / 4);
        } catch(e) {
            return 128;
        }
    }
});