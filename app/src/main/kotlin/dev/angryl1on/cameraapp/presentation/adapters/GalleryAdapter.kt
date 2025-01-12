package dev.angryl1on.cameraapp.presentation.adapters

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.videoFrameMillis
import dev.angryl1on.cameraapp.R
import dev.angryl1on.cameraapp.databinding.ItemMediaBinding
import dev.angryl1on.cameraapp.presentation.fragments.GalleryFragment

class GalleryAdapter(
    private val fragment: GalleryFragment,
    private var mediaUris: List<Uri>
) : RecyclerView.Adapter<GalleryAdapter.MediaViewHolder>() {

    // Holds the selected media URIs
    private val selectedUris = mutableSetOf<Uri>()

    /**
     * Indicates whether the adapter is in multi-select mode.
     * When disabled, the selected items are cleared.
     */
    var isMultiSelectMode = false
        set(value) {
            field = value
            if (!value) {
                selectedUris.clear() // Если режим отключился — сбрасываем выделения
            }
            notifyDataSetChanged()
        }

    /**
     * Callback invoked on single item click when not in multi-select mode.
     */
    var onItemClick: ((position: Int) -> Unit)? = null

    /**
     * Callback invoked when the selection count changes in multi-select mode.
     */
    var onSelectionChanged: ((count: Int) -> Unit)? = null

    /**
     * A ViewHolder class for binding media items to the RecyclerView.
     *
     * @param binding The view binding for the media item layout.
     */
    class MediaViewHolder(val binding: ItemMediaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemMediaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MediaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val uri = mediaUris[position]
        val mimeType = fragment.requireContext().contentResolver.getType(uri) ?: ""

        // Load the media URI into the ImageView, with special handling for video thumbnails
        holder.binding.imageViewMedia.load(uri) {
            crossfade(true)
            if (mimeType.startsWith("video")) {
                videoFrameMillis(1000)
            }
        }

        // Show or hide the play icon based on the media type
        holder.binding.imageViewPlayIcon.visibility =
            if (mimeType.startsWith("video")) View.VISIBLE else View.GONE

        // Handle item clicks and toggle selection if in multi-select mode
        holder.binding.root.setOnClickListener {
            if (!isMultiSelectMode) {
                onItemClick?.invoke(position)
            } else {
                toggleSelection(uri)
            }
        }

        // Highlight the item if it is selected
        if (selectedUris.contains(uri)) {
            holder.binding.root.setBackgroundColor(
                ContextCompat.getColor(fragment.requireContext(), R.color.selectedItemColor)
            )
        } else {
            holder.binding.root.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun getItemCount(): Int = mediaUris.size

    /**
     * Updates the list of media URIs displayed in the adapter.
     *
     * @param newMediaUris The new list of media URIs to display.
     */
    fun updateMediaUris(newMediaUris: List<Uri>) {
        mediaUris = newMediaUris
        notifyDataSetChanged()
    }

    /**
     * Toggles the selection state of a given media URI.
     *
     * @param uri The URI of the media item to toggle selection for.
     */
    private fun toggleSelection(uri: Uri) {
        if (selectedUris.contains(uri)) {
            selectedUris.remove(uri)
        } else {
            selectedUris.add(uri)
        }
        onSelectionChanged?.invoke(selectedUris.size)
        notifyDataSetChanged()
    }

    /**
     * Retrieves the list of currently selected media URIs.
     *
     * @return A list of selected media URIs.
     */
    fun getSelectedUris(): List<Uri> = selectedUris.toList()

    /**
     * Removes the selected media items from the adapter after successful deletion
     * from the MediaStore.
     */
    fun removeSelected() {
        if (selectedUris.isEmpty()) return
        val newList = mediaUris.toMutableList()
        newList.removeAll(selectedUris)
        mediaUris = newList
        selectedUris.clear()
        notifyDataSetChanged()
    }
}
