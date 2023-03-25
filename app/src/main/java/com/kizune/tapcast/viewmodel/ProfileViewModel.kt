package com.kizune.tapcast.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class ProfileViewModel(
    private val state: SavedStateHandle
) : ViewModel() {
    private companion object {
        const val PHOTO_KEY = "photo"
        const val EDIT_PHOTO_KEY = "edit_photo"
        const val USERNAME_KEY = "username"
    }

    val photoURI = state.getStateFlow(PHOTO_KEY, Uri.parse(""))
    val editPhotoURI = state.getStateFlow(EDIT_PHOTO_KEY, Uri.parse(""))
    private val username = state.getStateFlow(USERNAME_KEY, "")

    fun getPhotoUri(): Uri = photoURI.value

    fun isPhotoUriEmpty(): Boolean = photoURI.value.path.isNullOrEmpty()

    fun getEditPhotoUri(): Uri = editPhotoURI.value

    fun isEditPhotoUriEmpty(): Boolean = editPhotoURI.value.path.isNullOrEmpty()

    fun setPhotoUri(value: Uri) {
        state[PHOTO_KEY] = value
    }

    fun setEditPhotoUri(value: Uri) {
        state[EDIT_PHOTO_KEY] = value
    }

    fun setUsername(value: String) {
        state[USERNAME_KEY] = value
    }

    fun getUsername(): String = username.value
}