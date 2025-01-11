package dev.angryl1on.cameraapp.presentation.fragments

import android.net.Uri
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dev.angryl1on.cameraapp.databinding.FragmentMediaViewBinding
import dev.angryl1on.cameraapp.presentation.viewmodels.SharedViewModel
import dev.angryl1on.cameraapp.presentation.adapters.MediaPagerAdapter

class MediaViewFragment : Fragment() {

    private var _binding: FragmentMediaViewBinding? = null
    private val binding get() = _binding!!

    private lateinit var mediaAdapter: MediaPagerAdapter
    private lateinit var gestureDetector: GestureDetector

    private val sharedViewModel: SharedViewModel by activityViewModels()

    private var mediaUris: List<Uri> = emptyList()
    private var initialIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Получение аргументов
        arguments?.let {
            initialIndex = it.getInt("mediaIndex", 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMediaViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Наблюдение за списком mediaUris из SharedViewModel
        sharedViewModel.mediaUris.observe(viewLifecycleOwner) { uris ->
            mediaUris = uris
            mediaAdapter = MediaPagerAdapter(this, mediaUris)
            binding.viewPagerMedia.adapter = mediaAdapter
            binding.viewPagerMedia.setCurrentItem(initialIndex, false)
        }

        // Инициализация GestureDetector для вертикальных свайпов
        gestureDetector = GestureDetector(requireContext(), GestureDetector.SimpleOnGestureListener())

        // Установка обработчика касаний
        binding.viewPagerMedia.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
