package com.yalin.googleio.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.BitmapTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelCache;
import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestListener;
import com.yalin.googleio.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 作者：YaLin
 * 日期：2016/4/18.
 */
public class ImageLoader {
    private static final ModelCache<String, GlideUrl> urlCache = new ModelCache<>(150);
    private final BitmapTypeRequest<String> mGlideModelRequest;
    private final CenterCrop mCenterCrop;

    private int mPlaceHolderResId = -1;

    public ImageLoader(Context context) {
        VariableWidthImageLoader imageLoader = new VariableWidthImageLoader(context);
        mGlideModelRequest = Glide.with(context).using(imageLoader).from(String.class).asBitmap();
        mCenterCrop = new CenterCrop(Glide.get(context).getBitmapPool());
    }

    public ImageLoader(Context context, int placeHolderResId) {
        this(context);
        mPlaceHolderResId = placeHolderResId;
    }

    public void loadImage(String url, ImageView imageView) {
        loadImage(url, imageView, false);
    }

    public void loadImage(String url, ImageView imageView, boolean crop) {
        loadImage(url, imageView, null, null, crop);
    }

    public void loadImage(String url, ImageView imageView, RequestListener<String, Bitmap> requestListener) {
        loadImage(url, imageView, requestListener, null, false);
    }

    public void loadImage(String url, ImageView imageView, RequestListener<String, Bitmap> requestListener,
                          Drawable placeHolderOverride) {
        loadImage(url, imageView, requestListener, placeHolderOverride, false);
    }

    public void loadImage(String url, ImageView imageView, RequestListener<String, Bitmap> requestListener
            , Drawable placeHolderOverride, boolean crop) {
        BitmapRequestBuilder request = beginImageLoad(url, requestListener, crop)
                .animate(R.anim.image_fade_in);
        if (placeHolderOverride != null) {
            request.placeholder(placeHolderOverride);
        } else if (mPlaceHolderResId != -1) {
            request.placeholder(mPlaceHolderResId);
        }
        request.into(imageView);
    }

    public BitmapRequestBuilder beginImageLoad(String url,
                                               RequestListener<String, Bitmap> requestListener, boolean crop) {
        if (crop) {
            return mGlideModelRequest.load(url)
                    .listener(requestListener)
                    .transform(mCenterCrop);
        } else {
            return mGlideModelRequest.load(url)
                    .listener(requestListener);
        }
    }

    private static class VariableWidthImageLoader extends BaseGlideUrlLoader<String> {
        private static final Pattern PATTERN = Pattern.compile("__w-((?:-?\\d+)+)__");

        public VariableWidthImageLoader(Context context) {
            super(context, urlCache);
        }

        @Override
        protected String getUrl(String model, int width, int height) {
            Matcher m = PATTERN.matcher(model);
            int bestBucket = 0;
            if (m.find()) {
                String[] found = m.group(1).split("-");
                for (String bucketStr : found) {
                    bestBucket = Integer.parseInt(bucketStr);
                    if (bestBucket >= width) {
                        break;
                    }
                }
                if (bestBucket > 0) {
                    model = m.replaceFirst("w" + bestBucket);
                }
            }
            return model;
        }
    }
}
