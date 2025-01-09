package com.ph.securestash.ExternalPackages.PDFDecoder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.Rect
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.MemoryFile
import android.os.ParcelFileDescriptor
import com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder
import com.ph.securepdfviewer.Utils
import java.io.IOException

/**
 * Decodes and renders a given rect out of a [PdfRenderer.Page] into a [Bitmap]
 */
class MemoryPDFRegionDecoder(
    private val position: Int,
    private val memoryFile: MemoryFile,
    private val scale: Float,
    private val backgroundColorPdf: Int = Color.TRANSPARENT
) : ImageRegionDecoder {

    private lateinit var renderer: PdfRenderer
    private lateinit var page: PdfRenderer.Page
    private lateinit var descriptor: ParcelFileDescriptor

    override fun init(context: Context?, uri: Uri): Point {
        try {
            val fileDescriptor = Utils.getFileDescriptorViaReflection(memoryFile)
            descriptor = ParcelFileDescriptor.dup(fileDescriptor);
            renderer = PdfRenderer(descriptor)
            page = renderer.openPage(position)

            return Point(
                (page.width * scale + 0.5f).toInt(),
                (page.height * scale + 0.5f).toInt()
            )
        } catch (e: IOException) {
            e.printStackTrace()
            throw IOException("Error initializing PdfRenderer: ${e.message}")
        }
    }

    /**
     * Creates a [Bitmap] in the correct size and renders the region defined by rect of the
     * [PdfRenderer.Page] into it.
     *
     * @param rect the rect of the [PdfRenderer.Page] to be rendered to the bitmap
     * @param sampleSize the sample size
     * @return a bitmap containing the rendered rect of the page
     */
    override fun decodeRegion(rect: Rect, sampleSize: Int): Bitmap {
        val bitmapWidth = rect.width() / sampleSize
        val bitmapHeight = rect.height() / sampleSize

        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)

        val matrix = Matrix().apply {
            setScale(scale / sampleSize, scale / sampleSize)
            postTranslate(-rect.left / sampleSize.toFloat(), -rect.top / sampleSize.toFloat())
        }

        val canvas = Canvas(bitmap)
        canvas.drawColor(backgroundColorPdf)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        page.render(bitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        return bitmap
    }

    override fun isReady(): Boolean = true

    /**
     * close everything
     */
    override fun recycle() {
        page.close()
        renderer.close()
        try {
            descriptor.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}