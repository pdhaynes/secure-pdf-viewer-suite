package com.ph.securepdfviewer

import android.os.Bundle
import android.os.MemoryFile
import android.support.v4.view.ViewPager
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewAnimator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ph.securepdfviewer.Helpers.CryptographyHelper
import com.ph.securepdfviewer.PDFPagerAdapter.MemoryPDFPagerAdapter
import fr.castorflex.android.verticalviewpager.VerticalViewPager
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var pager: VerticalViewPager
    private lateinit var pages: TextView
    private lateinit var animator: ViewAnimator
    private lateinit var pagerAdapter: MemoryPDFPagerAdapter
    private var currentPosition: Int = 0

    private lateinit var memoryFile: MemoryFile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        /**
         * Example of use case
         * Sample PDF file AES encrypted using key from Android Keystore.
         *
         * File is AES encrypted and stored in app's cache directory
         * We don't want the PDF file to be written back to the directory unencrypted
         */
        val encryptedPdfFile = File(cacheDir, "sample.pdf")

        // Pulls sample file from Assets folder
        val pdfFileInputStream = baseContext.assets.open("sample.pdf")

        // Directly reads file bytes, does not write to disk
        val pdfFileBytes = ByteArray(pdfFileInputStream.available())
        pdfFileInputStream.read(pdfFileBytes)
        pdfFileInputStream.close()

        // File
        val fileKey = CryptographyHelper.getSecretKeyFromKeystore(encryptedPdfFile.name)
        val encryptedFileData = CryptographyHelper.encodeFile(pdfFileBytes, fileKey)
        CryptographyHelper.saveEncodedFile(encryptedPdfFile, encryptedFileData)

        val secretKey = CryptographyHelper.getSecretKeyFromKeystore(encryptedPdfFile.name)
        val decryptedFileBytes = CryptographyHelper.decodeFile(encryptedPdfFile, secretKey)

        pager = findViewById(R.id.pager)
        pages = findViewById<TextView>(R.id.pages)
        animator = findViewById<ViewAnimator>(R.id.animator)
        (animator as? ViewAnimator)?.visibility = View.VISIBLE

        pager.visibility = View.VISIBLE

        findViewById<View>(R.id.btnLoadExtern).setOnClickListener { _: View? ->
            val toast = Toast.makeText(
                this,
                "Implement an intent to show the pdf externally",
                Toast.LENGTH_SHORT
            )
            toast.show()
        }

        memoryFile = MemoryFile("temp_pdf", decryptedFileBytes.size)

        memoryFile.writeBytes(decryptedFileBytes, 0, 0, decryptedFileBytes.size)

        pagerAdapter =
            MemoryPDFPagerAdapter(
                this,
                memoryFile
            )
        pager.adapter = pagerAdapter

        updatePageCounter()

        pager.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                // Handle page
                this@MainActivity.onPageSelected(position)
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
    }

    override fun finish() {
        super.finish()
        memoryFile.close()
    }

    private fun updatePageCounter() {
        pages.text = "${currentPosition + 1}/${pagerAdapter?.count}"
    }

    private fun onPageSelected(position: Int) {
        currentPosition = position
        updatePageCounter()
    }
}