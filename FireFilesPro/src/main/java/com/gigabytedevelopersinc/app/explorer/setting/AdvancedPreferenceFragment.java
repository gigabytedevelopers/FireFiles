package com.gigabytedevelopersinc.app.explorer.setting;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;

import com.gigabytedevelopersinc.app.explorer.pro.R;

import static com.gigabytedevelopersinc.app.explorer.setting.SettingsActivity.KEY_ADVANCED_DEVICES;
import static com.gigabytedevelopersinc.app.explorer.setting.SettingsActivity.KEY_FOLDER_ANIMATIONS;
import static com.gigabytedevelopersinc.app.explorer.setting.SettingsActivity.KEY_ROOT_MODE;

public class AdvancedPreferenceFragment extends PreferenceFragment implements OnPreferenceClickListener {

	public AdvancedPreferenceFragment() {
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_advanced);
		findPreference(KEY_ADVANCED_DEVICES).setOnPreferenceClickListener(this);
		findPreference(KEY_ROOT_MODE).setOnPreferenceClickListener(this);
		findPreference(KEY_FOLDER_ANIMATIONS).setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		SettingsActivity.logSettingEvent(preference.getKey());
		return false;
	}
}