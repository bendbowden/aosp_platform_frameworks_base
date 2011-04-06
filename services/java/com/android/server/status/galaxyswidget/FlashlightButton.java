package com.android.server.status.galaxyswidget;

import com.android.internal.R;

import android.content.ContentResolver;
import android.content.Context;
import android.os.FileObserver;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class FlashlightButton extends PowerButton {
    private static final String TAG = "FlashlightButton";

    private static boolean runTimer = false;

    private static final File SPOTLIGHT_FILE = new File("/sys/class/leds/spotlight/brightness");
    private static final File FLASHLIGHT_FILE = new File("/sys/class/leds/flashlight/brightness");
    private static File FLASHLIGHT = null;
    static {
        if (SPOTLIGHT_FILE.exists()) {
            FLASHLIGHT = SPOTLIGHT_FILE;
        } else if (FLASHLIGHT_FILE.exists()) {
            FLASHLIGHT = FLASHLIGHT_FILE;
        }
    }

    private FileObserver mFlashlightObserver;
    private boolean mFlashlightObserved = false;

    public FlashlightButton() {
        mType = BUTTON_FLASHLIGHT;

        // only do this if the flashlight exists
        if(FLASHLIGHT != null && FLASHLIGHT.exists()) {
            mFlashlightObserver = new FileObserver(FLASHLIGHT.getAbsolutePath(),
                                                   FileObserver.MODIFY) {
                    public void onEvent(int event, String path) {
                        // update state/view if something happened
                        update();
                    }
                };
        }
    }

    @Override
    protected void setupButton(View view) {
        super.setupButton(view);

        if(mView == null && mFlashlightObserved) {
            Log.i(TAG, "Unregistering flashlight file listener");
            mFlashlightObserver.stopWatching();
            mFlashlightObserved = false;
        } else if(mView != null && !mFlashlightObserved) {
            Log.i(TAG, "Registering flashlight file listener");
            mFlashlightObserver.startWatching();
            mFlashlightObserved = true;
        }
    }

    @Override
    protected void updateState() {
        if(getFlashlightEnabled()) {
            mIcon = com.android.internal.R.drawable.stat_flashlight_on;
            mState = STATE_ENABLED;
        } else {
            mIcon = com.android.internal.R.drawable.stat_flashlight_off;
            mState = STATE_DISABLED;
        }
    }

    @Override
    protected void toggleState() {
        if(FLASHLIGHT == null) {
            // flashlight doesn't exist, warn the user and return
            Toast msg = Toast.makeText(mView.getContext(),
                    "Flashlight is not supported on your device",
                    Toast.LENGTH_LONG);
            msg.setGravity(Gravity.CENTER, msg.getXOffset() / 2, msg.getYOffset() / 2);
            msg.show();
            return;
        }

        setFlashlightEnabled(!getFlashlightEnabled());
    }

    private static boolean getFlashlightEnabled() {
        // catchall if flashlight isn't supported
        if(FLASHLIGHT == null) { return false; }

        FileReader fr = null;
        int result = '0';

        try {
            fr = new FileReader(FLASHLIGHT);
            result = fr.read();
        } catch (Exception e) {
            Log.e(TAG, "getFlashlightEnabled failed", e);
        } finally {
            // if something goes wrong, try at least to close this
            try { if(fr != null) { fr.close(); } } catch (Exception e) {
                Log.e(TAG, "setFlashlightEnabled failed", e);
            }
        }

        return (result != '0');
    }

    private static void setFlashlightEnabled(boolean enabled) {
        // catchall if flashlight isn't supported
        if(FLASHLIGHT == null) { return; }

        FileWriter fw = null;

        try {
            fw = new FileWriter(FLASHLIGHT);
            int value = (enabled ? 1 : 0);
            fw.write(String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "setFlashlightEnabled failed", e);
        } finally {
            // if something goes wrong, try at least to close this
            try { if(fw != null) { fw.close(); } } catch (Exception e) {
                Log.e(TAG, "setFlashlightEnabled failed", e);
            }
        }
    }
}

