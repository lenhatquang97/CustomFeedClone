package com.quangln2.customfeedui.ui.screens.viewimageorvideo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.quangln2.customfeedui.R
import com.quangln2.customfeedui.data.database.FeedDatabase
import com.quangln2.customfeedui.data.datasource.local.LocalDataSourceImpl
import com.quangln2.customfeedui.data.datasource.remote.RemoteDataSourceImpl
import com.quangln2.customfeedui.data.repository.FeedRepository
import com.quangln2.customfeedui.databinding.FragmentImageOrVideoBinding
import com.quangln2.customfeedui.imageloader.domain.ImageLoader
import com.quangln2.customfeedui.others.utils.CodeUtils
import com.quangln2.customfeedui.others.utils.DownloadUtils
import com.quangln2.customfeedui.ui.viewmodel.ViewFullViewModel
import com.quangln2.customfeedui.ui.viewmodelfactory.ViewModelFactory


class ImageOrVideoFragment(private val player: ExoPlayer) : Fragment() {
    private lateinit var binding: FragmentImageOrVideoBinding
    private var currentVideoPosition = -1L
    private var urlTmp = ""
    private val database by lazy { FeedDatabase.getFeedDatabase(requireContext()) }
    private val viewModel: ViewFullViewModel by viewModels {
        ViewModelFactory(FeedRepository(LocalDataSourceImpl(database.feedDao()), RemoteDataSourceImpl()))
    }

    private var urlLoadThumbnail = ""

    override fun onStart(){
        super.onStart()
        val listOfUrls = arguments?.getStringArrayList("listOfUrls")
        val position = arguments?.getInt("position")
        currentVideoPosition = arguments?.getLong("currentVideoPosition") ?: -1
        if (listOfUrls != null && position != null) {
            urlTmp = DownloadUtils.getTemporaryFilePath(listOfUrls[position], requireContext())
            urlLoadThumbnail = urlTmp
            loadImageThumbnail()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_image_or_video, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        player.addListener(
            object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    when (playbackState) {
                        Player.STATE_ENDED -> {
                            player.seekTo(0)
                        }
                    }
                }
            }
        )
        return binding.root
    }

    private fun loadImageThumbnail(){
        val url = if(urlLoadThumbnail.contains("/data/user")) "" else CodeUtils.convertVideoUrlToImageUrl(urlLoadThumbnail)
        val imageLoader = ImageLoader(requireContext(),0, 0, lifecycleScope)
        imageLoader.loadImage(url, binding.fullImageView)
    }

    private fun initializeVideoForLoading(url: String) {
        val mediaItem = MediaItem.fromUri(url)
        player.apply {
            setMediaItem(mediaItem)
            seekTo(currentVideoPosition)
            prepare()
            playWhenReady = true
        }
        binding.fullVideoView.player = player
    }

    override fun onPause() {
        super.onPause()
        val mimeType = DownloadUtils.getMimeType(urlTmp)
        mimeType?.apply {
            if (this.contains("video")) {
                player.pause()
                currentVideoPosition = player.currentPosition
                binding.fullVideoView.player = null
                viewModel.fullImageViewVisibility.value = true
                viewModel.fullVideoViewVisibility.value = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val mimeType = DownloadUtils.getMimeType(urlTmp)
        mimeType?.apply {
            if(this.contains("video")){
                initializeVideoForLoading(urlTmp)
                viewModel.fullImageViewVisibility.value = false
                viewModel.fullVideoViewVisibility.value = true
            } else {
                viewModel.fullImageViewVisibility.value = true
                viewModel.fullVideoViewVisibility.value = false
            }
        }
    }
}