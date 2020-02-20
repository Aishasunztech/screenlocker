// IPCWithSL.aidl
package com.secure.launcher;


interface IPCWithSL {


    /**
    * This method gets chat ID from SL application
    * return chat id of the  device
    */
    String getChatId();

    /**
     * return device id of the currnet device
     */
    String getDeviceId();

    /**
    *resturn pgp email assigned to device
    */

    String getPGPEmail();

    /*
    *query if package suspended
    */

    boolean isPackageSuspended(String packageName);

    /*
    *get if package is suspended
    */

    int getWhiteLabelType();
}
