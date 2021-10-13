package com.kinderjoey.cookiez.ui.detail

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.commit
import androidx.navigation.findNavController
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.material.tabs.TabLayoutMediator
import com.kinderjoey.cookiez.R
import com.kinderjoey.cookiez.adapter.DetailMenuPageAdapter
import com.kinderjoey.cookiez.data.util.Resource
import com.kinderjoey.cookiez.databinding.FragmentDetailMenuBinding
import com.kinderjoey.cookiez.ui.detail.menu.*
import com.kinderjoey.cookiez.ui.detail.order.DetailVariantMenuFragment
import com.kinderjoey.cookiez.util.Constanta
import org.koin.android.viewmodel.ext.android.viewModel

class DetailMenuFragment : Fragment() {

    private val binding by viewBinding<FragmentDetailMenuBinding>()
    private var simpleExoPlayer: SimpleExoPlayer? = null
    private var playerView: PlayerView? = null
    private lateinit var mediaDataSourceFactory: DataSource.Factory
    private val viewModel by viewModel<DetailMenuViewModel>()

    companion object {
        var STREAM_URL = "https://firebasestorage.googleapis.com/v0/b/cookiez-83063.appspot.com/o/Nasi%20Goreng%20Asia.mp4?alt=media&token=004413aa-a275-449f-a20e-76e4c1f8ecdb"
        val TAB_TITLES = listOf(
            "Petunjuk",
            "Tentang",
            "Ulasan"
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val menuName = activity?.intent?.getStringExtra(Constanta.KEY_OF_MENU_NAME)
        viewModel.getDetailMenu(menuName!!).observe(viewLifecycleOwner, {
            when(it) {
                is Resource.Empty -> {}
                is Resource.Error -> {}
                is Resource.Loading -> {}
                is Resource.Success -> STREAM_URL = it.data?.videoUrl.toString()
            }
        })

        initializePlayer()

        playerView?.setOnClickListener {
            if (simpleExoPlayer?.playWhenReady == true)
                onPause()
            else
                onStart()
        }

        binding.includeAppBarMiddle.tvTittle.text = menuName

        val fragmentManager = parentFragmentManager
        val fragment = DetailVariantMenuFragment()
        binding.includeBottomBarDetail.btnOrder.setOnClickListener {
            fragmentManager.commit {
                replace(R.id.detail_container, fragment, fragment::class.java.simpleName)
            }
        }

        val pagerAdapter = DetailMenuPageAdapter(
            requireActivity().supportFragmentManager,
            lifecycle,
            menuName
        )

        binding.apply {
            pagerAdapter.apply {
                viewPager.adapter = this
            }
            TabLayoutMediator(tabDetail, viewPager) { tab, position ->
                tab.text = TAB_TITLES[position]
            }.attach()

            includeBottomBarDetail.btnOrder.setOnClickListener {
                view.findNavController().navigate(
                    DetailMenuFragmentDirections
                        .actionDetailMenuFragmentToDetailVariantMenuFragment(menuName))
            }
        }
    }

    private fun initializePlayer() {
        mediaDataSourceFactory = DefaultDataSourceFactory(requireActivity(), Util.getUserAgent(requireActivity(), "mediaPlayerSample"))

        val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(
            MediaItem.fromUri(STREAM_URL))

        val mediaSourceFactory: MediaSourceFactory = DefaultMediaSourceFactory(mediaDataSourceFactory)

        simpleExoPlayer = SimpleExoPlayer.Builder(requireActivity())
            .setMediaSourceFactory(mediaSourceFactory)
            .build()

        simpleExoPlayer!!.addMediaSource(mediaSource)

        simpleExoPlayer!!.playWhenReady = true

        binding.playerView.setShutterBackgroundColor(Color.TRANSPARENT)
        binding.playerView.player = simpleExoPlayer
        binding.playerView.requestFocus()
    }

    private fun releasePlayer() {
        simpleExoPlayer?.release()
    }

    override fun onStart() {
        super.onStart()

        if (Util.SDK_INT > 23) initializePlayer()
    }

    override fun onResume() {
        super.onResume()

        if (Util.SDK_INT <= 23) initializePlayer()
    }

    override fun onPause() {
        super.onPause()

        if (Util.SDK_INT <= 23) releasePlayer()
    }

    override fun onStop() {
        super.onStop()

        if (Util.SDK_INT > 23) releasePlayer()
    }
}