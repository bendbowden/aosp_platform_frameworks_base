package com.android.server.status.galaxyswidget;

import com.android.internal.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ScreenTimeoutButton extends PowerButton {

    // low and high thresholds for screen timeout
    private static final int SCREEN_TIMEOUT_LT = 30000;
    private static final int SCREEN_TIMEOUT_HT = 120000;
    private static final int[] SCREEN_TIMEOUTS = new int[] {
            15000, // 15s
            30000, // 30s
            60000, // 1m
            120000, // 2m
            600000, // 10m
            1800000}; // 30m

    private static Toast TOAST = null;

    private static final List<Uri> OBSERVED_URIS = new ArrayList<Uri>();
    static {
        OBSERVED_URIS.add(Settings.System.getUriFor(Settings.System.SCREEN_OFF_TIMEOUT));
    }

    public ScreenTimeoutButton() { mType = BUTTON_SCREENTIMEOUT; }

    @Override
    protected void updateState() {
        int timeout=getScreenTtimeout(mView.getContext());

        if (timeout <= SCREEN_TIMEOUT_LT) {
            mIcon = R.drawable.stat_screen_timeout_off;
            mState = STATE_DISABLED;
        } else if (timeout <= SCREEN_TIMEOUT_HT) {
            mIcon = R.drawable.stat_screen_timeout_off;
            mState = STATE_INTERMEDIATE;
        } else {
            mIcon = R.drawable.stat_screen_timeout_on;
            mState = STATE_ENABLED;
        }
    }

    @Override
    protected void toggleState() {
        Context context = mView.getContext();
        int screentimeout = getScreenTtimeout(context);
        int newtimeout = -1;

        // cycle through the timeouts and set the new one in a cycle
        for(int timeout : SCREEN_TIMEOUTS) {
            // is this timeout greater than the last?
            if(screentimeout < timeout) {
                newtimeout = timeout;
                break;
            }
        }

        // if we didn't find a strictly greater timeout, it means we're cycling
        if (newtimeout < 0) {
            newtimeout = SCREEN_TIMEOUTS[0];
        }

        Settings.System.putInt(
                context.getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, newtimeout);

        // create our toast
        if(TOAST == null) {
            TOAST = Toast.makeText(context, "", Toast.LENGTH_LONG);
        }

        // cancel any previous toast
        TOAST.cancel();

        // inform users of how long the timeout is now
        TOAST.setText("Screen timeout set to: " + timeoutToString(newtimeout));
        TOAST.setGravity(Gravity.CENTER, TOAST.getXOffset() / 2, TOAST.getYOffset() / 2);
        TOAST.show();
    }

    @Override
    protected List<Uri> getObservedUris() {
        return OBSERVED_URIS;
    }

    private static int getScreenTtimeout(Context context) {
        return Settings.System.getInt(
                context.getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, 0);
    }

    private static String timeoutToString(int timeout) {
        String[] tags = new String[] {
                "second(s)",
                "minute(s)",
                "hour(s)"
            };

        // default to however many seconds we have
        int tmp = (timeout / 1000);
        String sTimeout = tmp + " " + tags[0];

        for(int i = 1; i < tags.length && tmp >= 60; i++) {
            tmp /= (60 * i);
            sTimeout = tmp + " " + tags[i];
        }

        return sTimeout;
    }
}


