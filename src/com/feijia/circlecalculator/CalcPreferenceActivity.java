package com.feijia.circlecalculator;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
public class CalcPreferenceActivity extends PreferenceActivity { 
	@SuppressLint("NewApi") @SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		//---load the preferences from an XML file--- 
		if (Build.VERSION.SDK_INT >= 11)
			getFragmentManager().beginTransaction().replace(android.R.id.content, new CalcPreferenceFragment()).commit();
		else addPreferencesFromResource(R.xml.calcpreferences);
		
	} 
	@SuppressLint("NewApi") public static class CalcPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.calcpreferences);
        }    
    }
}