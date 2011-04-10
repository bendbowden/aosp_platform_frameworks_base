package com.android.server.status.galaxyswidget;

import com.android.internal.R;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff.Mode;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.provider.Settings;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class PowerButton {
    public static final String TAG = "PowerButton";

    public static final int STATE_ENABLED = 1;
    public static final int STATE_DISABLED = 2;
    public static final int STATE_TURNING_ON = 3;
    public static final int STATE_TURNING_OFF = 4;
    public static final int STATE_INTERMEDIATE = 5;
    public static final int STATE_UNKNOWN = 6;

    public static final String BUTTON_WIFI = "wifi";
    public static final String BUTTON_GPS = "gps";
    public static final String BUTTON_BLUETOOTH = "bluetooth";
    public static final String BUTTON_BRIGHTNESS = "brightness";
    public static final String BUTTON_SOUND = "sound";
    public static final String BUTTON_SYNC = "sync";
    public static final String BUTTON_WIFIAP = "wifiap";
    public static final String BUTTON_SCREENTIMEOUT = "screentimeout";
    public static final String BUTTON_MOBILEDATA = "mobiledata";
    public static final String BUTTON_LOCKSCREEN = "lockscreen";
    public static final String BUTTON_NETWORKMODE = "networkmode";
    public static final String BUTTON_AUTOROTATE = "autorotate";
    public static final String BUTTON_AIRPLANE = "airplane";
    public static final String BUTTON_FLASHLIGHT = "flashlight";
    public static final String BUTTON_SLEEP = "sleep";
    public static final String BUTTON_MEDIA_PLAY_PAUSE = "media_play_pause";
    public static final String BUTTON_MEDIA_PREVIOUS = "media_previous";
    public static final String BUTTON_MEDIA_NEXT = "media_next";
    public static final String BUTTON_UNKNOWN = "unknown";

    private static final Mode MASK_MODE = Mode.SCREEN;

    // this is a list of all of our buttons and their corresponding classes
    private static final HashMap<String, Class<? extends PowerButton>> BUTTONS = new HashMap<String, Class<? extends PowerButton>>();
    static {
        BUTTONS.put(BUTTON_WIFI, WifiButton.class);
        BUTTONS.put(BUTTON_GPS, GPSButton.class);
        BUTTONS.put(BUTTON_BLUETOOTH, BluetoothButton.class);//
        BUTTONS.put(BUTTON_BRIGHTNESS, BrightnessButton.class);
        BUTTONS.put(BUTTON_SOUND, SoundButton.class);
        BUTTONS.put(BUTTON_SYNC, SyncButton.class);
        BUTTONS.put(BUTTON_WIFIAP, WifiApButton.class);//
        BUTTONS.put(BUTTON_SCREENTIMEOUT, ScreenTimeoutButton.class);
        BUTTONS.put(BUTTON_MOBILEDATA, MobileDataButton.class);
        BUTTONS.put(BUTTON_LOCKSCREEN, LockScreenButton.class);
        BUTTONS.put(BUTTON_NETWORKMODE, NetworkModeButton.class);//
        BUTTONS.put(BUTTON_AUTOROTATE, AutoRotateButton.class);
        BUTTONS.put(BUTTON_AIRPLANE, AirplaneButton.class);
        BUTTONS.put(BUTTON_FLASHLIGHT, FlashlightButton.class);//
        BUTTONS.put(BUTTON_SLEEP, SleepButton.class);
        BUTTONS.put(BUTTON_MEDIA_PLAY_PAUSE, MediaPlayPauseButton.class);
        BUTTONS.put(BUTTON_MEDIA_PREVIOUS, MediaPreviousButton.class);
        BUTTONS.put(BUTTON_MEDIA_NEXT, MediaNextButton.class);
    }
    // this is a list of our currently loaded buttons
    private static final HashMap<String, PowerButton> BUTTONS_LOADED = new HashMap<String, PowerButton>();

    protected int mIcon;
    protected int mState;
    protected View mView;
    protected String mType = BUTTON_UNKNOWN;

    // we use this to ensure we update our views on the UI thread
    private Handler mViewUpdateHandler = new Handler() {
            public void handleMessage(Message msg) {
                // this is only used to update the view, so do it
                if(mView != null) {
                    Context context = mView.getContext();
                    Resources res = context.getResources();
                    int buttonLayer = R.id.galaxy_s_widget_button;
                    int buttonIcon = R.id.galaxy_s_widget_button_image;
                    int buttonState = R.id.galaxy_s_widget_button_indic;

                    updateImageView(buttonIcon, mIcon);

                    int sColorMaskBase = Settings.System.getInt(context.getContentResolver(),
                            Settings.System.GALAXY_S_WIDGET_COLOR, 0xFFFFCB00);
                    int sColorMaskOn = (sColorMaskBase & 0x00FFFFFF) | 0xC0000000;
                    int sColorMaskOff = (sColorMaskBase & 0x00FFFFFF) | 0x30000000;
                    int sColorMaskInter = (sColorMaskBase & 0x00FFFFFF) | 0x70000000;

                    /* Button State */
                    switch(mState) {
                        case STATE_ENABLED:
                            updateImageView(buttonState,
                                    res.getDrawable(R.drawable.stat_bgon_custom, sColorMaskOn, MASK_MODE));
                            break;
                        case STATE_DISABLED:
                            updateImageView(buttonState,
                                    res.getDrawable(R.drawable.stat_bgon_custom, sColorMaskOff, MASK_MODE));
                            break;
                        default:
                            updateImageView(buttonState,
                                    res.getDrawable(R.drawable.stat_bgon_custom, sColorMaskInter, MASK_MODE));
                            break;
                    }
                }
            }
        };

    protected abstract void updateState();
    protected abstract void toggleState();

    protected void update() {
        updateState();
        updateView();
    }

    protected void onReceive(Context context, Intent intent) {
        // do nothing as a standard, override this if the button needs to respond
        // to broadcast events from the StatusBarService broadcast receiver
    }

    protected void onChangeUri(Uri uri) {
        // do nothing as a standard, override this if the button needs to respond
        // to a changed setting
    }

    protected IntentFilter getBroadcastIntentFilter() {
        return new IntentFilter();
    }

    protected List<Uri> getObservedUris() {
        return new ArrayList<Uri>();
    }

    protected void setupButton(View view) {
        mView = view;
        if(mView != null) {
            mView.setTag(mType);
            mView.setOnClickListener(mClickListener);
        }
    }

    protected void updateView() {
        mViewUpdateHandler.sendEmptyMessage(0);
    }

    private void updateImageView(int id, int resId) {
        ImageView imageIcon = (ImageView)mView.findViewById(id);
        imageIcon.setImageResource(resId);
    }

    private void updateImageView(int id, Drawable resDraw) {
        ImageView imageIcon = (ImageView)mView.findViewById(id);
        imageIcon.setImageResource(R.drawable.stat_bgon_custom);
        imageIcon.setImageDrawable(resDraw);
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            String type = (String)v.getTag();

            for(Map.Entry<String, PowerButton> entry : BUTTONS_LOADED.entrySet()) {
                if(entry.getKey().equals(type)) {
                    entry.getValue().toggleState();
                    break;
                }
            }
        }
    };

    public static boolean loadButton(String key, View view) {
        // first make sure we have a valid button
        if(BUTTONS.containsKey(key) && view != null) {
            synchronized (BUTTONS_LOADED) {
                if(BUTTONS_LOADED.containsKey(key)) {
                    // setup the button again
                    BUTTONS_LOADED.get(key).setupButton(view);
                } else {
                    try {
                        // we need to instantiate a new button and add it
                        PowerButton pb = BUTTONS.get(key).newInstance();
                        // set it up
                        pb.setupButton(view);
                        // save it
                        BUTTONS_LOADED.put(key, pb);
                    } catch(Exception e) {
                        Log.e(TAG, "Error loading button: " + key, e);
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public static void unloadButton(String key) {
        synchronized (BUTTONS_LOADED) {
            // first make sure we have a valid button
            if(BUTTONS_LOADED.containsKey(key)) {
                // wipe out the button view
                BUTTONS_LOADED.get(key).setupButton(null);
                // remove the button from our list of loaded ones
                BUTTONS_LOADED.remove(key);
            }
        }
    }

    public static void unloadAllButtons() {
        synchronized (BUTTONS_LOADED) {
            // cycle through setting the buttons to null
            for(PowerButton pb : BUTTONS_LOADED.values()) {
                pb.setupButton(null);
            }

            // clear our list
            BUTTONS_LOADED.clear();
        }
    }

    public static void updateAllButtons() {
        synchronized (BUTTONS_LOADED) {
            // cycle through our buttons and update them
            for(PowerButton pb : BUTTONS_LOADED.values()) {
                pb.update();
            }
        }
    }

    // glue for broadcast receivers
    public static IntentFilter getAllBroadcastIntentFilters() {
        IntentFilter filter = new IntentFilter();

        for(PowerButton button : BUTTONS_LOADED.values()) {
            IntentFilter tmp = button.getBroadcastIntentFilter();

            // cycle through these actions, and see if we need them
            int num = tmp.countActions();
            for(int i = 0; i < num; i++) {
                String action = tmp.getAction(i);
                if(!filter.hasAction(action)) {
                    filter.addAction(action);
                }
            }
        }

        // return our merged filter
        return filter;
    }

    // glue for content observation
    public static List<Uri> getAllObservedUris() {
        List<Uri> uris = new ArrayList<Uri>();

        for(PowerButton button : BUTTONS_LOADED.values()) {
            List<Uri> tmp = button.getObservedUris();

            for(Uri uri : tmp) {
                if(!uris.contains(uri)) {
                    uris.add(uri);
                }
            }
        }

        return uris;
    }

    public static void handleOnReceive(Context context, Intent intent) {
        String action = intent.getAction();

        // cycle through power buttons
        for(PowerButton button : BUTTONS_LOADED.values()) {
            // call "onReceive" on those that matter
            if(button.getBroadcastIntentFilter().hasAction(action)) {
                button.onReceive(context, intent);
            }
        }
    }

    public static void handleOnChangeUri(Uri uri) {
        for(PowerButton button : BUTTONS_LOADED.values()) {
            if(button.getObservedUris().contains(uri)) {
                button.onChangeUri(uri);
            }
        }
    }
}
