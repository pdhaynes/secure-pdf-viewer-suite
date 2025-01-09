package com.ph.securepdfviewer.PDFPagerAdapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.ph.securepdfviewer.PDFDecoder.MemoryPDFDecoder;
import com.ph.securepdfviewer.Utils;
import com.ph.securestash.ExternalPackages.PDFDecoder.MemoryPDFRegionDecoder;
import java.io.FileDescriptor;
import java.io.IOException;

/**
 * Created by Lindner Stefan on 30.09.16.
 * Modified by Peyton Haynes on 01/09/2025
 */

public class MemoryPDFPagerAdapter extends PagerAdapter {
    /**
     * context for the view
     */
    private Context context;

    /**
     * pdf memory file to show
     */
    private MemoryFile memFile;

    /**
     * file descriptor of the PDF
     */
    private ParcelFileDescriptor mFileDescriptor;

    /**
     * this scale sets the size of the {@link PdfRenderer.Page} in the {@link
     * MemoryPDFRegionDecoder}.
     * Since it rescales the picture, it also sets the possible zoom level.
     */
    private float scale;

    /**
     * this renderer is only used to count the pages
     */
    private PdfRenderer renderer;

    /**
     * @param memoryFile the in-memory pdf file
     */
    public MemoryPDFPagerAdapter(Context context, MemoryFile memoryFile) {
        super();
        this.context = context;
        this.memFile = memoryFile;
        this.scale = 8;
        try {
            FileDescriptor memFileDescriptor = Utils.INSTANCE.getFileDescriptorViaReflection(memoryFile);
            mFileDescriptor = ParcelFileDescriptor.dup(memFileDescriptor);
            renderer = new PdfRenderer(mFileDescriptor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Instantiate an item. Therefor a {@link SubsamplingScaleImageView} with special decoders is
     * initialized and rendered.
     *
     * @param container isn't used here
     * @param position the current pdf page position
     */
    public Object instantiateItem(ViewGroup container, int position) {
        SubsamplingScaleImageView imageView = new SubsamplingScaleImageView(context);

        // the smaller this number, the smaller the chance to get an "outOfMemoryException"
        // still, values lower than 100 really do affect the quality of the pdf picture
        int minimumTileDpi = 120;
        imageView.setMinimumTileDpi(minimumTileDpi);

        ImageSource source = null;

        //sets the PDFDecoder for the imageView
        imageView.setBitmapDecoderFactory(() -> new MemoryPDFDecoder(position, scale, memFile));

        //sets the PDFRegionDecoder for the imageView
        imageView.setRegionDecoderFactory(() -> new MemoryPDFRegionDecoder(position, memFile, scale, Color.TRANSPARENT));

        // The Bitmap factory expects a png/jpg so we can't pass our memory file to it directly,
        // instead we have to use the Android pdfrenderer to create a valid bitmap
        try (ParcelFileDescriptor pfd = ParcelFileDescriptor.dup(Utils.INSTANCE.getFileDescriptorViaReflection(memFile))) {
            PdfRenderer pdfRenderer = new PdfRenderer(pfd);
            PdfRenderer.Page page = pdfRenderer.openPage(position);
            Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            page.close();
            pdfRenderer.close();
            source = ImageSource.bitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        imageView.setImage(source);

        container.addView(imageView);
        return imageView;
    }

    /**
     * gets the pdf site count
     *
     * @return pdf site count
     */
    public int getCount() {
        return renderer.getPageCount();
    }

    @Override public void destroyItem(ViewGroup container, int position, Object view) {
        container.removeView((View) view);
    }

    @Override public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}