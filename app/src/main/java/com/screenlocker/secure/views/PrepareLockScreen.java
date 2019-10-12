package com.screenlocker.secure.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.screenlocker.secure.R;
import com.screenlocker.secure.socket.receiver.DeviceStatusReceiver;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.views.patternlock.PatternLockView;
import com.screenlocker.secure.views.patternlock.listener.PatternLockViewListener;
import com.screenlocker.secure.views.patternlock.utils.PatternLockUtils;

import java.util.List;

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
import static com.screenlocker.secure.utils.AppConstants.LOGIN_ATTEMPTS;
import static com.screenlocker.secure.utils.AppConstants.OFFLINE_DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.TIME_REMAINING;
import static com.screenlocker.secure.utils.AppConstants.TIME_REMAINING_REBOOT;
import static com.screenlocker.secure.utils.CommonUtils.getTimeRemaining;
import static com.screenlocker.secure.utils.CommonUtils.getTimeString;
import static com.screenlocker.secure.utils.CommonUtils.setTimeRemaining;

/**
 * @author Muhammad Nadeem
 * @Date 8/3/2019.
 */
public class PrepareLockScreen {

    private static boolean isClockTicking = false;

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
            rootView.setBackgroundResource(R.raw._1232);

        } else {
            try {
                rootView.setBackgroundResource(Integer.parseInt(bg));
            } catch (RuntimeException e) {
                rootView.setBackgroundResource(R.raw._1232);
            }
        }

        ImageView unLockButton = keypadView.findViewById(R.id.t9_unlock);
        EditText mPasswordField = keypadView.findViewById(R.id.password_field);
        String device_id = PrefUtils.getStringPref(context, DEVICE_ID);
        final PatternLockView mPatternLockView = keypadView.findViewById(R.id.patternLock);

        mPatternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {
            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {
            }

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {
                String device_status = getDeviceStatus(context);
                if (pattern.size() == 1) {
                    mPasswordField.append(String.valueOf(pattern.get(0).getRandom()));
                    mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                    mPatternLockView.clearPattern();
                    return;
                } else if (pattern.size() > 1 && pattern.size() < 4) {
                    mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                    Toast.makeText(context, "Pattern too Short", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(() -> mPatternLockView.clearPattern(), 500);
                }
                String patternString = PatternLockUtils.patternToString(mPatternLockView, pattern);
                boolean clearance;
                if (device_status == null) {
                    clearance = false;
                } else {
                    clearance = device_status.equals("suspended") || device_status.equals("expired");

                }

                if (patternString.equals(PrefUtils.getStringPref(context, AppConstants.GUEST_PATTERN))) {
                    if (clearance) {
                        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                        new Handler().postDelayed(() -> {
                            mPatternLockView.clearPattern();
                            chatLogin(context);
                            mPasswordField.setText(null);
                        }, 150);
                    } else {
                        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                        new Handler().postDelayed(() -> {
                            mPatternLockView.clearPattern();
                            loginAsGuest(context);
                            mPasswordField.setText(null);
                        }, 150);
                    }

                    return;
                } else if (patternString.equals(PrefUtils.getStringPref(context, AppConstants.ENCRYPT_PATTERN))) {

                    if (clearance) {
                        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                        new Handler().postDelayed(() -> {
                            mPatternLockView.clearPattern();
                            chatLogin(context);
                            mPasswordField.setText(null);
                        }, 150);
                    } else {
                        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                        new Handler().postDelayed(() -> {
                            mPatternLockView.clearPattern();
                            loginAsEncrypted(context);
                            mPasswordField.setText(null);
                        }, 150);
                    }
                    return;
                } else if (patternString.equals(PrefUtils.getStringPref(context, AppConstants.DURESS_PATTERN))) {
                    if (clearance) {
                        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                        new Handler().postDelayed(() -> {
                            mPatternLockView.clearPattern();
                            chatLogin(context);
                        }, 150);
                    } else {
                        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                        new Handler().postDelayed(() -> {
                            mPatternLockView.clearPattern();
                            if (!wipeDevice(context)) {
                                Toast.makeText(context, "Cannot Wipe Device for now.", Toast.LENGTH_SHORT).show();
                            }
                        }, 150);
                    }
                    return;
                } else if (device_status != null) {
                    String device_id = PrefUtils.getStringPref(context, DEVICE_ID);
                    setDeviceId(context, txtWarning, device_id, mPatternLockView, device_status);
                    if (clearance) {
                        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                        new Handler().postDelayed(() -> {
                            mPatternLockView.clearPattern();
                        }, 500);
                    }
                } else {
                    mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                    new Handler().postDelayed(() -> {
                        mPatternLockView.clearPattern();
                        wrongAttempt(context, txtWarning, unLockButton, mPatternLockView, mPasswordField);
                    }, 500);
                }
            }

            @Override
            public void onCleared() {

            }
        });


        if (device_id == null) {
            device_id = PrefUtils.getStringPref(context, OFFLINE_DEVICE_ID);
        }

        final String device_status = getDeviceStatus(context);

        if (device_status == null) {
//            keyboardView.clearWaringText();
            txtWarning.setVisibility(INVISIBLE);
            txtWarning.setText(null);
            mPatternLockView.setInputEnabled(true);
        }


        if (device_status != null) {
            setDeviceId(context, txtWarning, device_id, mPatternLockView, device_status);
        }


        deviceStatusReceiver.setListener(status -> {
            if (status == null) {

                if (!isClockTicking) {
                    txtWarning.setVisibility(INVISIBLE);
                    txtWarning.setText(null);
                    mPatternLockView.setInputEnabled(true);
                }

            } else {
                String dev_id = PrefUtils.getStringPref(context, DEVICE_ID);
                if (status.equals("suspended")) {
                    if (dev_id != null) {
                        txtWarning.setVisibility(VISIBLE);
                        txtWarning.setText(context.getResources().getString(R.string.account_device_id_suspended, dev_id));
                        // mPatternLockView.setInputEnabled(false);
//                        keyboardView.setWarningText("Your account with Device ID = " + finalDevice_id + " is Suspended. Please contact support");


                    } else {
                        txtWarning.setVisibility(VISIBLE);
                        txtWarning.setText(context.getResources().getString(R.string.account_device_id_suspended, "N/A"));
                        //mPatternLockView.setInputEnabled(false);

                    }
                } else if (status.equals("expired")) {
                    if (dev_id != null) {
                        txtWarning.setVisibility(VISIBLE);
                        txtWarning.setText(context.getResources().getString(R.string.account_device_id_expired, dev_id));
                        //mPatternLockView.setInputEnabled(false);

                    } else {
                        txtWarning.setVisibility(VISIBLE);
                        txtWarning.setText(context.getResources().getString(R.string.account_device_id_expired, "N/A"));
                        //mPatternLockView.setInputEnabled(false);


                    }
                } else if (status.equals("unlinked")) {
                    if (dev_id != null) {
                        txtWarning.setVisibility(VISIBLE);
                        txtWarning.setText(context.getResources().getString(R.string.account_device_id_unlinked, dev_id));
                        mPatternLockView.setInputEnabled(false);
                    } else {
                        txtWarning.setVisibility(VISIBLE);
                        txtWarning.setText(context.getResources().getString(R.string.account_device_id_unlinked, "N/A"));
                        mPatternLockView.setInputEnabled(false);
                    }
//                                keyboardView.setWarningText("Your account with Device ID = " + finalDevice_id1 + " is Expired. Please contact support ");
                } else if (status.equals("flagged")) {
                    txtWarning.setVisibility(VISIBLE);
                    txtWarning.setText(context.getResources().getString(R.string.account_device_id_flagged));
                    mPatternLockView.setInputEnabled(false);
                }else if(status.equals("transfered"))
                {
                    if (dev_id != null) {
                        txtWarning.setVisibility(VISIBLE);
                        txtWarning.setText(context.getResources().getString(R.string.account_device_id_transferred, dev_id));
                        mPatternLockView.setInputEnabled(false);
                    } else {
                        txtWarning.setVisibility(VISIBLE);
                        txtWarning.setText(context.getResources().getString(R.string.account_device_id_transferred, "N/A"));
                        mPatternLockView.setInputEnabled(false);
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
        LinearLayout supportButton = keypadView.findViewById(R.id.t9_key_support);
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
                        remainingTime(context, mPasswordField, mPatternLockView, txtWarning, unLockButton, time_remaining, count, x, AppConstants.attempt_5);
                        break;
                    case 6:
                        remainingTime(context, mPasswordField, mPatternLockView, txtWarning, unLockButton, time_remaining, count, x, AppConstants.attempt_6);
                        break;
                    case 7:
                        remainingTime(context, mPasswordField, mPatternLockView, txtWarning, unLockButton, time_remaining, count, x, AppConstants.attempt_7);
                        break;
                    case 8:
                        remainingTime(context, mPasswordField, mPatternLockView, txtWarning, unLockButton, time_remaining, count, x, AppConstants.attempt_8);
                        break;
                    case 9:
                        remainingTime(context, mPasswordField, mPatternLockView, txtWarning, unLockButton, time_remaining, count, x, AppConstants.attempt_9);
                        break;
                    case 10:
                        remainingTime(context, mPasswordField, mPatternLockView, txtWarning, unLockButton, time_remaining, count, x, AppConstants.attempt_10);
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
            String device_status1 = getDeviceStatus(context);
            boolean clearance;
            if (device_status1 == null) {
                clearance = false;
            } else {
                clearance = device_status1.equals("suspended") || device_status1.equals("expired");

            }

            if (enteredPin.length() != 0) {
                if (getUserType(enteredPin, context).equals("guest")) {
                    if (clearance) {
                        chatLogin(context);
                        mPasswordField.setText(null);
                    } else {
                        loginAsGuest(context);
                        mPasswordField.setText(null);
                    }
                }
                //if input is for eyncrypted
                else if (getUserType(enteredPin, context).equals("encrypted")) {
                    if (!clearance) {
                        loginAsEncrypted(context);
                        mPasswordField.setText(null);
                    } else {
                        chatLogin(context);
                        mPasswordField.setText(null);
                    }

                } else if (getUserType(enteredPin, context).equals("duress")) {
                    if (!clearance)
                        if (!wipeDevice(context)) {
                            Toast.makeText(context, "Cannot Wipe Device for now.", Toast.LENGTH_SHORT).show();
                        } else chatLogin(context);

                } else if (device_status1 != null) {
                    if (clearance) {
                        mPasswordField.setText(null);
                    }
                    setDeviceId(context, txtWarning, finalDevice_id1, mPatternLockView, device_status1);
                } else {
//                    PrefUtils.saveIntegerPref(context, LOGIN_ATTEMPTS, 0);

                    wrongAttempt(context, txtWarning, unLockButton, mPatternLockView, mPasswordField);

                }

            }

        });


        return params;
    }

    public static void setDeviceId(Context context, TextView txtWarning, String finalDevice_id1, PatternLockView patternLockView, String device_status1) {
        switch (device_status1) {
            case "suspended":
                if (finalDevice_id1 != null) {
                    txtWarning.setVisibility(VISIBLE);
                    txtWarning.setText(context.getResources().getString(R.string.account_device_id_suspended, finalDevice_id1));
                    //patternLockView.setInputEnabled(false);

//                                keyboardView.setWarningText("Your account with Device ID = " + finalDevice_id1 + " is Suspended. Please contact support");
                } else {
                    txtWarning.setVisibility(VISIBLE);
                    txtWarning.setText(context.getResources().getString(R.string.account_device_id_suspended, "N/A"));
                    //patternLockView.setInputEnabled(false);
//                                keyboardView.setWarningText("Your account with Device ID = N/A is Suspended. Please contact support");

                }
                break;
            case "expired":
                if (finalDevice_id1 != null) {
                    txtWarning.setVisibility(VISIBLE);
                    txtWarning.setText(context.getResources().getString(R.string.account_device_id_expired, finalDevice_id1));
                    //patternLockView.setInputEnabled(false);
//                                keyboardView.setWarningText("Your account with Device ID = " + finalDevice_id1 + " is Expired. Please contact support ");


                } else {
                    txtWarning.setVisibility(VISIBLE);
                    txtWarning.setText(context.getResources().getString(R.string.account_device_id_expired, "N/A"));
                    patternLockView.setInputEnabled(false);
//                                keyboardView.setWarningText("Your account with Device ID = N/A is Expired. Please contact support ");

                }
                break;
            case "unlinked":
                if (finalDevice_id1 != null) {
                    txtWarning.setVisibility(VISIBLE);
                    txtWarning.setText(context.getResources().getString(R.string.account_device_id_unlinked, finalDevice_id1));
                    patternLockView.setInputEnabled(false);
                } else {
                    txtWarning.setVisibility(VISIBLE);
                    txtWarning.setText(context.getResources().getString(R.string.account_device_id_unlinked, "N/A"));
                    patternLockView.setInputEnabled(false);
                }
//                                keyboardView.setWarningText("Your account with Device ID = " + finalDevice_id1 + " is Expired. Please contact support ");
                break;
            case "flagged":
                txtWarning.setVisibility(VISIBLE);
                txtWarning.setText(context.getResources().getString(R.string.account_device_id_flagged));
                patternLockView.setInputEnabled(false);
                break;

        }
    }

    private static void wrongAttempt(Context context, TextView txtWarning, ImageView unLockButton, PatternLockView patternLockView, EditText mPasswordField) {
        int attempts1 = 10;
        int count1 = PrefUtils.getIntegerPref(context, LOGIN_ATTEMPTS);
        int x1 = attempts1 - count1;

        if (count1 > 9) {
            wipeDevice(context);
        }

        switch (count1) {

            case 5:
                CountDownTimer countDownTimer = timer(unLockButton, mPasswordField, patternLockView, txtWarning, AppConstants.attempt_5, x1, context, count1);
                if (countDownTimer != null)
                    countDownTimer.start();
                break;
            case 6:
                countDownTimer = timer(unLockButton, mPasswordField, patternLockView, txtWarning, AppConstants.attempt_6, x1, context, count1);
                if (countDownTimer != null)
                    countDownTimer.start();
                break;
            case 7:
                countDownTimer = timer(unLockButton, mPasswordField, patternLockView, txtWarning, AppConstants.attempt_7, x1, context, count1);
                if (countDownTimer != null)
                    countDownTimer.start();
                break;
            case 8:
                countDownTimer = timer(unLockButton, mPasswordField, patternLockView, txtWarning, AppConstants.attempt_8, x1, context, count1);
                if (countDownTimer != null)
                    countDownTimer.start();
                break;
            case 9:
                countDownTimer = timer(unLockButton, mPasswordField, patternLockView, txtWarning, AppConstants.attempt_9, x1, context, count1);
                if (countDownTimer != null)
                    countDownTimer.start();
                break;
            case 10:
                countDownTimer = timer(unLockButton, mPasswordField, patternLockView, txtWarning, AppConstants.attempt_10, x1, context, count1);
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

    private static void remainingTime(Context context, EditText mPasswordField, PatternLockView patternLockView, TextView txtWarning, ImageView unLockButton, long time_remaining, int count, int x, int attempt_10) {
        long time;
        CountDownTimer countDownTimer;
        unLockButton.setEnabled(false);
        unLockButton.setClickable(false);
        patternLockView.setInputEnabled(false);
        time = (time_remaining > attempt_10) ? attempt_10 : time_remaining;
        PrefUtils.saveLongPref(context, TIME_REMAINING_REBOOT, 0);
        PrefUtils.saveLongPref(context, TIME_REMAINING, 0);
        countDownTimer = timer(unLockButton, mPasswordField, patternLockView, txtWarning, time, x, context, count);
        if (countDownTimer != null)
            countDownTimer.start();
    }

    private static CountDownTimer timer(ImageView unLockButton, EditText mPasswordField, PatternLockView patternLockView, TextView txtWarning, long timeRemaining, int x, Context context, int count) {

        CountDownTimer countDownTimer = null;
        try {

            unLockButton.setEnabled(false);
            unLockButton.setClickable(false);
            patternLockView.setInputEnabled(false);

            countDownTimer = new CountDownTimer(timeRemaining, 1000) {
                @Override
                public void onTick(long l) {
//                    String text_view_str = "Incorrect PIN! <br><br>You have " + x + " attempts before device resets <br>and all data is lost!<br><br>Next attempt in <b>" + String.format("%1$tM:%1$tS", l) + "</b>";
                    String text_view_str = context.getResources().getString(R.string.incorrect_pin) + "<br><br>" + context.getResources().getString(R.string.number_of_attempts_remaining, x + "") + "<br><br>" + context.getResources().getString(R.string.next_attempt_in) + " " + "<b>" + getTimeString(l) + "</b>";
                    mPasswordField.setText(null);
                    txtWarning.setVisibility(VISIBLE);
                    txtWarning.setText(String.valueOf(Html.fromHtml(text_view_str)));
                    PrefUtils.saveLongPref(context, TIME_REMAINING, l);
                    setTimeRemaining(context);
                    isClockTicking = true;
                }

                @Override
                public void onFinish() {
                    isClockTicking = false;
                    unLockButton.setEnabled(true);
                    patternLockView.setInputEnabled(true);
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
