package dev.angryl1on.cameraapp.presentation.fragments

import android.Manifest
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

    private var _binding: FragmentPhotoCaptureBinding? = null
    private val binding get() = _binding!!

    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService

    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK

    companion object {
        private const val TAG = "PhotoCaptureFragment"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
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
        _binding = FragmentPhotoCaptureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Проверка разрешений
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Инициализация камеры
        binding.buttonTakePhoto.setOnClickListener { takePhoto() }
        binding.buttonSwitchCamera.setOnClickListener { switchCamera() }
        binding.buttonOpenVideo.setOnClickListener {
            findNavController().navigate(R.id.action_photo_to_video)
        }

        binding.buttonOpenGallery.setOnClickListener {
            findNavController().navigate(R.id.action_photo_to_gallery)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Инициализация CameraProvider
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Настройка Preview
            val preview = Preview.Builder().build().also { previewbuilder ->
                previewbuilder.surfaceProvider = binding.previewView.surfaceProvider
            }

            // Настройка ImageCapture
            imageCapture = ImageCapture.Builder().build()

            // Выбор камеры
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            try {
                // Прерывание предыдущего использования камеры
                cameraProvider.unbindAll()

                // Привязка use cases к камере
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Ошибка привязки камеры: ${exc.message}")
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // Создание файла для сохранения фото
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

        // Сохранение фото
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Ошибка сохранения фото: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d(TAG, "Фото сохранено: ${output.savedUri}")
                }
            }
        )
    }

    private fun switchCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
            CameraSelector.LENS_FACING_FRONT
        else
            CameraSelector.LENS_FACING_BACK
        startCamera()
    }

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

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray,
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                // Закрытие приложения или показ сообщения
                requireActivity().finish()
            }
        }
    }
}
