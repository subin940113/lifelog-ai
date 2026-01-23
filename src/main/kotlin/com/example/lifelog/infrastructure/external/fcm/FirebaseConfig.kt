package com.example.lifelog.infrastructure.external.fcm

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import java.io.File
import java.io.FileNotFoundException

@Configuration
class FirebaseConfig(
    @Value("\${lifelog.push.fcm.serviceAccountPath}")
    private val serviceAccountPath: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val appName = "lifelog-fcm"
    private val projectId = "bluelog-4bc1c"
    private val fcmScope = "https://www.googleapis.com/auth/firebase.messaging"

    @Bean
    fun firebaseApp(): FirebaseApp {
        val resource = resolveResource(serviceAccountPath.trim())

        if (!resource.exists()) {
            throw FileNotFoundException(
                "FCM service account file not found. " +
                        "path='$serviceAccountPath', resolvedResource='${resource.description}'",
            )
        }

        resource.inputStream.use { input ->
            // 1) 서비스계정 로드 + FCM scope 지정
            val credentials =
                GoogleCredentials
                    .fromStream(input)
                    .createScoped(listOf(fcmScope))

            // 2) [핵심] 부팅 시점에 Access Token을 강제로 발급해본다.
            //    - 여기서 실패하면 FCM은 절대 동작할 수 없으니(Always-on) 서버를 즉시 실패시키는 게 맞다.
            try {
                val token = credentials.refreshAccessToken()
                log.info(
                    "[FCM] access token issued. len={} expiresAt={} projectId={}",
                    token.tokenValue.length,
                    token.expirationTime,
                    projectId,
                )
            } catch (e: Exception) {
                log.error(
                    "[FCM] access token issuance FAILED. " +
                            "Check: service account key validity, outbound network/proxy, server clock, IAM policies. " +
                            "resource={}",
                    resource.description,
                    e,
                )
                throw e
            }

            // 3) FirebaseOptions 구성 (projectId 명시)
            val options =
                FirebaseOptions
                    .builder()
                    .setCredentials(credentials)
                    .setProjectId(projectId)
                    .build()

            // 4) default app 충돌 방지: named app 사용
            val existingApp = FirebaseApp.getApps().firstOrNull { it.name == appName }
            val app = existingApp ?: FirebaseApp.initializeApp(options, appName)

            // 5) FirebaseMessaging 워밍업 (추가 안전장치)
            try {
                FirebaseMessaging.getInstance(app)
                log.info(
                    "[FCM] firebase app initialized name={} resource={} projectId={}",
                    app.name,
                    resource.description,
                    projectId,
                )
            } catch (e: Exception) {
                log.error(
                    "[FCM] firebase messaging init FAILED name={} resource={} projectId={}",
                    app.name,
                    resource.description,
                    projectId,
                    e,
                )
                throw e
            }

            return app
        }
    }

    private fun resolveResource(path: String): Resource =
        when {
            path.startsWith("classpath:") ->
                ClassPathResource(path.removePrefix("classpath:").removePrefix("/"))

            path.startsWith("file:") ->
                FileSystemResource(File(path.removePrefix("file:")))

            else ->
                FileSystemResource(File(path))
        }
}