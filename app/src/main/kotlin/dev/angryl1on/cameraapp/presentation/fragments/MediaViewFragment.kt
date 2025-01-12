package dev.angryl1on.cameraapp.presentation.fragments

import android.net.Uri
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dev.angryl1on.cameraapp.databinding.FragmentMediaViewBinding
import dev.angryl1on.cameraapp.presentation.viewmodels.SharedViewModel
import dev.angryl1on.cameraapp.presentation.adapters.MediaPagerAdapter

class MediaViewFragment : Fragment() {

    private var _binding: FragmentMediaViewBinding? = null
    private val binding get() = _binding!!

    private lateinit var mediaAdapter: MediaPagerAdapter
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private var initialIndex: Int = 0
    private var mediaUris: List<Uri> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            initialIndex = it.getInt("mediaIndex", 0)
        }
        setHasOptionsMenu(true) // Если хотим меню в Toolbar
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMediaViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sharedViewModel.mediaUris.observe(viewLifecycleOwner) { uris ->
            mediaUris = uris
            mediaAdapter = MediaPagerAdapter(this, mediaUris)
            binding.viewPagerMedia.adapter = mediaAdapter
            binding.viewPagerMedia.setCurrentItem(initialIndex, false)
        }

        // Пример: кнопка удаления (если вы не хотите меню)
        // Предположим, у нас есть кнопка в макете
        binding.buttonDeleteCurrent.setOnClickListener {
            deleteCurrentItem()
        }
    }

    private fun deleteCurrentItem() {
        val currentPosition = binding.viewPagerMedia.currentItem
        if (currentPosition < 0 || currentPosition >= mediaUris.size) return
        val currentUri = mediaUris[currentPosition]

        try {
            val rowsDeleted = requireContext().contentResolver.delete(currentUri, null, null)
            if (rowsDeleted > 0) {
                // Успешно удалили из MediaStore
                // Удаляем из ViewModel
                val updatedList = mediaUris.toMutableList()
                updatedList.removeAt(currentPosition)
                sharedViewModel.setMediaUris(updatedList)

                // Если нет элементов, просто закрываем экран
                if (updatedList.isEmpty()) {
                    findNavController().popBackStack()
                } else {
                    // Иначе обновим ViewPager
                    // mediaAdapter.notifyItemRemoved(currentPosition) – для ViewPager2 с FragmentStateAdapter
                    // ... Обычно достаточно просто пересоздать адаптер, но можно перехитрить
                    // Проще всего:
                    mediaAdapter = MediaPagerAdapter(this, updatedList)
                    binding.viewPagerMedia.adapter = mediaAdapter
                    // Ставим позицию на (currentPosition) или (currentPosition - 1)
                    val newPos = currentPosition.coerceAtMost(updatedList.lastIndex)
                    binding.viewPagerMedia.setCurrentItem(newPos, false)

                    Toast.makeText(requireContext(), "Файл удалён", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Ошибка при удалении", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "Нет прав на удаление", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
