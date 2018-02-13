package com.gigabytedevelopersinc.app.explorer.misc;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;


public class ViewCompat {
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressWarnings("deprecation")
	public static void setBackground(View view, Drawable drawable){
		if(Utils.hasJellyBean()){
			view.setBackground(drawable);
		}
		else{
			view.setBackgroundDrawable(drawable);
		}
	}
}
