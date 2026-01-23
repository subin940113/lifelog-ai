package com.example.lifelog.infrastructure.external.fcm

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import java.io.File

@Configuration
class FirebaseConfig(
    @Value("\${lifelog.push.fcm.serviceAccountPath}")
    private val serviceAccountPath: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val appName = "lifelog-fcm"
    private val fcmScope = "https://www.googleapis.com/auth/firebase.messaging"

    @Bean
    fun firebaseApp(): FirebaseApp {
        val resource = resolveResource(serviceAccountPath.trim())

        require(resource.exists()) {
            "FCM service account file not found. path='$serviceAccountPath', resolvedResource='${resource.description}'"
        }

        resource.inputStream.use { input ->
            // 1) 서비스계정 로드 + FCM scope 지정
            val credentials =
                GoogleCredentials
                    .fromStream(input)
                    .createScoped(listOf(fcmScope))

            // FirebaseOptions 구성
            val options =
                FirebaseOptions
                    .builder()
                    .setCredentials(credentials)
                    .build()

            // default app 충돌 방지: named app 사용
            val existingApp = FirebaseApp.getApps().firstOrNull { it.name == appName }
            val app = existingApp ?: FirebaseApp.initializeApp(options, appName)
            log.info(
                "[FCM] firebase app initialized name={} resource={}",
                app.name,
                resource.description,
            )
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
