# ShotDo - 사진 인증형 Daily To-Do Calendar Web App

**ShotDo**는 오늘의 할 일(To-do List)을 사진으로 인증해야만 달력 칸이 채워지는 자기 관리 및 습관 형성 서비스입니다.

## 📂 프로젝트 구조

본 프로젝트는 아래와 같이 클라이언트(Web, Mobile)와 백엔드가 분리된 멀티 모듈 아키텍처로 설계되었습니다:

```text
shotdo/
├── shotdo-web/       # Vue.js + Vite 프론트엔드 (캘린더, 투두 관리, 웹캠 인증 프로토타입)
├── shotdo-backend/   # Java (Spring Boot) 백엔드 API (AWS S3 Pre-signed URL 및 CRUD API 뼈대)
└── shotdo-mobile/    # Flutter 모바일 앱 (네이티브 카메라 연동 및 오늘 투두 관리 뼈대)
```

## 🛠️ 시작하기

### 1. Web Frontend (`shotdo-web`)
실제 기능이 모킹되어 로컬 스토리지에 동작하며, 완성도 높은 웹 인터페이스를 구동합니다.
```bash
cd shotdo-web
npm install
npm run dev
```

### 2. Backend Server (`shotdo-backend`)
```bash
cd shotdo-backend
./gradlew bootRun
```
*   `src/main/resources/application.yml`에서 DB 및 AWS S3 관련 설정을 채워 넣어야 동작합니다.

### 3. Mobile App (`shotdo-mobile`)
```bash
cd shotdo-mobile
flutter pub get
flutter run
```

---
*개발자: yeojisun (GitHub)*
