package com.android.server.status.galaxyswidget;

import com.android.internal.R;

import android.content.Context;
import android.view.KeyEvent;

public class MediaPreviousButton extends MediaKeyEventButton {
    public MediaPreviousButton() { mType = BUTTON_MEDIA_PREVIOUS; }

    @Override
    protected void updateState() {
        mIcon = com.android.internal.R.drawable.stat_media_previous;
        if(getAudioManager(mView.getContext()).isMusicActive()) {
            mState = STATE_ENABLED;
        } else {
            mState = STATE_DISABLED;
        }
    }

    @Override
    protected void toggleState() {
        sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
    }
}
