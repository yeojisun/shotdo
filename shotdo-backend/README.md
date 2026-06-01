# ShotDo Backend (Spring Boot)

ShotDo의 비즈니스 로직 및 이미지 인증 관리를 위한 REST API 서버 모듈입니다.

## 핵심 기술 아키텍처
*   **Java 17** & **Spring Boot 3.2.3**
*   **Spring Data JPA** & **MySQL**
*   **AWS S3 SDK (v2)** (사진 업로드 최적화를 위한 Pre-signed URL 생성 탑재)

## 데이터베이스 연결
`src/main/resources/application.yml` 파일에서 데이터베이스 및 AWS S3 버킷 설정을 커스텀하여 구동할 수 있습니다.

## 주요 기능
*   **Pre-signed URL 생성**: 모바일(Flutter) 클라이언트가 서버를 거치지 않고 AWS S3로 다이렉트 업로드하도록 지원.
*   **달력 인증 및 투두 매칭**: 할 일 목록 완료 검증 후 최종 인증 성공 처리.
