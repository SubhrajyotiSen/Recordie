package it.jertlok.screenrecorder.tasks

import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.AsyncTask
import android.provider.MediaStore
import android.widget.ImageView
import it.jertlok.screenrecorder.R
import it.jertlok.screenrecorder.adapters.VideoAdapter
import java.lang.ref.WeakReference

class CreateThumbnailTask(context: VideoAdapter.VideoHolder) : AsyncTask<String, Void, Boolean>() {
    // We need to do operations on the caller, so we create a weak reference.
    private val holderRef: WeakReference<VideoAdapter.VideoHolder> = WeakReference(context)
    private var mThumbnail: Bitmap? = null

    override fun doInBackground(vararg params: String?): Boolean {
        val holder = holderRef.get() ?: return false
        if (params.size > 1) {
            return false
        }
        val fileUri = params[0]

        synchronized(holder.mCache) {
            if (holder.mCache.get(fileUri) == null) {
                mThumbnail = ThumbnailUtils.createVideoThumbnail(
                    fileUri,
                    MediaStore.Video.Thumbnails.MINI_KIND
                )
                if (mThumbnail != null) {
                    holder.mCache.put(fileUri, mThumbnail)
                }
            } else {
                mThumbnail = holder.mCache.get(fileUri)
            }
        }
        return true
    }

    override fun onPostExecute(result: Boolean) {
        super.onPostExecute(result)
        // Get element
        val holder = holderRef.get() ?: return
        // Set out thumbnail to be center crop
        if (mThumbnail != null) {
            holderRef.get()?.image?.setImageBitmap(mThumbnail)
            holderRef.get()?.image?.scaleType = ImageView.ScaleType.CENTER_CROP
        } else {
            // We couldn't create / load a thumbnail, so we set the placeholder.
            val placeholder = holder.itemView.context.getDrawable(R.drawable.ic_movie)
            holder.image.setImageDrawable(placeholder)
        }
    }
}