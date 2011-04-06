package com.android.server.status.galaxyswidget;

import com.android.internal.R;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Vibrator;
import android.provider.Settings;

public class SoundButton extends PowerButton {

    public static final int RINGER_MODE_UNKNOWN = 0;
    public static final int RINGER_MODE_SILENT = 1;
    public static final int RINGER_MODE_VIBRATE_ONLY = 2;
    public static final int RINGER_MODE_SOUND_ONLY = 3;
    public static final int RINGER_MODE_SOUND_AND_VIBRATE = 4;

    public static final int VIBRATE_DURATION = 500; // 0.5s

    public static AudioManager AUDIO_MANAGER = null;
    public static Vibrator VIBRATOR = null;

    public SoundButton() { mType = BUTTON_SOUND; }

    @Override
    protected void updateState() {
        switch (getSoundState(mView.getContext())) {
        case RINGER_MODE_SOUND_AND_VIBRATE:
                mIcon = R.drawable.stat_ring_vibrate_on;
                mState = STATE_ENABLED;
            break;
        case RINGER_MODE_SOUND_ONLY:
                mIcon = R.drawable.stat_ring_on;
                mState = STATE_ENABLED;
            break;
        case RINGER_MODE_VIBRATE_ONLY:
                mIcon = R.drawable.stat_vibrate_off;
                mState = STATE_DISABLED;
            break;
        case RINGER_MODE_SILENT:
                mIcon = R.drawable.stat_silent;
                mState = STATE_DISABLED;
            break;

        }
    }

    @Override
    protected void toggleState() {
        Context context = mView.getContext();
        int currentState = getSoundState(context);

        // services should be initialized in the last call, but we do this for completeness anyway
        initServices(context);

        switch (currentState) {
        case RINGER_MODE_SOUND_AND_VIBRATE: // go back to silent, no vibrate
            Settings.System.putInt(context.getContentResolver(),Settings.System.VIBRATE_IN_SILENT,0);
            AUDIO_MANAGER.
                setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,AudioManager.VIBRATE_SETTING_OFF);
            AUDIO_MANAGER.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            break;
        case RINGER_MODE_SOUND_ONLY: // go to sound and vibrate
            Settings.System.putInt(context.getContentResolver(),Settings.System.VIBRATE_IN_SILENT,1);
            AUDIO_MANAGER.
                setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,AudioManager.VIBRATE_SETTING_ON);
            AUDIO_MANAGER.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

            // vibrate so the user knows vibrator is on
            VIBRATOR.vibrate(VIBRATE_DURATION);
            break;
        case RINGER_MODE_VIBRATE_ONLY: // go to sound
            Settings.System.putInt(context.getContentResolver(),Settings.System.VIBRATE_IN_SILENT,1);
            AUDIO_MANAGER.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            AUDIO_MANAGER.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,AudioManager.VIBRATE_SETTING_ONLY_SILENT);
            break;
        case RINGER_MODE_SILENT: // go to vibrate
            Settings.System.putInt(context.getContentResolver(),Settings.System.VIBRATE_IN_SILENT,1);
            AUDIO_MANAGER.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            AUDIO_MANAGER.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,AudioManager.VIBRATE_SETTING_ONLY_SILENT);

            // vibrate so the user knows vibrator is on
            VIBRATOR.vibrate(VIBRATE_DURATION);
            break;
        default: // default going to sound
            Settings.System.putInt(context.getContentResolver(),Settings.System.VIBRATE_IN_SILENT,1);
            AUDIO_MANAGER.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            AUDIO_MANAGER.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,AudioManager.VIBRATE_SETTING_ONLY_SILENT);
            break;
        }
    }

    @Override
    protected IntentFilter getBroadcastIntentFilter() {
        // note, we don't actually have an "onReceive", so the caught intent will be ignored, but we want
        // to catch it anyway so the ringer status is updated if changed externally :D
        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        filter.addAction(AudioManager.VIBRATE_SETTING_CHANGED_ACTION);
        return filter;
    }

    private static int getSoundState(Context context) {
        // ensure our services are initialized
        initServices(context);

        int ringMode = AUDIO_MANAGER.getRingerMode();
        int vibrateMode = AUDIO_MANAGER.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER);

        if (ringMode == AudioManager.RINGER_MODE_NORMAL && vibrateMode == AudioManager.VIBRATE_SETTING_ON) {
            return RINGER_MODE_SOUND_AND_VIBRATE;
        } else if (ringMode == AudioManager.RINGER_MODE_NORMAL) {
            return RINGER_MODE_SOUND_ONLY;
        } else if (ringMode == AudioManager.RINGER_MODE_VIBRATE) {
            return RINGER_MODE_VIBRATE_ONLY;
        } else if (ringMode == AudioManager.RINGER_MODE_SILENT) {
            return RINGER_MODE_SILENT;
        } else {
            return RINGER_MODE_UNKNOWN;
        }
    }

    private static void initServices(Context context) {
        if(AUDIO_MANAGER == null) {
            AUDIO_MANAGER = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }
        if(VIBRATOR == null) {
            VIBRATOR = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
    }
}


