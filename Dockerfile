# ===== build stage =====
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# (1) Gradle wrapper 파일만 먼저 복사 → 캐시 효율
COPY gradlew .
COPY gradle gradle
COPY build.gradle* settings.gradle* gradle.properties* ./

# (2) 실행권한 부여
RUN chmod +x ./gradlew

# (3) 의존성 캐시(실패해도 원인 로그가 남게)
RUN ./gradlew --no-daemon dependencies || true

# (4) 소스 복사 후 실제 빌드
COPY . .
RUN ./gradlew --no-daemon clean bootJar -x test

# ===== run stage =====
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]