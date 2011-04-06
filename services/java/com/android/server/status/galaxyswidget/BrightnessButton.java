package com.android.server.status.galaxyswidget;

import com.android.internal.R;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IPowerManager;
import android.os.Power;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class BrightnessButton extends PowerButton {

    /**
* Minimum and maximum brightnesses. Don't go to 0 since that makes the
* display unusable
*/
    private static final int MIN_BACKLIGHT = Power.BRIGHTNESS_DIM + 10;
    private static final int MAX_BACKLIGHT = Power.BRIGHTNESS_ON;
    /**
* Auto-backlight level
*/
    private static final int AUTO_BACKLIGHT = -1;
    /**
* Mid-range brightness values + thresholds
*/
    private static final int[] BACKLIGHT_MIDLEVELS = new int[] {
            (int) (Power.BRIGHTNESS_ON * 0.25f),
            (int) (Power.BRIGHTNESS_ON * 0.40f),
            (int) (Power.BRIGHTNESS_ON * 0.50f),
            (int) (Power.BRIGHTNESS_ON * 0.75f)};

    private static Boolean SUPPORTS_AUTO_BACKLIGHT=null;

    private static final List<Uri> OBSERVED_URIS = new ArrayList<Uri>();
    static {
        OBSERVED_URIS.add(Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS));
        OBSERVED_URIS.add(Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE));
    }

    public BrightnessButton() { mType = BUTTON_BRIGHTNESS; }

    @Override
    protected void updateState() {
        Context context = mView.getContext();
        if (isBrightnessSetToAutomatic(context)) {
            mIcon = R.drawable.stat_brightness_auto;
            mState = STATE_ENABLED;
        } else {
            switch(getBrightnessState(context)) {
            case STATE_ENABLED:
                mIcon = R.drawable.stat_brightness_on;
                mState = STATE_ENABLED;
                break;
            case STATE_TURNING_ON:
                mIcon = R.drawable.stat_brightness_on;
                mState = STATE_INTERMEDIATE;
                break;
            case STATE_TURNING_OFF:
                mIcon = R.drawable.stat_brightness_off;
                mState = STATE_INTERMEDIATE;
                break;
            default:
                mIcon = R.drawable.stat_brightness_off;
                mState = STATE_DISABLED;
                break;
            }
        }
    }

    @Override
    protected void toggleState() {
        Context context = mView.getContext();
        try {
            IPowerManager power = IPowerManager.Stub.asInterface(ServiceManager
                    .getService("power"));
            if (power != null) {
                int brightness = getNextBrightnessValue(context);
                ContentResolver contentResolver = context.getContentResolver();
                if (brightness == AUTO_BACKLIGHT) {
                    Settings.System.putInt(contentResolver,
                            Settings.System.SCREEN_BRIGHTNESS_MODE,
                            Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                } else {
                    if (isAutomaticModeSupported(context)) {
                        Settings.System.putInt(contentResolver,
                                Settings.System.SCREEN_BRIGHTNESS_MODE,
                                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    }
                    power.setBacklightBrightness(brightness);
                    Settings.System.putInt(contentResolver,
                            Settings.System.SCREEN_BRIGHTNESS, brightness);
                }
            }
        } catch (RemoteException e) {
            Log.d("PowerWidget", "toggleBrightness: " + e);
        }
    }

    @Override
    protected List<Uri> getObservedUris() {
        return OBSERVED_URIS;
    }

    private static int getNextBrightnessValue(Context context) {
        int brightness = Settings.System.getInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS,0);

        // take care of some default loops
        if(isAutomaticModeSupported(context)) {
            if(isBrightnessSetToAutomatic(context)) {
                // if we're in auto mode, go to min
                return MIN_BACKLIGHT;
            } else if(brightness >= MAX_BACKLIGHT) {
                // if we're at max and auto is supported, go to it
                return AUTO_BACKLIGHT;
            }
        } else {
            if(brightness >= MAX_BACKLIGHT) {
                // if we're at max and auto isn't supported, go to min
                return MIN_BACKLIGHT;
            }
        }

        // we're either in mid-range or at minimum, so cycle up
        for(int level : BACKLIGHT_MIDLEVELS) {
            // is this brightness greater than the last?
            if(brightness < level) {
                // yes it is so return it
                return level;
            }
        }

        // if we got here, then we must be at the top of the mid-range, so return max
        return MAX_BACKLIGHT;
    }

    private static int getBrightnessState(Context context) {
        int brightness = Settings.System.getInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS,0);

        if (brightness <= MIN_BACKLIGHT) {
            return STATE_DISABLED;
        } else if (brightness >= MAX_BACKLIGHT) {
            return STATE_ENABLED;
        } else {
            return STATE_TURNING_ON;
        }
    }

    private static boolean isAutomaticModeSupported(Context context) {
        if (SUPPORTS_AUTO_BACKLIGHT == null) {
            if (context.getResources().getBoolean(
                    com.android.internal.R.bool.config_automatic_brightness_available)) {
                SUPPORTS_AUTO_BACKLIGHT=true;
            } else {
                SUPPORTS_AUTO_BACKLIGHT=false;
            }
        }

        return SUPPORTS_AUTO_BACKLIGHT;
    }

    private static boolean isBrightnessSetToAutomatic(Context context) {
        try {
            IPowerManager power = IPowerManager.Stub.asInterface(ServiceManager
                    .getService("power"));
            if (power != null) {
                int brightnessMode = Settings.System.getInt(context
                        .getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS_MODE);
                return brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
            }
        } catch (Exception e) {
            Log.d("PowerWidget", "getBrightnessMode: " + e);
        }

        return false;
    }
}

