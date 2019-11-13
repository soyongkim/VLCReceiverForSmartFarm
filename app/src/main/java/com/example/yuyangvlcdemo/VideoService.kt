package com.example.yuyangvlcdemo

import android.app.Service
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Binder
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.Util
import java.util.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.FileDataSource.FileDataSourceException
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory


class VideoService : Service() {
    private var cnt = 0
    private var player: SimpleExoPlayer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("pudroid", "onStartCommand - " + this.cnt)
        return super.onStartCommand(intent, flags, startId)
    }

    inner class LocalBinder : Binder() {
        fun getService(): VideoService {
            this@VideoService.cnt++
            //.makeText(this@VideoService, "Bind ${this@VideoService.cnt}", Toast.LENGTH_SHORT).show()
            return this@VideoService
        }
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun release(conn: ServiceConnection) {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                this@VideoService.run {
                    if (this.cnt > 0) {
                        this.cnt--
                        if (this.cnt == 0) {
                            unbindService(conn)
                        }
                    }
                }
            }
        }, 1000)
    }

    override fun onDestroy() {
        this.cnt = 0
        player!!.release()
        super.onDestroy()
    }

    fun pause() {
        this.player!!.playWhenReady = false
    }

    fun play() {
        this.player!!.playWhenReady = true
    }

    fun initExoPlayer(mExoPlayerView: PlayerView) {
        if (this.player == null) {
            val bandwidthMeter = DefaultBandwidthMeter()
            val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
            val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
            val loadControl = DefaultLoadControl()
            this.player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl)

            val userAgent =
                Util.getUserAgent(this, applicationContext.applicationInfo.packageName)

            val httpDataSourceFactory = DefaultHttpDataSourceFactory(
                userAgent, null, DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS, true
            )

            val dataSourceFactory =
                DefaultDataSourceFactory(this, null, httpDataSourceFactory)
            val sample = "http://cloud.godopu.net/index.php/s/7XMWHcI3vMLmolw/download"
            val videoSource =
                ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(sample))
            this.player!!.prepare(videoSource)
        }
        val id = "1"
        this.player!!.prepare(prepareExoPlayerFromFileUri("${Environment.getExternalStorageDirectory().getAbsolutePath()}/Download/VLC/video_vt${id}.mp4"))
        mExoPlayerView.player = this.player
        this.player!!.playWhenReady = false
    }

    fun prepareVideo(id : Integer)
    {
        this.player!!.prepare(prepareExoPlayerFromFileUri("${Environment.getExternalStorageDirectory().getAbsolutePath()}/Download/VLC/video_vt${id}.mp4"))
        this.player!!.playWhenReady = false
    }

    private fun prepareExoPlayerFromFileUri(videoUrl: String): MediaSource {
        val uri = Uri.parse(videoUrl);
        return buildMediaSource(uri)
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        return ExtractorMediaSource.Factory(
            DefaultDataSourceFactory(this, applicationContext.applicationInfo.packageName)
        ).createMediaSource(uri)
    }

}