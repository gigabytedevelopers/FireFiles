package com.gigabytedevelopersinc.app.explorer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.gigabytedevelopersinc.app.explorer.misc.AnalyticsManager;
import com.gigabytedevelopersinc.app.explorer.misc.SystemBarTintManager;
import com.gigabytedevelopersinc.app.explorer.misc.Utils;
import com.gigabytedevelopersinc.app.explorer.pro.BuildConfig;
import com.gigabytedevelopersinc.app.explorer.pro.R;
import com.gigabytedevelopersinc.app.explorer.setting.SettingsActivity;
import com.gigabytedevelopersinc.app.explorer.misc.ColorUtils;

import static com.gigabytedevelopersinc.app.explorer.DocumentsActivity.getStatusBarHeight;
import static com.gigabytedevelopersinc.app.explorer.misc.Utils.getSuffix;
import static com.gigabytedevelopersinc.app.explorer.misc.Utils.openFeedback;
import static com.gigabytedevelopersinc.app.explorer.misc.Utils.openPlaystore;
import static com.gigabytedevelopersinc.app.explorer.misc.ColorUtils.MIN_CONTRAST_TITLE_TEXT;

public class AboutActivity extends ActionBarActivity implements View.OnClickListener {

	public static final String TAG = "About";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(Utils.hasKitKat() && !Utils.hasLollipop()){
			setTheme(R.style.Theme_Document_Translucent);
		}
		setContentView(R.layout.activity_about);

		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mToolbar.setTitleTextAppearance(this, R.style.TextAppearance_AppCompat_Widget_ActionBar_Title);
		if(Utils.hasKitKat() && !Utils.hasLollipop()) {
			//((LinearLayout.LayoutParams) mToolbar.getLayoutParams()).setMargins(0, getStatusBarHeight(this), 0, 0);
			mToolbar.setPadding(0, getStatusBarHeight(this), 0, 0);
		}
		int color = SettingsActivity.getPrimaryColor();
		mToolbar.setBackgroundColor(color);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(null);
		setUpDefaultStatusBar();

		initControls();
	}

	@Override
	public String getTag() {
		return TAG;
	}

	private void initControls() {

		int accentColor = ColorUtils.getTextColorForBackground(SettingsActivity.getPrimaryColor(),
				MIN_CONTRAST_TITLE_TEXT);
		TextView logo = (TextView)findViewById(R.id.logo);
		logo.setTextColor(accentColor);
		String header = logo.getText() + getSuffix() + " v" + BuildConfig.VERSION_NAME;
		logo.setText(header);

		TextView action_rate = (TextView)findViewById(R.id.action_rate);
		TextView action_support = (TextView)findViewById(R.id.action_support);
		TextView action_share = (TextView)findViewById(R.id.action_share);
		TextView action_feedback = (TextView)findViewById(R.id.action_feedback);

		action_rate.setOnClickListener(this);
		action_support.setOnClickListener(this);
		action_share.setOnClickListener(this);
		action_feedback.setOnClickListener(this);

		if(Utils.isOtherBuild()){
			action_rate.setVisibility(View.GONE);
			action_support.setVisibility(View.GONE);
		} else if(DocumentsApplication.isTelevision()){
			action_share.setVisibility(View.GONE);
			action_feedback.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

    @Override
    public void startActivity(Intent intent) {
        if(Utils.isIntentAvailable(this, intent)) {
            super.startActivity(intent);
        }
    }

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.action_github:
				startActivity(new Intent("android.intent.action.VIEW",
						Uri.parse("https://github.com/gigabytedevelopers")));
				break;
			case R.id.action_gplus:
				startActivity(new Intent("android.intent.action.VIEW",
						Uri.parse("https://plus.google.com/+GigabyteDevelopers")));
				break;
			case R.id.action_twitter:
				startActivity(new Intent("android.intent.action.VIEW",
						Uri.parse("https://twitter.com/gigabytedevsinc")));
				break;
			case R.id.action_feedback:
				//Crashlytics.getInstance().crash();
				openFeedback(this);
				break;
			case R.id.action_rate:
				openPlaystore(this);
				AnalyticsManager.logEvent("app_rate");
				break;
			case R.id.action_support:
				Intent intentMarketAll = new Intent("android.intent.action.VIEW");
				intentMarketAll.setData(Utils.getAppStoreUri());
				startActivity(intentMarketAll);
				AnalyticsManager.logEvent("app_love");
				break;
			case R.id.action_share:

				String shareText = "I found this file mananger very useful. Give it a try. "
						+ Utils.getAppShareUri().toString();
				ShareCompat.IntentBuilder
						.from(this)
						.setText(shareText)
						.setType("text/plain")
						.setChooserTitle("Share FireFiles")
						.startChooser();
				AnalyticsManager.logEvent("app_share");
				break;
		}
	}

	public void setUpDefaultStatusBar() {
		int color = Utils.getStatusBarColor(SettingsActivity.getPrimaryColor());
		if(Utils.hasLollipop()){
			getWindow().setStatusBarColor(color);
		}
		else if(Utils.hasKitKat()){
			SystemBarTintManager systemBarTintManager = new SystemBarTintManager(this);
			systemBarTintManager.setTintColor(Utils.getStatusBarColor(color));
			systemBarTintManager.setStatusBarTintEnabled(true);
		}
	}
}