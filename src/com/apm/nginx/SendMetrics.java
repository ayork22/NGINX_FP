package com.apm.nginx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import org.json.simple.JSONObject;

public class SendMetrics {
	
	/**
	 * @param json
	 * @param hostname
	 * @param port
	 * @return String = is what we're returning (has to be some sort of object
	 *         i.e. file, URL,)
	 * @throws IOException
	 */
	public static String sendMetric(JSONObject jsonObject, String hostname, int port) throws IOException {

		String output = "";

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

		while ((output = in.readLine()) != null) {
			output = output + "\n";
		}

		in.close();

		return output;
	}
}
