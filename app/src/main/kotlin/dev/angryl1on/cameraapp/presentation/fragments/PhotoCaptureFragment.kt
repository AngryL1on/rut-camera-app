package dev.angryl1on.cameraapp.presentation.fragments

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dev.angryl1on.cameraapp.R
import dev.angryl1on.cameraapp.databinding.FragmentPhotoCaptureBinding
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PhotoCaptureFragment : Fragment() {

    private lateinit var binding: FragmentPhotoCaptureBinding
    private lateinit var cameraExecutor: ExecutorService

    private var imageCapture: ImageCapture? = null
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK

    companion object {
        private const val TAG = "PhotoCaptureFragment"
        private const val REQUEST_CODE_PERMISSIONS = 10

        /**
         * List of required permissions for the camera and storage functionality.
         */
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.RECORD_AUDIO)
            }
        }.toTypedArray()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPhotoCaptureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        startCamera()

        binding.buttonTakePhoto.setOnClickListener {
            takePhoto()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                animateFlash()
            }
        }
        binding.buttonSwitchCamera.setOnClickListener { switchCamera() }
        binding.buttonOpenVideo.setOnClickListener {
            findNavController().navigate(R.id.action_photo_to_video)
        }

        binding.buttonOpenGallery.setOnClickListener {
            findNavController().navigate(R.id.action_photo_to_gallery)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    /**
     * Starts the camera with the current lens facing.
     * Configures the preview and sets up the [ImageCapture] use case.
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        // Configure the Preview use case
        val preview = Preview.Builder().build().also {
            it.surfaceProvider = binding.previewView.surfaceProvider
        }

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Configure the ImageCapture use case
            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )

                Log.d(TAG, "Camera successfully bound: $cameraSelector")
            } catch (exc: Exception) {
                Log.e(TAG, "Error binding camera: ${exc.message}", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /**
     * Captures a photo and saves it to external storage using the [ImageCapture] use case.
     */
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraApp")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            requireContext().contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, getString(R.string.error_saving_photo, exc.message), exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d(TAG, getString(R.string.photo_saved, output.savedUri))
                }
            }
        )
    }

    /**
     * Toggles between the front and back cameras and restarts the camera preview.
     */
    private fun switchCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
            CameraSelector.LENS_FACING_FRONT
        else
            CameraSelector.LENS_FACING_BACK

        startCamera()
    }

    /**
     * Checks if all required permissions have been granted.
     *
     * @return `true` if all permissions are granted, `false` otherwise.
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Cleans up resources, including shutting down the camera executor.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }

    /**
     * Handles the result of the permission request. If permissions are granted, the camera is started.
     * If permissions are denied, the activity is finished.
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

    /**
     * Animates a flash effect on the screen by temporarily overlaying a white foreground.
     *
     * The method uses `postDelayed` to create a brief flash effect by setting the `foreground`
     * of the root view to a white `ColorDrawable`, then removing it after 50 milliseconds.
     *
     * This method requires a minimum API level of 23 (Android 6.0, Marshmallow) due to the use
     * of the `foreground` property.
     *
     * @throws IllegalStateException If called on a device running a lower API level than 23.
     *
     * @see android.os.Build.VERSION_CODES.M
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun animateFlash() {
        binding.root.postDelayed({
            binding.root.foreground = ColorDrawable(Color.WHITE)
            binding.root.postDelayed({
                binding.root.foreground = null
            }, 50)
        }, 100)
    }
}
