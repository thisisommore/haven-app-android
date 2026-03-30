package com.example.haven.xxdk

import android.util.Base64
import java.io.ByteArrayOutputStream
import java.util.zip.Deflater

/**
 * Message encoding utilities
 * Mirrors iOS encodeMessage functionality
 */
object MessageEncoding {
    
    /**
     * Encode a message string for sending via XXDK
     * Compresses with zlib (with proper header and ADLER32 checksum) and base64 encodes
     */
    fun encodeMessage(message: String, compress: Boolean = true): String? {
        return try {
            val utf8Data = message.toByteArray(Charsets.UTF_8)
            
            val dataToEncode = if (compress) {
                compressZlib(utf8Data) ?: return null
            } else {
                utf8Data
            }
            
            Base64.encodeToString(dataToEncode, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Compress data using zlib format with proper header and ADLER32 checksum
     * Matches iOS compressZlib implementation
     */
    private fun compressZlib(data: ByteArray): ByteArray? {
        val deflater = Deflater(Deflater.DEFAULT_COMPRESSION, true) // true = no zlib header
        deflater.setInput(data)
        deflater.finish()
        
        val outputStream = ByteArrayOutputStream(data.size + 32)
        
        // Add zlib header (2 bytes) - 0x78 0x9C is default compression
        outputStream.write(0x78)
        outputStream.write(0x9C)
        
        // Compress the data
        val buffer = ByteArray(1024)
        while (!deflater.finished()) {
            val count = deflater.deflate(buffer)
            outputStream.write(buffer, 0, count)
        }
        deflater.end()
        
        // Calculate and append ADLER32 checksum (4 bytes, big-endian)
        val adler32 = calculateAdler32(data)
        outputStream.write((adler32 shr 24) and 0xFF)
        outputStream.write((adler32 shr 16) and 0xFF)
        outputStream.write((adler32 shr 8) and 0xFF)
        outputStream.write(adler32 and 0xFF)
        
        return outputStream.toByteArray()
    }
    
    /**
     * Calculate ADLER32 checksum for zlib format
     */
    private fun calculateAdler32(data: ByteArray): Int {
        var a = 1
        var b = 0
        
        for (byte in data) {
            a = (a + (byte.toInt() and 0xFF)) % 65521
            b = (b + a) % 65521
        }
        
        return (b shl 16) or a
    }
}
