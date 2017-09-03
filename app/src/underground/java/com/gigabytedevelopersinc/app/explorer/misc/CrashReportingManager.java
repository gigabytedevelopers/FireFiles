package com.gigabytedevelopersinc.app.explorer.misc;

import com.gigabytedevelopersinc.app.explorer.BuildConfig;

/**
 * Created by Emmanuel Nwokoma on 23/05/17.
 */

public class CrashReportingManager {

    public static void logException(Exception e) {
        logException(e, false);
    }

    public static void logException(Exception e, boolean log) {
        if(BuildConfig.DEBUG){
            e.printStackTrace();
        } else if(log) {
        }
    }

    public static void log(String s) {
    }

    public static void log(String tag, String s) {
    }
}
