package cn.jianke.imageswitcher.module.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.ListPopupWindow;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.jianke.imageswitcher.R;
import cn.jianke.imageswitcher.adapter.FolderAdapter;
import cn.jianke.imageswitcher.adapter.ImageAdapter;
import cn.jianke.imageswitcher.bean.Folder;
import cn.jianke.imageswitcher.bean.Image;
import cn.jianke.imageswitcher.module.ImageConfig;
import cn.jianke.imageswitcher.module.ImageSelector;
import cn.jianke.imageswitcher.utils.FileUtils;
import cn.jianke.imageswitcher.utils.TimeUtils;


public class ImageSelectorFragment extends Fragment {
    private static final int LOADER_ALL = 0;
    private static final int LOADER_CATEGORY = 1;
    private static final int REQUEST_CAMERA = 100;
    private ArrayList<String> resultList = new ArrayList<>();
    private List<Folder> folderList = new ArrayList<>();
    private List<Image> imageList = new ArrayList<>();
    private Callback callback;
    private ImageAdapter imageAdapter;
    private FolderAdapter folderAdapter;
    private ListPopupWindow folderPopupWindow;
    private TextView mTimeTextTv;
    private TextView mCategoryTv;
    private View mPopupAnchorVw;
    private GridView mImageGv;
    private int gridWidth, gridHeight;
    private boolean hasFolderGened = false;
    private File tempFile;
    private ImageConfig imageConfig;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callback = (Callback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("The Activity must implement " +
                    "ImageSelectorFragment.Callback interface...");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.imageselector_main_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getSupportLoaderManager().initLoader(LOADER_ALL, null, mLoaderCallback);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTimeTextTv = (TextView) view.findViewById(R.id.tv_time_text);
        mCategoryTv = (TextView) view.findViewById(R.id.btn_category);
        mImageGv = (GridView) view.findViewById(R.id.grid_image);
        mPopupAnchorVw = view.findViewById(R.id.rly_footer);
        mTimeTextTv.setVisibility(View.GONE);

        init();
    }

    private void init() {
        imageConfig = ImageSelector.getImageConfig();
        folderAdapter = new FolderAdapter(getActivity(), imageConfig);
        imageAdapter = new ImageAdapter(getActivity(), imageList, imageConfig);
        imageAdapter.setShowCamera(imageConfig.isShowCamera());
        imageAdapter.setShowSelectIndicator(imageConfig.isMutiSelect());
        mImageGv.setAdapter(imageAdapter);
        resultList = imageConfig.getPathList();

        mCategoryTv.setText(R.string.all_folder);
        mCategoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (folderPopupWindow == null) {
                    createPopupFolderList(gridWidth, gridHeight);
                }

                if (folderPopupWindow.isShowing()) {
                    folderPopupWindow.dismiss();
                } else {
                    folderPopupWindow.show();
                    int index = folderAdapter.getSelectIndex();
                    index = index == 0 ? index : index - 1;
                    folderPopupWindow.getListView().setSelection(index);
                }
            }
        });

        mImageGv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    mTimeTextTv.setVisibility(View.GONE);
                } else if (scrollState == SCROLL_STATE_FLING) {
                    mTimeTextTv.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                if (mTimeTextTv.getVisibility() == View.VISIBLE) {
                    int index = firstVisibleItem + 1 == view.getAdapter().getCount() ?
                            view.getAdapter().getCount() - 1 : firstVisibleItem + 1;
                    Image image = (Image) view.getAdapter().getItem(index);
                    if (image != null) {
                        mTimeTextTv.setText(TimeUtils.formatPhotoDate(image.path));
                    }
                }
            }
        });

        mImageGv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            public void onGlobalLayout() {

                final int width = mImageGv.getWidth();
                final int height = mImageGv.getHeight();

                gridWidth = width;
                gridHeight = height;

                final int desireSize = getResources().getDimensionPixelOffset(R.dimen.image_size);
                final int numCount = width / desireSize;
                final int columnSpace = getResources().getDimensionPixelOffset(R.dimen.space_size);
                int columnWidth = (width - columnSpace * (numCount - 1)) / numCount;
                imageAdapter.setItemSize(columnWidth);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mImageGv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mImageGv.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

        mImageGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (imageAdapter.isShowCamera()) {
                    if (i == 0) {
                        showCameraAction();
                    } else {
                        Image image = (Image) adapterView.getAdapter().getItem(i);
                        selectImageFromGrid(image, imageConfig.isMutiSelect());
                    }
                } else {
                    Image image = (Image) adapterView.getAdapter().getItem(i);
                    selectImageFromGrid(image, imageConfig.isMutiSelect());
                }
            }
        });
    }

    private void createPopupFolderList(int width, int height) {
        folderPopupWindow = new ListPopupWindow(getActivity());
        folderPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        folderPopupWindow.setAdapter(folderAdapter);
        folderPopupWindow.setContentWidth(width);
        folderPopupWindow.setWidth(width);
        folderPopupWindow.setHeight(height * 5 / 8);
        folderPopupWindow.setAnchorView(mPopupAnchorVw);
        folderPopupWindow.setModal(true);
        folderPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                folderAdapter.setSelectIndex(i);
                final int index = i;
                final AdapterView v = adapterView;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        folderPopupWindow.dismiss();
                        if (index == 0) {
                            getActivity().getSupportLoaderManager().restartLoader(LOADER_ALL, null, mLoaderCallback);
                            mCategoryTv.setText(R.string.all_folder);
                            if (imageConfig.isShowCamera()) {
                                imageAdapter.setShowCamera(true);
                            } else {
                                imageAdapter.setShowCamera(false);
                            }
                        } else {
                            Folder folder = (Folder) v.getAdapter().getItem(index);
                            if (null != folder) {
                                imageList.clear();
                                imageList.addAll(folder.images);
                                imageAdapter.notifyDataSetChanged();
                                mCategoryTv.setText(folder.name);
                                if (resultList != null && resultList.size() > 0) {
                                    imageAdapter.setDefaultSelected(resultList);
                                }
                            }
                            imageAdapter.setShowCamera(false);
                        }
                        mImageGv.smoothScrollToPosition(0);
                    }
                }, 100);

            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (folderPopupWindow != null) {
            if (folderPopupWindow.isShowing()) {
                folderPopupWindow.dismiss();
            }
        }
        mImageGv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            public void onGlobalLayout() {

                final int height = mImageGv.getHeight();
                final int desireSize = getResources().getDimensionPixelOffset(R.dimen.image_size);
                final int numCount = mImageGv.getWidth() / desireSize;
                final int columnSpace = getResources().getDimensionPixelOffset(R.dimen.space_size);
                int columnWidth = (mImageGv.getWidth() - columnSpace * (numCount - 1)) / numCount;
                imageAdapter.setItemSize(columnWidth);
                if (folderPopupWindow != null) {
                    folderPopupWindow.setHeight(height * 5 / 8);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mImageGv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mImageGv.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
        super.onConfigurationChanged(newConfig);
    }

    private void showCameraAction() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            tempFile = FileUtils.createTmpFile(getActivity(), imageConfig.getFilePath());
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
            startActivityForResult(cameraIntent, REQUEST_CAMERA);
        } else {
            Toast.makeText(getActivity(), R.string.msg_no_camera, Toast.LENGTH_SHORT).show();
        }
    }

    private void selectImageFromGrid(Image image, boolean isMulti) {
        if (image != null) {
            if (isMulti) {
                if (resultList.contains(image.path)) {
                    resultList.remove(image.path);
                    if (callback != null) {
                        callback.onImageUnselected(image.path);
                    }
                } else {
                    if (imageConfig.getMaxSize() == resultList.size()) {
                        Toast.makeText(getActivity(), R.string.msg_amount_limit,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    resultList.add(image.path);
                    if (callback != null) {
                        callback.onImageSelected(image.path);
                    }
                }
                imageAdapter.select(image);
            } else {
                if (callback != null) {
                    callback.onSingleImageSelected(image.path);
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                if (tempFile != null) {
                    if (callback != null) {
                        callback.onCameraShot(tempFile);
                    }
                }
            } else {
                if (tempFile != null && tempFile.exists()) {
                    tempFile.delete();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        private final String[] IMAGE_PROJECTION = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media._ID};

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (id == LOADER_ALL) {
                CursorLoader cursorLoader =
                        new CursorLoader(getActivity(),
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                                null, null, IMAGE_PROJECTION[2] + " DESC");
                return cursorLoader;
            } else if (id == LOADER_CATEGORY) {
                CursorLoader cursorLoader = new CursorLoader(getActivity(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                        IMAGE_PROJECTION[0] + " like '%" + args.getString("path") + "%'",
                        null, IMAGE_PROJECTION[2] + " DESC");
                return cursorLoader;
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {
                int count = data.getCount();
                if (count > 0) {
                    List<Image> tempImageList = new ArrayList<>();
                    data.moveToFirst();
                    do {
                        String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                        String name = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                        long dateTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
                        Image image = new Image(path, name, dateTime);
                        tempImageList.add(image);
                        if (!hasFolderGened) {
                            File imageFile = new File(path);
                            File folderFile = imageFile.getParentFile();
                            Folder folder = new Folder();
                            folder.name = folderFile.getName();
                            folder.path = folderFile.getAbsolutePath();
                            folder.cover = image;
                            if (!folderList.contains(folder)) {
                                List<Image> imageList = new ArrayList<>();
                                imageList.add(image);
                                folder.images = imageList;
                                folderList.add(folder);
                            } else {
                                Folder f = folderList.get(folderList.indexOf(folder));
                                f.images.add(image);
                            }
                        }

                    } while (data.moveToNext());

                    imageList.clear();
                    imageList.addAll(tempImageList);
                    imageAdapter.notifyDataSetChanged();

                    if (resultList != null && resultList.size() > 0) {
                        imageAdapter.setDefaultSelected(resultList);
                    }

                    folderAdapter.setData(folderList);

                    hasFolderGened = true;

                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    public interface Callback {
        void onSingleImageSelected(String path);
        void onImageSelected(String path);
        void onImageUnselected(String path);
        void onCameraShot(File imageFile);
    }
}