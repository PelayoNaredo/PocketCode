package com.pocketcode.features.marketplace.ui.upload

import android.net.Uri

data class AssetUploadState(
    val name: String = "",
    val description: String = "",
    val selectedFileUri: Uri? = null,
    val isUploading: Boolean = false,
    val uploadSuccess: Boolean = false,
    val error: String? = null
)
