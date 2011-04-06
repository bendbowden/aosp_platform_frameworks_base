package com.android.server.status.galaxyswidget;

import com.android.internal.R;

import android.content.ContentResolver;
import android.content.Context;
import android.location.LocationManager;
import android.provider.Settings;

public class GPSButton extends PowerButton {

    public GPSButton() { mType = BUTTON_GPS; }

    @Override
    protected void updateState() {
        if(getGpsState(mView.getContext())) {
            mIcon = com.android.internal.R.drawable.stat_gps_on;
            mState = STATE_ENABLED;
        } else {
            mIcon = com.android.internal.R.drawable.stat_gps_off;
            mState = STATE_DISABLED;
        }
    }

    @Override
    protected void toggleState() {
        Context context = mView.getContext();
        ContentResolver resolver = context.getContentResolver();
        boolean enabled = getGpsState(context);
        Settings.Secure.setLocationProviderEnabled(resolver,
                LocationManager.GPS_PROVIDER, !enabled);
    }

    private static boolean getGpsState(Context context) {
        ContentResolver resolver = context.getContentResolver();
        return Settings.Secure.isLocationProviderEnabled(resolver,
                LocationManager.GPS_PROVIDER);
    }
}
