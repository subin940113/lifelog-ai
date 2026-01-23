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
            // 1. 명시적으로 FCM scope 포함한 credentials 생성
            val credentials =
                GoogleCredentials
                    .fromStream(input)
                    .createScoped(
                        listOf("https://www.googleapis.com/auth/firebase.messaging"),
                    )

            // 2. FirebaseOptions 구성 (projectId 반드시 명시)
            val options =
                FirebaseOptions
                    .builder()
                    .setCredentials(credentials)
                    .setProjectId("bluelog-4bc1c")
                    .build()

            // 3. default app 충돌 방지를 위해 named app 사용
            val existingApp =
                FirebaseApp.getApps().firstOrNull { it.name == appName }

            val app =
                existingApp ?: FirebaseApp.initializeApp(options, appName)

            // 4. 부팅 시점 워밍업 (인증 문제를 send 시점이 아니라 여기서 터뜨리기)
            try {
                FirebaseMessaging.getInstance(app)
                log.info(
                    "[FCM] firebase app initialized name={} resource={}",
                    app.name,
                    resource.description,
                )
            } catch (e: Exception) {
                log.error(
                    "[FCM] firebase app initialization failed name={} resource={}",
                    app.name,
                    resource.description,
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
