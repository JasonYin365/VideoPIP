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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.jason.pip.videopip.R;
import com.jason.pip.videopip.adapter.VideoAdapter;
import com.jason.pip.videopip.data.VideoListData;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by Yin.Jason on 17/4/4.
 * Email:Jason.Yin365@gmail.com
 */

public class VideoListLayout extends RelativeLayout {

    private Context mContext;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView videoList;

    private VideoAdapter mVideoAdapter;
    private FrameLayout videoLayout;

    private FrameLayout fullScreen;
    private RelativeLayout smallLayout;
    private ImageView close;
    private VideoPlayView videoItemView;
    private VideoListData listData;

    public VideoListLayout(Context context) {
        super(context);
        initView(context);
        initActions();
    }


    public VideoListLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        initActions();
    }

    public VideoListLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
        initActions();
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_video_list, this, true);
        this.mContext = context;

        mLayoutManager = new LinearLayoutManager(context);
        videoList = (RecyclerView) findViewById(R.id.video_list);
        videoList.setLayoutManager(mLayoutManager);

        mVideoAdapter = new VideoAdapter(context);
        videoList.setAdapter(mVideoAdapter);

        fullScreen = (FrameLayout) findViewById(R.id.full_screen);
        videoLayout = (FrameLayout) findViewById(R.id.layout_video);
        videoItemView = new VideoPlayView(context);
        String data = readTextFileFromRawResourceId(context, R.raw.video_list);
        listData = new Gson().fromJson(data, VideoListData.class);
        mVideoAdapter.refresh(listData.getList());
        smallLayout = (RelativeLayout) findViewById(R.id.small_preview);
        close = (ImageView) findViewById(R.id.close);
    }

    private int postion = -1;
    private int lastPostion = -1;

    private void initActions() {
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoItemView.isPlay()) {
                    videoItemView.stop();
                    postion = -1;
                    lastPostion = -1;
                    videoLayout.removeAllViews();
                    smallLayout.setVisibility(View.GONE);
                }
            }
        });
        smallLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                smallLayout.setVisibility(View.GONE);
                ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        });
        videoItemView.setCompletionListener(new VideoPlayView.CompletionListener() {
            @Override
            public void completion(IMediaPlayer mp) {

                //播放完还原播放界面
                if (smallLayout.getVisibility() == View.VISIBLE) {
                    videoLayout.removeAllViews();
                    smallLayout.setVisibility(View.GONE);
                    videoItemView.setShowContoller(true);
                }

                FrameLayout frameLayout = (FrameLayout) videoItemView.getParent();
                videoItemView.release();
                if (frameLayout != null && frameLayout.getChildCount() > 0) {
                    frameLayout.removeAllViews();
                    View itemView = (View) frameLayout.getParent();

                    if (itemView != null) {
                        itemView.findViewById(R.id.showview).setVisibility(View.VISIBLE);
                    }
                }

                lastPostion = -1;
            }
        });
        mVideoAdapter.setClick(new VideoAdapter.onClick() {
            @Override
            public void onclick(int position) {
                VideoListLayout.this.postion = position;

                if (videoItemView.VideoStatus() == IjkVideoView.STATE_PAUSED) {
                    if (position != lastPostion) {

                        videoItemView.stop();
                        videoItemView.release();
                    }
                }

                if (smallLayout.getVisibility() == View.VISIBLE)

                {
                    smallLayout.setVisibility(View.GONE);
                    videoLayout.removeAllViews();
                    videoItemView.setShowContoller(true);
                }

                if (lastPostion != -1)

                {
                    ViewGroup last = (ViewGroup) videoItemView.getParent();//找到videoitemview的父类，然后remove
                    if (last != null) {
                        last.removeAllViews();
                        View itemView = (View) last.getParent();
                        if (itemView != null) {
                            itemView.findViewById(R.id.showview).setVisibility(View.VISIBLE);
                        }
                    }
                }

                if (videoItemView.getParent() != null) {
                    ((ViewGroup) videoItemView.getParent()).removeAllViews();
                }

                View view = videoList.findViewHolderForAdapterPosition(postion).itemView;
                FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.item_layout_video);
                frameLayout.removeAllViews();
                frameLayout.addView(videoItemView);
                videoItemView.start(listData.getList().get(position).getMp4_url());
                lastPostion = position;
            }
        });
        videoList.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
                int index = videoList.getChildAdapterPosition(view);
                view.findViewById(R.id.showview).setVisibility(View.VISIBLE);
                if (index == postion) {
                    FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.item_layout_video);
                    frameLayout.removeAllViews();
                    if (videoItemView != null &&
                            ((videoItemView.isPlay()) || videoItemView.VideoStatus() == IjkVideoView.STATE_PAUSED)) {
                        view.findViewById(R.id.showview).setVisibility(View.GONE);
                    }

                    if (videoItemView.VideoStatus() == IjkVideoView.STATE_PAUSED) {
                        if (videoItemView.getParent() != null)
                            ((ViewGroup) videoItemView.getParent()).removeAllViews();
                        frameLayout.addView(videoItemView);
                        return;
                    }

                    if (smallLayout.getVisibility() == View.VISIBLE && videoItemView != null && videoItemView.isPlay()) {
                        smallLayout.setVisibility(View.GONE);
                        videoLayout.removeAllViews();
                        videoItemView.setShowContoller(true);
                        frameLayout.addView(videoItemView);
                    }
                }
            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
                int index = videoList.getChildAdapterPosition(view);
                if (index == postion) {
                    FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.item_layout_video);
                    frameLayout.removeAllViews();
                    if (smallLayout.getVisibility() == View.GONE && videoItemView != null
                            && videoItemView.isPlay()) {
                        videoLayout.removeAllViews();
                        videoItemView.setShowContoller(false);
                        videoLayout.addView(videoItemView);
                        smallLayout.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (videoItemView != null) {
            videoItemView.onChanged(newConfig);
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                fullScreen.setVisibility(View.GONE);
                videoList.setVisibility(View.VISIBLE);
                fullScreen.removeAllViews();
                if (postion <= mLayoutManager.findLastVisibleItemPosition()
                        && postion >= mLayoutManager.findFirstVisibleItemPosition()) {
                    View view = videoList.findViewHolderForAdapterPosition(postion).itemView;
                    FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.layout_video);
                    frameLayout.removeAllViews();
                    frameLayout.addView(videoItemView);
                    videoItemView.setShowContoller(true);
                } else {
                    videoLayout.removeAllViews();
                    videoLayout.addView(videoItemView);
                    videoItemView.setShowContoller(false);
                    smallLayout.setVisibility(View.VISIBLE);
                }
                videoItemView.setContorllerVisiable();
            } else {
                ViewGroup viewGroup = (ViewGroup) videoItemView.getParent();
                if (viewGroup == null)
                    return;
                viewGroup.removeAllViews();
                fullScreen.addView(videoItemView);
                smallLayout.setVisibility(View.GONE);
                videoList.setVisibility(View.GONE);
                fullScreen.setVisibility(View.VISIBLE);
            }
        } else {
            mVideoAdapter.notifyDataSetChanged();
            videoList.setVisibility(View.VISIBLE);
            fullScreen.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (videoItemView == null)
            videoItemView = new VideoPlayView(mContext);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (videoLayout == null)
            return;
        if (smallLayout.getVisibility() == View.VISIBLE) {
            smallLayout.setVisibility(View.GONE);
            videoLayout.removeAllViews();
        }

        if (postion != -1) {
            ViewGroup view = (ViewGroup) videoItemView.getParent();
            if (view != null) {
                view.removeAllViews();
            }
        }
        videoItemView.stop();
        videoItemView.release();
        videoItemView.onDestroy();
        videoItemView = null;
    }

    public String readTextFileFromRawResourceId(Context context, int resourceId) {
        StringBuilder builder = new StringBuilder();

        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(
                resourceId)));

        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                builder.append(line).append("\n");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return builder.toString();
    }
}
