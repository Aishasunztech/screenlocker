package com.screenlocker.secure.utils;

public class AppConstants {


    public static final String KEY_SHOWN_CHOOSER = "shown_chooser";

    public static final String KEY_GUEST = "guest";
    public static final String KEY_GUEST_PASSWORD = "guest_password";
    public static final String KEY_MAIN_PASSWORD = "main_password";
    public static final String KEY_DURESS_PASSWORD = "duress_password";
    public static final String KEY_GUEST_IMAGE = "guest_image";
    public static final String KEY_MAIN_IMAGE = "main_image";
    public static final String CURRENT_KEY = "current_key";
    public static final String DATABASE_NAME = "app_db";
    public static final String KEY_MAIN = "main";
    public static final String KEY_DURESS = "duress";
    public static final int LAUNCHER_GRID_SPAN = 3;
    public static final String BROADCAST_ACTION = "intentKey";

    public static final String BROADCAST_APPS_ACTION = "appInfoKey";


    public static final String BROADCAST_KEY = "key";
    public static final String SUPER_ADMIN_KEY = "11111";

    public static final String KEY_SERVICE_RUNNING = "lock_service";
    public static final String KEY_GUEST_ALL = "guest_all";
    public static final String KEY_DISABLE_ALL = "disable_all";
    public static final String KEY_ENCRYPTED_ALL = "encrypted_all";
    public static final String KEY_SHUT_DOWN = "shut_down";
    public static final String KEY_ADMIN_ALLOWED = "admin_allowed";
    public static final String VALUE_SHUT_DOWN_FALSE = "false_shut_down";
    public static final String VALUE_SHUT_DOWN_TRUE = "true_shut_down";
    public static final String KEY_START_DATE = "start_date";
    public static final String KEY_END_DATE = "end_date";
    public static final String KEY_IMEI_NUMBER = "imei_number";
    //..........................................................//
    public static final String API_KEY_IMEI = "imei";
    public static final String API_KEY_START_DATE = "stdate";

    public static final String KEY_TRIAL_ENDED = "trial_ended";
    public static final String KEY_DEVICE_LINKED = "device_linked";

    public static final String KEY_DEVICE_ACTIVE = "device_active";

    public static final String VALUE_ACTIVE = "active";


    public static final String KEY_DEVICE_MSG = "message";

    public static final String KEY_ALLOW_SCREENSHOT = "allow_screenshot";
    public static final String KEY_CODE_PASSWORD = "code_password";
    public static final String KEY_CODE = "code";
    public static final String DEFAULT_PASSWORD = "0000";

    public static final String KEY_ENABLE_SCREENSHOT = "enable_screen_shot";
    public static final String VALUE_SCREENSHOT_ENABLE = "enable";
    public static final String VALUE_SCREENSHOT_DISABLE = "disable";

    public static final String KEY_DISABLE_CALLS = "disable_calls";
    public static final String DEVICE_ID = "device_id";
    public static final String TOKEN = "token";


    public static final String LOGO_URL = "http://134.209.124.196:3000/users/getFile/";
    public static String STAGING_BASE_URL = "http://134.209.124.196:3000/mobile/";//live server
//    String STAGING_BASE_URL = "http://10.0.2.2:3000/mobile/";//for localhost emulator
//    public static final String STAGING_BASE_URL = "http://192.168.18.219:3000/mobile/";//for localhost real device

    //  SocketUtils constants
    public static final String SOCKET_SERVER_URL = "http://134.209.124.196:3000";//live
//    public static final String SOCKET_SERVER_URL = "http://192.168.18.219:3000";//local host with real device


    public static final String RESPONSE_TO_SERVER = "sendApps_";
    public static final String SEND_APPS = "sendApps_";
    public static final String SEND_SETTINGS = "sendSettings_";
    public static final String GET_SYNC_STATUS = "get_sync_status_";
    public static final String GET_APPLIED_SETTINGS = "get_applied_settings_";
    public static final String IS_SYNCED = "is_synced";
    public static final String ICON_BASE_URL = "http://46.101.243.120:3000/users/getFile/";
    public static final String APPS_SETTING_CHANGE = "APPS_SETTING_CHANGE";
    public static final String SETTINGS_CHANGE = "SETTINGS_CHANGE";
    public static final String DEVICE_STATUS = "device_status_";
    public static final String LOCK_SCREEN_STATUS = "lock_screen_status";
    public static final String DEVICE_STATUS_CHANGE_RECEIVER = "com.vortexlocker.app.DEVICE_STATUS_CHANGE";
    public static final String DB_STATUS = "db_status";
    public static final String SETTINGS_APPLIED_STATUS = "settings_applied_status_";
    public static final String DEFAULT_MAIN_PASS = "5678";
    public static final String DEFAULT_GUEST_PASS = "1234";


    // MDM

    public static final String KEY_DEALER_ID = "dId";
    public static final String KEY_CONNECTED_ID = "connectedDid";

    public static String AUTH_TOKEN = "com.secureportal.barryapp.utils.authorization_token";
    public static String AUTO_LOGIN_PIN = "com.secureportal.barryapp.utils.auto_login_pin";
    public static String TEMP_AUTO_LOGIN_PIN = "com.secureportal.barryapp.utils.temp_auto_login_pin";
    public static final String TOKEN_INVALID = "TOKEN_INVALID";
    public static final String TOKEN_EXPIRED = "TOKEN_EXPIRED";
    public static final String TOKEN_NOT_PROVIDED = "TOKEN_NOT_PROVIDED";

    public static final String DEVICE_NEW = "-1";
    public static final String DEVICE_PENDING = "0";
    public static final String DEVICE_LINKED = "1";
    public static final String DEVICE_LINKED_STATUS = "device_linked_status";


    //Stepps

    public static final String TOUR_STATUS = "tour_status";
    public static final String CURRENT_STEP = "current_step";


    //Permissions codes

    public static final int CODE_WRITE_SETTINGS_PERMISSION = 2;
    public static final int RESULT_ENABLE = 11;
    public static final int PERMISSION_REQUEST_READ_PHONE_STATE = 100;
    public static final int REQUEST_READ_PHONE_STATE = 2020;

    // expiry date
    public static final String VALUE_EXPIRED = "expired";
    // pgp id
    public static final String PGP_EMAIL = "pgp_email";
    // chat id
    public static final String CHAT_ID = "chat_id";
    // sim id
    public static final String SIM_ID = "sim_id";


    //login attempts
    public static final String LOGIN_ATTEMPTS = "login_attempts";
    public static final String TIME_REMAINING = "time_remaining";
    public static final String TIME_REMAINING_REBOOT = "time_remaining_reboot";

    //launcher settings status

    public static final String SETTINGS_STATUS = "settings_status";

    // unique name for SecureSettings extension
    public static final String SECURE_SETTINGS_UNIQUE = "com.secureSetting.SecureSettingsMainSecure Settings";
    // package name for SecureSettings
    public static final String SECURE_SETTINGS_PACKAGE = "com.secureSetting.SecureSettingsMain";


    // secure app

    public static final int LOCATION_SETTINGS_CODE = 111;
    public static final int RC_PERMISSION = 123;


}
