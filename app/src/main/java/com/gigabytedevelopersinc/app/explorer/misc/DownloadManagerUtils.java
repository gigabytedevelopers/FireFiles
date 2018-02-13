package com.gigabytedevelopersinc.app.explorer.misc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;

public final class DownloadManagerUtils {

    public static void setAccessAllDownloads(DownloadManager downloadManager) {
    	try {
            Method setAccessAllDownloads = DownloadManager.class.getMethod("setAccessAllDownloads", boolean.class);
			setAccessAllDownloads.invoke(downloadManager, true);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
    }
    
    public static void setOnlyIncludeVisibleInDownloadsUi(Query query){
       	try {
            Method setOnlyIncludeVisibleInDownloadsUi = Query.class.getMethod("setOnlyIncludeVisibleInDownloadsUi", boolean.class);
			setOnlyIncludeVisibleInDownloadsUi.invoke(query, true);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
    }
}