package cn.jianke.imageswitcher.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.List;
import cn.jianke.imageswitcher.R;
import cn.jianke.imageswitcher.bean.Image;
import cn.jianke.imageswitcher.module.ImageConfig;

public class ImageAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater mLayoutInflater;
    private List<Image> imageList;
    private static final int TYPE_CAMERA = 0;
    private static final int TYPE_NORMAL = 1;
    private boolean showCamera = true;
    private boolean showSelectIndicator = true;
    private List<Image> selectedImageList = new ArrayList<>();
    private int mItemSize;
    private GridView.LayoutParams mItemLayoutParams;
    private ImageConfig imageConfig;

    public ImageAdapter(Context context, List<Image> imageList, ImageConfig imageConfig) {
        mLayoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.imageList = imageList;
        this.imageConfig = imageConfig;
        mItemLayoutParams = new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT);
    }

    public void setDefaultSelected(ArrayList<String> resultList) {
        for (String filePath : resultList) {
            Image image = getImageByPath(filePath);
            if (image != null) {
                selectedImageList.add(image);
            }
        }
        if (selectedImageList.size() > 0) {
            notifyDataSetChanged();
        }
    }

    private Image getImageByPath(String filePath) {
        if (imageList != null && imageList.size() > 0) {
            for (Image image : imageList) {
                if (image.path.equalsIgnoreCase(filePath)) {
                    return image;
                }
            }
        }
        return null;
    }

    public void setItemSize(int columnWidth) {
        if (mItemSize == columnWidth) {
            return;
        }
        mItemSize = columnWidth;
        mItemLayoutParams = new GridView.LayoutParams(mItemSize, mItemSize);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return showCamera ? imageList.size() + 1 : imageList.size();
    }

    @Override
    public Image getItem(int position) {
        if (showCamera) {
            if (position == 0) {
                return null;
            }
            return imageList.get(position - 1);
        } else {
            return imageList.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        int type = getItemViewType(position);

        if (type == TYPE_CAMERA) {
            convertView = mLayoutInflater.inflate(R.layout.imageselector_item_camera, parent, false);
            convertView.setTag(null);
        } else if (type == TYPE_NORMAL) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.imageselector_item_image, parent, false);
                holder = new ViewHolder(convertView);
            } else {
                holder = (ViewHolder) convertView.getTag();
                if (holder == null) {
                    convertView = mLayoutInflater.inflate(R.layout.imageselector_item_image, parent, false);
                    holder = new ViewHolder(convertView);
                }
            }

            if (showSelectIndicator) {
                holder.mPhotoCheckIv.setVisibility(View.VISIBLE);
                if (selectedImageList.contains(getItem(position))) {
                    holder.mPhotoCheckIv.setImageResource(R.mipmap.imageselector_select_checked);
                    holder.mPhotoMaskVw.setVisibility(View.VISIBLE);
                } else {
                    holder.mPhotoCheckIv.setImageResource(R.mipmap.imageselector_select_uncheck);
                    holder.mPhotoMaskVw.setVisibility(View.GONE);
                }
            } else {
                holder.mPhotoCheckIv.setVisibility(View.GONE);
            }

            if (mItemSize > 0) {
                imageConfig.getInterfaceImageLoader().displayImage(context, getItem(position).path,
                        holder.mPhotoImageIv);
            }
        }

        GridView.LayoutParams layoutParams = (GridView.LayoutParams) convertView.getLayoutParams();
        if (layoutParams.height != mItemSize) {
            convertView.setLayoutParams(mItemLayoutParams);
        }

        return convertView;
    }

    class ViewHolder {
        private ImageView mPhotoImageIv;
        private View mPhotoMaskVw;
        private ImageView mPhotoCheckIv;

        ViewHolder(View itemView) {
            mPhotoImageIv = (ImageView) itemView.findViewById(R.id.iv_photo_image);
            mPhotoMaskVw = itemView.findViewById(R.id.vw_photo_mask);
            mPhotoCheckIv = (ImageView) itemView.findViewById(R.id.iv_photo_check);
            itemView.setTag(this);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (showCamera && position == 0) {
            return TYPE_CAMERA;
        }
        return TYPE_NORMAL;
    }

    public void setShowSelectIndicator(boolean showSelectIndicator) {
        this.showSelectIndicator = showSelectIndicator;
    }

    public void setShowCamera(boolean showCamera) {
        if (this.showCamera == showCamera)
            return;
        this.showCamera = showCamera;
        notifyDataSetChanged();
    }

    public void select(Image image) {
        if (selectedImageList.contains(image)) {
            selectedImageList.remove(image);
        } else {
            selectedImageList.add(image);
        }
        notifyDataSetChanged();
    }

    public boolean isShowCamera() {
        return showCamera;
    }
}