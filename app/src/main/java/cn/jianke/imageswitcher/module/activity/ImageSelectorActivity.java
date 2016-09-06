package cn.jianke.imageswitcher.module.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;
import cn.jianke.imageswitcher.R;
import cn.jianke.imageswitcher.module.ImageConfig;
import cn.jianke.imageswitcher.module.ImageSelector;
import cn.jianke.imageswitcher.utils.Utils;

public class ImageSelectorActivity extends FragmentActivity implements ImageSelectorFragment.Callback ,View.OnClickListener{
    public static final String EXTRA_RESULT = "select_result";
    private ArrayList<String> pathList = new ArrayList<>();
    private ImageConfig imageConfig;
    private TextView mTitleTextTv;
    private TextView mSubmitTv;
    private RelativeLayout mImageSelectorTitleBarRly;
    private String cropImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imageselector_activity);
        imageConfig = ImageSelector.getImageConfig();
        Utils.hideTitleBar(this, R.id.imageselector_activity_layout, imageConfig.getSteepToolBarColor());

        getSupportFragmentManager().beginTransaction()
                .add(R.id.image_grid, Fragment.instantiate(this, ImageSelectorFragment.class.getName(), null))
                .commit();
        mSubmitTv = (TextView) super.findViewById(R.id.tv_submit);
        mTitleTextTv = (TextView) super.findViewById(R.id.tv_title_text);
        mImageSelectorTitleBarRly = (RelativeLayout) super.findViewById(R.id.rly_imageselector_title_bar);
        init();
        findViewById(R.id.ly_back).setOnClickListener(this);
        findViewById(R.id.tv_submit).setOnClickListener(this);
    }

    private void init() {
        mSubmitTv.setTextColor(imageConfig.getTitleSubmitTextColor());
        mTitleTextTv.setTextColor(imageConfig.getTitleTextColor());
        mImageSelectorTitleBarRly.setBackgroundColor(imageConfig.getTitleBgColor());
        pathList = imageConfig.getPathList();

        if (pathList == null || pathList.size() <= 0) {
            mSubmitTv.setText(R.string.finish);
            mSubmitTv.setEnabled(false);
        } else {
            mSubmitTv.setText((getResources().getText(R.string.finish)) +
                    "(" + pathList.size() + "/" + imageConfig.getMaxSize() + ")");
            mSubmitTv.setEnabled(true);
        }
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.ly_back) {
            setResult(RESULT_CANCELED);
            finish();

        } else if (i == R.id.tv_submit) {
            if (pathList != null && pathList.size() > 0) {
                Intent data = new Intent();
                data.putStringArrayListExtra(EXTRA_RESULT, pathList);
                setResult(RESULT_OK, data);
                finish();
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ImageSelector.IMAGE_CROP_CODE && resultCode == RESULT_OK) {
            Intent intent = new Intent();
            pathList.add(cropImagePath);
            intent.putStringArrayListExtra(EXTRA_RESULT, pathList);
            setResult(RESULT_OK, intent);
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void crop(String imagePath, int aspectX, int aspectY, int outputX, int outputY) {
        File file;
        if (Utils.existSDCard()) {
            file = new File(Environment.getExternalStorageDirectory() +
                    imageConfig.getFilePath(), Utils.getImageName());
        } else {
            file = new File(getCacheDir(), Utils.getImageName());
        }
        cropImagePath = file.getAbsolutePath();
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(Uri.fromFile(new File(imagePath)), "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", aspectX);
        intent.putExtra("aspectY", aspectY);
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        startActivityForResult(intent, ImageSelector.IMAGE_CROP_CODE);
    }

    @Override
    public void onSingleImageSelected(String path) {
        if (imageConfig.isCrop()) {
            crop(path, imageConfig.getAspectX(), imageConfig.getAspectY(),
                    imageConfig.getOutputX(), imageConfig.getOutputY());
        } else {
            Intent data = new Intent();
            pathList.add(path);
            data.putStringArrayListExtra(EXTRA_RESULT, pathList);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    public void onImageSelected(String path) {
        if (!pathList.contains(path)) {
            pathList.add(path);
        }
        if (pathList.size() > 0) {
            mSubmitTv.setText((getResources().getText(R.string.finish)) +
                    "(" + pathList.size() + "/" + imageConfig.getMaxSize() + ")");
            if (!mSubmitTv.isEnabled()) {
                mSubmitTv.setEnabled(true);
            }
        }
    }

    @Override
    public void onImageUnselected(String path) {
        if (pathList.contains(path)) {
            pathList.remove(path);
            mSubmitTv.setText((getResources().getText(R.string.finish)) +
                    "(" + pathList.size() + "/" + imageConfig.getMaxSize() + ")");
        } else {
            mSubmitTv.setText((getResources().getText(R.string.finish)) +
                    "(" + pathList.size() + "/" + imageConfig.getMaxSize() + ")");
        }
        if (pathList.size() == 0) {
            mSubmitTv.setText(R.string.finish);
            mSubmitTv.setEnabled(false);
        }
    }

    @Override
    public void onCameraShot(File imageFile) {
        if (imageFile != null) {
            Intent data = new Intent();
            pathList.add(imageFile.getAbsolutePath());
            data.putStringArrayListExtra(EXTRA_RESULT, pathList);
            setResult(RESULT_OK, data);
            finish();
        }
        if (imageFile != null) {
            if (imageConfig.isCrop()) {
                crop(imageFile.getAbsolutePath(), imageConfig.getAspectX(), imageConfig.getAspectY(),
                        imageConfig.getOutputX(), imageConfig.getOutputY());
            } else {
                Intent data = new Intent();
                pathList.add(imageFile.getAbsolutePath());
                data.putStringArrayListExtra(EXTRA_RESULT, pathList);
                setResult(RESULT_OK, data);
                finish();
            }
        }
    }
}