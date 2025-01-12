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

    // Храним выделенные элементы
    private val selectedUris = mutableSetOf<Uri>()

    // Флаг, указывающий, что мы в режиме множественного выбора
    var isMultiSelectMode = false
        set(value) {
            field = value
            if (!value) {
                // Если режим отключился — сбрасываем выделения
                selectedUris.clear()
            }
            notifyDataSetChanged()
        }

    // Колбэк на обычный клик
    var onItemClick: ((position: Int) -> Unit)? = null
    // Колбэк на множественный выбор (изменился список выделенных элементов)
    var onSelectionChanged: ((count: Int) -> Unit)? = null

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

        holder.binding.imageViewMedia.load(uri) {
            crossfade(true)
            if (mimeType.startsWith("video")) {
                videoFrameMillis(1000)
            }
        }

        holder.binding.imageViewPlayIcon.visibility =
            if (mimeType.startsWith("video")) View.VISIBLE else View.GONE

        // 1) Если не в режиме множественного выбора, то при клике просто открываем элемент
        // 2) Если в режиме — переключаем выделение
        holder.binding.root.setOnClickListener {
            if (!isMultiSelectMode) {
                onItemClick?.invoke(position)
            } else {
                toggleSelection(uri)
            }
        }

        // Визуально выделяем, если элемент выбран
        if (selectedUris.contains(uri)) {
            holder.binding.root.setBackgroundColor(
                ContextCompat.getColor(fragment.requireContext(), R.color.selectedItemColor)
            )
        } else {
            holder.binding.root.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun getItemCount(): Int = mediaUris.size

    fun updateMediaUris(newMediaUris: List<Uri>) {
        mediaUris = newMediaUris
        notifyDataSetChanged()
    }

    /**
     * Переключает выделение конкретного Uri
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
     * Возвращает список выделенных Uri
     */
    fun getSelectedUris(): List<Uri> = selectedUris.toList()

    /**
     * Удаляет все выделенные элементы из списка адаптера (после успешного удаления из MediaStore)
     */
    fun removeSelected() {
        if (selectedUris.isEmpty()) return
        val newList = mediaUris.toMutableList()
        newList.removeAll(selectedUris)
        mediaUris = newList
        selectedUris.clear()
        notifyDataSetChanged()
    }

    /**
     * Удаляет одиночный элемент (когда не в режиме multi-select, но удаляем в деталях)
     */
    fun removeItem(uri: Uri) {
        val newList = mediaUris.toMutableList()
        if (newList.remove(uri)) {
            mediaUris = newList
            notifyDataSetChanged()
        }
    }
}
