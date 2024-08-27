package com.example.finalproject.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.finalproject.R


class PostPageFragment : Fragment() {

    private var _binding: FragmentPostPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPostPageBinding.inflate(inflater, container, false)

        val recommendation = arguments?.let { PostPageFragmentArgs.fromBundle(it).recommendation }

        binding.textViewPlaceName.text = recommendation?.placeName
        binding.textViewLocation.text = recommendation?.location
        binding.textViewDescription.text = recommendation?.description

        // Load the main image and other images
        Glide.with(binding.imageViewMain)
            .load(recommendation?.mainImageUrl)
            .into(binding.imageViewMain)

        // Load additional images if available
        // Add logic to handle comments and likes

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
