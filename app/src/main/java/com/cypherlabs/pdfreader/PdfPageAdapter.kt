package com.cypherlabs.pdfreader

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.pdf.PdfRenderer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PdfPageAdapter(
    private val renderer: PdfRenderer,
    private val pageWidthPx: Int
) : RecyclerView.Adapter<PdfPageAdapter.PageViewHolder>() {

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var shutDown = false

    // Pre-computed [width,height] pairs so list items don't jump around while pages render.
    private val pageSizes: Array<IntArray> = Array(renderer.pageCount) { i ->
        synchronized(renderer) {
            val page = renderer.openPage(i)
            val size = intArrayOf(page.width, page.height)
            page.close()
            size
        }
    }

    var isInverted: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private val invertFilter = ColorMatrixColorFilter(
        ColorMatrix(
            floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    )

    override fun getItemCount(): Int = renderer.pageCount

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pdf_page, parent, false)
        return PageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val (w, h) = pageSizes[position]
        val scaledHeight = if (w > 0) (h.toFloat() / w * pageWidthPx).toInt() else pageWidthPx
        val params = holder.imageView.layoutParams
        params.height = scaledHeight
        holder.imageView.layoutParams = params
        holder.imageView.setImageBitmap(null)
        holder.imageView.colorFilter = if (isInverted) invertFilter else null
        holder.imageView.tag = position

        executor.execute {
            if (shutDown) return@execute
            val bitmap = try {
                renderPage(position)
            } catch (_: Exception) {
                null
            }
            mainHandler.post {
                if (holder.imageView.tag == position && bitmap != null) {
                    holder.imageView.setImageBitmap(bitmap)
                    holder.imageView.colorFilter = if (isInverted) invertFilter else null
                }
            }
        }
    }

    private fun renderPage(position: Int): Bitmap {
        synchronized(renderer) {
            val page = renderer.openPage(position)
            val scale = pageWidthPx.toFloat() / page.width
            val height = (page.height * scale).toInt().coerceAtLeast(1)
            val bitmap = Bitmap.createBitmap(pageWidthPx, height, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            return bitmap
        }
    }

    override fun onViewRecycled(holder: PageViewHolder) {
        holder.imageView.setImageBitmap(null)
        holder.imageView.tag = null
    }

    fun shutdown() {
        shutDown = true
        executor.shutdownNow()
    }

    class PageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.pageImage)
    }
}
