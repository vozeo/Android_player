package com.example.exoplayerdemo.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.exoplayerdemo.R
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.ParametersBuilder
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.ui.StyledPlayerControlView
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.util.EventLogger


open class PlayerActivity : AppCompatActivity(), StyledPlayerControlView.VisibilityListener {

    private var mPlayer: SimpleExoPlayer? = null
    private var trackSelector: DefaultTrackSelector? = null
    private var trackSelectorParameters: DefaultTrackSelector.Parameters? = null
    private var mPlayerView: StyledPlayerView? = null



    /*
    private var debugViewHelper: DebugTextViewHelper? = null
    private var debugTextView: TextView? = null
    private var debugRootView: LinearLayout? = null*/
    private var rebufferingview: TextView? = null
    private var rebufferListView: TextView? = null
    var rebufferCount: Int = -1
    var rebufferList = ArrayList<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        val playUri = intent.getStringExtra("streamPath")
        mPlayerView = findViewById(R.id.player_view)
        rebufferingview = findViewById(R.id.rebuffering_time)
        rebufferListView = findViewById(R.id.rebuffering_list)
        //debugTextView = findViewById(R.id.debug_text_view)
        //debugRootView = findViewById(R.id.controls_root)
        initPlayer(playUri)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mPlayer != null) {
            mPlayer!!.release()
        }
    }

    private fun initPlayer(playUri: String?) {

        if (playUri == null) {
            Log.d("ExoTest", "playUri is null!")
            return
        }
        Log.d("chenjh", "initplayer")
        /* 1.??????SimpleExoPlayer?????? */
        mPlayer = SimpleExoPlayer.Builder(this).build()

        /* 2.??????????????????????????????????????? */
        val firstLocalMediaItem = MediaItem.fromUri(playUri)
        mPlayer!!.addMediaItem(firstLocalMediaItem)

        /* ??????:?????????????????? */
        mPlayer!!.repeatMode = Player.REPEAT_MODE_ALL
        /* ????????????????????????listener */
        val builder = ParametersBuilder( /* context = */this)
        trackSelectorParameters = builder.build()
        trackSelector = DefaultTrackSelector( /* context = */this)
        trackSelector!!.parameters = trackSelectorParameters!!
        mPlayer!!.addAnalyticsListener(VideoQoEListener(trackSelector))

        /* 3.????????????????????????????????? */
        mPlayer!!.playWhenReady = true

        /* ????????????????????????????????????  */
        mPlayerView?.setControllerVisibilityListener(this)
        /* 4.???SimpleExoPlayer???????????????StyledPlayerView??? */
        mPlayerView!!.player = mPlayer
        /* ???????????????exoplayer?????????debug helper???????????????????????????
        debugViewHelper = DebugTextViewHelper(mPlayer!!, debugTextView!!)
        debugViewHelper!!.start()*/

        /* 5???????????????????????????prepare */
        mPlayer!!.prepare()

        // mPlayer!!.addAnalyticsListener(analyticsListener)
        rebufferingview?.text = "?????????????????????0"
    }

    override fun onVisibilityChange(visibility: Int) {
        Log.d("ExoTest", "visibility:$visibility")
        // debugRootView?.visibility = visibility
    }

    inner class VideoQoEListener(trackSelector: MappingTrackSelector?) :
        EventLogger(trackSelector) {
        private var start_buffer_time: Long = 0
        private var end_buffer_time: Long = 0

        @SuppressLint("SetTextI18n")
        override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
            super.onPlaybackStateChanged(eventTime, state)
            if (state == 2) {
                start_buffer_time = System.currentTimeMillis()
                rebufferCount += 1
                rebufferingview?.text = "?????????????????????$rebufferCount??????????????????"
                rebufferListView?.text = "???????????????????????????[${rebufferList.joinToString(", ")}]"
            } else if (state == 3 && rebufferCount != 0) {
                end_buffer_time = System.currentTimeMillis()
                val lastBufferTime  = end_buffer_time - start_buffer_time
                rebufferList.add(lastBufferTime)
                rebufferingview?.text =
                    "?????????????????????" + rebufferCount.toString() + "??????????????????????????????" + lastBufferTime.toString() + "ms"
                rebufferListView?.text = "???????????????????????????[${rebufferList.joinToString(", ")}]"
            }
        }
    }
}