package dev.angryl1on.cameraapp.presentation.fragments

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import coil.load
import dev.angryl1on.cameraapp.databinding.FragmentImageViewBinding

class ImageViewFragment : Fragment() {

    private var _binding: FragmentImageViewBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_URI = "mediaUri"

        fun newInstance(uri: String) = ImageViewFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_URI, uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentImageViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val uriString = arguments?.getString(ARG_URI) ?: return
        val uri = Uri.parse(uriString)

        binding.imageViewFull.load(uri) {
            crossfade(true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}