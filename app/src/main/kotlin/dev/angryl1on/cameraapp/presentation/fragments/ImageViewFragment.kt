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

    private lateinit var binding: FragmentImageViewBinding

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
        binding = FragmentImageViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val uriString = arguments?.getString(ARG_URI) ?: return
        val uri = Uri.parse(uriString)

        // Load the image into the ImageView with a crossfade effect
        binding.imageViewFull.load(uri) {
            crossfade(true)
        }
    }
}