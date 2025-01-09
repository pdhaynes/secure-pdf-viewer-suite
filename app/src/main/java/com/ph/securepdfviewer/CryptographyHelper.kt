package com.ph.securepdfviewer.Helpers

import android.content.Context
import android.net.Uri
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptographyHelper {
    fun encodeFile(fileBytes: ByteArray, fileKey: SecretKey): Pair<ByteArray, ByteArray> {
        val encryptCipher = Cipher.getInstance("AES/GCM/NoPadding")

        encryptCipher.init(Cipher.ENCRYPT_MODE, fileKey)

        val encryptedData = encryptCipher.doFinal(fileBytes)

        return Pair(encryptCipher.iv, encryptedData)
    }

    fun saveEncodedFile(file: File, fileData: Pair<ByteArray, ByteArray>) {
        FileOutputStream(file).use { outputStream ->
            outputStream.write(fileData.first)
            outputStream.write(fileData.second)
        }
    }

    fun decodeFile(file: File, secretKey: SecretKey): ByteArray {
        val encodedData = ByteArrayOutputStream()
        val readIV = ByteArray(12)
        FileInputStream(file).use { fileInputStream ->
            val ivBytesRead = fileInputStream.read(readIV)
            if (ivBytesRead != readIV.size) {
                throw IOException("Failed to read the entire IV from the file")
            }

            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                encodedData.write(buffer, 0, bytesRead)
            }
        }

        val resultData = encodedData.toByteArray()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, readIV)

        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return cipher.doFinal(resultData)
    }

    fun getSecretKeyFromKeystore(fileName: String): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }

        var key = keyStore.getKey(fileName, null) as SecretKey?

        if (key == null) {
            Log.w("SecretKey", "${fileName} not found in keystore.")
            key = generateSecretKey(fileName)
        }

        return key
    }

    fun generateSecretKey(fileName: String): SecretKey {
        val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")

        val keyGenSpec = KeyGenParameterSpec.Builder(fileName,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()

        keyGen.init(keyGenSpec)

        return keyGen.generateKey()
    }
}