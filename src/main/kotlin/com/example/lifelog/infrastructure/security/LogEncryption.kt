package com.example.lifelog.infrastructure.security

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

/**
 * 로그 내용 암호화/복호화 컴포넌트
 * AES-256-GCM 사용
 */
@Component
class LogEncryption(
    @Value("\${lifelog.encryption.key:}")
    private val encryptionKey: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val algorithm = "AES/GCM/NoPadding"
    private val keyLength = 256
    private val ivLength = 12 // GCM 권장 IV 길이
    private val tagLength = 128 // GCM 태그 길이 (비트)

    private val secretKey: SecretKey by lazy {
        if (encryptionKey.isBlank()) {
            log.warn("LOG_ENCRYPTION_KEY not set, using in-memory key (NOT SECURE FOR PRODUCTION)")
            generateTemporaryKey()
        } else {
            val keyBytes = encryptionKey.toByteArray(Charsets.UTF_8)
            if (keyBytes.size < 32) {
                throw IllegalArgumentException("Encryption key must be at least 32 bytes (256 bits)")
            }
            SecretKeySpec(keyBytes.take(32).toByteArray(), "AES")
        }
    }

    /**
     * 로그 내용 암호화
     */
    fun encrypt(plaintext: String): String {
        if (plaintext.isBlank()) return plaintext

        try {
            val cipher = Cipher.getInstance(algorithm)
            val iv =
                ByteArray(ivLength).apply {
                    Random.Default.nextBytes(this)
                }
            val parameterSpec = GCMParameterSpec(tagLength, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)

            val encrypted = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            // IV + 암호문을 Base64로 인코딩
            val combined = iv + encrypted
            return Base64.getEncoder().encodeToString(combined)
        } catch (e: Exception) {
            log.error("Failed to encrypt log content", e)
            throw RuntimeException("Encryption failed", e)
        }
    }

    /**
     * 로그 내용 복호화
     */
    fun decrypt(encrypted: String): String {
        if (encrypted.isBlank()) return encrypted

        try {
            val decoded = Base64.getDecoder().decode(encrypted)
            if (decoded.size < ivLength) {
                // 암호화되지 않은 기존 데이터일 수 있음
                log.debug("Data too short for decryption, returning as-is")
                return encrypted
            }

            val iv = decoded.take(ivLength).toByteArray()
            val ciphertext = decoded.drop(ivLength).toByteArray()

            val cipher = Cipher.getInstance(algorithm)
            val parameterSpec = GCMParameterSpec(tagLength, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)

            val decrypted = cipher.doFinal(ciphertext)
            return String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            // 암호화되지 않은 기존 데이터일 수 있음
            log.debug("Failed to decrypt, returning as-is (may be unencrypted legacy data): ${e.message}")
            return encrypted
        }
    }

    /**
     * 데이터가 암호화되어 있는지 확인
     */
    fun isEncrypted(data: String): Boolean {
        if (data.isBlank()) return false
        return try {
            val decoded = Base64.getDecoder().decode(data)
            decoded.size >= ivLength
        } catch (e: Exception) {
            false
        }
    }

    private fun generateTemporaryKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(keyLength)
        return keyGenerator.generateKey()
    }
}
