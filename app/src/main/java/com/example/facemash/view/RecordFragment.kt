package com.example.facemash.view

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.example.facemash.R
import com.example.facemash.databinding.FragmentRecordBinding
import com.example.facemash.base.BaseFragment
import com.example.facemash.utils.ShareHelper
import com.example.facemash.utils.VideoStorage
import com.example.facemash.utils.setSingleClick
import java.io.File

class RecordFragment : BaseFragment<FragmentRecordBinding>(FragmentRecordBinding::inflate) {

    private lateinit var recordAdapter: RecordAdapter

    override fun FragmentRecordBinding.initView() {
        recordAdapter = RecordAdapter(
            onItemClick = { file ->
                // Navigate to Preview screen if needed
                navigateTo(R.id.recordDetailFragment, Bundle().apply {
                    putString("video_path", file.absolutePath)
                })
            },
            onSelectionChanged = {
                updateTopButtons()
            }
        )

        rvRecord.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = recordAdapter
        }

        toggleSelectionMode(false)
        loadVideos()
    }

    private fun loadVideos() {
        val videos = VideoStorage.getAllVideos(requireContext())
        android.util.Log.d("RecordFragment", "Loaded ${videos.size} videos from ${VideoStorage.getVideoDirectory(requireContext())?.absolutePath}")
        recordAdapter.submitList(videos)
    }

    override fun FragmentRecordBinding.initListener() {
        btnSelect.setSingleClick {
            toggleSelectionMode(true)
        }

        btnBack.setSingleClick {
            toggleSelectionMode(false)
        }

        btnDelete.setSingleClick {
            deleteSelectedFiles()
        }

        btnSend.setSingleClick {
            shareSelectedFiles()
        }

        btnSetting.setSingleClick {
            navigateTo(R.id.settingFragment)
        }
    }

    private fun toggleSelectionMode(isSelection: Boolean) {
        recordAdapter.isSelectionMode = isSelection
        binding.btnSelect.isVisible = !isSelection
        binding.btnBack.isVisible = isSelection
        binding.btnSetting.isVisible = !isSelection
        binding.btnDelete.isVisible = isSelection
        binding.btnSend.isVisible = isSelection
        updateTopButtons()
    }

    private fun updateTopButtons() {
        val count = recordAdapter.selectedFiles.size
        binding.btnDelete.alpha = if (count > 0) 1.0f else 0.5f
        binding.btnDelete.isEnabled = count > 0
        binding.btnSend.alpha = if (count > 0) 1.0f else 0.5f
        binding.btnSend.isEnabled = count > 0
    }

    private fun deleteSelectedFiles() {
        val selected = recordAdapter.selectedFiles.toList()
        selected.forEach { it.delete() }
        toggleSelectionMode(false)
        loadVideos()
    }

    private fun shareSelectedFiles() {
        val selected = recordAdapter.selectedFiles.toList()
        if (selected.isNotEmpty()) {
            ShareHelper(requireContext()).shareFiles(selected)
        }
    }

    override fun onResume() {
        super.onResume()
        loadVideos()
    }

    override fun initObserver() {

    }
}
