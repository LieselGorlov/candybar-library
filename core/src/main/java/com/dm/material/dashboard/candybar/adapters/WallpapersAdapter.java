package com.dm.material.dashboard.candybar.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.activities.CandyBarWallpaperActivity;
import com.dm.material.dashboard.candybar.fragments.dialog.WallpaperOptionsFragment;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;
import com.dm.material.dashboard.candybar.helpers.DrawableHelper;
import com.dm.material.dashboard.candybar.helpers.WallpaperHelper;
import com.dm.material.dashboard.candybar.items.Wallpaper;
import com.dm.material.dashboard.candybar.utils.ImageConfig;
import com.kogitune.activitytransition.ActivityTransitionLauncher;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.List;

/*
 * CandyBar - Material Dashboard
 *
 * Copyright (c) 2014-2016 Dani Mahardhika
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class WallpapersAdapter extends RecyclerView.Adapter<WallpapersAdapter.ViewHolder> {

    private final Context mContext;
    private final List<Wallpaper> mWallpapers;
    private final DisplayImageOptions.Builder mOptions;

    public static boolean sIsClickable = true;
    private final boolean mIsAutoGeneratedColor;
    private final boolean mIsShowName;

    public WallpapersAdapter(@NonNull Context context, @NonNull List<Wallpaper> wallpapers) {
        sIsClickable = true;
        mContext = context;
        mWallpapers = wallpapers;
        mIsAutoGeneratedColor = mContext.getResources().getBoolean(
                R.bool.card_wallpaper_auto_generated_color);
        mIsShowName = mContext.getResources().getBoolean(R.bool.wallpaper_show_name_author);

        Drawable loading = DrawableHelper.getDefaultImage(
                mContext, R.drawable.ic_default_image_loading);
        Drawable failed = DrawableHelper.getDefaultImage(
                mContext, R.drawable.ic_default_image_failed);

        mOptions = ImageConfig.getRawDefaultImageOptions();
        mOptions.resetViewBeforeLoading(true);
        mOptions.cacheInMemory(true);
        mOptions.cacheOnDisk(true);
        mOptions.showImageForEmptyUri(failed);
        mOptions.showImageOnFail(failed);
        mOptions.showImageOnLoading(loading);
        mOptions.displayer(new FadeInBitmapDisplayer(700));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.fragment_wallpapers_item_grid_alt, parent, false);
        if (mIsShowName) {
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_wallpapers_item_grid, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mIsShowName) {
            holder.name.setText(mWallpapers.get(position).getName());
            holder.author.setText(mWallpapers.get(position).getAuthor());
        }

        String url = WallpaperHelper.getThumbnailUrl(mContext,
                mWallpapers.get(position).getURL(),
                mWallpapers.get(position).getThumbUrl());
        //LogUtil.d("loading wallpaper thumbnail: " +url);

        ImageLoader.getInstance().displayImage(url, new ImageViewAware(holder.image),
                mOptions.build(), ImageConfig.getThumbnailSize(mContext), new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        super.onLoadingStarted(imageUri, view);
                        if (mIsAutoGeneratedColor && mIsShowName) {
                            int vibrant = ColorHelper.getAttributeColor(
                                    mContext, R.attr.card_background);
                            holder.card.setCardBackgroundColor(vibrant);
                            int primary = ColorHelper.getAttributeColor(
                                    mContext, android.R.attr.textColorPrimary);
                            holder.name.setTextColor(primary);
                            holder.author.setTextColor(primary);
                        }
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        if (mIsAutoGeneratedColor && mIsShowName) {
                            if (loadedImage != null) {
                                Palette.from(loadedImage).generate(palette -> {
                                    int defaultColor = ColorHelper.getAttributeColor(
                                            mContext, R.attr.card_background);
                                    int color = palette.getVibrantColor(defaultColor);
                                    if (color == defaultColor)
                                        color = palette.getMutedColor(defaultColor);
                                    holder.card.setCardBackgroundColor(color);
                                    int text = ColorHelper.getTitleTextColor(color);
                                    holder.name.setTextColor(text);
                                    holder.author.setTextColor(text);
                                });
                            }
                        }
                    }
                }, null);
    }

    @Override
    public int getItemCount() {
        return mWallpapers.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {

        private CardView card;
        private LinearLayout container;
        private final ImageView image;
        private TextView name;
        private TextView author;

        ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);

            if (mIsShowName) {
                card = (CardView) itemView.findViewById(R.id.card);
                container = (LinearLayout) itemView.findViewById(R.id.container);
                name = (TextView) itemView.findViewById(R.id.name);
                author = (TextView) itemView.findViewById(R.id.author);
                container.setOnClickListener(this);
                container.setOnLongClickListener(this);
            } else {
                image.setOnClickListener(this);
                image.setOnLongClickListener(this);
            }
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            int position = getAdapterPosition();
            if (id == R.id.container || id == R.id.image) {
                if (sIsClickable) {
                    sIsClickable = false;
                    try {
                        final Intent intent = new Intent(mContext, CandyBarWallpaperActivity.class);
                        intent.putExtra("url", mWallpapers.get(position).getURL());
                        intent.putExtra("author", mWallpapers.get(position).getAuthor());
                        intent.putExtra("name", mWallpapers.get(position).getName());

                        ActivityTransitionLauncher.with((AppCompatActivity) mContext)
                                .from(image, "image")
                                .image(((BitmapDrawable) image.getDrawable()).getBitmap())
                                .launch(intent);
                    } catch (Exception e) {
                        sIsClickable = true;
                    }
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            int id = view.getId();
            int position = getAdapterPosition();
            if (id == R.id.container || id == R.id.image) {
                if (position < 0 || position > mWallpapers.size()) return true;
                WallpaperOptionsFragment.showWallpaperOptionsDialog(
                        ((AppCompatActivity) mContext).getSupportFragmentManager(),
                        mWallpapers.get(position).getURL(),
                        mWallpapers.get(position).getName());
                return true;
            }
            return false;
        }
    }
}
