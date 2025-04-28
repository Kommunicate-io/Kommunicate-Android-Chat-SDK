package io.kommunicate.ui.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import io.kommunicate.ui.R
import io.kommunicate.ui.alphanumbericcolor.AlphaNumberColorUtil
import io.kommunicate.commons.people.contact.Contact
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Locale

object KmViewHelper {
    private const val PDF = "pdf"
    private const val TXT = "txt"
    private const val DOC = "doc"
    private const val TXT_PLAIN = "text/plain"

    @JvmStatic
    fun loadContactImage(
        context: Context,
        imageView: CircleImageView,
        textView: TextView,
        contact: Contact,
        placeholderImage: Int
    ) {
        try {
            textView.visibility = View.VISIBLE
            imageView.visibility = View.GONE
            var contactNumber = ""
            var firstLetter = 0.toChar()
            contactNumber = contact.displayName.uppercase(Locale.getDefault())
            firstLetter = contact.displayName.uppercase(Locale.getDefault())[0]

            if (firstLetter != '+') {
                textView.text = firstLetter.toString()
            } else if (contactNumber.length >= 2) {
                textView.text = contactNumber[1].toString()
            }

            val colorKey =
                if (AlphaNumberColorUtil.alphabetBackgroundColorMap.containsKey(firstLetter)) firstLetter else null
            val bgShape = textView.background as GradientDrawable
            bgShape.setColor(context.resources.getColor(AlphaNumberColorUtil.alphabetBackgroundColorMap[colorKey]!!))

            if (contact.isDrawableResources) {
                textView.visibility = View.GONE
                imageView.visibility = View.VISIBLE
                val drawableResourceId = context.resources.getIdentifier(
                    contact.getrDrawableName(),
                    "drawable",
                    context.packageName
                )
                imageView.setImageResource(drawableResourceId)
            } else if (contact.imageURL != null) {
                loadImage(context, imageView, textView, contact.imageURL, placeholderImage)
            } else {
                textView.visibility = View.VISIBLE
                imageView.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun loadImage(
        context: Context?,
        imageView: CircleImageView,
        textImage: TextView?,
        imageUrl: String?,
        placeholderImage: Int
    ) {
        if (context == null || textImage == null || imageUrl == null) {
            return
        }

        val options = RequestOptions()
            .centerCrop()
            .placeholder(placeholderImage)
            .error(placeholderImage)


        Glide.with(context).load(imageUrl).apply(options)
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    textImage.visibility = View.VISIBLE
                    imageView.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any,
                    target: Target<Drawable?>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    textImage.visibility = View.GONE
                    imageView.visibility = View.VISIBLE
                    return false
                }
            }).into(imageView)
    }

    @JvmStatic
    fun loadImage(
        context: Context,
        imageView: CircleImageView,
        placeholderImage: Int
    ) {
        Glide.with(context).load(placeholderImage).into(imageView)
    }

    @JvmStatic
    fun setDocumentIcon(mimeType: String?, documentIcon: ImageView) {
        if (mimeType == null && TextUtils.isEmpty(mimeType)) {
            documentIcon.setImageResource(R.drawable.ic_documentreceive)
            return
        }
        if (mimeType!!.contains(PDF)) {
            documentIcon.setImageResource(R.drawable.km_pdf_icon)
        } else if (mimeType.contains(TXT) || mimeType.contains(DOC) || mimeType.contains(TXT_PLAIN)) {
            documentIcon.setImageResource(R.drawable.km_doc_icon)
        } else {
            documentIcon.setImageResource(R.drawable.ic_documentreceive)
        }
    }
}
