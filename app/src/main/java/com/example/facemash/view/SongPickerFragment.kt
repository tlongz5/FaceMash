package com.example.facemash.view

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.baseproject.model.MusicData
import com.example.facemash.databinding.FragmentSongPickerBinding
import com.example.facemash.view.main.MainViewModel
import com.example.magicface.base.BaseFragment
import com.example.magicface.utils.PlayerInstance.stop
import com.example.magicface.utils.playMusic
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class SongPickerFragment : BaseFragment<FragmentSongPickerBinding>(FragmentSongPickerBinding::inflate){
    private val mainViewModel: MainViewModel by activityViewModels()

    private lateinit var songPickerAdapter: SongPickerAdapter

    private var selectedMusic: MusicData? = null

    override fun FragmentSongPickerBinding.initView() {
        songPickerAdapter = SongPickerAdapter { music ->
            selectedMusic = music
            playMusic(music)
        }

        rcvSound.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = songPickerAdapter
        }
    }

    override fun FragmentSongPickerBinding.initListener() {
        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        icSelect.setOnClickListener {
            selectedMusic?.let { music ->
                mainViewModel.setSelectedMusic(music)
            }

            findNavController().navigateUp()
        }
    }

    override fun initObserver() {
        mainViewModel.dataMusicFromAssets.observe(viewLifecycleOwner) { musicList ->
            val noSoundItem = MusicData(
                id = "",
                name = "",
                isDefault = false,
                type = "",
                url = ""
            )
            songPickerAdapter.submitList(listOf(noSoundItem) + musicList)

            if (selectedMusic == null) {
                selectedMusic = musicList.firstOrNull()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stop()
    }

}
