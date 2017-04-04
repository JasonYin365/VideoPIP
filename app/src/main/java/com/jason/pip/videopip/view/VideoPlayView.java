/*
 * Copyright 2017 Jason Yin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jason.pip.videopip.view;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.jason.pip.videopip.R;
import com.jason.pip.videopip.controller.CustomMediaController;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by Yin.Jason on 17/4/4.
 * Email:Jason.Yin365@gmail.com
 */

public class VideoPlayView extends RelativeLayout implements MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener {
    private CustomMediaController mediaController;
    private View mView;
    private IjkVideoView mVideoView;
    private Handler mHandler = new Handler();
    private View rView;
    private Context mContext;
    private boolean portrait;

    public VideoPlayView(Context context) {

        super(context);
        mContext = context;
        initData();
        initViews();
        initActions();
    }

    private void initActions() {
    }

    private void initViews() {
        rView = LayoutInflater.from(mContext).inflate(R.layout.view_video_item, this, true);
        mView = findViewById(R.id.media_contoller);
        mVideoView = (IjkVideoView) findViewById(R.id.main_video);
        mediaController = new CustomMediaController(mContext, rView);
        mVideoView.setMediaController(mediaController);

        mVideoView.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer mp) {
                mView.setVisibility(View.GONE);
                if (mediaController.getScreenOrientation((Activity) mContext)
                        == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    //横屏播放完毕，重置
                    ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    ViewGroup.LayoutParams layoutParams = getLayoutParams();
                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    setLayoutParams(layoutParams);
                }
                if (completionListener != null)
                    completionListener.completion(mp);
            }
        });
    }

    private void initData() {
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    public boolean isPlay() {
        return mVideoView.isPlaying();
    }

    public void pause() {
        if (isPlay()) {
            mVideoView.pause();
        } else {
            mVideoView.start();
        }
    }

    public void start(String path) {
        Uri uri = Uri.parse(path);
        if (mediaController != null)
            mediaController.start();
        if (!mVideoView.isPlaying()) {
            mVideoView.setVideoURI(uri);
            mVideoView.start();
        } else {
            mVideoView.stopPlayback();
            mVideoView.setVideoURI(uri);
            mVideoView.start();
        }
    }

    public void start() {
        if (mVideoView.isPlaying()) {
            mVideoView.start();
        }
    }

    public void setContorllerVisiable() {
        mediaController.setVisiable();
    }

    public void seekTo(int msec) {
        mVideoView.seekTo(msec);
    }

    public void onChanged(Configuration configuration) {
        portrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT;
        doOnConfigurationChanged(portrait);
    }

    public void doOnConfigurationChanged(final boolean portrait) {
        if (mVideoView != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    setFullScreen(!portrait);
                    if (portrait) {
                        ViewGroup.LayoutParams layoutParams = getLayoutParams();
                        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                        Log.e("handler", "400");
                        setLayoutParams(layoutParams);
                        requestLayout();
                    } else {
                        int heightPixels = ((Activity) mContext).getResources().getDisplayMetrics().heightPixels;
                        int widthPixels = ((Activity) mContext).getResources().getDisplayMetrics().widthPixels;
                        ViewGroup.LayoutParams layoutParams = getLayoutParams();
                        layoutParams.height = heightPixels;
                        layoutParams.width = widthPixels;
                        Log.e("handler", "height==" + heightPixels + "\nwidth==" + widthPixels);
                        setLayoutParams(layoutParams);
                    }
                }
            });
//            orientationEventListener.enable();
        }
    }

    public void stop() {
        if (mVideoView.isPlaying()) {
            mVideoView.stopPlayback();
        }
    }

    public void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
//        orientationEventListener.disable();
    }

    private void setFullScreen(boolean fullScreen) {
        if (mContext != null && mContext instanceof Activity) {
            WindowManager.LayoutParams attrs = ((Activity) mContext).getWindow().getAttributes();
            if (fullScreen) {
                attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                ((Activity) mContext).getWindow().setAttributes(attrs);
                ((Activity) mContext).getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            } else {
                attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
                ((Activity) mContext).getWindow().setAttributes(attrs);
                ((Activity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            }
        }

    }

    public void setShowContoller(boolean isShowContoller) {
        mediaController.setShowContoller(isShowContoller);
    }

    public void release() {
        mVideoView.release(true);
    }

    public int VideoStatus() {
        return mVideoView.getCurrentStatue();
    }

    private CompletionListener completionListener;

    public void setCompletionListener(CompletionListener completionListener) {
        this.completionListener = completionListener;
    }

    public interface CompletionListener {
        void completion(IMediaPlayer mp);
    }
}
