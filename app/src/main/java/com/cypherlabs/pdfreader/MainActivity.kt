package com.cypherlabs.pdfreader

import android.app.AlertDialog
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.github.barteksc.pdfviewer.PDFView
import java.io.File

class MainActivity : AppCompatActivity() {

    private var fileDescriptor: ParcelFileDescriptor? = null

    private lateinit var pdfView: PDFView
    private lateinit var toolbar: View
    private lateinit var titleText: TextView
    private lateinit var pageIndicator: TextView
    private lateinit var searchButton: ImageButton
    private lateinit var shareButton: ImageButton

    private var toolbarVisible = true
    private var currentUri: Uri? = null
    private var currentDisplayName: String? = null
    private var currentPassword: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pdfView = findViewById(R.id.pdfView)
        toolbar = findViewById(R.id.toolbar)
        titleText = findViewById(R.id.titleText)
        pageIndicator = findViewById(R.id.pageIndicator)
        searchButton = findViewById(R.id.searchButton)
        shareButton = findViewById(R.id.shareButton)

        val uri = intent?.data
        if (uri == null) {
            Toast.makeText(this, "No PDF was supplied", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        currentUri = uri
        currentDisplayName = queryDisplayName(uri)
        openPdf(uri, null)

        pdfView.setOnClickListener { toggleToolbar() }

        pageIndicator.setOnClickListener { showPageSearchDialog() }
        searchButton.setOnClickListener { showPageSearchDialog() }

        shareButton.setOnClickListener { shareCurrentPdf() }
    }

    private fun openPdf(uri: Uri, password: String?) {
        try {
            fileDescriptor?.close()
            fileDescriptor = contentResolver.openFileDescriptor(uri, "r")
            fileDescriptor ?: throw IllegalStateException("Could not open file")

            currentPassword = password
            titleText.text = queryDisplayName(uri) ?: getString(R.string.app_name)
            pageIndicator.text = "1 / 1"

            pdfView.fromUri(uri)
                .password(password)
                .defaultPage(0)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .onPageChange { page, pageCount ->
                    pageIndicator.text = "${page + 1} / $pageCount"
                }
                .onError { error ->
                    if (password.isNullOrBlank() && error.message?.contains("password", ignoreCase = true) == true) {
                        promptForPassword(uri)
                    } else {
                        Toast.makeText(this, "Couldn't open this PDF: ${error.message}", Toast.LENGTH_LONG).show()
                    }
                }
                .load()
        } catch (e: Exception) {
            Toast.makeText(this, "Couldn't open this PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showPageSearchDialog() {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "Page number"
            setSingleLine(true)
            setText((pdfView.currentPage + 1).toString())
            setSelection(text?.length ?: 0)
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    performPageJump(this)
                    true
                } else {
                    false
                }
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Go to page")
            .setView(input)
            .setPositiveButton("Go") { _, _ -> performPageJump(input) }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            input.post {
                input.requestFocus()
                showSoftKeyboard(input)
            }
        }
        dialog.show()
    }

    private fun promptForPassword(uri: Uri) {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            hint = "PDF password"
            setSingleLine(true)
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    submitPassword(uri, this)
                    true
                } else {
                    false
                }
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("This PDF is password protected")
            .setView(input)
            .setPositiveButton("Open") { _, _ -> submitPassword(uri, input) }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            input.post {
                input.requestFocus()
                showSoftKeyboard(input)
            }
        }
        dialog.show()
    }

    private fun performPageJump(input: EditText) {
        val page = input.text.toString().trim().toIntOrNull()
        if (page == null || page < 1) {
            Toast.makeText(this, "Please enter a valid page number", Toast.LENGTH_SHORT).show()
            return
        }
        val count = pdfView.pageCount
        if (count <= 0 || page > count) {
            Toast.makeText(this, "Page $page is not available", Toast.LENGTH_SHORT).show()
            return
        }
        pdfView.jumpTo(page - 1)
    }

    private fun submitPassword(uri: Uri, input: EditText) {
        val password = input.text.toString()
        if (password.isNotBlank()) {
            openPdf(uri, password)
        } else {
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSoftKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun shareCurrentPdf() {
        val uri = currentUri ?: return
        val name = currentDisplayName ?: queryDisplayName(uri) ?: "document.pdf"
        val safeName = if (name.lowercase().endsWith(".pdf")) name else "$name.pdf"

        try {
            val tempFile = File(cacheDir, safeName)
            contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: throw IllegalStateException("Could not read PDF content")

            val contentUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", tempFile)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, safeName)
                putExtra(Intent.EXTRA_TEXT, safeName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                clipData = ClipData.newUri(contentResolver, safeName, contentUri)
            }
            startActivity(Intent.createChooser(shareIntent, "Share PDF"))
        } catch (e: Exception) {
            Toast.makeText(this, "Couldn't share this PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        if (uri.scheme != "content") return uri.lastPathSegment
        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) return cursor.getString(idx)
            }
        } catch (_: Exception) {
        } finally {
            cursor?.close()
        }
        return uri.lastPathSegment
    }

    private fun toggleToolbar() {
        toolbarVisible = !toolbarVisible
        toolbar.animate().cancel()
        if (toolbarVisible) {
            toolbar.visibility = View.VISIBLE
            toolbar.alpha = 0f
            toolbar.translationY = -toolbar.height.toFloat() * 0.35f
            toolbar.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(220)
                .start()
        } else {
            toolbar.animate()
                .alpha(0f)
                .translationY(-toolbar.height.toFloat() * 0.35f)
                .setDuration(180)
                .withEndAction { toolbar.visibility = View.INVISIBLE }
                .start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fileDescriptor?.close()
    }
}
