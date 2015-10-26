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
	public boolean addMetric(String type, String name, Object value) {
		this.metrics.add(createMetric(type, name, value));		
		return this.eson.replace("metrics", this.eson.get("metrics"), this.metrics);
	}
	
	public int removeMetrics(String metricLocation, String regex){
		if(regex.matches(""))
			return 0;
		int count = 0;
		for(int i=0; i<this.metrics.size(); i++) {
			if(((JSONObject)this.metrics.get(i)).get("name").toString().replace(metricLocation, "").substring(1).matches(regex)){
				this.metrics.remove(i);
				count++;
			}
		}
		return count;
	}

	@SuppressWarnings("unchecked")
	public void convert2MetricJSON(String metricLocation, JSONObject object) {
		Collection<String> keys = object.keySet();
		
		for(String key : keys) {
			if(object.get(key).getClass() == Long.class) {
				if((Long)object.get(key) > System.currentTimeMillis()-(6936289280L)) {
					metrics.add(createMetric("Timestamp", metricLocation + ":" + key, object.get(key)));
				}
				else if(key.matches("pid")) {
					metrics.add(createMetric("StringEvent", metricLocation + ":" + key, "" + object.get(key)));
				}
				else {
					metrics.add(createMetric("PerintervalCounter", metricLocation + ":" + key, object.get(key)));
				}
			}
			else if(object.get(key).getClass() == String.class) {
				metrics.add(createMetric("StringEvent", metricLocation + ":" + key, "" + object.get(key)));
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

	@SuppressWarnings("unchecked")
	public JSONObject createMetric(String type, String name, Object value) {
		JSONObject m = new JSONObject();
		m.put("type", type);
		m.put("name", name);
		m.put("value", value);
		return m;
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException, InterruptedException {
		Json2EPAMetrics j2e = new Json2EPAMetrics("NGINX|server1", (JSONObject) new JSONParser().parse(new FileReader("resources" + File.separator + "json.json")));
		
		System.out.println(j2e.json.toString());
		System.out.println("-----------------------------------");
		System.out.println(j2e.eson.toString());
//		System.out.println("-----------------------------------");
//		for(int i=0; i<j2e.metrics.size(); i++) {
//			System.out.println(j2e.metrics.get(i));
//		}
	}

}
