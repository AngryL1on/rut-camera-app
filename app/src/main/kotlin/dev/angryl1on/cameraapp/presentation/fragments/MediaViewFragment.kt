package dev.angryl1on.cameraapp.presentation.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dev.angryl1on.cameraapp.R
import dev.angryl1on.cameraapp.databinding.FragmentMediaViewBinding
import dev.angryl1on.cameraapp.presentation.viewmodels.SharedViewModel
import dev.angryl1on.cameraapp.presentation.adapters.MediaPagerAdapter

class MediaViewFragment : Fragment() {

    private lateinit var binding: FragmentMediaViewBinding

    private lateinit var mediaAdapter: MediaPagerAdapter
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private var initialIndex: Int = 0
    private var mediaUris: List<Uri> = emptyList()

    /**
     * Reads the initial index of the media item to display from the fragment arguments.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            initialIndex = it.getInt("mediaIndex", 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMediaViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Observe media URIs from the shared ViewModel and update the ViewPager
        sharedViewModel.mediaUris.observe(viewLifecycleOwner) { uris ->
            mediaUris = uris
            mediaAdapter = MediaPagerAdapter(this, mediaUris)
            binding.viewPagerMedia.adapter = mediaAdapter
            binding.viewPagerMedia.setCurrentItem(initialIndex, false)
        }

        // Handle delete button click
        binding.buttonDeleteCurrent.setOnClickListener {
            deleteCurrentItem()
        }
    }

    /**
     * Deletes the currently displayed media item from the device storage and updates the UI.
     * If the last item is deleted, navigates back to the previous screen.
     */
    private fun deleteCurrentItem() {
        val currentPosition = binding.viewPagerMedia.currentItem
        if (currentPosition < 0 || currentPosition >= mediaUris.size) return
        val currentUri = mediaUris[currentPosition]

        try {
            // Attempt to delete the media URI
            val rowsDeleted = requireContext().contentResolver.delete(currentUri, null, null)

            if (rowsDeleted > 0) {
                // Update the media list and ViewPager if deletion is successful
                val updatedList = mediaUris.toMutableList()

                updatedList.removeAt(currentPosition)
                sharedViewModel.setMediaUris(updatedList)

                if (updatedList.isEmpty()) {
                    // If no media items remain, navigate back
                    findNavController().popBackStack()
                } else {
                    // Update the ViewPager with the new list
                    mediaAdapter = MediaPagerAdapter(this, updatedList)
                    binding.viewPagerMedia.adapter = mediaAdapter
                    val newPos = currentPosition.coerceAtMost(updatedList.lastIndex)
                    binding.viewPagerMedia.setCurrentItem(newPos, false)

                    Toast.makeText(
                        requireContext(),
                        getString(R.string.file_deleted), Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // Show error if deletion failed
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_when_deleting), Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: SecurityException) {
            // Handle security exception if lacking permission to delete
            Toast.makeText(
                requireContext(),
                getString(R.string.no_rights_to_delete), Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            // Handle any other exceptions
            Toast.makeText(
                requireContext(),
                getString(R.string.error, e.message), Toast.LENGTH_SHORT
            ).show()
        }
    }
}
