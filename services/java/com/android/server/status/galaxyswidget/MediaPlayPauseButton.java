package com.android.server.status.galaxyswidget;

import com.android.internal.R;

import android.content.Context;
import android.view.KeyEvent;

public class MediaPlayPauseButton extends MediaKeyEventButton {
    public MediaPlayPauseButton() { mType = BUTTON_MEDIA_PLAY_PAUSE; }

    @Override
    protected void updateState() {
        if(getAudioManager(mView.getContext()).isMusicActive()) {
            mIcon = com.android.internal.R.drawable.stat_media_pause;
            mState = STATE_ENABLED;
        } else {
            mIcon = com.android.internal.R.drawable.stat_media_play;
            mState = STATE_DISABLED;
        }
    }

    @Override
    protected void toggleState() {
        sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
    }
}


