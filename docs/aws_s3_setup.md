# AWS S3 구축 및 CORS 설정 가이드 (초보자용)

본 가이드는 **ShotDo** 앱에서 사용할 AWS S3 이미지 저장소를 생성하고, 보안 및 웹 업로드(CORS) 설정을 완료하기 위한 단계별 안내서입니다.

---

## 1단계: S3 버킷(Bucket) 생성하기

버킷은 S3에서 파일을 담는 일종의 '폴더' 개념입니다.

1. **AWS 콘솔 로그인**: [AWS Management Console](https://aws.amazon.com/console/)에 로그인합니다.
2. **S3 서비스 검색**: 상단 검색창에 **S3**를 입력하고 클릭하여 이동합니다.
3. **버킷 만들기 클릭**: 우측의 **버킷 만들기 (Create bucket)** 버튼을 누릅니다.
4. **기본 설정 입력**:
   * **버킷 이름**: 전 세계에서 고유해야 합니다. 예: `shotdo-auth-photos-yeojisun` (본인만의 고유 접미사 붙이기).
   * **리전**: **아시아 태평양(서울) ap-northeast-2** 선택.
5. **객체 소유권**: **ACL 비활성화됨(권장)** 그대로 유지.
6. **이 버킷의 퍼블릭 액세스 차단 설정**:
   * ⚠️ **주의**: 사용자들이 업로드한 사진을 달력에서 링크로 볼 수 있어야 하므로, **"모든 퍼블릭 액세스 차단" 체크를 해제**합니다.
   * 체크 해제 시 하단에 나타나는 *"현재 설정으로 인해 이 버킷과 그 안의 객체가 퍼블릭이 될 수 있음을 알고 있습니다"*라는 **경고 문구 옆 체크박스에 체크**해 줍니다.
7. **버킷 만들기 완료**: 맨 아래로 내려가 **버킷 만들기** 버튼을 클릭합니다.

---

## 2단계: CORS(교차 출처 리소스 공유) 설정하기 (중요 🌟)

웹캠 촬영 이미지나 파일을 브라우저(localhost나 배포된 웹사이트)에서 S3로 직접 업로드하려면, S3가 브라우저의 업로드 요청을 허용하도록 허가증(CORS)을 발급해 주어야 합니다. 이 설정을 안 하면 `CORS Policy` 에러가 나며 업로드가 실패합니다.

1. 방금 만든 버킷 이름을 클릭하여 상세 페이지로 이동합니다.
2. 상단 탭 중 **권한 (Permissions)** 탭을 선택합니다.
3. 맨 아래로 스크롤하여 **CORS (교차 출처 리소스 공유)** 섹션의 **편집 (Edit)** 버튼을 클릭합니다.
4. 아래의 JSON 설정을 그대로 복사하여 붙여넣고 **변경 사항 저장**을 누릅니다.

```json
[
    {
        "AllowedHeaders": [
            "*"
        ],
        "AllowedMethods": [
            "PUT",
            "POST",
            "GET"
        ],
        "AllowedOrigins": [
            "http://localhost:5173",
            "https://yeojisun.github.io"
        ],
        "ExposeHeaders": [
            "ETag"
        ],
        "MaxAgeSeconds": 3000
    }
]
```
> **설명**: 
> * `AllowedOrigins`에 허용할 도메인을 적어줍니다. 테스트 중인 `localhost:5173`과 깃허브 배포 주소(`https://yeojisun.github.io`)가 허용됩니다.

---

## 3단계: 버킷 퍼블릭 조회 권한 부여 (Bucket Policy)

인증샷 URL을 가진 사람 누구나 달력에서 사진을 조회할 수 있도록 읽기 권한을 열어둡니다.

1. **권한 (Permissions)** 탭의 **버킷 정책 (Bucket policy)** 섹션에서 **편집 (Edit)** 버튼을 클릭합니다.
2. 아래 JSON 코드 중 `YOUR-BUCKET-NAME` 부분을 **본인의 실제 버킷 이름**으로 바꾼 뒤 붙여넣고 저장합니다.

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "PublicReadGetObject",
            "Effect": "Allow",
            "Principal": "*",
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::YOUR-BUCKET-NAME/*"
        }
    ]
}
```

---

## 4단계: Spring Boot 연결을 위한 IAM 사용자의 액세스 키 발급하기

자바 백엔드 서버가 S3 버킷을 제어(Pre-signed URL 생성 등)하기 위한 전용 보안 열쇠를 생성합니다.

1. AWS 콘솔 상단 검색창에 **IAM**을 입력하고 이동합니다.
2. 좌측 메뉴에서 **사용자 (Users)** -> **사용자 생성 (Create user)**을 클릭합니다.
3. **사용자 이름**: `shotdo-backend-user` 입력 후 [다음].
4. **권한 설정**: **"정책 직접 연결 (Attach policies directly)"** 선택.
5. 검색창에 **AmazonS3FullAccess**를 검색하여 왼쪽에 체크 후 [다음] -> [사용자 생성]을 누릅니다.
6. 다시 사용자 목록에서 방금 만든 `shotdo-backend-user`를 클릭합니다.
7. 중간의 **보안 자격 증명 (Security credentials)** 탭으로 이동합니다.
8. 아래로 내려가 **액세스 키 생성 (Create access key)**을 클릭합니다.
9. **사용 사례**: **Application running outside AWS (AWS 외부에서 실행되는 애플리케이션)** 선택 후 [다음].
10. 태그는 생략하고 [액세스 키 생성]을 완료합니다.
11. ⚠️ **매우 중요**: 생성된 **액세스 키 ID(Access Key ID)**와 **비밀 액세스 키(Secret Access Key)**를 파일이나 메모장에 안전하게 저장해 둡니다. (비밀 키는 이 창을 닫으면 다시는 조회할 수 없습니다!)

---

## 5단계: Spring Boot 설정 적용

키를 모두 확보하셨다면, 백엔드 프로젝트의 `application.yml` 및 자바 환경변수에 다음과 같이 등록하여 연동을 마무리합니다.

[application.yml](file:///C:/Users/jisun.yeo/.gemini/antigravity/scratch/shotdo/shotdo-backend/src/main/resources/application.yml)을 수정하거나 컴퓨터의 환경변수로 주입합니다:

```yaml
aws:
  s3:
    bucket: YOUR-BUCKET-NAME       # 예: shotdo-auth-photos-yeojisun
    region: ap-northeast-2
  credentials:
    accessKey: YOUR-ACCESS-KEY-ID  # 4단계에서 발급받은 액세스 키 ID
    secretKey: YOUR-SECRET-KEY     # 4단계에서 발급받은 비밀 액세스 키
```
