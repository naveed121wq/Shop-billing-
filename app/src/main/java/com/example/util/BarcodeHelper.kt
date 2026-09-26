package com.example.util

import android.graphics.Bitmap
import android.graphics.Color
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.*
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

object BarcodeHelper {

    fun generateBarcodeBitmap(
        contents: String,
        format: BarcodeFormat = BarcodeFormat.CODE_128,
        width: Int = 600,
        height: Int = 200
    ): Bitmap? {
        if (contents.isBlank()) return null
        return try {
            val writer = MultiFormatWriter()
            val bitMatrix: BitMatrix = writer.encode(contents, format, width, height)
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bmp
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun generateQrBitmap(
        contents: String,
        size: Int = 500
    ): Bitmap? {
        return generateBarcodeBitmap(contents, BarcodeFormat.QR_CODE, size, size)
    }

    class BarcodeScannerAnalyzer(
        private val onBarcodeScanned: (String) -> Unit
    ) : ImageAnalysis.Analyzer {

        private val reader = MultiFormatReader().apply {
            val hints = mapOf(
                DecodeHintType.POSSIBLE_FORMATS to listOf(
                    BarcodeFormat.CODE_128,
                    BarcodeFormat.CODE_39,
                    BarcodeFormat.EAN_13,
                    BarcodeFormat.EAN_8,
                    BarcodeFormat.UPC_A,
                    BarcodeFormat.UPC_E,
                    BarcodeFormat.QR_CODE
                ),
                DecodeHintType.TRY_HARDER to true
            )
            setHints(hints)
        }

        private var isScanning = true

        fun resumeScanning() {
            isScanning = true
        }

        fun pauseScanning() {
            isScanning = false
        }

        override fun analyze(image: ImageProxy) {
            if (!isScanning) {
                image.close()
                return
            }

            val plane = image.planes[0]
            val buffer: ByteBuffer = plane.buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)

            val width = image.width
            val height = image.height

            val source = PlanarYUVLuminanceSource(
                bytes, width, height, 0, 0, width, height, false
            )
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

            try {
                val result = reader.decodeWithState(binaryBitmap)
                val text = result.text
                if (!text.isNullOrBlank()) {
                    isScanning = false
                    onBarcodeScanned(text)
                }
            } catch (_: NotFoundException) {
                // No barcode in this frame
            } catch (e: Exception) {
                // Ignore decoding errors
            } finally {
                reader.reset()
                image.close()
            }
        }
    }
}
