package com.mtunes.app.security

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileValidator @Inject constructor() {

    private val allowedExtensions = setOf(
        "mp3", "flac", "aac", "m4a", "ogg", "wav"
    )

    private val blockedExtensions = setOf(
        "exe", "apk", "zip", "rar", "7z", "bat", "ps1",
        "jar", "scr", "mp4", "mkv", "avi", "mov", "wmv",
        "dll", "sys", "cmd", "vbs", "js", "msi", "com"
    )

    private val audioMimeTypes = setOf(
        "audio/mpeg",
        "audio/flac",
        "audio/aac",
        "audio/mp4",
        "audio/ogg",
        "audio/wav",
        "audio/x-wav",
        "audio/x-flac",
        "audio/x-m4a"
    )

    // Magic bytes for audio formats
    private val audioSignatures = mapOf(
        "mp3" to listOf(
            byteArrayOf(0xFF.toByte(), 0xFB.toByte()),
            byteArrayOf(0xFF.toByte(), 0xF3.toByte()),
            byteArrayOf(0xFF.toByte(), 0xF2.toByte()),
            byteArrayOf(0x49, 0x44, 0x33) // ID3
        ),
        "flac" to listOf(
            byteArrayOf(0x66, 0x4C, 0x61, 0x43) // fLaC
        ),
        "ogg" to listOf(
            byteArrayOf(0x4F, 0x67, 0x67, 0x53) // OggS
        ),
        "wav" to listOf(
            byteArrayOf(0x52, 0x49, 0x46, 0x46) // RIFF
        ),
        "m4a" to listOf(
            byteArrayOf(0x00, 0x00, 0x00, 0x20, 0x66, 0x74, 0x79, 0x70),
            byteArrayOf(0x00, 0x00, 0x00, 0x18, 0x66, 0x74, 0x79, 0x70)
        )
    )

    fun isAllowedExtension(filename: String): Boolean {
        val ext = filename.substringAfterLast('.', "").lowercase()
        return ext in allowedExtensions
    }

    fun isBlockedExtension(filename: String): Boolean {
        val ext = filename.substringAfterLast('.', "").lowercase()
        return ext in blockedExtensions
    }

    fun isAllowedMimeType(mimeType: String): Boolean {
        return mimeType.lowercase() in audioMimeTypes
    }

    fun validateFileSignature(bytes: ByteArray, expectedExtension: String): Boolean {
        val signatures = audioSignatures[expectedExtension.lowercase()] ?: return false
        return signatures.any { sig ->
            bytes.size >= sig.size && bytes.sliceArray(sig.indices).contentEquals(sig)
        }
    }

    fun validateTorrentFiles(filenames: List<String>): ValidationResult {
        if (filenames.isEmpty()) {
            return ValidationResult(false, "No files in torrent")
        }

        val hasBlocked = filenames.any { isBlockedExtension(it) }
        if (hasBlocked) {
            return ValidationResult(false, "Torrent contains blocked file types")
        }

        val audioFiles = filenames.filter { isAllowedExtension(it) }
        val nonAudioFiles = filenames.filter { !isAllowedExtension(it) }

        if (audioFiles.isEmpty()) {
            return ValidationResult(false, "No audio files found in torrent")
        }

        if (nonAudioFiles.isNotEmpty()) {
            val unknownExts = nonAudioFiles.map { it.substringAfterLast('.') }.toSet()
            // Allow common metadata files
            val allowedMeta = setOf("txt", "nfo", "jpg", "jpeg", "png", "cue", "log")
            val suspicious = unknownExts - allowedMeta
            if (suspicious.isNotEmpty()) {
                return ValidationResult(false, "Torrent contains suspicious files: $suspicious")
            }
        }

        return ValidationResult(true, "Safe: ${audioFiles.size} audio files")
    }

    data class ValidationResult(
        val isValid: Boolean,
        val message: String
    )
}
