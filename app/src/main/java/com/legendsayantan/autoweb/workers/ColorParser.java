package com.legendsayantan.autoweb.workers;

import android.app.Activity;
import android.content.res.Configuration;

import androidx.core.content.ContextCompat;

import com.legendsayantan.autoweb.R;

/**
 * @author legendsayantan
 */
public class ColorParser {
    public static int getPrimary(Activity activity){
        return ContextCompat.getColor(activity, (activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES ? R.color.ic_launcher_background : R.color.ic_launcher_foreground);
    }
    public static int getSecondary(Activity activity){
        return ContextCompat.getColor(activity, (activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES ? R.color.ic_launcher_background : R.color.ic_launcher_foreground);
    }
    public static int getLight(Activity activity){
        return ContextCompat.getColor(activity,R.color.ic_launcher_foreground);
    }
    public static int getDark(Activity activity){
        return ContextCompat.getColor(activity,R.color.ic_launcher_background);
    }
}
