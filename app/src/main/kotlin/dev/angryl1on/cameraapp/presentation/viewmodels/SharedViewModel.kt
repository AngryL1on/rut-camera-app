package dev.angryl1on.cameraapp.presentation.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _mediaUris = MutableLiveData<List<Uri>>()
    val mediaUris: LiveData<List<Uri>> get() = _mediaUris

    fun setMediaUris(uris: List<Uri>) {
        _mediaUris.value = uris
    }
}
