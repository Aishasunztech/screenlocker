package com.screenlocker.secure.utils;

import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;

public class AppConstants {

    public static final long ONE_DAY_INTERVAL = 15 * 60 * 1000L;
    public static final long FIVE_MIN_INTERVAL = 5 * 60 * 1000L;
    public static final String KEY_SHOWN_CHOOSER = "shown_chooser";

    public static final String KEY_THEME = "is_dark";

    public static final String ALLOW_GUEST_ALL = "allow_guest_all";
    public static final String SOME_ERROR = "Some Error Occurred";
    public static final String SEVER_NOT_RESPONSIVE = MyApplication.getAppContext().getResources().getString(R.string.server_error);
    public static final String SIM_0_ICCID = "sim_0_iccid";
    public static final String SIM_1_ICCID = "sim_1_iccid";
    public static final String ALLOW_ENCRYPTED_ALL = "allow_encrypted_all";
    public static final String KEY_GUEST = "guest";
    public static final String KEY_ENCRYPTED = "encrypted";
    public static final String KEY_ENABLE = "key_enable";
    public static final String INSTALLED_PACKAGES = "installed_packages";
    public static final String UNINSTALLED_PACKAGES = "uninstalled_packages";
    public static final String KEY_GUEST_PASSWORD = "guest_password";
    public static final String KEY_SUPPORT_PASSWORD = "suppoert_password";
    public static final String KEY_MAIN_PASSWORD = "main_password";
    public static final String KEY_DURESS_PASSWORD = "duress_password";
    public static final String KEY_GUEST_IMAGE = "guest_image";
    public static final String KEY_SUPPORT_IMAGE = "support_image";
    public static final String KEY_MAIN_IMAGE = "main_image";
    public static final String KEY_LOCK_IMAGE = "lock_image";
    public static final String CURRENT_KEY = "current_key";
    public static final String DATABASE_NAME = "app_db";
    public static final String KEY_MAIN = "main";
    public static final String KEY_DURESS = "duress";
    public static final int LAUNCHER_GRID_SPAN = 3;
    public static final String BROADCAST_ACTION = "intentKey";

    public static final String BROADCAST_APPS_ACTION = "appInfoKey";
    public static final String BROADCAST_DATABASE = "broadcast_database";


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
    public static final String DFAULT_MAC = "default_mac";


    //servers links

   // public static final String URL_1 = "http://api.meshguard.co";//live for mesgaurd

  //  public static final String URL_1 = "https://api.lockmesh.com";//live SL
    public static final String URL_1 = "https://devapi.lockmesh.com";// Dev
    //    public static final String URL_1 = "https://api.titansecureserver.com";//live TL
     //    public static final String URL_1 = "http://192.168.0.119:3000";//local
    public static final String URL_2 = "https://securenet.guru";

    //super admin domain
  //  public static final String SUPER_ADMIN = "http://api.meshguard.co";//live
    public static final String SUPER_ADMIN = "https://devapi.meshguard.co";//dev
    //   public static final String SUPER_ADMIN = "http://192.168.0.121:8042";//local
    public static final String SUPER_END_POINT = "/api/v1/mobile/";


    //End points
    public static final String LOGO_END_POINT = "/users/getFile/";
//    public static final String MOBILE_END_POINT = "/mobile/";
    public static final String MOBILE_END_POINT = "/api/v1/mobile/";

    //available live host
    public static final String LIVE_URL = "live_url";
    //End points

    public static final String LOGO_URL = "http://api.lockmesh.com/users/getFile/";
    //    public static final String STAGING_BASE_URL = "http://api.lockmesh.com/mobile/";//live server
    public static final String STAGING_BASE_URL = "http://192.168.0.120:3000/mobile/";//for localhost real device
//    String STAGING_BASE_URL = "http://10.0.2.2:3000/mobile/";//for localhost emulator

    //  SocketUtils constants
//    public static final String SOCKET_SERVER_URL = "http://api.lockmesh.com";//live
    public static final String SOCKET_SERVER_URL = "http://192.168.0.120:3000";//local host with real device

    public static final String APPS_SENT_STATUS = "apps_sent_status";
    public static final String EXTENSIONS_SENT_STATUS = "extensions_sent_status";
    public static final String SETTINGS_SENT_STATUS = "settings_sent_status";
    public static final String IS_SYNCED = "is_synced";


    public static final String SEND_EXTENSIONS = "sendExtensions_";
    public static final String SEND_APPS = "sendApps_";
    public static final String SEND_SETTINGS = "sendSettings_";


    public static final String GET_SYNC_STATUS = "get_sync_status_";
    public static final String GET_PUSHED_APPS = "get_pushed_apps_";
    public static final String GET_PULLED_APPS = "get_pulled_apps_";
    public static final String SEND_PULLED_APPS_STATUS = "send_pulled_apps_status_";
    public static final String SEND_PUSHED_APPS_STATUS = "send_pushed_apps_status_";
    public static final String FINISHED_PUSHED_APPS = "finished_push_apps_";
    public static final String FINISHED_PULLED_APPS = "finished_pulled_apps_";


    public static final String GET_APPLIED_SETTINGS = "get_applied_settings_";
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

    /*Permissions Strings*/
    public static final String PER_ADMIN = "per_admin";
    public static final String PER_OVERLAY = "per_overlay";
    public static final String PER_MODIFIY = "per_modify";
    public static final String PER_USAGE = "per_usage";
    public static final String PER_UNKNOWN = "per_unknown";
    public static final String PER_RUNTIME = "per_runtime";
    public static final String PER_BATTERY = "per_batteryopt";
    public static final String PER_NOTIFICATION = "per_notification";
    public static final String PERMISSIONS_NUMBER = "permission_numbers";
    public static final String DEF_PAGE_NO = "def_page_no";

    public static final int NOFICATION_REQUEST = 1323;

    // MDM

    public static final String KEY_DEALER_ID = "dId";
    public static final String KEY_CONNECTED_ID = "connectedDid";
    public static final String COPIED_DEVICE_ID = "COPIED_DEVICE_ID";
    public static final String COPIED_LINKED_STATUS = "COPIED_LINKED_STATUS";
    public static final String COPIED_DEVICE_STATUS = "COPIED_DEVICE_STATUS";
    public static final String COPIED_IMEI_1 = "COPIED_IMEI_1";
    public static final String COPIED_IMEI_2 = "COPIED_IMEI_2";
    public static final String COPIED_URL = "COPIED_URL";

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
    public static final int CODE_UNKNOWN_RESOURCES = 225;
    public static final int CODE_MODIFY_SYSTEMS_STATE = 223;
    public static final int CODE_USAGE_ACCESS = 224;
    public static final int CODE_BATERY_OPTIMIZATION = 2255;
    public static final int CODE_LAUNCHER = 2265;

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


    //unique name for secure clear
    public static final String SECURE_CLEAR_UNIQUE = "com.secureClear.SecureClearActivitySecure Clear";
    public static final String SECURE_CLEAR_PACKAGE = "com.secureClear.SecureClearActivity";
    //unige name for Support Admin
    public static final String SUPPORT_UNIQUE = "com.contactSupport.ChatActivityContact Support";
    public static final String SUPPORT_PACKAGE = "com.contactSupport.ChatActivity";

    //unique name for secure market
    public static final String SECURE_MARKET_UNIQUE = "com.secureMarket.SecureMarketActivitySecure Market";
    public static final String SECURE_MARKET_PACKAGE = "com.secureMarket.SecureMarketActivity";

    // secure app

    public static final int LOCATION_SETTINGS_CODE = 111;
    public static final int RC_PERMISSION = 123;


    public static final String EXTENSION_GUEST_CHECKED = "ExtensionGuest";
    public static final String EXTENSION_ENCRYPTED_CHECKED = "ExtensionEncrypted";


    public static final String KEY_DATABASE_CHANGE = "database_change";


    public static final String SECURE_SETTINGS_CHANGE = "secure_settings_change";


    /*
     * Screen Locker
     * */

//    public static int attempt_5 = 1;
//    public static int attempt_6 = 5;
//    public static int attempt_7 = 10;
//    public static int attempt_8 = 15;
//    public static int attempt_9 = 30;
//    public static int attempt_10 = 30;

    /*
     *Titan Locker
     * */

    public static int attempt_5 = 1;
    public static int attempt_6 = 1;
    public static int attempt_7 = 3;
    public static int attempt_8 = 5;
    public static int attempt_9 = 5;
    public static int attempt_10 = 5;


    public static final String IMEI_CHANGED = "imeiChanged";
    public static final String REBOOT_STATUS = "rebootStatus";


    // acure xlear


//
//    DEVICE_ACTIVATED: "Active",
//    DEVICE_SUSPENDED: "Suspended",
//    DEVICE_EXPIRED: "Expired",
//    DEVICE_UNLINKED: "Unlinked",
//    DEVICE_PENDING_ACTIVATION: "Pending activation",


//    NEW_DEVICE: "new device",


    // push pulled apps

    public static final String ACTION_PUSH_APPS = "action_push_apps";
    public static final String ACTION_PULL_APPS = "action_pull_apps";
    public static final String APPS_HASH_MAP = "apps_hash_map";
    public static final String DELETE_HASH_MAP = "delete_hash_map";
    public static final String WRITE_IMEI = "write_imei_";
    public static final String IMEI_APPLIED = "imei_applied_";
    public static final String IMEI_HISTORY = "imei_changed_";
    public static final String LOAD_POLICY = "load_policy_";
    public static final String GET_POLICY = "get_policy_";
    public static final String LOADING_POLICY = "loading_policy";
    public static final String PENDING_FINISH_DIALOG = "pending_status";


    public static final String IMEI1 = "imei1";
    public static final String IMEI2 = "imei2";


    public static final String SYSTEM_LOGIN_TOKEN = "system_login_token";

    public static final String FORCE_UPDATE_CHECK = "force_update_check_";


    public static final String FINISH_POLICY_PUSH_APPS = "finish_policy_push_apps_";
    public static final String FINISH_POLICY_APPS = "finish_policy_apps_";
    public static final String FINISH_POLICY_SETTINGS = "finish_policy_settings_";
    public static final String FINISH_POLICY_EXTENSIONS = "finish_policy_extensions_";
    public static final String FINISH_POLICY = "finish_policy_";
    public static final String POLICY_NAME = "policy_name";


    public static String INSTALLING_APP_NAME;
    public static String INSTALLING_APP_PACKAGE;


    // Device Status and errors
    public static final String ACTIVE = "Active";
    public static final String EXPIRED = "Expired";
    public static final String SUSPENDED = "Suspended";
    public static final String TRIAL = "Trial";
    public static final String PENDING = "Pending activation";
    public static final String NEW_DEVICE = "new device";
    public static final String UNLINKED_DEVICE = "Unlinked";

    public static final String DUPLICATE_MAC = "duplicate_mac";
    public static final String DUPLICATE_SERIAL = "duplicate_serial";
    public static final String DUPLICATE_MAC_AND_SERIAL = "duplicate_mac_and_serial";
    public static final String DEALER_NOT_FOUND = "dealer_not_found";


    public static final String DEVICE_STATUS_KEY = "device_status_key";
    public static final String PENDING_STATE = "pending_state";
    public static final String ACTIVE_STATE = "active_state";

    public static final String HOST_ERROR = "Some Error Occurred!";


    public static volatile boolean isProgress = false;
    public static volatile boolean result = false;

    public static final String ALARM_TIME_COMPLETED = "com.vortexlocker.app.alarm_time_completed";

    public static final String IS_EMERGANCY = "is_emergency";

    public static final String OFFLINE_DEVICE_ID = "of_device_id";


    public static final String TEXT1 = "Your account with Device ID = ";
    public static final String LANGUAGE_PREF = "language_pref";

}
