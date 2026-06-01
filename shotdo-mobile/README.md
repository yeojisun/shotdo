# ShotDo Mobile Client (Flutter)

ShotDo의 일일 투두 확인 및 하드웨어 카메라를 활용한 사진 인증 처리를 담당하는 모바일 클라이언트 앱 모듈입니다.

## 핵심 기술 플러그인
*   **camera**: 기기 카메라 모듈 제어 및 뷰파인더 렌더링.
*   **image_picker**: 갤러리 이미지 선택 처리.
*   **flutter_image_compress**: 고화질 사진을 전송 전 압축하여 모바일 데이터 소모 최소화 및 백엔드 로드 저감.
*   **http**: 백엔드 REST API 통신 및 AWS S3 Pre-signed URL로의 이미지 다이렉트 업로드.

## 실행 방법
1. Flutter SDK 설치 여부를 확인합니다.
2. 디렉토리로 이동: `cd shotdo-mobile`
3. 의존성 설치: `flutter pub get`
4. 디바이스 또는 에뮬레이터 연결 후 실행: `flutter run`
