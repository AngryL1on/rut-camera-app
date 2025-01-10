package dev.angryl1on.cameraapp.presentation.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.fragment.app.Fragment
import coil.load
import dev.angryl1on.cameraapp.databinding.FragmentMediaViewBinding

class MediaViewFragment : Fragment() {

    private var _binding: FragmentMediaViewBinding? = null
    private val binding get() = _binding!!

    private var mediaUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            mediaUri = it.getString("mediaUri")
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
        mediaUri?.let { uriString ->
            val uri = Uri.parse(uriString)
            val mimeType = requireContext().contentResolver.getType(uri)

            if (mimeType != null) {
                if (mimeType.startsWith("image")) {
                    binding.imageViewFull.visibility = View.VISIBLE
                    binding.imageViewFull.load(uri) {
                        crossfade(true)
                    }
                } else if (mimeType.startsWith("video")) {
                    binding.videoViewFull.visibility = View.VISIBLE
                    binding.videoViewFull.setVideoURI(uri)
                    val mediaController = MediaController(requireContext())
                    mediaController.setAnchorView(binding.videoViewFull)
                    binding.videoViewFull.setMediaController(mediaController)
                    binding.videoViewFull.start()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
