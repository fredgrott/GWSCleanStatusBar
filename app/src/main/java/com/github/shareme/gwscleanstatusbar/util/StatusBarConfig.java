/*
 * Copyright 2014 Emma Guy
 * Modifications Copyright(C) 2015 Fred Grott(GrottWorkShop)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.shareme.gwscleanstatusbar.util;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.github.shareme.gwscleanstatusbar.R;


public class StatusBarConfig {
    private static final String RESOURCE_NAME_STATUS_BAR_HEIGHT = "status_bar_height";

    private final int mApiLevel;
    private final boolean mIsKitKatGradientEnabled;

    private final Resources mResources;
    private final AssetManager mAssetManager;

    public StatusBarConfig(int apiLevel, boolean isKitKatGradientEnabled, Resources r, AssetManager a) {
        mApiLevel = apiLevel;
        mIsKitKatGradientEnabled = isKitKatGradientEnabled;
        mResources = r;
        mAssetManager = a;
    }

    public int getStatusBarHeight() {
        return getInternalDimensionSize(mResources, RESOURCE_NAME_STATUS_BAR_HEIGHT);
    }

    public boolean shouldDrawGradient() {
        return mIsKitKatGradientEnabled && mApiLevel == Build.VERSION_CODES.KITKAT;
    }

    @SuppressWarnings("deprecation")
    public int getForegroundColour() {
        int colourResId = R.color.android_jellybean_status_bar;

        switch (mApiLevel) {
            case Build.VERSION_CODES.KITKAT:
                if (shouldDrawGradient()) {
                    colourResId = R.color.android_kitkat_status_bar_gradient;
                } else {
                    colourResId = R.color.android_kitkat_status_bar_default;
                }
                break;
            case Build.VERSION_CODES.LOLLIPOP:
                colourResId = android.R.color.white;
                break;
        }
        //TODO: getColor is depreciated
        return mResources.getColor(colourResId);
    }

    private boolean isAndroidL() {
        return mApiLevel == Build.VERSION_CODES.LOLLIPOP;
    }

    public Typeface getFont() {
        if (isAndroidL()) {
            return Typeface.createFromAsset(mAssetManager, "fonts/Roboto-Medium.ttf");
        }

        return null;
    }

    private int getInternalDimensionSize(Resources res, String key) {
        int result = 0;
        int resourceId = res.getIdentifier(key, "dimen", "android");
        if (resourceId > 0) {
            result = res.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public float getFontSize() {
        if (isAndroidL()) {
            return 14f;
        }
        return 16.0f;
    }

    public Drawable getNetworkIconDrawable(int icon) {
        if (isAndroidL()) {
            switch (icon) {
                case 1:
                    icon = R.drawable.network_icon_g_l;
                    break;

                case 2:
                    icon = R.drawable.network_icon_e_l;
                    break;

                case 3:
                    icon = R.drawable.network_icon_3g_l;
                    break;

                case 4:
                    icon = R.drawable.network_icon_h_l;
                    break;

                case 5:
                    icon = R.drawable.network_icon_lte_l;
                    break;

                case 99:
                    icon = R.drawable.network_icon_roam_l;
                    break;

                default:
                    icon = R.drawable.network_icon_off_l;
            }
            return getTintedDrawable(mResources, icon, getForegroundColour());
        } else {
            switch (icon) {
                case 1:
                    icon = R.drawable.network_icon_g;
                    break;

                case 2:
                    icon = R.drawable.network_icon_e;
                    break;

                case 3:
                    icon = R.drawable.network_icon_3g;
                    break;

                case 4:
                    icon = R.drawable.network_icon_h;
                    break;

                case 5:
                    icon = R.drawable.network_icon_lte;
                    break;

                case 99:
                    icon = R.drawable.network_icon_roam;
                    break;

                default:
                    icon = R.drawable.network_icon_off;
            }
            return getTintedDrawable(mResources, icon, getForegroundColour());
        }
    }

    public Drawable getGPSDrawable() {
        int icon;

        if (mApiLevel >= Build.VERSION_CODES.KITKAT) {
            icon = R.drawable.stat_sys_gps_on_19;
        } else {
            icon = R.drawable.stat_sys_gps_on_16;
        }
        return getTintedDrawable(mResources, icon, getForegroundColour());
    }

    public Drawable getWifiDrawable() {
        int icon;

        if (isAndroidL()) {
            icon = R.drawable.stat_sys_wifi_signal_4_fully_l;
        } else {
            icon = R.drawable.stat_sys_wifi_signal_4_fully;
        }
        return getTintedDrawable(mResources, icon, getForegroundColour());
    }

    @SuppressWarnings("deprecation")
    public Drawable getTintedDrawable(Resources res, int drawableResId, int colour) {
        //TODO: getDrawable is depreciated
        Drawable drawable = res.getDrawable(drawableResId);
        if (drawable != null) {
            drawable.setColorFilter(colour, PorterDuff.Mode.SRC_IN);
        }else{
            Log.e("me", "drawable is null oh oh");
        }
        return drawable;
    }

    public int getRightPadding() {
        if (isAndroidL()) {
            return dpToPx(8);
        }
        return dpToPx(6);
    }

    private int dpToPx(float dp) {
        return (int) (dp * mResources.getDisplayMetrics().density);
    }

    private int getBatteryViewWidth() {
        if (isAndroidL()) {
            return dpToPx(10);
        }
        return dpToPx(10.5f);
    }

    private int getBatteryViewHeight() {
        if (isAndroidL()) {
            return dpToPx(15.5f);
        }
        return dpToPx(16);
    }

    public int getNetworkIconPaddingOffset() {
        if (isAndroidL()) {
            return dpToPx(3);
        }
        return 0;
    }

    public int getWifiPaddingOffset() {
        if (isAndroidL()) {
            return dpToPx(4);
        }
        return 0;
    }

    public void setBatteryViewDimensions(View v) {
        v.getLayoutParams().width = getBatteryViewWidth();
        v.getLayoutParams().height = getBatteryViewHeight();
        if (!isAndroidL()) {
            ((LinearLayout.LayoutParams) v.getLayoutParams()).bottomMargin = dpToPx(0.33f);
        }
    }
}
