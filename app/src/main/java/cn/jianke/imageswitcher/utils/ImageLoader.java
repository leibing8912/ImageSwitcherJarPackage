package cn.jianke.imageswitcher.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import java.io.File;
import cn.jianke.imageswitcher.module.ThreadManager;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class ImageLoader {
    private static ImageLoader instance = null;
    private ImageLoader(){
    }

    public static ImageLoader getInstance(){
        if (instance == null)
            instance = new ImageLoader();
        return instance;
    }

    public void load(Context context, ImageView imageView, String remoteUrl){
        load(context, imageView, remoteUrl, null, null,false);
    }

    public void load(Context context, ImageView imageView, File localFile){
        load(context, imageView, localFile, null, null,false);
    }

    public void load(Context context, ImageView imageView, File localFile, boolean isCropCircle){
        load(context, imageView, localFile, null, null,isCropCircle);
    }

    public void load(Context context, ImageView imageView, File localFile, Drawable defaultImage){
        load(context, imageView, localFile, defaultImage, null, false);
    }

    public void load(Context context, ImageView imageView, String remoteUrl, boolean isCropCircle){
        load(context, imageView, remoteUrl, null, null,isCropCircle);
    }

    public void load(Context context, ImageView imageView, String url, Drawable defaultImage){
        load(context, imageView, url, defaultImage, null, false);
    }

    public void load(Context context, ImageView imageView, String url, Drawable defaultImage, Drawable errorImage ,boolean isCropCircle){
                DrawableTypeRequest request = Glide.with(context).load(url);
                request.centerCrop();
                if (isCropCircle)
                    request.bitmapTransform(new CropCircleTransformation(context));
                request.thumbnail(0.1f)
                .placeholder(defaultImage)
                .crossFade()
                .priority(Priority.NORMAL)
                .fallback(null)
                .error(errorImage)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model,
                                               Target<GlideDrawable> target,
                                               boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource,
                                                   String model,
                                                   Target<GlideDrawable> target,
                                                   boolean isFromMemoryCache,
                                                   boolean isFirstResource) {
                        return false;
                    }
                })
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(imageView);
    }

    public void load(Context context, ImageView imageView, File localPath, Drawable defaultImage, Drawable errorImage , boolean isCropCircle){
        DrawableTypeRequest request = Glide.with(context).load(localPath);
        request.centerCrop();
        if (isCropCircle)
            request.bitmapTransform(new CropCircleTransformation(context));
        request.thumbnail(0.1f)
                .placeholder(defaultImage)
                .crossFade()
                .priority(Priority.NORMAL)
                .fallback(null)
                .error(errorImage)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(imageView);
    }

    public void clearMemory(Context context){
        Glide.get(context).clearMemory();
    }

    public void clearDiskCache(final Context context){
        ThreadManager.getInstance().getNewCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                Glide.get(context).clearDiskCache();
            }
        });
    }

    public void clearViewCache(View view){
        Glide.clear(view);
    }

    public static String getSDSource(String fullPath){
        return "file://"+ fullPath;
    }

    public static String getAssetsSource(String fileName){
        return "file:///android_asset/"+fileName;
    }

    public static String getRawSource(Context context,int rawRid){
        return "android.resource://"+context.getPackageName()+"/raw/"+rawRid;
    }

    public static String getDrawableSource(Context context,int drawRid){
        return "android.resource://"+context.getPackageName()+"/drawable/"+drawRid;
    }
}
