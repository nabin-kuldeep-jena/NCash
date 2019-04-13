package com.asjngroup.ncash.common.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class StringResolver {
	public static String localeInfo="";
	private  Map<String,Locale> LocaleInfoMap=new HashMap<String,Locale>();
	public String resolveString(String key) {
		try {
			java.util.ResourceBundle resourceBundle = ResourceBundle.getBundle("login", getLocale());
			if (resourceBundle == null)
				return key;

			return resourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}
	private Locale getLocale() {
		LocaleInfoMap.put("en-GB", Locale.UK);
		LocaleInfoMap.put("en-US", Locale.US);
		LocaleInfoMap.put("fr-FR", Locale.FRANCE);
	    Locale locale=LocaleInfoMap.get(localeInfo)!=null?LocaleInfoMap.get(localeInfo):Locale.ROOT;
	    
		return locale;
	}
}
