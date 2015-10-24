package com.apm.nginx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Json2EPAMetrics {
	
	private JSONObject json;
	JSONObject eson;
	private JSONArray metrics;
	
	{
		json = new JSONObject();
		eson = new JSONObject();
		metrics = new JSONArray();
	}

	public Json2EPAMetrics(JSONObject jsonObject) {
		json = jsonObject;
	}

	@SuppressWarnings("unchecked")
	public Json2EPAMetrics(String metricLocation, JSONObject jsonObject) {
		json = jsonObject;
		convert2MetricJSON(metricLocation, jsonObject);
		eson.put("metrics", metrics);
	}

	@SuppressWarnings("unchecked")
	public void convert2MetricJSON(String metricLocation, JSONObject object) {
		Collection<String> keys = object.keySet();
		
		for(String key : keys) {
			if(object.get(key).getClass() == Long.class) {
				JSONObject m = new JSONObject();
				if((Long)object.get(key) > System.currentTimeMillis()-(6936289280L)) {
					m.put("type", "Timestamp");
					m.put("name", metricLocation + ":" + key);
					m.put("value", object.get(key));
					metrics.add(m);
				}
				else {
					m.put("type", "PerintervalCounter");
					m.put("name", metricLocation + ":" + key);
					m.put("value", object.get(key));
					metrics.add(m);
				}
			}
			else if(object.get(key).getClass() == String.class) {
				JSONObject m = new JSONObject();
				m.put("type", "StringEvent");
				m.put("name", metricLocation + ":" + key);
				m.put("value", object.get(key));
				metrics.add(m);
			}
			else if(object.get(key).getClass() == JSONObject.class){
				this.convert2MetricJSON(metricLocation + "|" + key, (JSONObject) object.get(key));
			}
			else if(object.get(key).getClass() == JSONArray.class) {
				JSONArray jsonArray = (JSONArray) object.get(key);
				for(Object s : jsonArray.toArray()) {
					this.convert2MetricJSON(metricLocation + "|" + key, (JSONObject)s);
				}
			}
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException, InterruptedException {
		Json2EPAMetrics j2e = new Json2EPAMetrics("NGINX|servername", (JSONObject) new JSONParser().parse(new FileReader("resources" + File.separator + "json.json")));
		
		System.out.println(j2e.json.toString());
		System.out.println("-----------------------------------");
		System.out.println(j2e.eson.toString());
//		System.out.println("-----------------------------------");
//		for(int i=0; i<j2e.metrics.size(); i++) {
//			System.out.println(j2e.metrics.get(i));
//		}
	}

}
