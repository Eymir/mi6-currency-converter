package com.mi6.currencyconverter.utils;

import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences.Editor;

public class PrefferencesUtil {

public static void SetUserPrefferences(Activity activity, String key, Object object) {
		
		Editor e = activity.getPreferences(Context.MODE_PRIVATE).edit();
		if (object instanceof Boolean) {
			e.putBoolean(key, (Boolean)object);
		}
		if (object instanceof Float) {
			e.putFloat(key, (Float)object);	
		}
		if (object instanceof Integer) {
			e.putInt(key, (Integer)object);	
		}
		if (object instanceof Long) {
			e.putLong(key, (Long)object);	
		}
		if (object instanceof String) {
			e.putString(key, (String)object);	
		}
		if (object instanceof Set) {
			e.putStringSet(key, (Set<String>)object);	
		}
		e.commit();
		
	}

}
