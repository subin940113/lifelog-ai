# LifeLog AI

자연어 기반 개인 라이프로그를 기록하고, AI 기반 인사이트를 제공하는 백엔드 서비스입니다.

## 📋 목차

- [주요 기능](#주요-기능)
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [시작하기](#시작하기)
- [API 문서](#api-문서)
- [아키텍처](#아키텍처)
- [환경 변수](#환경-변수)

## 주요 기능

### 1. 인증 및 사용자 관리
- OAuth 2.0 기반 소셜 로그인 (Google, Kakao, Naver, Apple)
- JWT 기반 인증 및 토큰 갱신
- 사용자 프로필 관리

### 2. 로그 관리
- 자연어 기반 로그 기록
- 커서 기반 페이지네이션으로 로그 조회
- 로그 기반 패턴 분석

### 3. AI 인사이트 생성
- 로그 내용을 분석하여 자동 인사이트 생성
- OpenAI GPT 모델 기반 인사이트 생성
- 키워드 기반 인사이트 필터링
- 인사이트 피드백 시스템 (좋아요/싫어요)

### 4. 관심사 관리
- 최대 5개까지 관심 키워드 등록
- 키워드별 시그널 상태 추적
- 키워드 기반 인사이트 생성

### 5. 푸시 알림
- 시간 패턴 기반 푸시 알림 (평소 기록 시간대에 기록이 없을 때)
- 키워드 기반 푸시 알림 (관심 키워드 관련 기록 유도)
- 인사이트 생성 시 푸시 알림
- Firebase Cloud Messaging (FCM) 연동

## 기술 스택

### Backend
- **Language**: Kotlin 2.2.21
- **Framework**: Spring Boot 4.0.1
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA / Hibernate
- **Security**: Spring Security + JWT
- **AI**: OpenAI API (GPT-4o-mini)

### 주요 라이브러리
- Jackson (JSON 직렬화)
- WebFlux (비동기 HTTP 클라이언트)
- Firebase Admin SDK (FCM)
- Google API Client (OAuth)

### 개발 도구
- ktlint (코드 스타일 검사)
- Gradle (빌드 도구)

## 프로젝트 구조

```
src/main/kotlin/com/example/lifelog/
├── application/          # Application Layer (Use Cases)
│   ├── auth/            # 인증 관련 Use Cases
│   ├── home/            # 홈 화면 Use Case
│   ├── insight/          # 인사이트 관련 Use Cases
│   │   ├── feedback/    # 인사이트 피드백
│   │   ├── pipeline/    # 인사이트 생성 파이프라인
│   │   └── settings/    # 인사이트 설정
│   ├── interest/         # 관심사 관리 Use Cases
│   ├── log/             # 로그 관리 Use Cases
│   ├── push/            # 푸시 알림 Use Cases
│   ├── signal/           # 키워드 시그널 관리
│   └── user/             # 사용자 관리 Use Cases
│
├── domain/               # Domain Layer (Entities & Interfaces)
│   ├── auth/            # 인증 도메인
│   ├── insight/         # 인사이트 도메인
│   ├── interest/        # 관심사 도메인
│   ├── log/             # 로그 도메인
│   ├── push/            # 푸시 도메인
│   ├── signal/          # 키워드 시그널 도메인
│   └── user/            # 사용자 도메인
│
├── infrastructure/       # Infrastructure Layer
│   ├── analyzer/        # 분석 유틸리티 (시간 패턴 분석 등)
│   ├── cache/           # 캐시 구현
│   ├── config/           # 설정 클래스
│   ├── event/            # 도메인 이벤트 발행
│   ├── external/         # 외부 서비스 연동
│   │   ├── fcm/         # Firebase FCM
│   │   ├── insight/     # 인사이트 생성 관련
│   │   ├── oauth/       # OAuth 제공자 구현
│   │   └── openai/      # OpenAI 클라이언트
│   ├── persistence/     # JPA 리포지토리 구현
│   ├── scheduler/        # 스케줄러
│   └── security/         # 보안 관련 (JWT, 인증 필터)
│
├── presentation/         # Presentation Layer
│   ├── api/             # REST API Controllers
│   └── exception/       # 예외 처리 핸들러
│
└── common/              # 공통 유틸리티
    ├── exception/       # 예외 정의
    ├── pagination/      # 페이지네이션 유틸리티
    └── time/            # 시간 관련 유틸리티
```

### 아키텍처 원칙

- **Clean Architecture**: 레이어별 의존성 방향 준수
  - `domain` → `infrastructure`, `application`, `presentation` 의존 없음
  - `application` → `presentation` 의존 없음
  - `presentation` → `application` 의존 허용
- **Use Case Pattern**: 비즈니스 로직을 Use Case로 캡슐화
- **Repository Pattern**: 도메인과 인프라 분리

## 시작하기

### 사전 요구사항

- Java 17 이상
- PostgreSQL 12 이상
- Gradle 7.0 이상 (또는 Gradle Wrapper 사용)

### 환경 설정

1. **PostgreSQL 데이터베이스 생성**

```sql
CREATE DATABASE lifelog;
```

2. **환경 변수 설정**

`.env` 파일을 생성하거나 환경 변수로 설정:

```bash
# OpenAI
OPENAI_API_KEY=your_openai_api_key

# JWT
JWT_SECRET=your_jwt_secret_key

# OAuth
GOOGLE_CLIENT_ID_IOS=your_google_client_id_ios
GOOGLE_CLIENT_ID_AOS=your_google_client_id_aos

# FCM
FCM_SERVICE_ACCOUNT_PATH=classpath:fcm/service-account.json
```

3. **Firebase 서비스 계정 설정**

`src/main/resources/fcm/service-account.json` 파일에 Firebase 서비스 계정 키를 배치합니다.

### 실행

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun
```

서버는 `http://localhost:8080`에서 실행됩니다.

## API 문서

### 인증

#### 소셜 로그인
```http
POST /api/auth/oauth/google
Content-Type: application/json

{
  "idToken": "google_id_token"
}
```

**응답:**
```json
{
  "accessToken": "jwt_access_token",
  "refreshToken": "refresh_token",
  "displayName": "사용자 이름",
  "isNewUser": false
}
```

```http
POST /api/auth/oauth/kakao
POST /api/auth/oauth/naver
Content-Type: application/json

{
  "accessToken": "oauth_access_token"
}
```

#### 토큰 갱신
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "refresh_token"
}
```

**응답:**
```json
{
  "accessToken": "new_jwt_access_token",
  "refreshToken": "new_refresh_token"
}
```

#### 로그아웃
```http
POST /api/auth/logout
Content-Type: application/json

{
  "refreshToken": "refresh_token",
  "allDevices": false
}
```

### 로그

#### 로그 생성
```http
POST /api/logs
Content-Type: application/json
Authorization: Bearer {token}

{
  "content": "오늘 점심에 파스타를 먹었다"
}
```

**응답:**
```json
{
  "logId": 123
}
```

#### 로그 목록 조회
```http
GET /api/logs?limit=50&cursor={cursor}
Authorization: Bearer {token}
```

**응답:**
```json
{
  "items": [
    {
      "logId": 123,
      "createdAt": "2026-01-15T10:30:00Z",
      "createdAtLabel": "2026-01-15T10:30:00Z",
      "dateLabel": "2026.01.15",
      "timeLabel": "19:30",
      "preview": "오늘 점심에 파스타를 먹었다"
    }
  ],
  "nextCursor": "encoded_cursor_string",
  "hasNext": true
}
```

### 인사이트

#### 인사이트 목록 조회
```http
GET /api/insights?limit=20&cursor={cursor}
Authorization: Bearer {token}
```

**응답:**
```json
{
  "items": [
    {
      "id": 456,
      "kind": "PATTERN",
      "title": "인사이트 제목",
      "body": "인사이트 본문",
      "evidence": "증거",
      "keyword": "관련 키워드",
      "createdAt": "2026-01-15T10:30:00Z",
      "createdAtLabel": "2026-01-15T10:30:00Z",
      "dateLabel": "2026.01.15",
      "timeLabel": "19:30"
    }
  ],
  "nextCursor": "encoded_cursor_string",
  "hasNext": true
}
```

#### 인사이트 피드백 제출
```http
POST /api/insights/{insightId}/feedback
Content-Type: application/json
Authorization: Bearer {token}

{
  "vote": "LIKE",
  "reason": "RELEVANT",
  "score": 5,
  "comment": "유용한 인사이트입니다"
}
```

**응답:**
```json
{
  "insightId": 456,
  "vote": "LIKE",
  "reason": "RELEVANT",
  "comment": "유용한 인사이트입니다",
  "updatedAt": "2026-01-15T10:30:00Z"
}
```

#### 인사이트 피드백 조회
```http
GET /api/insights/{insightId}/feedback
Authorization: Bearer {token}
```

#### 인사이트 설정 조회
```http
GET /api/insights/settings
Authorization: Bearer {token}
```

**응답:**
```json
{
  "enabled": true
}
```

#### 인사이트 설정 수정
```http
POST /api/insights/settings
Content-Type: application/json
Authorization: Bearer {token}

{
  "enabled": true
}
```

### 관심사

#### 관심사 조회
```http
GET /api/interests
Authorization: Bearer {token}
```

**응답:**
```json
{
  "keywords": ["운동", "독서", "요리"]
}
```

#### 관심사 키워드 추가
```http
POST /api/interests
Content-Type: application/json
Authorization: Bearer {token}

{
  "keyword": "운동"
}
```

**응답:**
```json
{
  "keywords": ["운동", "독서", "요리", "운동"]
}
```

#### 관심사 키워드 삭제
```http
POST /api/interests/remove
Content-Type: application/json
Authorization: Bearer {token}

{
  "keyword": "운동"
}
```

### 홈

#### 홈 화면 조회
```http
GET /api/home?period=day&limitLogs=3&limitInsights=2
Authorization: Bearer {token}
```

**응답:**
```json
{
  "topInsight": {
    "date": "2026-01-15",
    "headline": "인사이트 제목",
    "signalCount": 5,
    "axes": [],
    "lastTimeLabel": "19:30"
  },
  "insights": [
    {
      "id": 456,
      "kind": "PATTERN",
      "title": "인사이트 제목",
      "body": "인사이트 본문",
      "evidence": "증거"
    }
  ],
  "recentLogs": [
    {
      "logId": 123,
      "timeLabel": "19:30",
      "preview": "오늘 점심에 파스타를 먹었다"
    }
  ]
}
```

### 사용자

#### 내 정보 조회
```http
GET /api/users/me
Authorization: Bearer {token}
```

**응답:**
```json
{
  "id": 1,
  "displayName": "사용자 이름",
  "createdAt": "2026-01-01T00:00:00Z",
  "lastLoginAt": "2026-01-15T10:30:00Z"
}
```

#### 내 정보 수정
```http
PATCH /api/users/me
Content-Type: application/json
Authorization: Bearer {token}

{
  "displayName": "새로운 이름"
}
```

#### 회원 탈퇴
```http
DELETE /api/users/me
Authorization: Bearer {token}
```

### 푸시

#### 푸시 토큰 등록/수정
```http
POST /api/push/token
Content-Type: application/json
Authorization: Bearer {token}

{
  "token": "fcm_device_token",
  "platform": "android"
}
```

**응답:**
```json
{
  "ok": true
}
```

#### 푸시 토큰 삭제
```http
DELETE /api/push/token?token={fcm_token}
Authorization: Bearer {token}
```

#### 푸시 설정 조회
```http
GET /api/push/settings
Authorization: Bearer {token}
```

**응답:**
```json
{
  "enabled": true
}
```

#### 푸시 설정 수정
```http
PUT /api/push/settings
Content-Type: application/json
Authorization: Bearer {token}

{
  "enabled": true
}
```

### 시그널

#### 시그널 오브젝트 조회
```http
GET /api/signal/objects
Authorization: Bearer {token}
```

**응답:**
```json
{
  "serverTime": "2026-01-15T10:30:00Z",
  "totalCandyCount": 100,
  "activeKeywords": [
    {
      "keywordKey": "운동",
      "insightText": "인사이트 텍스트",
      "candyCount": 50,
      "updatedAt": "2026-01-15T10:30:00Z"
    }
  ],
  "waterDrops": [
    {
      "keywordKey": "삭제된키워드",
      "createdAt": "2026-01-01T00:00:00Z",
      "updatedAt": "2026-01-10T00:00:00Z",
      "snapshotText": "스냅샷 텍스트"
    }
  ]
}
```

## 아키텍처

### 인사이트 생성 파이프라인

1. **로그 생성 이벤트 발생** (`RawLogCreatedEvent`)
2. **트리거 정책 검사** (`InsightTriggerPolicy`)
   - 인사이트 게이트 상태 확인
   - 키워드 매칭 확인
   - 쿨다운 시간 확인
   - 일일 제한 확인
3. **컨텍스트 빌드** (`InsightContextBuilder`)
   - 최근 로그 수집
   - 관련 인사이트 수집
4. **인사이트 생성** (`InsightGenerator`)
   - OpenAI API 호출 또는 휴리스틱 생성
5. **인사이트 저장 및 이벤트 발행**
   - `InsightCreatedEvent` 발행
   - 푸시 알림 발송

### 키워드 시그널 인사이트

- 키워드가 로그에 포함될 때마다 `candyCount` 증가
- `candyCount`가 마지막 생성 시점 대비 10 이상 증가하면 인사이트 재생성
- 스케줄러가 10분마다 모든 사용자의 ACTIVE 키워드를 스캔

### 푸시 알림 전략

1. **시간 패턴 기반 푸시** (`TIME_PATTERN_MISS`)
   - 평소 기록 시간대를 분석하여 해당 시간대에 기록이 없을 때 알림
   - 최근 14일 데이터 분석
   - 하루 최대 1회

2. **키워드 기반 푸시** (`KEYWORD_NUDGE`)
   - 관심 키워드 관련 기록을 유도하는 알림
   - 키워드별 쿨다운 적용 (기본 2일)
   - 하루 최대 1회

3. **인사이트 생성 푸시** (`INSIGHT_CREATED`)
   - 새로운 인사이트가 생성될 때 알림
   - 하루 최대 5회

## 환경 변수

### 필수 환경 변수

| 변수명 | 설명 | 기본값 |
|--------|------|--------|
| `OPENAI_API_KEY` | OpenAI API 키 | - |
| `JWT_SECRET` | JWT 서명 키 | - |
| `FCM_SERVICE_ACCOUNT_PATH` | FCM 서비스 계정 파일 경로 | `classpath:fcm/service-account.json` |

### 선택적 환경 변수

| 변수명 | 설명 | 기본값 |
|--------|------|--------|
| `GOOGLE_CLIENT_ID_IOS` | Google OAuth iOS 클라이언트 ID | - |
| `GOOGLE_CLIENT_ID_AOS` | Google OAuth Android 클라이언트 ID | - |

### 데이터베이스 설정

`application.yml`에서 데이터베이스 연결 정보를 설정합니다:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/lifelog
    username: postgres
    password: postgres
```

## 개발 가이드

### 코드 컨벤션

- Kotlin 코딩 컨벤션 준수
- ktlint를 사용한 코드 스타일 검사
- Use Case는 `execute()` 메서드로 단일 책임 원칙 준수
- DTO는 `Response`, `Request` 접미사 사용

### 빌드 및 테스트

```bash
# 코드 포맷팅
./gradlew ktlintFormat

# 코드 스타일 검사
./gradlew ktlintCheck

# 빌드
./gradlew build

# 테스트 실행
./gradlew test
```

## 라이선스

이 프로젝트는 개인 프로젝트입니다.
