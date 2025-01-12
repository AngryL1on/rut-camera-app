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

    /**
     * Returns the total number of media items to be displayed in the ViewPager2.
     *
     * @return The number of media items in the [mediaUris] list.
     */
    override fun getItemCount(): Int = mediaUris.size

    /**
     * Creates a new [Fragment] for the media item at the specified position.
     * If the media is an image, an [ImageViewFragment] is created.
     * If the media is a video, a [VideoViewFragment] is created.
     *
     * @param position The position of the media item in the [mediaUris] list.
     * @return A [Fragment] instance to display the media item.
     */
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