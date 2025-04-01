# 커뮤니티 게시판 서비스

Spring Boot와 AWS S3를 활용한 게시판 커뮤니티 서비스의 백엔드 API입니다.
<br/>
<br/>
## 📚 프로젝트 개요
본 프로젝트는 Spring Boot 기반의 RESTful API 서비스로, 회원 관리 및 게시판 기능을 구현한 웹 애플리케이션의 백엔드입니다.  
JWT(Json Web Token)를 활용해 토큰 기반 인증 시스템을 구축하였으며, AWS S3 스토리지를 통해 이미지 처리를 효율적으로 관리합니다.
<br/>
<br/>
## ⚙️ 요구 스펙

- **언어**: Java  
- **프레임워크**: Spring Boot  
- **데이터베이스**: MySQL  
- **ORM**: JPA  
- **인증**: Spring Security, JWT  
- **스토리지**: AWS S3  
- **빌드 도구**: Gradle
<br/>
<br/>

## 🗂️ 주요 기능

### 회원(User) 기능

- **인증 관련**
  - 회원가입 및 로그인
  - JWT 토큰 기반 인증
  - 프로필 이미지 업로드 및 관리 (S3 스토리지 활용)

- **회원 정보 관리**
  - 회원 정보 조회 및 수정
  - 비밀번호 변경
  - 회원 탈퇴

### 게시판(Post) 기능

- **게시글 관리**
  - 게시글 생성, 수정, 삭제
  - 게시글 이미지 첨부 기능 (S3 스토리지 활용)
  - 전체 게시글 목록 조회 (페이징 처리)
  - 게시글 상세 조회
  - 조회수 관리

- **상호작용**
  - 게시글 좋아요 기능
  - 댓글 생성, 조회, 삭제
<br/>
<br/>

## 🏷️ ERD

![2- rick-lee-ERD](https://github.com/user-attachments/assets/e2e4667d-f077-42d5-8f17-61a21fb18f8f)


### 테이블 속성 요약

**User (유저)**

| 컬럼명            | 설명                                   |
|-------------------|----------------------------------------|
| `user_id`         | 유저 고유 ID (PK, Auto Increment)       |
| `email`           | 유저 이메일 (중복 불가, Unique)         |
| `password`        | 유저 비밀번호                          |
| `nickname`        | 유저 닉네임 (중복 불가, Unique)         |
| `profile_img_url` | 프로필 이미지 경로 (S3 URL, Nullable)   |
| `created_at`      | 계정 생성 일시                         |
| `updated_at`      | 계정 정보 수정 일시                    |
| `deleted_at`      | 계정 삭제 요청 일시 (Soft Delete용)     |


**Post (게시글)**

| 컬럼명           | 설명                                       |
|------------------|--------------------------------------------|
| `post_id`        | 게시글 고유 ID (PK, Auto Increment)        |
| `user_id`        | 작성자 유저 ID (FK)                         |
| `title`          | 게시글 제목 (최대 26자)                    |
| `content`        | 게시글 본문 내용                            |
| `post_img_url`   | 게시글 이미지 URL (S3 URL, Nullable)       |
| `view_num`       | 조회수 (기본값 0)                          |
| `comment_num`    | 댓글 수                                   |
| `created_at`     | 게시글 생성 일시                           |
| `updated_at`     | 게시글 수정 일시                           |
| `deleted_at`     | 게시글 삭제 일시 (Soft Delete용)           |

**Comment (댓글)**

| 컬럼명            | 설명                                          |
|-------------------|-----------------------------------------------|
| `comment_id`      | 댓글 고유 ID (PK, Auto Increment)             |
| `user_id`         | 댓글 작성자 ID (FK)                           |
| `post_id`         | 댓글이 달린 게시글 ID (FK)                    |
| `comment_body`    | 댓글 내용                                     |
| `created_at`      | 댓글 작성 일시                                |
| `update_at`       | 댓글 수정 일시                                |
| `deleted_at`      | 댓글 삭제 일시 (Soft Delete용)                |


**Like (좋아요)**

| 컬럼명        | 설명                                          |
|---------------|-----------------------------------------------|
| `id`          | 좋아요 고유 ID (PK, Auto Increment)           |
| `post_id`     | 좋아요한 게시글 ID (FK)                       |
| `user_id`     | 좋아요를 누른 유저 ID (FK)                    |
| `created_at`  | 좋아요 클릭 일시                              |



## 🌟 프로젝트 구조

```
└─src
    ├─main
    │  ├─java
    │  │  └─com
    │  │      └─ricklee
    │  │          └─community
    │  │              ├─config
    │  │              │  ├─jwt
    │  │              │  └─security
    │  │              ├─controller
    │  │              ├─domain
    │  │              ├─dto
    │  │              │  ├─comment
    │  │              │  ├─common
    │  │              │  ├─like
    │  │              │  ├─post
    │  │              │  └─user
    │  │              ├─exception
    │  │              │  ├─custom
    │  │              │  └─handler
    │  │              ├─repository
    │  │              ├─service
    │  │              └─util
    │  │                  └─jwt
    │  └─resources
    └─test
        └─java
            └─com
                └─ricklee
                    └─community
                        ├─config
                        ├─controller
                        └─service
```

---

## 🏃 설치 및 실행 방법

### 1. 사전 요구사항

- Java 23
- MySQL
- AWS 계정 및 S3 버킷

### 2. 프로젝트 클론

```bash
git clone https://github.com/your-username/community-backend.git
cd community-backend
```

### 3. MySQL 데이터베이스 설정

```sql
CREATE DATABASE community;
```

### 4. `application.properties` 또는 `application.yml` 설정

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/community
    username: your_username
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update

jwt:
  secret: your_jwt_secret_key
  expiration: 86400000

aws:
  s3:
    access-key: ${AWS_S3_ACCESS_KEY}
    secret-key: ${AWS_S3_SECRET_KEY}
    region: ap-northeast-2
    bucket-name: your-bucket-name
```

### 5. 환경 변수 설정

```bash
export AWS_S3_ACCESS_KEY=your_access_key
export AWS_S3_SECRET_KEY=your_secret_key
```

### 6. 애플리케이션 빌드 및 실행

```bash
./gradlew build
./gradlew bootRun
```

---

## 📌 API 명세

### 인증 API

| 기능       | 메서드 | 엔드포인트          | 설명              |
|------------|--------|----------------------|-------------------|
| 회원가입    | POST   | `/users/signup`       | 새 사용자 등록      |
| 로그인      | POST   | `/users/login`        | 사용자 인증 및 토큰 발급 |

### 사용자 API

| 기능               | 메서드 | 엔드포인트                      | 설명                    |
|--------------------|--------|----------------------------------|-------------------------|
| 사용자 정보 조회      | GET    | `/users/{userId}`               | 특정 사용자 정보 조회        |
| 프로필 이미지 업로드 | POST   | `/users/{userId}/profile-image` | 프로필 이미지 업로드        |
| 사용자 정보 수정      | PUT    | `/users/{userId}`               | 사용자 정보 업데이트        |
| 비밀번호 변경         | PUT    | `/users/{userId}/password`      | 비밀번호 변경             |
| 회원 탈퇴            | DELETE | `/users`                        | 사용자 계정 삭제           |

### 게시글 API

| 기능             | 메서드 | 엔드포인트               | 설명               |
|------------------|--------|---------------------------|--------------------|
| 게시글 작성        | POST   | `/posts`                 | 새 게시글 작성        |
| 게시글 목록 조회    | GET    | `/posts`                 | 게시글 목록 조회 (페이징) |
| 게시글 상세 조회    | GET    | `/posts/{postId}`        | 특정 게시글 상세 조회   |
| 게시글 수정        | PUT    | `/posts/{postId}`        | 게시글 내용 수정      |
| 게시글 삭제        | DELETE | `/posts/{postId}`        | 게시글 삭제          |
| 게시글 좋아요       | POST   | `/posts/{postId}/like`   | 게시글 좋아요 추가     |
| 게시글 좋아요 취소   | DELETE | `/posts/{postId}/like`   | 게시글 좋아요 취소     |

### 댓글 API

| 기능           | 메서드 | 엔드포인트                      | 설명               |
|----------------|--------|----------------------------------|--------------------|
| 댓글 작성        | POST   | `/posts/{postId}/comments`       | 게시글에 새 댓글 작성  |
| 댓글 목록 조회    | GET    | `/posts/{postId}/comments`       | 댓글 목록 조회        |
| 댓글 수정        | PUT    | `/comments/{commentId}`          | 댓글 내용 수정        |
| 댓글 삭제        | DELETE | `/comments/{commentId}`          | 댓글 삭제            |

---

## 💬 소감

이번 프로젝트를 진행하며 예상치 못한 문제들을 여럿 마주했지만, 그 과정을 통해 많은 것을 배웠습니다. 테스트 코드를 잘못된 폴더에 넣어 발생한 `@Autowired` 오류부터 `@ExceptionHandler`를 제대로 활용하지 못한 점, 그리고 API 명세서와 실제 구현 간의 디테일 차이 등 다양한 시행착오를 겪었습니다. 특히, 프론트와의 데이터 양식 불일치나 변수명 미흡으로 인한 JWT 관련 이슈는 백엔드와 프론트 간의 긴밀한 소통의 중요성을 다시금 깨닫게 해주었습니다. 팀원 피드백을 통해 테스트에 과도하게 매몰되어 있었음을 인지하고, 앞으로는 더 유연하게 개발 흐름을 조율할 계획입니다. 실제로 구현하면서 이론과 현실의 간극을 체감하며, 이 경험들이 더욱 단단한 성장의 발판이 되었습니다.
