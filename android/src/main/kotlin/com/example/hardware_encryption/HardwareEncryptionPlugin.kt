package com.example.hardware_encryption

import androidx.annotation.NonNull
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.*
import javax.crypto.*
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64
import androidx.annotation.VisibleForTesting

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** HardwareEncryptionPlugin */
class HardwareEncryptionPlugin : FlutterPlugin, MethodCallHandler {

    companion object {
        private const val AES_MODE_M = "AES/GCM/NoPadding"
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    }

    private lateinit var keyStore: KeyStore

    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {

        keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)


        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "hardware_encryption")
        channel.setMethodCallHandler(this)
    }

    private fun generateEncryptKey(alias: String) {
        try {
            if (!keyStore.containsAlias(alias)) {

                val keyGenerator =
                    KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
                val keyGenParameterSpecBuilder = KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )

                val keyGenParameter = keyGenParameterSpecBuilder
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(false).build()

                keyGenerator.init(keyGenParameter)
                keyGenerator.generateKey()
            }
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        }

    }


    fun encrypt(input: String, alias: String): Pair<String, String>? {
        return try {
            val aesIv = generateRandomIV()
            val cipher = getCipherFromIv(aesIv, Cipher.ENCRYPT_MODE, alias)
            val encodedBytes: ByteArray = cipher.doFinal(input.toByteArray(Charsets.UTF_8))

            Pair(aesIv, Base64.encodeToString(encodedBytes, Base64.NO_WRAP))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }

    fun decrypt(publicIv: String, encrypted: String, alias: String): String? {
        return try {
            val decodedValue = Base64.decode(encrypted.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            val cipher = getCipherFromIv(publicIv, Cipher.DECRYPT_MODE, alias)
            val decryptedVal: ByteArray = cipher.doFinal(decodedValue)

            String(decryptedVal)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }


    private fun generateRandomIV(): String {
        val random = SecureRandom()
        val generated: ByteArray = random.generateSeed(12)
        return Base64.encodeToString(generated, Base64.NO_WRAP)
    }

    private fun getCipherFromIv(iv: String, cipherMode: Int, alias: String): Cipher {
        val cipher: Cipher = Cipher.getInstance(AES_MODE_M)

        val aesKey: SecretKey = keyStore.getKey(alias, null) as SecretKey
        try {

            val parameterSpec = GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP))
            cipher.init(cipherMode, aesKey, parameterSpec)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cipher
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        if (call.method == "getPlatformVersion") {
            result.success("Android ${android.os.Build.VERSION.RELEASE}")
        } else if (call.method == "generateKey") {
            val alias = call.argument<String>("alias")!!
            generateEncryptKey(alias)
            result.success(true)
        } else if (call.method == "encrypt") {
            val alias = call.argument<String>("alias")!!
            val input = call.argument<String>("data")!!
            val encrypted = encrypt(input, alias)
            if (encrypted == null) {
                result.error("ENCRYPT_ERROR", "Error while encrypting", null)
                return
            }
            result.success(encrypted.first + encrypted.second)
        } else if (call.method == "decrypt") {
            val alias = call.argument<String>("alias")!!
            val input = call.argument<String>("data")!!
            val iv = input.substring(0, 16)
            val encrypted = input.substring(16)
            val decrypted = decrypt(iv, encrypted, alias)
            if (decrypted == null) {
                result.error("DECRYPT_ERROR", "Error while decrypting", null)
                return
            }
            result.success(decrypted)
        } else {
            result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}
