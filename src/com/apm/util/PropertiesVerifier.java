package com.apm.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class PropertiesVerifier {
	private final Map<String, PropertyInformation> allProperties;

	{
		allProperties = new HashMap<String, PropertyInformation>();
	}

	public PropertiesVerifier(PropertyInformation[] properties) {
		for (PropertyInformation pi : properties) {
			allProperties.put(pi.key, pi);
		}
	}

	public boolean isRequired(String key) {
		for (Iterator<String> iterator = this.allProperties.keySet().iterator(); iterator.hasNext();) {
			String keyRegex = iterator.next();
			if (key.matches(keyRegex))
				return this.allProperties.get(keyRegex).required;
		}
		return false;
	}

	public boolean isOptional(String key) {
		for (Iterator<String> iterator = this.allProperties.keySet().iterator(); iterator.hasNext();) {
			String keyRegex = iterator.next();
			if (key.matches(keyRegex))
				return !this.allProperties.get(keyRegex).required;
		}
		return false;
	}

	public boolean isKnown(String key) {
		return (isRequired(key) || isOptional(key));
	}

	public boolean verify(Properties properties) throws IllegalArgumentException {
		if (properties.isEmpty())
			throw new IllegalArgumentException(
					"ERROR verifying properties:" + this.getClass() + "\n\tNo properties found!");

		String illegalArgumentExceptionMessage = "ERROR verifying properties:" + this.getClass() + "\n\t";
		int requiredKeysFound_Count = 0, requiredKeys_Count = 0;
		for (Object key : properties.keySet()) {
			if (!this.isKnown((String) key))
				throw new IllegalArgumentException(
						illegalArgumentExceptionMessage + "Unrecognized property found [" + (String) key + "]");
			if (this.isRequired((String) key))
				requiredKeysFound_Count++;

			for (Iterator<String> allKeys = allProperties.keySet().iterator(); allKeys.hasNext();) {
				String regex = allKeys.next();
				if (((String) key).matches(regex)) {
					if (!properties.getProperty((String) key).matches(this.allProperties.get(regex).value))
						throw new IllegalArgumentException(illegalArgumentExceptionMessage + "Bad property value ["
								+ (String) key + " : " + properties.getProperty((String) key) + "]");
				}
			}
		}
		for (Iterator<String> allKeys = allProperties.keySet().iterator(); allKeys.hasNext();) {
			String regex = allKeys.next();
			if (this.allProperties.get((String) regex).required)
				requiredKeys_Count++;
		}
		if (requiredKeysFound_Count < requiredKeys_Count)
			throw new IllegalArgumentException(illegalArgumentExceptionMessage + "Missing required properties.");
		return true;
	}

	public final static class PropertyInformation {
		private final String key;
		private final String value;
		// true == required, false == optional
		private final boolean required;

		public PropertyInformation(final String k, final String v, final boolean r) {
			this.key = k;
			this.value = v;
			this.required = r;
		}
	}

	public static void main(String[] args) {
		PropertiesVerifier verifier = new PropertiesVerifier(new PropertiesVerifier.PropertyInformation[] {
				new PropertiesVerifier.PropertyInformation("key\\.one", "value\\ on.", true), // required
				new PropertiesVerifier.PropertyInformation("key\\.two", "value.t(.*)", false),// optional
		});

		System.out.println("true = " + verifier.isRequired("key.one"));
		System.out.println("true = " + verifier.isOptional("key.two"));
		System.out.println("false = " + verifier.isRequired("key.two"));
		System.out.println("false = " + verifier.isOptional("key.one"));

		System.out.println("\n---------------------No properties--------------------");

		Properties properties = new Properties();

		try {
			if (verifier.verify(properties))
				System.out.println("Verified");
		} catch (IllegalArgumentException e) {
			System.out.println(e);
		}

		System.out.println("\n---------------------Unrecognized property------------");

		Properties properties1 = new Properties();
		properties1.put("key.one", "value one");
		properties1.put("key.two", "value two");
		properties1.put("bad.key", "bad value");

		try {
			if (verifier.verify(properties1))
				System.out.println("Verified");
		} catch (IllegalArgumentException e) {
			System.out.println(e);
		}

		System.out.println("\n---------------------Bad property value---------------");

		Properties properties2 = new Properties();
		properties2.put("key.one", "value one");
		properties2.put("key.two", "bad value on good key");
		properties2.put("bad.key", "bad value on a bad key");
		try {
			if (verifier.verify(properties2))
				System.out.println("Verified");
		} catch (IllegalArgumentException e) {
			System.out.println(e);
		}

		System.out.println("\n---------------------Verified-------------------------");

		Properties properties3 = new Properties();
		properties3.put("key.one", "value one");
		properties3.put("key.two", "value two");
		try {
			if (verifier.verify(properties3))
				System.out.println("Verified");
		} catch (IllegalArgumentException e) {
			System.out.println(e);
		}

		System.out.println("\n---------------------Missing required key(s)----------");

		Properties properties4 = new Properties();
		properties4.put("key.two", "value two");
		try {
			if (verifier.verify(properties4))
				System.out.println("Verified");
		} catch (IllegalArgumentException e) {
			System.out.println(e);
		}

	}
}