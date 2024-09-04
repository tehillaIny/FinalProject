package com.example.finalproject.fragments

import com.example.finalproject.R
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.finalproject.databinding.FragmentUploadRecommendationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.example.finalproject.models.Recommendation
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Deferred

class UploadRecommendationFragment : Fragment() {

    private var _binding: FragmentUploadRecommendationBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private var selectedImageUri: Uri? = null
    private val selectedMoreImageUris = mutableListOf<Uri>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUploadRecommendationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        binding.uploadPostPhotoButton.setOnClickListener {
            pickImageFromGallery()
        }

        binding.uploadMorePhotosButton.setOnClickListener {
            pickMultipleImagesFromGallery()
        }

        binding.submitRecommendationButton.setOnClickListener {
            uploadRecommendation()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1000)
    }

    private fun pickMultipleImagesFromGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.type = "image/*"
        startActivityForResult(intent, 2000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            binding.uploadPostPhotoButton.text = "Image Selected"
        } else if (requestCode == 2000 && resultCode == Activity.RESULT_OK) {
            data?.clipData?.let { clipData ->
                for (i in 0 until clipData.itemCount) {
                    selectedMoreImageUris.add(clipData.getItemAt(i).uri)
                }
                binding.uploadMorePhotosButton.text =
                    "${selectedMoreImageUris.size} Images Selected"
            }
        }
    }

    private fun uploadRecommendation() {
        if (auth.currentUser == null) {
            Toast.makeText(
                requireContext(),
                "User not authenticated. Please log in.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val restaurantName = binding.restaurantNameEditText.text.toString()
        val address = binding.addressEditText.text.toString()
        val type = binding.typeEditText.text.toString()
        val description = binding.descriptionEditText.text.toString()

        // Ensure all required fields are filled
        if (restaurantName.isEmpty() || address.isEmpty() || type.isEmpty() || description.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        // Check if main image is selected
        if (selectedImageUri == null) {
            Toast.makeText(requireContext(), "Please select a main image", Toast.LENGTH_SHORT)
                .show()
            return
        }
        binding.uploadProgressBar.visibility = View.VISIBLE
        binding.uploadProgressTextView.visibility = View.VISIBLE
        binding.submitRecommendationButton.isEnabled = false

        // Upload main image
        val ref =
            storage.reference.child("recommendation_photos/${auth.uid}/${System.currentTimeMillis()}.jpg")
        ref.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    uploadMorePhotos(restaurantName, address, type, description, uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
                binding.uploadProgressBar.visibility = View.GONE
                binding.uploadProgressTextView.visibility = View.GONE
                binding.submitRecommendationButton.isEnabled = true
            }
    }

    private fun uploadMorePhotos(restaurantName: String, address: String, type: String, description: String, mainImageUrl: String) {
        val imageUrls = mutableListOf<String>()
        val deferredUploads = mutableListOf<Deferred<Unit>>()

        if (selectedMoreImageUris.isEmpty()) {
            saveRecommendationToDatabase(restaurantName, address, type, description, mainImageUrl, imageUrls)
            return
        }

        val scope = CoroutineScope(Dispatchers.IO)

        // Show the progress bar and text view
        binding.uploadProgressBar.visibility = View.VISIBLE
        binding.uploadProgressTextView.visibility = View.VISIBLE

        selectedMoreImageUris.forEachIndexed { index, uri ->
            val deferred: Deferred<Unit> = scope.async {
                val ref = storage.reference.child("recommendation_photos/${auth.uid}/${System.currentTimeMillis()}.jpg")
                val uploadTask = ref.putFile(uri)

                try {
                    val downloadUrl = uploadTask.await().storage.downloadUrl.await()
                    imageUrls.add(downloadUrl.toString())
                    // Update progress
                    withContext(Dispatchers.Main) {
                        val progress = ((index + 1) / selectedMoreImageUris.size.toFloat()) * 100
                        binding.uploadProgressBar.progress = progress.toInt()
                        binding.uploadProgressTextView.text = "Uploaded ${progress.toInt()}%"
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Failed to upload additional photos", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            deferredUploads.add(deferred)
        }

        scope.launch {
            // Wait for all uploads to complete
            deferredUploads.awaitAll()

            // Hide the progress bar and text view
            withContext(Dispatchers.Main) {
                binding.uploadProgressBar.visibility = View.GONE
                binding.uploadProgressTextView.visibility = View.GONE
                binding.submitRecommendationButton.isEnabled = true
            }

            // Once all images are uploaded, save to the database
            withContext(Dispatchers.Main) {
                saveRecommendationToDatabase(restaurantName, address, type, description, mainImageUrl, imageUrls)
            }
        }
    }

    private fun saveRecommendationToDatabase(
        restaurantName: String,
        address: String,
        type: String,
        description: String,
        mainImageUrl: String,
        imageUrls: List<String>
    ) {
        val userId = auth.uid ?: return
        val recommendationId = database.reference.child("recommendations").push().key ?: return

        val recommendation = Recommendation(
            userId,
            restaurantName,
            address,
            type,
            description,
            mainImageUrl,
            imageUrls

        )
        database.reference.child("recommendations").child(recommendationId).setValue(recommendation)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Recommendation submitted", Toast.LENGTH_SHORT)
                    .show()
                clearFields()
                findNavController().navigate(R.id.action_uploadRecommendationFragment_to_mainFeedFragment)
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Failed to submit recommendation",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun clearFields() {
        // Clear text fields
        binding.restaurantNameEditText.text.clear()
        binding.addressEditText.text.clear()
        binding.typeEditText.text.clear()
        binding.descriptionEditText.text.clear()

        // Reset image buttons
        binding.uploadPostPhotoButton.text = "Select Main Image"
        binding.uploadMorePhotosButton.text = "Select More Images"

        // Clear selected images
        selectedImageUri = null
        selectedMoreImageUris.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}