package com.android.server.status.galaxyswidget;

import com.android.internal.R;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings;

import com.android.internal.telephony.Phone;

public class NetworkModeButton extends PowerButton{

    public static final String NETWORK_MODE_CHANGED = "com.android.internal.telephony.NETWORK_MODE_CHANGED";
    public static final String REQUEST_NETWORK_MODE = "com.android.internal.telephony.REQUEST_NETWORK_MODE";
    public static final String MODIFY_NETWORK_MODE = "com.android.internal.telephony.MODIFY_NETWORK_MODE";

    public static final String NETWORK_MODE_KEY = "NETWORK_MODE";

    private static final int NO_NETWORK_MODE_YET = -99;
    private static final int NETWORK_MODE_UNKNOWN = -100;

    private static final int MODE_3G2G = 0;
    private static final int MODE_3GONLY = 1;
    private static final int MODE_BOTH = 2;

    private static final int DEFAULT_SETTING = 0;

    private static int NETWORK_MODE = NO_NETWORK_MODE_YET;
    private static int INTENDED_NETWORK_MODE = NO_NETWORK_MODE_YET;
    private static int CURRENT_INTERNAL_STATE = STATE_INTERMEDIATE;

    public NetworkModeButton() { mType = BUTTON_NETWORKMODE; }

    @Override
    protected void updateState() {
        Context context = mView.getContext();
        NETWORK_MODE = get2G3G(context);
        mState = networkModeToState(context);

        switch (mState) {
        case STATE_DISABLED:
            mIcon = R.drawable.stat_2g3g_off;
            break;
        case STATE_ENABLED:
            if (NETWORK_MODE == Phone.NT_MODE_WCDMA_ONLY) {
                mIcon = R.drawable.stat_3g_on;
            } else {
                mIcon = R.drawable.stat_2g3g_on;
            }
            break;
        case STATE_INTERMEDIATE:
            // In the transitional state, the bottom green bar
            // shows the tri-state (on, off, transitioning), but
            // the top dark-gray-or-bright-white logo shows the
            // user's intent. This is much easier to see in
            // sunlight.
            if (CURRENT_INTERNAL_STATE == STATE_TURNING_ON) {
                if (INTENDED_NETWORK_MODE == Phone.NT_MODE_WCDMA_ONLY) {
                    mIcon = R.drawable.stat_3g_on;
                } else {
                    mIcon = R.drawable.stat_2g3g_on;
                }
            } else {
                mIcon = R.drawable.stat_2g3g_off;
            }
            break;
        }
    }

    @Override
    protected void toggleState() {
        Intent intent = new Intent(MODIFY_NETWORK_MODE);
        switch (NETWORK_MODE) {
        case Phone.NT_MODE_WCDMA_PREF:
        case Phone.NT_MODE_GSM_UMTS:
            intent.putExtra(NETWORK_MODE_KEY, Phone.NT_MODE_GSM_ONLY);
            CURRENT_INTERNAL_STATE = STATE_TURNING_OFF;
            INTENDED_NETWORK_MODE=Phone.NT_MODE_GSM_ONLY;
            break;
        case Phone.NT_MODE_WCDMA_ONLY:
            intent.putExtra(NETWORK_MODE_KEY, Phone.NT_MODE_WCDMA_PREF);
            CURRENT_INTERNAL_STATE = STATE_TURNING_ON;
            INTENDED_NETWORK_MODE = Phone.NT_MODE_WCDMA_PREF;
            break;
        case Phone.NT_MODE_GSM_ONLY:
            intent.putExtra(NETWORK_MODE_KEY, Phone.NT_MODE_WCDMA_PREF);
            CURRENT_INTERNAL_STATE = STATE_TURNING_ON;
            INTENDED_NETWORK_MODE = Phone.NT_MODE_WCDMA_PREF;
            break;
        }

        NETWORK_MODE = NETWORK_MODE_UNKNOWN;
        mView.getContext().sendBroadcast(intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras() != null) {
            NETWORK_MODE = intent.getExtras().getInt(NETWORK_MODE_KEY);
            //Update to actual state
            INTENDED_NETWORK_MODE=NETWORK_MODE;
        }

        //need to clear intermediate states
        CURRENT_INTERNAL_STATE=STATE_ENABLED;

        int widgetState = networkModeToState(context);
        CURRENT_INTERNAL_STATE = widgetState;
        if (widgetState == STATE_ENABLED) {
// MobileDataButton.getInstance().networkModeChanged(context, NETWORK_MODE);
        }
    }

    @Override
    protected IntentFilter getBroadcastIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(NETWORK_MODE_CHANGED);
        return filter;
    }

    private static int get2G3G(Context context) {
        int state = 99;
        try {
            state = android.provider.Settings.Secure.getInt(context
                    .getContentResolver(),
                    android.provider.Settings.Secure.PREFERRED_NETWORK_MODE);
        } catch (SettingNotFoundException e) {
        }
        return state;
    }

    private static int networkModeToState(Context context) {
        if (CURRENT_INTERNAL_STATE == STATE_TURNING_ON ||
                CURRENT_INTERNAL_STATE == STATE_TURNING_OFF)
            return STATE_INTERMEDIATE;

        switch(NETWORK_MODE) {
            case Phone.NT_MODE_WCDMA_PREF:
            case Phone.NT_MODE_WCDMA_ONLY:
            case Phone.NT_MODE_GSM_UMTS:
                return STATE_ENABLED;
            case Phone.NT_MODE_GSM_ONLY:
                return STATE_DISABLED;
        }
        return STATE_INTERMEDIATE;
    }
}
