package com.android.tv.settings.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.os.SystemProperties;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialog;
import androidx.core.content.ContextCompat;

import com.android.tv.settings.R;

public class AIIndepSwitchDialog extends AppCompatDialog implements Animation.AnimationListener, CompoundButton.OnCheckedChangeListener {

    private AlgoType mType;
    private static final String Prop_ASR_ENABLE = "sys.audio.asr";
    private static final String Prop_KALAOK_ENABLE = "vendor.kalaok.enable";
    private static final String Prop_KALAOK_AGC = "vendor.kalaok.agc.enable";
    private static final String Prop_VOCAL_ENABLE = "system.audio.vocal.mode";
    private static final String Prop_OTHER_LEVEL = "persist.sys.seperate.other_level";
    private static final String Prop_VOCAL_LEVEL = "persist.sys.seperate.vocal_level";
    private static final String Prop_SCULPTOR_ENABLE = "persist.vendor.sculptor.mode";
    private static final String Prop_SCULPTOR_C2_ENABLE = "persist.vendor.sculptor.c2.mode";
    private static final String Prop_SD_ENABLE = "persist.vendor.rkpq.hwpq_aisd_enable";
    private static final String Prop_HWPQ_SD_ENABLE = "persist.vendor.rkhwpq.aisd_enable";
    private static final String Prop_MEMC_ENABLE = "persist.vendor.rkpq.memc.enable";
    private static final String Prop_MEMC_SRSTRENGH = "persist.vendor.rkpq.memc.strength";
    private static final String Prop_SR_ENABLE = "persist.vendor.rkpq.sr.enable";
    private static final String Prop_SR_SRSTRENGH = "persist.vendor.rkpq.sr.strength";
    private static final String Prop_SLIDER_ENABLE = "persist.vendor.sculptor.slider.enable";
    private static final String Prop_SLIDER_DYNAMIC = "persist.vendor.sculptor.slider.dynamic";

    private static final String Prop_WATERMARK_ENABLE = "persist.vendor.rkpq.memc.watermark";
    private TextView mAiDescrip;
    private TextView mAiValue;
    private ImageView mLeftArrow;
    private ImageView mRightArrow;
    private Switch mAiSwitch;
    private LinearLayout mLlTextTwoArrow;
    private String[] mValues;
    private int mValueIndex;
    private Context mContext;
    private Animation mLeftAnimation, mRightAnimation;
    private String[] mProValues;

    public AIIndepSwitchDialog(@NonNull Context context) {
        super(context);
    }

    public AIIndepSwitchDialog(@NonNull Context context, int theme, AlgoType type) {
        super(context, theme);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(View.inflate(context, R.layout.dialog_ai_indepswitch, null));
        getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        updateDialogSize(context);
        mContext = context;
        mType = type;
        initValues();
        initViews();
        initAnimations();
    }

    protected AIIndepSwitchDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);

        setCancelable(cancelable);
        setOnCancelListener(cancelListener);
    }

    private void updateDialogSize(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.y = 50;
        getWindow().setAttributes(params);
        getWindow().setGravity(Gravity.BOTTOM | Gravity.RIGHT);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(153, 0, 0, 0)));
    }

    public enum AlgoType {
        NONE,
        SR,
        SD,
        MEMC,
        SLIDER,
        VOCAL,
        KALAOK,
        ASR,
        LLM,
    }

    private void initValues() {
        switch (mType) {
            case SR:
            case MEMC:
                mValues = mContext.getResources().getStringArray(R.array.ai_lab_key_choose);
                mProValues = new String[]{"-1", "50", "75", "100"};
                break;
            case SLIDER:
                mValues = mContext.getResources().getStringArray(R.array.ai_lab_slider_choose);
                mProValues = new String[]{"-1", "false", "true"};
                break;
            case VOCAL:
                mValues = mContext.getResources().getStringArray(R.array.ai_lab_vocal_choose);
                mProValues = new String[]{"-1", "100", "0"};
                break;
            case KALAOK:
                mValues = mContext.getResources().getStringArray(R.array.ai_lab_kalaok_choose);
                mProValues = new String[]{"-1", "true", "false"};
                break;
        }
    }

    private void initViews() {
        mLlTextTwoArrow = findViewById(R.id.ai_ll_text_twoArrow);
        mAiDescrip = findViewById(R.id.ai_switch_item);
        mAiValue = findViewById(R.id.ai_switch_text);
        mLeftArrow = findViewById(R.id.ai_switch_iv_left);
        mRightArrow = findViewById(R.id.ai_switch_iv_right);
        mAiSwitch = findViewById(R.id.ai_indep_switch);

        Drawable trackDrawable = mAiSwitch.getTrackDrawable();
        trackDrawable.setColorFilter(ContextCompat.getColor(mContext, R.color.tv_white), PorterDuff.Mode.MULTIPLY);

        switch (mType) {
            case SR:
                initSRViews();
                break;
            case SD:
                mLlTextTwoArrow.setVisibility(View.GONE);
                mAiSwitch.setVisibility(View.VISIBLE);
                initSDViews();
                break;
            case MEMC:
                initMEMCViews();
                break;
            case SLIDER:
                initSLIDERViews();
                break;
            case VOCAL:
                initVOCALViews();
                break;
            case KALAOK:
                initKALAOKViews();
                break;
            case ASR:
                mLlTextTwoArrow.setVisibility(View.GONE);
                mAiSwitch.setVisibility(View.VISIBLE);
                initASRViews();
                break;
            case LLM:
                mLlTextTwoArrow.setVisibility(View.GONE);
                mAiSwitch.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    private long lastKeyDownTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {

        switch (mType) {
            case SR:
            case MEMC:
            case SLIDER:
            case VOCAL:
            case KALAOK:
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    long currentTime = System.currentTimeMillis();

                    if (mValueIndex > 0 && (currentTime - lastKeyDownTime) > 500) {
                        lastKeyDownTime = currentTime;
                        mAiValue.startAnimation(mLeftAnimation);
                    }
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    long currentTime = System.currentTimeMillis();

                    if (mValueIndex >= 0 && mValueIndex < mValues.length - 1 && (currentTime - lastKeyDownTime) > 500) {
                        lastKeyDownTime = currentTime;
                        mAiValue.startAnimation(mRightAnimation);
                    }
                    return true;
                }
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void initAnimations() {
        // mLeftAnimation
        mLeftAnimation = new TranslateAnimation(0, dip2px(mContext, -20), 0, 0);
        mLeftAnimation.setDuration(300);
        mLeftAnimation.setAnimationListener(this);
        // mRightAnimation
        mRightAnimation = new TranslateAnimation(0, dip2px(mContext, 20), 0, 0);
        mRightAnimation.setDuration(300);
        mRightAnimation.setAnimationListener(this);
    }

    @Override
    public void onAnimationStart(Animation animation) {
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if (animation.equals(mLeftAnimation)) {
            if (mValueIndex > 0) {
                mAiValue.setText(mValues[--mValueIndex]);
                mRightArrow.setVisibility(View.VISIBLE);

                if (mValueIndex == 0) {
                    updateStatus(false, "-1");
                    mRightArrow.setVisibility(View.VISIBLE);
                    mLeftArrow.setVisibility(View.INVISIBLE);
                } else {
                    updateStatus(true, mProValues[mValueIndex]);
                }
            }

        } else if (animation.equals(mRightAnimation)) {
            if (mValueIndex >= 0 && mValueIndex < mValues.length - 1) {
                mAiValue.setText(mValues[++mValueIndex]);
                mLeftArrow.setVisibility(View.VISIBLE);

                if (mValueIndex == mValues.length - 1) {
                    mLeftArrow.setVisibility(View.VISIBLE);
                    mRightArrow.setVisibility(View.INVISIBLE);
                }
                updateStatus(true, mProValues[mValueIndex]);
            }

        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        switch (mType) {
            case SD:
                setSdModeStatus(checked ? true : false);
                break;
            case ASR:
                setASRStatus(checked ? "true" : "false");
                break;
        }

    }

    @Override
    public void onAnimationRepeat(Animation animation) {
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private void updateStatus(boolean isEnable, String level) {
        switch (mType) {
            case SR:
                setSrModeStatus(isEnable, level);
                break;
            case MEMC:
                setMemcStatus(isEnable, level);
                break;
            case SLIDER:
                boolean dynamic = false;
                if ("true".equals(level)) {
                    dynamic = true;
                }
                setSilderModeStatus(isEnable, dynamic);
                break;
            case VOCAL:
                if (isEnable) {
                    setVocalStatus("1");
                    setVocalLevel(Integer.valueOf(level));
                } else {
                    // disable
                    setVocalStatus("0");
                }
                break;
            case KALAOK:
                if (isEnable) {
                    setKalaokStatus("true");
                    if ("true".equals(level)) {
                        setKalaokAGC(true);
                    } else {
                        setKalaokAGC(false);
                    }
                } else {
                    // disable
                    setKalaokStatus("false");
                }
                break;
            default:
                break;
        }
    }

    //SR
    private boolean getSrModeStatus() {
        return "-1".equals(SystemProperties.get(Prop_SR_ENABLE, "0")) && "1".equals(SystemProperties.get(Prop_SCULPTOR_ENABLE, "0"));
    }

    private void setSrModeStatus(boolean isEnable, String level) {
        if (isEnable) {
            updateVisionPq(true);
        }
        SystemProperties.set(Prop_SR_ENABLE, String.valueOf(isEnable ? -1 : 0));
        SystemProperties.set("persist.vendor.rkpq.dc.enable", String.valueOf(isEnable ? 1 : 0));
        SystemProperties.set("persist.vendor.rkpq.hwpq_shp_en", String.valueOf(isEnable ? 1 : 0));
        SystemProperties.set("persist.vendor.rkpq.hwpq_lce_ratio", String.valueOf(isEnable ? 10 : 0));
        SystemProperties.set("vendor.tvinput.rkpq.vdpp_shp_en", String.valueOf(isEnable ? 1 : 0));
        SystemProperties.set("vendor.tvinput.rkpq.update_vdpp_cfg", String.valueOf(1));

        if (!"-1".equals(level))
            SystemProperties.set(Prop_SR_SRSTRENGH, level);//0~100 50-75-100
    }
    // SD
    private boolean getSdModeStatus() {
        return "1".equals(SystemProperties.get(Prop_SD_ENABLE, "0")) && "1".equals(SystemProperties.get(Prop_SCULPTOR_ENABLE, "0"));
    }

    private void setSdModeStatus(boolean isEnable) {
        if (isEnable) {
            updateVisionPq(true);
        }
        SystemProperties.set(Prop_SD_ENABLE, String.valueOf(isEnable ? 1 : 0));
        SystemProperties.set(Prop_HWPQ_SD_ENABLE, String.valueOf(isEnable ? 1 : 0));
    }
    // MEMC
    private boolean getMemcStatus() {
        return "-1".equals(SystemProperties.get(Prop_MEMC_ENABLE, "0")) && "1".equals(SystemProperties.get(Prop_SCULPTOR_ENABLE, "0"));
    }
    private void setMemcStatus(boolean isEnable, String level) {
        if (isEnable) {
            updateVisionPq(true);
        }
        SystemProperties.set(Prop_MEMC_ENABLE, String.valueOf(isEnable ? -1 : 0));
        if (!"-1".equals(level))
            SystemProperties.set(Prop_MEMC_SRSTRENGH, level);//0~100 50-75-100
    }
    // SLIDER
    private boolean getSilderModeStatus() {
        return "1".equals(SystemProperties.get(Prop_SLIDER_ENABLE, "0")) && "1".equals(SystemProperties.get(Prop_SCULPTOR_ENABLE, "0"));
    }

    private void setSilderModeStatus(boolean isEnable, boolean dynamic) {
        if (isEnable) {
            updateVisionPq(true);
        }
        SystemProperties.set(Prop_SLIDER_ENABLE, String.valueOf(isEnable ? 1 : 0));
        if (isEnable)
            SystemProperties.set(Prop_SLIDER_DYNAMIC, String.valueOf(dynamic ? 1 : 0));
    }

    private void updateVisionPq(boolean isEnable) {
        if (isEnable) {
            setKalaokStatus("false");
            setASRStatus("false");
            setVocalStatus("false");
            if("0".equals(SystemProperties.get(Prop_SCULPTOR_ENABLE, "0")))
                Toast.makeText(mContext, mContext.getString(R.string.ai_lab_switch_visionpq,"Vision PQ"), Toast.LENGTH_SHORT).show();
            SystemProperties.set(Prop_WATERMARK_ENABLE,"1");
        } else {
            //disable visionpq config
            setSrModeStatus(false, "-1");
            setSdModeStatus(false);
            setMemcStatus(false, "-1");
            setSilderModeStatus(false, false);
            Toast.makeText(mContext, mContext.getString(R.string.ai_lab_close_visionpq,"Vision PQ"), Toast.LENGTH_SHORT).show();
            SystemProperties.set(Prop_WATERMARK_ENABLE,"0");
        }
        SystemProperties.set(Prop_SCULPTOR_ENABLE, isEnable ? "1" : "0");
        SystemProperties.set(Prop_SCULPTOR_C2_ENABLE, isEnable ? "1" : "0");

    }
    // VOCAL
    private boolean getVocalStatus() {
        return SystemProperties.getBoolean(Prop_VOCAL_ENABLE, false);
    }

    private void setVocalStatus(String state) {
        boolean isEnable = "1".equals(state);
        SystemProperties.set(Prop_VOCAL_ENABLE, state);
        if (isEnable)
            updateVisionPq(false);
    }

    private void setVocalLevel(int level) {
        SystemProperties.set(Prop_VOCAL_LEVEL, level + "");
        SystemProperties.set(Prop_OTHER_LEVEL, (100 - level) + "");
    }
    // KALAOK
    private boolean getKalaokStatus() {
        return SystemProperties.getBoolean(Prop_KALAOK_ENABLE, false);
    }

    private void setKalaokStatus(String state) {
        boolean isEnable = "true".equals(state);
        SystemProperties.set(Prop_KALAOK_ENABLE, state);
        if (isEnable)
            updateVisionPq(false);
    }

    private void setKalaokAGC(boolean on) {
        if(on)
            SystemProperties.set(Prop_KALAOK_AGC, "1");
        else
            SystemProperties.set(Prop_KALAOK_AGC, "0");
    }
    // ASR
    private boolean getASRStatus() {
        return SystemProperties.getBoolean(Prop_ASR_ENABLE, false);
    }

    private void setASRStatus(String state) {
        boolean isEnable = "true".equals(state);
        SystemProperties.set(Prop_ASR_ENABLE, state);
        if (isEnable)
            updateVisionPq(false);
    }

    // init
    private void initSRViews() {
        mAiDescrip.setText(R.string.ai_lab_sr_mode);
        String sr_strength = SystemProperties.get(Prop_SR_SRSTRENGH, "100");
        if (getSrModeStatus()) {
            switch (sr_strength) {
                case "50":
                    mAiValue.setText(R.string.ai_lab_weak);
                    mValueIndex = 1;
                    break;
                case "75":
                    mAiValue.setText(R.string.ai_lab_medium);
                    mValueIndex = 2;
                    break;
                default:
                    mAiValue.setText(R.string.ai_lab_strong);
                    mValueIndex = 3;
                    mRightArrow.setVisibility(View.INVISIBLE);
                    break;
            }
        } else {
            mAiValue.setText(R.string.ai_lab_close);
            mValueIndex = 0;
            mLeftArrow.setVisibility(View.INVISIBLE);
        }
    }

    private void initSDViews() {
        mAiDescrip.setText(R.string.ai_lab_sd_mode);
        mAiSwitch.setChecked(getSdModeStatus());
        mAiSwitch.setOnCheckedChangeListener(this);
    }

    private void initMEMCViews() {
        mAiDescrip.setText(R.string.ai_lab_memc_mode);
        String memc_strength = SystemProperties.get(Prop_MEMC_SRSTRENGH, "100");
        if (getMemcStatus()) {
            switch (memc_strength) {
                case "50":
                    mAiValue.setText(R.string.ai_lab_weak);
                    mValueIndex = 1;
                    break;
                case "75":
                    mAiValue.setText(R.string.ai_lab_medium);
                    mValueIndex = 2;
                    break;
                default:
                    mAiValue.setText(R.string.ai_lab_strong);
                    mValueIndex = 3;
                    mRightArrow.setVisibility(View.INVISIBLE);
                    break;
            }
        } else {
            mAiValue.setText(R.string.ai_lab_close);
            mValueIndex = 0;
            mLeftArrow.setVisibility(View.INVISIBLE);
        }
    }

    private void initSLIDERViews() {
        mAiDescrip.setText(R.string.ai_lab_slider_mode);
        ViewGroup.LayoutParams params = mAiValue.getLayoutParams();
        params.width = dip2px(mContext, 140); // 140dp
        mAiValue.setLayoutParams(params);

        if (getSilderModeStatus()) {
            if ("1".equals(SystemProperties.get(Prop_SLIDER_DYNAMIC, "0"))) {
                mAiValue.setText(R.string.ai_lab_slider_dynamic);
                mValueIndex = 2;
                mRightArrow.setVisibility(View.INVISIBLE);
            } else {
                mAiValue.setText(R.string.ai_lab_slider_enable);
                mValueIndex = 1;
            }
        } else {
            mAiValue.setText(R.string.ai_lab_slider_close);
            mValueIndex = 0;
            mLeftArrow.setVisibility(View.INVISIBLE);
        }
    }

    private void initVOCALViews() {
        mAiDescrip.setText(R.string.ai_lab_vocal);
        ViewGroup.LayoutParams params = mAiValue.getLayoutParams();
        params.width = dip2px(mContext, 140); // 140dp
        mAiValue.setLayoutParams(params);

        if (getVocalStatus()) {
            if ("0".equals(SystemProperties.get(Prop_VOCAL_LEVEL, "0"))) {
                mAiValue.setText(R.string.ai_lab_vocal_min);
                mValueIndex = 2;
                mRightArrow.setVisibility(View.INVISIBLE);
            } else {
                mAiValue.setText(R.string.ai_lab_vocal_max);
                mValueIndex = 1;
            }

        } else {
            mAiValue.setText(R.string.ai_lab_vocal_close);
            mValueIndex = 0;
            mLeftArrow.setVisibility(View.INVISIBLE);
        }
    }

    private void initKALAOKViews() {
        mAiDescrip.setText(R.string.ai_lab_kalaok);
        ViewGroup.LayoutParams params = mAiValue.getLayoutParams();
        params.width = dip2px(mContext, 140); // 140dp
        mAiValue.setLayoutParams(params);

        if (getKalaokStatus()) {
            if ("0".equals(SystemProperties.get(Prop_KALAOK_AGC, "0"))) {
                mAiValue.setText(R.string.ai_lab_kalaok_agc_close);
                mValueIndex = 2;
                mRightArrow.setVisibility(View.INVISIBLE);
            } else {
                mAiValue.setText(R.string.ai_lab_kalaok_agc);
                mValueIndex = 1;
            }

        } else {
            mAiValue.setText(R.string.ai_lab_kalaok_close);
            mValueIndex = 0;
            mLeftArrow.setVisibility(View.INVISIBLE);
        }

    }

    private void initASRViews() {
        mAiDescrip.setText(R.string.ai_lab_asr);
        mAiSwitch.setChecked(getASRStatus());
        mAiSwitch.setOnCheckedChangeListener(this);
    }
}
