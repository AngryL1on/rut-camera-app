package dev.angryl1on.cameraapp.presentation.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dev.angryl1on.cameraapp.R
import dev.angryl1on.cameraapp.databinding.FragmentVideoCaptureBinding
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class VideoCaptureFragment : Fragment() {

    private var _binding: FragmentVideoCaptureBinding? = null
    private val binding get() = _binding!!

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var isRecording = false

    private lateinit var cameraExecutor: ExecutorService

    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK

    companion object {
        private const val TAG = "VideoCaptureFragment"
        private const val REQUEST_CODE_PERMISSIONS = 20

        /**
         * List of required permissions for video recording functionality.
         */
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentVideoCaptureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        startCamera()

        binding.buttonRecordVideo.setOnClickListener { toggleRecording() }
        binding.buttonSwitchCamera.setOnClickListener { switchCamera() }
        binding.buttonOpenPhoto.setOnClickListener {
            findNavController().navigate(R.id.action_video_to_photo)
        }
        binding.buttonOpenGallery.setOnClickListener {
            findNavController().navigate(R.id.action_video_to_gallery)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    /**
     * Starts the camera and initializes the video capture functionality.
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener( {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = binding.previewView.surfaceProvider
            }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    videoCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Camera binding error: ${exc.message}")
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /**
     * Toggles the recording state. Starts a new recording if not already recording,
     * or stops the current recording and saves the video.
     */
    @SuppressLint("MissingPermission")
    private fun toggleRecording() {
        if (isRecording) {
            recording?.stop()
            isRecording = false
            binding.buttonRecordVideo.setImageResource(R.drawable.ic_shutter_button)
            Toast.makeText(requireContext(), "Recording stopped", Toast.LENGTH_SHORT).show()
        } else {
            val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(System.currentTimeMillis())
            val contentValues = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, name)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraApp")
                }
            }

            val mediaStoreOutputOptions = MediaStoreOutputOptions.Builder(
                requireContext().contentResolver,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            ).setContentValues(contentValues).build()

            recording = videoCapture?.output
                ?.prepareRecording(requireContext(), mediaStoreOutputOptions)
                ?.withAudioEnabled()
                ?.start(ContextCompat.getMainExecutor(requireContext())) { event ->
                    when (event) {
                        is VideoRecordEvent.Start -> {
                            isRecording = true
                            binding.buttonRecordVideo.setImageResource(R.drawable.ic_shutter_button)
                            Toast.makeText(requireContext(),
                                getString(R.string.recording_has_started), Toast.LENGTH_SHORT).show()
                        }
                        is VideoRecordEvent.Finalize -> {
                            if (!event.hasError()) {
                                val uri = event.outputResults.outputUri
                                Log.d(TAG, "Video saved: $uri")
                                Toast.makeText(requireContext(),
                                    getString(R.string.video_saved), Toast.LENGTH_SHORT).show()
                            } else {
                                Log.e(TAG, "Video recording error: ${event.error}")
                                Toast.makeText(requireContext(),
                                    getString(R.string.video_recording_error), Toast.LENGTH_SHORT).show()
                            }
                            isRecording = false
                            binding.buttonRecordVideo.setImageResource(R.drawable.ic_shutter_button)
                        }
                    }
                }
        }
    }

    /**
     * Toggles the camera between the front and back lenses.
     */
    private fun switchCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
            CameraSelector.LENS_FACING_FRONT
        else
            CameraSelector.LENS_FACING_BACK
    }

    /**
     * Checks if all required permissions for video recording are granted.
     *
     * @return `true` if all permissions are granted, `false` otherwise.
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }

    /**
     * Handles the result of the permission request. If permissions are granted, starts the camera.
     * If not, finishes the activity.
     *
     * @param requestCode The request code for the permissions.
     * @param permissions The array of requested permissions.
     * @param grantResults The results for each requested permission.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray,
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                requireActivity().finish()
            }
        }
    }
}
