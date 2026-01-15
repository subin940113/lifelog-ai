package com.example.lifelog.push.fcm

import com.example.lifelog.push.PushTokenRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class FirebaseFcmClient(
    private val firebaseApp: FirebaseApp?,
    private val pushTokenRepo: PushTokenRepository,
) : FcmClient {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun send(
        token: String,
        title: String,
        body: String,
        data: Map<String, String>,
    ) {
        val app =
            firebaseApp ?: run {
                log.info("[FCM] skipped (disabled) title={}", title)
                return
            }

        val builder =
            Message
                .builder()
                .setToken(token)
                .setNotification(
                    Notification
                        .builder()
                        .setTitle(title)
                        .setBody(body)
                        .build(),
                )

        data.forEach { (k, v) -> builder.putData(k, v) }

        val message = builder.build()

        try {
            val id = FirebaseMessaging.getInstance(app).send(message)
            log.info("[FCM] sent id={} tokenPrefix={} title={}", id, token.take(8), title)
        } catch (e: FirebaseMessagingException) {
            // UNREGISTERED면 토큰 제거
            val isUnregistered =
                e.messagingErrorCode?.name == "UNREGISTERED" ||
                    (e.cause?.message?.contains("UNREGISTERED", ignoreCase = true) == true)

            if (isUnregistered) {
                val deleted = pushTokenRepo.deleteByToken(token)
                log.info(
                    "[FCM] token unregistered -> deleted={} tokenPrefix={} title={}",
                    deleted,
                    token.take(8),
                    title,
                )
                return // 여기서 끝내고 스케줄러는 계속 돌게
            }

            log.warn("[FCM] failed tokenPrefix={} title={}", token.take(8), title, e)
            throw e
        } catch (e: Exception) {
            log.warn("[FCM] failed tokenPrefix={} title={}", token.take(8), title, e)
            throw e
        }
    }
}
