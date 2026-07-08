package com.example.saive.utils;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.saive.R;

public class ImageUtils {

    /**
     * Set image from resource ID using Glide for better memory management.
     */
    public static void setSafeImage(ImageView imageView, int resId) {
        if (imageView == null) return;
        
        Context context = imageView.getContext();
        int safeResId = (resId != 0) ? resId : R.drawable.model1;

        Glide.with(context)
                .load(safeResId)
                .placeholder(R.drawable.model1)
                .error(R.drawable.model1)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }

    /**
     * Set image from Uri using Glide.
     */
    public static void setSafeImage(ImageView imageView, Uri uri) {
        if (imageView == null) return;
        
        Context context = imageView.getContext();
        Object loadTarget = (uri != null) ? uri : R.drawable.model1;

        Glide.with(context)
                .load(loadTarget)
                .placeholder(R.drawable.model1)
                .error(R.drawable.model1)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }

    /**
     * Set image from URL or fallback to Resource ID.
     */
    public static void setSafeImage(ImageView imageView, String imageUrl, int fallbackResId) {
        if (imageView == null) return;

        Context context = imageView.getContext();
        int safeFallback = (fallbackResId != 0) ? fallbackResId : R.drawable.model1;

        if (imageUrl == null || imageUrl.isEmpty() || imageUrl.equalsIgnoreCase("null")) {
            setSafeImage(imageView, safeFallback);
            return;
        }

        // Handle res:// scheme
        if (imageUrl.startsWith("res://")) {
            try {
                String resName = imageUrl.replace("res://", "");
                int resId = 0;
                if (resName.contains("mipmap/")) {
                    resId = context.getResources().getIdentifier(
                            resName.replace("mipmap/", ""), "mipmap", context.getPackageName());
                } else if (resName.contains("drawable/")) {
                    resId = context.getResources().getIdentifier(
                            resName.replace("drawable/", ""), "drawable", context.getPackageName());
                }
                if (resId != 0) {
                    setSafeImage(imageView, resId);
                    return;
                }
            } catch (Exception ignored) {}
        }

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.model1)
                .error(safeFallback)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerInside()
                .into(imageView);
    }
}
