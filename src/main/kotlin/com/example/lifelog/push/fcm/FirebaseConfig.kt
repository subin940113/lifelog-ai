package com.example.lifelog.push.fcm

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource

@Configuration
class FirebaseConfig(
    @Value("\${lifelog.push.fcm.serviceAccountPath}")
    private val credentialsResource: Resource,
) {
    @Bean
    fun firebaseApp(): FirebaseApp {
        credentialsResource.inputStream.use { input ->
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
}
