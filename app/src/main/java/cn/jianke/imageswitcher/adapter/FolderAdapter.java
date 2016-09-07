package cn.jianke.imageswitcher.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import cn.jianke.imageswitcher.R;
import cn.jianke.imageswitcher.bean.Folder;
import cn.jianke.imageswitcher.module.ImageConfig;

public class FolderAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater mLayoutInflater;
    private List<Folder> folderList = new ArrayList<>();
    private int lastSelected = 0;
    private ImageConfig imageConfig;

    public FolderAdapter(Context context, ImageConfig imageConfig) {
        mLayoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.imageConfig = imageConfig;
    }

    public void setData(List<Folder> folders) {
        folderList.clear();
        if (folders != null && folders.size() > 0) {
            folderList.addAll(folders);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return folderList.size() + 1;
    }

    @Override
    public Folder getItem(int position) {
        if (position == 0)
            return null;
        return folderList.get(position - 1);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.imageselector_item_folder, parent, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (holder != null) {
            if (position == 0) {
                holder.mFolderNameText.setText(R.string.all_folder);
                holder.mImageNumTextTv.setText("" + getTotalImageSize() + (context.getResources().getText(R.string.sheet)));

                if (folderList.size() > 0) {
                    Folder folder = folderList.get(0);

                    imageConfig.getInterfaceImageLoader().displayImage(context,
                            folder.cover.path, holder.mFolderImageIv);

                }
            } else {

                Folder folder = getItem(position);
                holder.mFolderNameText.setText(folder.name);
                holder.mImageNumTextTv.setText("" + folder.images.size() + (context.getResources().getText(R.string.sheet)));

                imageConfig.getInterfaceImageLoader().displayImage(context, folder.cover.path, holder.mFolderImageIv);

            }

            if (lastSelected == position) {
                holder.mIndicatorIv.setVisibility(View.VISIBLE);
            } else {
                holder.mIndicatorIv.setVisibility(View.INVISIBLE);
            }
        }

        return convertView;
    }

    class ViewHolder {
        ImageView mFolderImageIv;
        TextView mFolderNameText;
        TextView mImageNumTextTv;
        ImageView mIndicatorIv;

        ViewHolder(View itemView) {
            mFolderImageIv = (ImageView) itemView.findViewById(R.id.iv_folder_image);
            mFolderNameText = (TextView) itemView.findViewById(R.id.tv_folder_name_text);
            mImageNumTextTv = (TextView) itemView.findViewById(R.id.tv_image_num_text);
            mIndicatorIv = (ImageView) itemView.findViewById(R.id.iv_indicator);
            itemView.setTag(this);
        }
    }

    public int getSelectIndex() {
        return lastSelected;
    }

    private int getTotalImageSize() {
        int result = 0;
        if (folderList != null && folderList.size() > 0) {
            for (Folder folder : folderList) {
                result += folder.images.size();
            }
        }
        return result;
    }

    public void setSelectIndex(int position) {
        if (lastSelected == position)
            return;
        lastSelected = position;
        notifyDataSetChanged();
    }
}