package com.apm.nginx;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONException;
import org.json.JSONObject;

public class SendMetrics {

	/**
	 * @param json
	 * @param hostname
	 * @param port
	 * @return String = is what we're returning  (has to be some sort of object i.e. file, URL,)
	 */
	public String sendMetric(JSONObject jsonObject, String hostname, int port){

		String output="";
		try {
 
			System.out.println(jsonObject);
 
			// Pass JSON File Data to REST Service
			try {
				URL url = new URL("http://" + hostname + ":" + Integer.toString(port) + "/apm/metricFeed");
				URLConnection connection = url.openConnection();
				connection.setDoOutput(true);
				connection.setRequestProperty("Content-Type", "application/json");
				connection.setConnectTimeout(5000);
				connection.setReadTimeout(5000);
				OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
				out.write(jsonObject.toString());
				out.close();
 
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				//NewLine
				// This returns output from EPAgent after sending it metrics
				while ((output = in.readLine()) != null) {
					output = output + "\n";
				}
				System.out.println(output);
				
				System.out.println("\nMetrics Posted Successfully..");
				in.close();
			} catch (Exception e) {
				System.out.println("\nError while Posting Metrics");
				System.out.println(e);
			}
 

		} catch (Exception e) {
			e.printStackTrace();
		}
		//returning output from EPAgent
		return output;
	}
	
	public static void main(String[] args) {
		
//		ParseJSON rawMetricJsonObjct = new ParseJSON();
		
		
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject("{ \"metrics\": [   {  \"type\": \"IntCounter\",  \"name\": \"TestMetrics|MetricInfo|MetricName:Connections\",  \"value\": \"55\"  },]  }");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		SendMetrics sendMyMetric = new SendMetrics();
		sendMyMetric.sendMetric(jsonObject, "localhost", 9080);
		
		
/*		String metrics = "";
		try {
 
			// Read file from fileSystem

			InputStream metricsInputStream = new FileInputStream("/Users/yoral01/Desktop/MetricJSON.txt");
			InputStreamReader metricReader = new InputStreamReader(metricsInputStream);
			BufferedReader br = new BufferedReader(metricReader);
			String line;
			while ((line = br.readLine()) != null) {
				metrics += line + "\n";
			}
			//System.out.println(metrics);
			
			JSONObject jsonObject = new JSONObject(metrics);
			System.out.println(jsonObject);
 
			// Pass JSON File Data to REST Service
			try {
				URL url = new URL("http://localhost:9080/apm/metricFeed");
				URLConnection connection = url.openConnection();
				connection.setDoOutput(true);
				connection.setRequestProperty("Content-Type", "application/json");
				connection.setConnectTimeout(5000);
				connection.setReadTimeout(5000);
				OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
				out.write(jsonObject.toString());
				out.close();
 
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				
				//NewLine
				String output="";
				
				while ((output = in.readLine()) != null) {
					output = output + "\n";
				}
				System.out.println(output);
				System.out.println("\nMetrics Posted Successfully..");
				in.close();
			} catch (Exception e) {
				System.out.println("\nError while Posting Metrics");
				System.out.println(e);
			}
 
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
*/
	}
}

