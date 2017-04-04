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

package com.jason.pip.videopip.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jason.pip.videopip.R;
import com.jason.pip.videopip.data.VideoItemData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yin.Jason on 17/4/4.
 * Email:Jason.Yin365@gmail.com
 */

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {
    private List<VideoItemData> mList;

    public VideoAdapter(Context context) {
        mList = new ArrayList<>();
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        VideoViewHolder holder = new VideoViewHolder(view);
        view.setTag(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {
        holder.update(position);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void refresh(List<VideoItemData> list) {
        this.mList.clear();
        this.mList.addAll(list);
        notifyDataSetChanged();
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder {

        private FrameLayout videoLayout;
        private int position;
        private RelativeLayout showView;
        private TextView title;
        private TextView from;

        public VideoViewHolder(View itemView) {
            super(itemView);
            videoLayout = (FrameLayout) itemView.findViewById(R.id.item_layout_video);
            showView = (RelativeLayout) itemView.findViewById(R.id.showview);
            title = (TextView) itemView.findViewById(R.id.title);
            from = (TextView) itemView.findViewById(R.id.from);
        }

        public void update(final int position) {
            this.position = position;
            title.setText(mList.get(position).getTitle());
            from.setText(mList.get(position).getVideosource());
            showView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showView.setVisibility(View.GONE);
                    if (click != null) {
                        click.onclick(position);
                    }
                }
            });
        }
    }

    private onClick click;

    public void setClick(onClick click) {
        this.click = click;
    }

    public interface onClick {
        void onclick(int position);
    }
}
