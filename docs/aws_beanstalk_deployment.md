# AWS Elastic Beanstalk을 통한 백엔드(Spring Boot) 무료 배포 가이드

이 가이드는 AWS의 간편 배포 서비스인 **Elastic Beanstalk**을 활용하여 Spring Boot JAR 파일을 빌드하고 100% 프리 티어 범위 내에서 배포하는 단계를 제공합니다.

---

## 1단계: 로컬에서 Spring Boot 프로젝트 빌드하기 (JAR 파일 생성)

서버에 올릴 실행 파일(JAR)을 컴파일하여 생성합니다.

1. **프로젝트 터미널을 열고 백엔드 폴더로 이동합니다.**
   ```bash
   cd shotdo-backend
   ```
2. **Gradle Wrapper 생성 (처음 한 번만 실행)**:
   프로젝트 빌드 실행 도구를 설치합니다. (자바 JDK 17 이상이 로컬에 설치되어 있어야 작동합니다.)
   ```bash
   gradle wrapper
   ```
3. **프로젝트 빌드 실행**:
   테스트 코드를 스킵하고 빌드하여 배포용 파일을 뽑아냅니다.
   ```bash
   ./gradlew bootJar
   ```
4. **생성된 파일 확인**:
   빌드가 성공하면 `shotdo-backend/build/libs/` 폴더 내에 **`shotdo-backend-0.0.1-SNAPSHOT.jar`** 파일이 생성됩니다. 이 파일을 AWS에 업로드할 것입니다.

---

## 2단계: AWS Elastic Beanstalk 애플리케이션 생성

1. **AWS 콘솔 로그인**: [AWS Console](https://aws.amazon.com/console/) 접속 후 검색창에 **Elastic Beanstalk**을 입력해 이동합니다.
2. **애플리케이션 생성 (Create application)** 클릭.
3. **환경 구성**:
   * **환경 티어 (Environment tier)**: **웹 서버 환경 (Web server environment)** 선택
   * **애플리케이션 이름**: `shotdo-backend-app` 입력
   * **플랫폼 (Platform)**:
     * **플랫폼**: **Java** 선택
     * **플랫폼 브랜치**: **Corretto 17** 선택 (Spring Boot 3 자바 17 버전 호환)
     * **플랫폼 버전**: 기본값 유지
   * **애플리케이션 코드**: **코드 업로드 (Upload your code)** 선택
     * **버전 레이블**: `v1.0.0`
     * **소스 코드 원본**: **로컬 파일 (Local file)** 선택 ➡️ **[파일 선택]** 버튼을 눌러 위 1단계에서 빌드된 **`shotdo-backend-0.0.1-SNAPSHOT.jar`** 파일을 업로드합니다.
4. **프리셋 (Preset)**: **단일 인스턴스 (Single instance - free tier eligible)** 선택 ➡️ **(과금 방지 ⚠️)**

---

## 3단계: ⚠️ 보안 및 환경 변수 구성 (핵심 설정!)

깃허브에 올리지 않았던 비밀 키들을 AWS 배포 서버의 환경 변수로 설정해 줍니다.

1. 구성 화면 중 **[구성 수정 (Configure updates, monitoring and logging)]** 또는 설정 단계에서 **[업데이트, 모니터링 및 로깅 구성]** 항목으로 갑니다.
2. **환경 속성 (Environment properties)** 또는 **소프트웨어 (Software)** 편집 설정으로 이동합니다.
3. 아래 목록의 이름(Name)과 값(Value)을 하나씩 추가해 줍니다:

| 이름 (Name) | 값 (Value) | 설명 |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | `prod` (또는 비워둠) | 로컬 설정(local)이 아닌 환경변수를 사용하도록 지정 |
| `DB_HOST` | `YOUR_RDS_ENDPOINT_ADDRESS` | 생성한 AWS RDS 데이터베이스 엔드포인트 주소 |
| `DB_NAME` | `shotdo` | 초기 데이터베이스 이름 |
| `DB_USERNAME` | `root` | 데이터베이스 마스터 사용자 이름 |
| `DB_PASSWORD` | `YOUR_DB_PASSWORD` | 데이터베이스 접속 비밀번호 |
| `AWS_ACCESS_KEY_ID` | `YOUR_IAM_ACCESS_KEY_ID` | S3 제어용 IAM 액세스 키 |
| `AWS_SECRET_ACCESS_KEY` | `YOUR_IAM_SECRET_ACCESS_KEY` | S3 제어용 IAM 비밀 키 |
| `SERVER_PORT` | `5000` | Beanstalk 기본 포트인 5000으로 자바 포트 설정 |

4. **[검토 및 생성]**을 누르고 최종적으로 배포를 시작합니다.
5. 배포가 완료되면 대시보드 상단에 **초록색 체크(정상)** 아이콘과 함께 **`http://shotdo-backend-app-xxx.ap-northeast-2.elasticbeanstalk.com`** 형태의 **퍼블릭 API 서버 주소**가 제공됩니다!

---

## 4단계: 프론트엔드(`shotdo-web`)와 배포 서버 주소 연결하기

자바 서버가 성공적으로 배포되면, Vue.js 프론트엔드가 이 서버를 바라보도록 코드의 API 주소를 업데이트해 주기만 하면 완벽하게 연동됩니다.
