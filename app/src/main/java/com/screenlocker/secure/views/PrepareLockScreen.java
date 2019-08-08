package com.screenlocker.secure.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.screenlocker.secure.R;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.socket.receiver.DeviceStatusReceiver;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.views.patternlock.PatternLockView;
import com.screenlocker.secure.views.patternlock.listener.PatternLockViewListener;
import com.screenlocker.secure.views.patternlock.utils.PatternLockUtils;


import java.util.List;

import timber.log.Timber;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.screenlocker.secure.socket.utils.utils.chatLogin;
import static com.screenlocker.secure.socket.utils.utils.getDeviceStatus;
import static com.screenlocker.secure.socket.utils.utils.getUserType;
import static com.screenlocker.secure.socket.utils.utils.loginAsEncrypted;
import static com.screenlocker.secure.socket.utils.utils.loginAsGuest;
import static com.screenlocker.secure.socket.utils.utils.registerDeviceStatusReceiver;
import static com.screenlocker.secure.socket.utils.utils.wipeDevice;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.LOCK_SCREEN_STATUS;
import static com.screenlocker.secure.utils.AppConstants.LOGIN_ATTEMPTS;
import static com.screenlocker.secure.utils.AppConstants.OFFLINE_DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.TIME_REMAINING;
import static com.screenlocker.secure.utils.AppConstants.TIME_REMAINING_REBOOT;
import static com.screenlocker.secure.utils.CommonUtils.getTimeRemaining;
import static com.screenlocker.secure.utils.CommonUtils.setTimeRemaining;

/**
 * @author Muhammad Nadeem
 * @Date 8/3/2019.
 */
public class PrepareLockScreen {
    @SuppressLint("ResourceType")
    public static WindowManager.LayoutParams getParams(final Context context, final RelativeLayout layout) {

        int windowType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            windowType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            windowType = WindowManager.LayoutParams.TYPE_TOAST |
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        } else {
            windowType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        DeviceStatusReceiver deviceStatusReceiver = new DeviceStatusReceiver();

        registerDeviceStatusReceiver(context, deviceStatusReceiver);


        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                windowType,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                        | WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
                        | WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,

                PixelFormat.TRANSLUCENT);

        params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        params.gravity = Gravity.CENTER;


//        ((MdmMainActivity)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final LayoutInflater inflater = LayoutInflater.from(context);
        //whole view
        final View keypadView = inflater.inflate(R.layout.keypad_screen, layout);

        TextView txtWarning = keypadView.findViewById(R.id.txtWarning);
        ConstraintLayout rootView = keypadView.findViewById(R.id.background);
        String bg = PrefUtils.getStringPref(context, AppConstants.KEY_LOCK_IMAGE);
        if (bg == null || bg.equals("")) {
            rootView.setBackgroundResource(R.raw.remountan);

        } else {
            rootView.setBackgroundResource(Integer.parseInt(bg));
        }

        Button unLockButton = keypadView.findViewById(R.id.t9_unlock);
        EditText mPasswordField = keypadView.findViewById(R.id.password_field);

        final PatternLockView mPatternLockView = keypadView.findViewById(R.id.patternLock);

        mPatternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {
                Log.d(getClass().getName(), "Pattern drawing started");
            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {
//                Log.d(getClass().getName(), "Pattern progress: " +
//                        PatternLockUtils.patternToString(mPatternLockView, progressPattern));
            }

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {
                if (pattern.size() == 1){
                    mPasswordField.append(String.valueOf(pattern.get(0).getRandom()));
                    mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                    return;
                }
                String patternString = PatternLockUtils.patternToString(mPatternLockView, pattern);
                if (patternString.equals(PrefUtils.getStringPref(context, AppConstants.GUEST_PATTERN))){
                    mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                    new Handler().postDelayed(mPatternLockView::clearPattern, 150);
                    loginAsGuest(context);
                    mPasswordField.setText(null);
                    return;
                }
                mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                new Handler().postDelayed(mPatternLockView::clearPattern, 500);

//                mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
            }

            @Override
            public void onCleared() {
                Log.d(getClass().getName(), "Pattern has been cleared");
            }
        });
        String device_id = PrefUtils.getStringPref(context, DEVICE_ID);

        if (device_id == null) {
            device_id = PrefUtils.getStringPref(context, OFFLINE_DEVICE_ID);
        }

        final String device_status = getDeviceStatus(context);

        if (device_status == null) {
//            keyboardView.clearWaringText();
            txtWarning.setVisibility(INVISIBLE);
            txtWarning.setText(null);
        }


        if (device_status != null) {
            switch (device_status) {
                case "suspended":
                    if (device_id != null) {
//                        keyboardView.setWarningText("Your account with Device ID = " + device_id + " is Suspended. Please contact support");
                        txtWarning.setVisibility(VISIBLE);
                        txtWarning.setText(context.getResources().getString(R.string.account_device_id_suspended, device_id));

                    } else {
                        txtWarning.setVisibility(VISIBLE);
                        txtWarning.setText(context.getResources().getString(R.string.account_device_id_suspended, "N/A"));

                    }
                    break;
                case "expired":
                    if (device_id != null) {
                        txtWarning.setVisibility(VISIBLE);
                        txtWarning.setText(context.getResources().getString(R.string.account_device_id_expired, device_id));

                    } else {
                        txtWarning.setVisibility(VISIBLE);
                        txtWarning.setText(context.getResources().getString(R.string.account_device_id_expired, "N/A"));
//                        keyboardView.setWarningText("Your account with Device ID = N/A is Expired. Please contact support ");
                    }
                    break;
            }
        }


        String finalDevice_id = device_id;
        deviceStatusReceiver.setListener(status -> {
            if (status == null) {
                txtWarning.setVisibility(INVISIBLE);
                txtWarning.setText(null);

            } else {
                if (status.equals("suspended")) {
                    if (finalDevice_id != null) {
                        txtWarning.setVisibility(VISIBLE);
                        txtWarning.setText(context.getResources().getString(R.string.account_device_id_suspended, finalDevice_id));
//                        keyboardView.setWarningText("Your account with Device ID = " + finalDevice_id + " is Suspended. Please contact support");


                    } else {
                        txtWarning.setVisibility(VISIBLE);
                        txtWarning.setText(context.getResources().getString(R.string.account_device_id_suspended, "N/A"));

                    }
                } else if (status.equals("expired")) {
                    if (finalDevice_id != null) {
                        txtWarning.setVisibility(VISIBLE);
                        txtWarning.setText(context.getResources().getString(R.string.account_device_id_expired, finalDevice_id));

                    } else {
                        txtWarning.setVisibility(VISIBLE);
                        txtWarning.setText(context.getResources().getString(R.string.account_device_id_expired, "N/A"));


                    }
                }
            }

        });

        ImageView backPress = keypadView.findViewById(R.id.t9_key_backspace);
        backPress.setOnClickListener(v -> {
            Editable editable = mPasswordField.getText();
            int charCount = editable.length();
            if (charCount > 0) {
                editable.delete(charCount - 1, charCount);
            }
        });
        TextView supportButton = keypadView.findViewById(R.id.t9_key_support);
        TextView clearAll = keypadView.findViewById(R.id.t9_key_clear);
        clearAll.setOnClickListener(v -> {
            mPasswordField.setText(null);
        });

        mPasswordField.setTransformationMethod(new HiddenPassTransformationMethod());
        mPasswordField.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        mPasswordField.setTextColor(context.getResources().getColor(R.color.textColorPrimary));
        mPasswordField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String device_status = getDeviceStatus(context);
                if (device_status == null) {
                    txtWarning.setVisibility(INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        supportButton.setOnClickListener(v -> {
            chatLogin(context);

            mPasswordField.setText(null);
        });

        long time_remaining = getTimeRemaining(context);


        int attempts = 10;
        int count = PrefUtils.getIntegerPref(context, LOGIN_ATTEMPTS);
        int x = attempts - count;

        if (time_remaining != 0) {

            if (count >= 5) {

                if (count > 9) {
                    wipeDevice(context);
                }

                switch (count) {
                    case 5:
                        remainingTime(context, mPasswordField, txtWarning, unLockButton, time_remaining, count, x, AppConstants.attempt_5);
                        break;
                    case 6:
                        remainingTime(context, mPasswordField, txtWarning, unLockButton, time_remaining, count, x, AppConstants.attempt_6);
                        break;
                    case 7:
                        remainingTime(context, mPasswordField, txtWarning, unLockButton, time_remaining, count, x, AppConstants.attempt_7);
                        break;
                    case 8:
                        remainingTime(context, mPasswordField, txtWarning, unLockButton, time_remaining, count, x, AppConstants.attempt_8);
                        break;
                    case 9:
                        remainingTime(context, mPasswordField, txtWarning, unLockButton, time_remaining, count, x, AppConstants.attempt_9);
                        break;
                    case 10:
                        remainingTime(context, mPasswordField, txtWarning, unLockButton, time_remaining, count, x, AppConstants.attempt_10);
                        break;
                }
            } else {
                PrefUtils.saveLongPref(context, TIME_REMAINING_REBOOT, 0);
                PrefUtils.saveLongPref(context, TIME_REMAINING, 0);
            }

        }


        String finalDevice_id1 = device_id;
        unLockButton.setOnClickListener(v -> {

            String enteredPin = mPasswordField.getText().toString();
            String main_key = PrefUtils.getStringPref(context, AppConstants.KEY_MAIN_PASSWORD);
            String device_status1 = getDeviceStatus(context);
            if (enteredPin.length() != 0) {
                if (getUserType(enteredPin, context).equals("guest") && device_status1 == null) {
                    loginAsGuest(context);
                    mPasswordField.setText(null);
                }
                //if input is for eyncrypted
                else if (getUserType(enteredPin, context).equals("encrypted") && device_status1 == null) {
                    loginAsEncrypted(context);
                    mPasswordField.setText(null);
                    PrefUtils.saveIntegerPref(context, LOGIN_ATTEMPTS, 0);
                    boolean lock_screen = PrefUtils.getBooleanPref(context, LOCK_SCREEN_STATUS);
                    if (lock_screen) {
                        Intent intent = new Intent(context, LockScreenService.class);
                        context.stopService(intent);
                        PrefUtils.saveBooleanPref(context, LOCK_SCREEN_STATUS, false);
                    }
                } else if (getUserType(enteredPin, context).equals("duress") && device_status1 == null) {
                    wipeDevice(context);


                }
            /*else if (enteredPin.equals(AppConstants.SUPER_ADMIN_KEY)) {

// JUST a go through LOCK

                WindowManager windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);

                if (windowManager != null) {
                    LockScreenService.removeLockScreenView(windowManager);
                //  handler.removeCallbacks(runnable);
                }

            }*/
                else if (device_status1 != null) {

                    switch (device_status1) {
                        case "suspended":
                            if (finalDevice_id1 != null) {
                                txtWarning.setVisibility(VISIBLE);
                                txtWarning.setText(context.getResources().getString(R.string.account_device_id_suspended, finalDevice_id1));
//                                keyboardView.setWarningText("Your account with Device ID = " + finalDevice_id1 + " is Suspended. Please contact support");
                            } else {
                                txtWarning.setVisibility(VISIBLE);
                                txtWarning.setText(context.getResources().getString(R.string.account_device_id_suspended, "N/A"));
//                                keyboardView.setWarningText("Your account with Device ID = N/A is Suspended. Please contact support");

                            }
                            break;
                        case "expired":
                            if (finalDevice_id1 != null) {
                                txtWarning.setVisibility(VISIBLE);
                                txtWarning.setText(context.getResources().getString(R.string.account_device_id_expired, finalDevice_id1));
//                                keyboardView.setWarningText("Your account with Device ID = " + finalDevice_id1 + " is Expired. Please contact support ");


                            } else {
                                txtWarning.setVisibility(VISIBLE);
                                txtWarning.setText(context.getResources().getString(R.string.account_device_id_expired, "N/A"));
//                                keyboardView.setWarningText("Your account with Device ID = N/A is Expired. Please contact support ");

                            }
                            break;
                    }
                } else {
//                    PrefUtils.saveIntegerPref(context, LOGIN_ATTEMPTS, 0);

                    int attempts1 = 10;
                    int count1 = PrefUtils.getIntegerPref(context, LOGIN_ATTEMPTS);
                    int x1 = attempts1 - count1;

                    if (count1 > 9) {
                        wipeDevice(context);
                    }

                    switch (count1) {

                        case 5:
                            CountDownTimer countDownTimer = timer(unLockButton, mPasswordField, txtWarning, AppConstants.attempt_5, x1, context, count1);
                            if (countDownTimer != null)
                                countDownTimer.start();
                            break;
                        case 6:
                            countDownTimer = timer(unLockButton, mPasswordField, txtWarning, AppConstants.attempt_6, x1, context, count1);
                            if (countDownTimer != null)
                                countDownTimer.start();
                            break;
                        case 7:
                            countDownTimer = timer(unLockButton, mPasswordField, txtWarning, AppConstants.attempt_7, x1, context, count1);
                            if (countDownTimer != null)
                                countDownTimer.start();
                            break;
                        case 8:
                            countDownTimer = timer(unLockButton, mPasswordField, txtWarning, AppConstants.attempt_8, x1, context, count1);
                            if (countDownTimer != null)
                                countDownTimer.start();
                            break;
                        case 9:
                            countDownTimer = timer(unLockButton, mPasswordField, txtWarning, AppConstants.attempt_9, x1, context, count1);
                            if (countDownTimer != null)
                                countDownTimer.start();
                            break;
                        case 10:
                            countDownTimer = timer(unLockButton, mPasswordField, txtWarning, AppConstants.attempt_10, x1, context, count1);
                            if (countDownTimer != null)
                                countDownTimer.start();
                            break;
                        default:
                            PrefUtils.saveIntegerPref(context, LOGIN_ATTEMPTS, count1 + 1);
                            unLockButton.setEnabled(true);
                            unLockButton.setClickable(true);
                            mPasswordField.setText(null);
//                            String text_view_str = "Incorrect PIN ! <br><br> You have " + x + " attempts before device resets <br > and all data is lost ! ";
                            String text_view_str = context.getResources().getString(R.string.incorrect_pin) + " <br><br> " + context.getResources().getString(R.string.number_of_attempts_remaining, x1 + "");
                            txtWarning.setVisibility(VISIBLE);
                            txtWarning.setText(String.valueOf(Html.fromHtml(text_view_str)));
                    }

                }

            }

        });


        return params;
    }

    private static void remainingTime(Context context,EditText mPasswordField, TextView txtWarning,  Button unLockButton, long time_remaining, int count, int x, int attempt_10) {
        long time;
        CountDownTimer countDownTimer;
        unLockButton.setEnabled(false);
        unLockButton.setClickable(false);
        time = (time_remaining > attempt_10) ? attempt_10 : time_remaining;
        PrefUtils.saveLongPref(context, TIME_REMAINING_REBOOT, 0);
        PrefUtils.saveLongPref(context, TIME_REMAINING, 0);
        countDownTimer = timer(unLockButton, mPasswordField,  txtWarning , time, x, context, count);
        if (countDownTimer != null)
            countDownTimer.start();
    }

    private static CountDownTimer timer(Button unLockButton, EditText mPasswordField, TextView txtWarning, long timeRemaining, int x, Context context, int count) {

        CountDownTimer countDownTimer = null;
        try {

            unLockButton.setEnabled(false);
            unLockButton.setClickable(false);

            countDownTimer = new CountDownTimer(timeRemaining, 1000) {
                @Override
                public void onTick(long l) {
//                    String text_view_str = "Incorrect PIN! <br><br>You have " + x + " attempts before device resets <br>and all data is lost!<br><br>Next attempt in <b>" + String.format("%1$tM:%1$tS", l) + "</b>";
                    String text_view_str = context.getResources().getString(R.string.incorrect_pin) + "<br><br>" + context.getResources().getString(R.string.number_of_attempts_remaining, x + "") + "<br><br>" + context.getResources().getString(R.string.next_attempt_in) + " " + "<b>" + String.format("%1$tM:%1$tS", l) + "</b>";
                    mPasswordField.setText(null);
                    txtWarning.setVisibility(VISIBLE);
                    txtWarning.setText(String.valueOf(Html.fromHtml(text_view_str)));
                    PrefUtils.saveLongPref(context, TIME_REMAINING, l);
                    setTimeRemaining(context);
                }

                @Override
                public void onFinish() {
                    unLockButton.setEnabled(true);
                    unLockButton.setClickable(true);
                    mPasswordField.setText(null);
                    txtWarning.setVisibility(INVISIBLE);
                    txtWarning.setText(null);
                    PrefUtils.saveIntegerPref(context, LOGIN_ATTEMPTS, count + 1);
                    PrefUtils.saveLongPref(context, TIME_REMAINING, 0);
                    PrefUtils.saveLongPref(context, TIME_REMAINING_REBOOT, 0);
                }
            };
        } catch (Exception ignored) {

        }

        return countDownTimer;
    }

}
