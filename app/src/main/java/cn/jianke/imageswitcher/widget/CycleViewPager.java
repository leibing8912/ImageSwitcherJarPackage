package cn.jianke.imageswitcher.widget;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;
import java.io.File;
import java.util.List;
import cn.jianke.imageswitcher.R;
import cn.jianke.imageswitcher.utils.ImageLoader;

public class CycleViewPager extends FrameLayout implements GestureDetector.OnGestureListener{
    public final static int AUTO_ROTATION_TIME = 6000;
    private ViewFlipper mCycleViewVf;
    private LinearLayout mIndicatorsLy;
    private ImageView[] mIndicators;
    private GestureDetector detector;
    private int mIndicatorSelected = R.mipmap.btn_appraise_selected;
    private int mIndicatorUnselected = R.mipmap.btn_appraise_normal;
    private int currentPosition = 0;
    private int pictureSize = 0;
    private Animation leftInAnimation;
    private Animation leftOutAnimation;
    private Animation rightInAnimation;
    private Animation rightOutAnimation;
    private Handler mHandler;
    private int autoRotationTime;
    private boolean isStopAuto = false;
    private boolean isAuto = false;
    private Runnable mAutoRotationRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isStopAuto) {
                onFlingRight();
                mHandler.postDelayed(mAutoRotationRunnable, autoRotationTime);
            }
        }
    };

    public CycleViewPager(Context context) {
        super(context);
        initView(context);
    }

    public CycleViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public CycleViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_cycle_view, null);
        mCycleViewVf = (ViewFlipper) view.findViewById(R.id.vf_cycle_view);
        mIndicatorsLy = (LinearLayout) view.findViewById(R.id.ly_indicators);
        detector = new GestureDetector(this);
        leftInAnimation = AnimationUtils.loadAnimation(context, R.anim.left_in);
        leftOutAnimation = AnimationUtils.loadAnimation(context, R.anim.left_out);
        rightInAnimation = AnimationUtils.loadAnimation(context, R.anim.right_in);
        rightOutAnimation = AnimationUtils.loadAnimation(context, R.anim.right_out);
        this.addView(view);
    }

    public void loadRemoteImage(List<String> remoteUrlList, Context context){
        if (mCycleViewVf == null)
            return;
        pictureSize = remoteUrlList.size();
        initIndicators(pictureSize,context);
        if (pictureSize >=1)
            setIndicator(0);
        for (int i=0;i<pictureSize;i++){
            ImageView imageView = new ImageView(context);
            ImageLoader.getInstance().load(context, imageView, remoteUrlList.get(i));
            mCycleViewVf.addView(imageView);
        }
    }

    public void loadLocalImage(List<File> localUrlList, Context context){
        if (mCycleViewVf == null)
            return;
        pictureSize = localUrlList.size();
        initIndicators(pictureSize,context);
        if (pictureSize >=1)
            setIndicator(0);
        for (int i=0;i<pictureSize;i++){
            ImageView imageView = new ImageView(context);
            ImageLoader.getInstance().load(context, imageView, localUrlList.get(i));
            mCycleViewVf.addView(imageView);
        }
    }

    private void initIndicators(int pictureSize, Context context) {
        mIndicators = new ImageView[pictureSize];
        mIndicatorsLy.removeAllViews();
        for (int i = 0; i < mIndicators.length; i++) {
            mIndicators[i] = new ImageView(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(10, 0, 10, 0);
            mIndicators[i].setLayoutParams(lp);
            mIndicatorsLy.addView(mIndicators[i]);
        }
    }

    private void setIndicator(int selectedPosition) {
        if (mIndicators == null || mIndicators.length == 0)
            return;
        try {
            for (int i = 0; i < mIndicators.length; i++) {
                mIndicators[i]
                        .setBackgroundResource(mIndicatorUnselected);
            }
            if (mIndicators.length > selectedPosition)
                mIndicators[selectedPosition]
                        .setBackgroundResource(mIndicatorSelected);
        } catch (Exception e) {
        }
    }

    public void setIndicatorSelected(int mIndicatorSelected){
        this.mIndicatorSelected = mIndicatorSelected;
    }

    public void setIndicatorUnselected(int mIndicatorUnselected){
        this.mIndicatorUnselected = mIndicatorUnselected;
    }

    public void startAutoRotation(int time){
        isStopAuto = false;
        isAuto = true;
        if (time == 0)
            autoRotationTime = AUTO_ROTATION_TIME;
        else
            autoRotationTime = time;
        if (mHandler == null)
            mHandler = new Handler();
        else {
            if (mAutoRotationRunnable != null)
                mHandler.removeCallbacks(mAutoRotationRunnable);
        }
        mHandler.postDelayed(mAutoRotationRunnable, time);
    }

    public void stopAutoRotation(){
        if (mHandler != null && mAutoRotationRunnable != null){
            mHandler.removeCallbacks(mAutoRotationRunnable);
            mHandler = null;
            isStopAuto = true;
            isAuto = false;
        }
    }

    private void onFlingRight(){
        if (mCycleViewVf != null) {
            mCycleViewVf.setInAnimation(leftInAnimation);
            mCycleViewVf.setOutAnimation(leftOutAnimation);
            mCycleViewVf.showNext();
            currentPosition++;
            if (currentPosition >= pictureSize)
                currentPosition = currentPosition - pictureSize;
            setIndicator(currentPosition);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.detector.onTouchEvent(event);
    }

    private void onFlingLeft(){
        if (mCycleViewVf != null) {
            mCycleViewVf.setInAnimation(rightInAnimation);
            mCycleViewVf.setOutAnimation(rightOutAnimation);
            mCycleViewVf.showPrevious();
            currentPosition--;
            if (currentPosition < 0)
                currentPosition = currentPosition + pictureSize;
            setIndicator(currentPosition);
        }
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
        if (!isAuto){
            startAutoRotation(AUTO_ROTATION_TIME);
        }else {
            stopAutoRotation();
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float v, float v1) {
        if(e1.getX()-e2.getX()>120){
            onFlingRight();
            return true;
        }else if(e1.getX()-e2.getY()<-120){
            onFlingLeft();
            return true;
        }
        return false;
    }
}
