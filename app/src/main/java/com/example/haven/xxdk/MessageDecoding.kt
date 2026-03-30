package com.example.haven.xxdk

import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.DataFormatException
import java.util.zip.Inflater

object MessageDecoding {

    /**
     * Decompress zlib-compressed data.
     * Uses raw DEFLATE mode - skips zlib wrapper (2-byte header + 4-byte ADLER32)
     */
    fun decompressZlib(data: ByteArray): ByteArray? {
        val headerSize = 2
        val adler32Size = 4
        if (data.size <= headerSize + adler32Size) {
            android.util.Log.e("MessageDecoding", "Data too small for zlib: ${data.size}")
            return null
        }

        return try {
            // Skip 2-byte zlib header, exclude 4-byte ADLER32 checksum at end
            val compressedData = data.copyOfRange(headerSize, data.size - adler32Size)
            
            android.util.Log.d("MessageDecoding", "Compressed data range: ${headerSize} to ${data.size - adler32Size}, size: ${compressedData.size}")
            
            // Use raw deflate mode (nowrap=true) - no zlib header/footer expected
            val inflater = Inflater(true)
            inflater.setInput(compressedData)
            
            val outputStream = ByteArrayOutputStream(data.size * 4)
            val buffer = ByteArray(1024)
            
            while (!inflater.finished()) {
                val count = inflater.inflate(buffer)
                if (count == 0 && inflater.needsInput()) {
                    android.util.Log.e("MessageDecoding", "Inflater needs more input")
                    break
                }
                if (count == 0 && inflater.finished()) {
                    // Normal completion
                    break
                }
                outputStream.write(buffer, 0, count)
            }
            
            val result = outputStream.toByteArray()
            inflater.end()
            
            android.util.Log.d("MessageDecoding", "Decompressed ${compressedData.size} -> ${result.size} bytes")
            result
        } catch (e: DataFormatException) {
            android.util.Log.e("MessageDecoding", "DataFormatException: ${e.message}")
            null
        } catch (e: Exception) {
            android.util.Log.e("MessageDecoding", "Decompression error: ${e.message}")
            null
        }
    }

    /**
     * Decode a message that may be base64-wrapped, optionally UTF-8 directly,
     * or zlib-compressed UTF-8 payload.
     */
    fun decodeMessage(b64: String): String? {
        android.util.Log.d("MessageDecoding", "Decoding message, length: ${b64.length}")
        
        // Convert base64 to Data
        val data = try {
            Base64.getDecoder().decode(b64)
        } catch (e: IllegalArgumentException) {
            android.util.Log.e("MessageDecoding", "Base64 decode failed: ${e.message}")
            return null
        }
        android.util.Log.d("MessageDecoding", "Base64 decoded, data size: ${data.size}, first byte: 0x${data[0].toUByte().toString(16)}")

        // Try direct UTF-8 decoding first
        val utf8String = data.toString(Charsets.UTF_8)
        if (utf8String.isNotEmpty() && !utf8String.contains("\uFFFD")) {
            android.util.Log.d("MessageDecoding", "Direct UTF-8 decode success")
            return utf8String
        }

        // Check if it looks like zlib/deflate (starts with 0x78)
        if (data.isNotEmpty() && data[0] == 0x78.toByte()) {
            android.util.Log.d("MessageDecoding", "Detected zlib format, decompressing...")
            decompressZlib(data)?.let { decompressed ->
                val decompressedString = decompressed.toString(Charsets.UTF_8)
                if (decompressedString.isNotEmpty() && !decompressedString.contains("\uFFFD")) {
                    android.util.Log.d("MessageDecoding", "Zlib decompression success")
                    return decompressedString
                }
            }
            android.util.Log.e("MessageDecoding", "Zlib decompression failed")
        }

        android.util.Log.e("MessageDecoding", "All decode attempts failed")
        return null
    }
}
