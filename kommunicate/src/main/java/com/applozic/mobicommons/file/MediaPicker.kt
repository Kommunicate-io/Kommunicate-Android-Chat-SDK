package com.applozic.mobicommons.file

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

object MediaPicker {
    private const val MIME_TYPE_AUDIO = "audio/*"

    fun registerImageVideoPicker(
        activity: ComponentActivity,
        callback: (List<Uri>) -> Unit,
        isMultipleSelectionEnabled: Boolean
    ): ActivityResultLauncher<PickVisualMediaRequest> {
        return if (isMultipleSelectionEnabled) {
            activity.registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
                callback(uris)
            }
        } else {
            activity.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                uri?.let {
                    callback(listOf(uri))
                } ?: emptyList<Uri>()
            }
        }
    }

    fun createMediaPickerIntent(
        picker: ActivityResultLauncher<PickVisualMediaRequest>,
        choosenOption: FileUtils.GalleryFilterOptions
    ) {
        when (choosenOption) {
            FileUtils.GalleryFilterOptions.IMAGE_VIDEO -> {
                picker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                )
            }
            FileUtils.GalleryFilterOptions.IMAGE_ONLY -> {
                picker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
            FileUtils.GalleryFilterOptions.VIDEO_ONLY -> {
                picker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                )
            }
            FileUtils.GalleryFilterOptions.ALL_FILES,
            FileUtils.GalleryFilterOptions.AUDIO_ONLY -> {
                // Do nothing..
            }
        }
    }

    fun createAudioFilesPickerIntent(
        isMultipleSectionEnabled: Boolean,
        title: String
    ): Intent {
        val audioIntent = Intent(Intent.ACTION_PICK).apply {
            setDataAndType(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MIME_TYPE_AUDIO)
            if (isMultipleSectionEnabled) {
                addCategory(Intent.ACTION_SEND_MULTIPLE)
            }
        }
        return Intent.createChooser(audioIntent, title)
    }

    fun createAllFilesPickerIntent(
        isMultipleSectionEnabled: Boolean,
        title: String
    ): Intent {
        val attachmentIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, isMultipleSectionEnabled)
            putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        }
        return Intent.createChooser(attachmentIntent, title)
    }
}