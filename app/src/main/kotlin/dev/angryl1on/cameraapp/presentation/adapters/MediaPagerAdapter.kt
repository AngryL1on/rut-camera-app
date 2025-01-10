package dev.angryl1on.cameraapp.presentation.adapters

import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import dev.angryl1on.cameraapp.presentation.fragments.ImageViewFragment
import dev.angryl1on.cameraapp.presentation.fragments.VideoViewFragment

class MediaPagerAdapter(
    private val fragment: Fragment,
    private val mediaUris: List<Uri>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = mediaUris.size

    override fun createFragment(position: Int): Fragment {
        val uri = mediaUris[position]
        val mimeType = fragment.requireContext().contentResolver.getType(uri) ?: ""

        return if (mimeType.startsWith("image")) {
            ImageViewFragment.newInstance(uri.toString())
        } else {
            VideoViewFragment.newInstance(uri.toString())
        }
    }
}