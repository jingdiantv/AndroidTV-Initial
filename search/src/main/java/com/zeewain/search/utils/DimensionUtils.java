package com.zeewain.search.utils;

import android.content.res.Resources;

import androidx.annotation.DimenRes;

public class DimensionUtils {
    public static int getSizeFromDP(Resources resources, @DimenRes int id) {
        return resources.getDimensionPixelSize(id);
    }

    public static int getSizeFromSP(Resources resources, @DimenRes int id) {
        return (int) (resources.getDimension(id) * resources.getDisplayMetrics().scaledDensity);
    }

    public static int getSizeFromPX(Resources resources, @DimenRes int id) {
        return resources.getDimensionPixelSize(id);
    }
}
