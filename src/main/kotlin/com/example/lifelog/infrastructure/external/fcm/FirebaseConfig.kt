package com.example.lifelog.infrastructure.external.fcm

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
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
            val options =
                FirebaseOptions
                    .builder()
                    .setCredentials(GoogleCredentials.fromStream(input))
                    .build()

            return if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
            } else {
                FirebaseApp.getInstance()
            }
        }
    }

    private fun resolveResource(path: String): Resource {
        if (path.startsWith("classpath:")) {
            return ClassPathResource(path.removePrefix("classpath:").removePrefix("/"))
        }
        if (path.startsWith("file:")) {
            return FileSystemResource(File(path.removePrefix("file:")))
        }
        return FileSystemResource(File(path))
    }
}
