package com.example.haven.xxdk

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.util.Base64
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.EnumMap

/**
 * QR Code utilities for sharing and scanning DM contact info
 * Matches iOS QRData format: haven://dm?token=<token>&pubKey=<base64>&codeset=<codeset>
 */
object QRCodeUtils {
    
    private const val QR_SCHEME = "haven"
    private const val QR_HOST = "dm"
    private const val PARAM_TOKEN = "token"
    private const val PARAM_PUBKEY = "pubKey"
    private const val PARAM_CODESET = "codeset"
    
    /**
     * Data class representing QR code content
     */
    data class QRData(
        val token: Long,
        val pubKey: ByteArray,
        val codeset: Int
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as QRData
            return token == other.token && 
                   pubKey.contentEquals(other.pubKey) && 
                   codeset == other.codeset
        }
        
        override fun hashCode(): Int {
            var result = token.hashCode()
            result = 31 * result + pubKey.contentHashCode()
            result = 31 * result + codeset
            return result
        }
    }
    
    /**
     * Parse scanned QR code string into QRData
     * Format: haven://dm?token=<token>&pubKey=<base64>&codeset=<codeset>
     */
    fun parseQRCode(code: String): QRData? {
        val uri = try {
            Uri.parse(code)
        } catch (e: Exception) {
            return null
        }
        
        // Validate scheme and host
        if (uri.scheme != QR_SCHEME || uri.host != QR_HOST) {
            return null
        }
        
        // Parse query parameters
        val tokenStr = uri.getQueryParameter(PARAM_TOKEN) ?: return null
        val pubKeyStr = uri.getQueryParameter(PARAM_PUBKEY) ?: return null
        val codesetStr = uri.getQueryParameter(PARAM_CODESET) ?: return null
        
        // Parse values
        val token = tokenStr.toLongOrNull() ?: return null
        val pubKey = Base64.decode(pubKeyStr, Base64.DEFAULT)
        if (pubKey.isEmpty()) return null
        
        val codeset = codesetStr.toIntOrNull() ?: return null
        
        return QRData(token, pubKey, codeset)
    }
    
    /**
     * Generate QR code URL string from QRData
     */
    fun generateQRUrl(data: QRData): String {
        val pubKeyBase64 = Base64.encodeToString(data.pubKey, Base64.URL_SAFE or Base64.NO_WRAP)
        return Uri.Builder()
            .scheme(QR_SCHEME)
            .authority(QR_HOST)
            .appendQueryParameter(PARAM_TOKEN, data.token.toString())
            .appendQueryParameter(PARAM_PUBKEY, pubKeyBase64)
            .appendQueryParameter(PARAM_CODESET, data.codeset.toString())
            .build()
            .toString()
    }
    
    /**
     * Generate QR code bitmap from QRData
     * @param size Size in pixels (default 512)
     */
    fun generateQRBitmap(data: QRData, size: Int = 512): Bitmap? {
        val url = generateQRUrl(data)
        return generateQRBitmap(url, size)
    }
    
    /**
     * Generate QR code bitmap from string
     */
    fun generateQRBitmap(content: String, size: Int = 512): Bitmap? {
        return try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.MARGIN, 2)
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
            }
            
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            
            bitmap
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get dmToken as Int32 (truncated from Int64) like iOS does
     */
    fun getDMTokenAsInt(token: Long): Int {
        return token.toInt()
    }
}
