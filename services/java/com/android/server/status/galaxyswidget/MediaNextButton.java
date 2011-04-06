package com.android.server.status.galaxyswidget;

import com.android.internal.R;

import android.content.Context;
import android.view.KeyEvent;

public class MediaNextButton extends MediaKeyEventButton {
    public MediaNextButton() { mType = BUTTON_MEDIA_NEXT; }

    @Override
    protected void updateState() {
        mIcon = com.android.internal.R.drawable.stat_media_next;
        if(getAudioManager(mView.getContext()).isMusicActive()) {
            mState = STATE_ENABLED;
        } else {
            mState = STATE_DISABLED;
        }
    }

    @Override
    protected void toggleState() {
        sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT);
    }
}
