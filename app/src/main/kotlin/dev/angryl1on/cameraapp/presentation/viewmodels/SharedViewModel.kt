package dev.angryl1on.cameraapp.presentation.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * A [ViewModel] class for sharing media data between fragments.
 * Provides a shared state for managing a list of media URIs (images and videos).
 */
class SharedViewModel : ViewModel() {

    // Backing property for the list of media URIs
    private val _mediaUris = MutableLiveData<List<Uri>>()

    /**
     * LiveData exposing the list of media URIs to observers.
     */
    val mediaUris: LiveData<List<Uri>> get() = _mediaUris

    /**
     * Updates the list of media URIs in the shared state.
     *
     * @param uris A list of [Uri] objects representing the media files.
     */
    fun setMediaUris(uris: List<Uri>) {
        _mediaUris.value = uris
    }
}

