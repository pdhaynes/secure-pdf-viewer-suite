package com.ph.securepdfviewer.PDFDecoder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.MemoryFile
import android.os.ParcelFileDescriptor
import com.davemorrissey.labs.subscaleview.decoder.ImageDecoder
import com.ph.securepdfviewer.Utils

/**
 * Decodes and renders a [PdfRenderer.Page] into a [Bitmap]
 */
class MemoryPDFDecoder(
    private val position: Int,
    private val scale: Float,
    private val memoryFile: MemoryFile
    ) : ImageDecoder {

    class InvalidPageIndexException(message: String) : Exception(message) {
        // You can add additional constructors if needed
        constructor(pageIndex: Int, pageCount: Int) : this("Tried to access page of index $pageIndex but the page count is $pageCount")
    }

    override fun decode(context: Context?, uri: Uri): Bitmap {
        val descriptor: ParcelFileDescriptor

        // MemoryFile > Fd > ParcelFd
        val fileDescriptor = Utils.getFileDescriptorViaReflection(memoryFile)
        descriptor = ParcelFileDescriptor.dup(fileDescriptor);
        val renderer = PdfRenderer(descriptor)

        val pageCount = renderer.pageCount
        if (pageCount <= 0 || position >= pageCount) {
            throw InvalidPageIndexException(position, pageCount)
        }

        val page = renderer.openPage(position)

        val bitmap = Bitmap.createBitmap(
            (page.width * scale + 0.5f).toInt(),
            (page.height * scale + 0.5f).toInt(),
            Bitmap.Config.ARGB_8888
        )

        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        page.close()
        renderer.close()
        descriptor.close()

        return bitmap
    }
}