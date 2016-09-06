package cn.jianke.imageswitcher.module;

import android.content.Context;
import android.widget.ImageView;
import java.io.Serializable;

public interface InterfaceImageLoader extends Serializable {
    void displayImage(Context context, String path, ImageView imageView);
}