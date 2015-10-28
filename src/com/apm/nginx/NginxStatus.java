package com.apm.nginx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.apm.util.PropertiesVerifier;
import com.apm.util.PropertiesVerifier.PropertyInformation;

public class NginxStatus {

	String propertiesFileName = "";
	Properties propertiesFile;

	{
		propertiesFile = new Properties();
	}

	public NginxStatus(String propertiesFileLocation) throws FileNotFoundException, IOException {
		propertiesFile.load(new FileReader(propertiesFileLocation));
		propertiesFileName = (new File(propertiesFileLocation)).getName();
	}

	public static void LOG(String fileName, String logLevel, String message) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
		System.out.println(sdf.format(date) + " [" + logLevel + "] [" + fileName + "] " + message);
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		PropertiesVerifier verifier = null;
		ArrayList<PropertyInformation> required_and_optional_properites = new ArrayList<PropertyInformation>();
		NginxStatus ns = null;
		// check that properties file is passed as argument
		if (args.length != 1) {
			LOG("NginxStatus", "ERROR", "No properties file provided!");
			System.exit(0);
		}
		// load properties file
		try {
			ns = new NginxStatus(args[0]);
		} catch (IOException e) {
			LOG(args[0], "ERROR", "Proplem reading properties file.");
			e.printStackTrace();
			System.exit(0);
		}
		LOG("NginxStatus", "INFO", "Loaded properties file" + args[0]);
		// make sure list of nginx servers are added
		if (ns.propertiesFile.get("nginx.server.list").toString().isEmpty()) {
			LOG("PropertiesVerifier", "ERROR", "nginx.server.list is empty.");
			System.exit(0);
		} else
			LOG("PropertiesVerifier", "INFO",
					"nginx.server.list = " + ns.propertiesFile.getProperty("nginx.server.list").toString());
		// create the optional/required list of key/values for each nginx server
		// listed
		required_and_optional_properites.add(
				new PropertyInformation("nginx.server.list", ns.propertiesFile.getProperty("nginx.server.list"), true));
		for (String key : ((String) ns.propertiesFile.get("nginx.server.list")).split(",")) {
			if (!key.isEmpty() && key.trim().matches("[a-zA-Z0-9-_]*")) {
				required_and_optional_properites.add(new PropertyInformation(key + "\\.statusURL", "http(.*)", true));
				required_and_optional_properites.add(new PropertyInformation(key + "\\.epa.host", "(..*)", true));
				required_and_optional_properites.add(new PropertyInformation(key + "\\.epa.port", "[0-9]*", true));
				required_and_optional_properites.add(new PropertyInformation(key + "\\.epa.data.port", "[0-9]*", true));
				required_and_optional_properites.add(new PropertyInformation(key + "\\.delaytime", "[0-9]*", false));
				required_and_optional_properites
						.add(new PropertyInformation(key + "\\.filter\\.exclude\\.regex", "(.*)", false));
			} else {
				LOG("PropertiesVerifier", "ERROR", "Value of nginx.server.list key, \"" + key
						+ "\" is using special characters. Use only alphanumeric characters with \"_\" and \"-\".");
				System.exit(0);
			}
		}
		PropertyInformation[] tmp = new PropertyInformation[required_and_optional_properites.size()];
		int i = 0;
		for (PropertyInformation p : required_and_optional_properites) {
			tmp[i++] = p;
		}
		verifier = new PropertiesVerifier(tmp);
		// verify each property and log info/errors
		try {
			verifier.verify(ns.propertiesFile);
		} catch (IllegalArgumentException e) {
			LOG(verifier.getClass().getName(), "ERROR",
					e.getMessage().replaceFirst("ERROR", "").trim().split("\n")[1].trim());
			System.exit(0);
		}
		for (String key : ((String) ns.propertiesFile.get("nginx.server.list")).split(",")) {
			if (ns.propertiesFile.containsKey(key + ".statusURL"))
				LOG("PropertiesVerifier", "INFO",
						key + ".statusURL = " + ns.propertiesFile.getProperty((String) key + ".statusURL").toString());
			if (ns.propertiesFile.containsKey(key + ".epa.host"))
				LOG("PropertiesVerifier", "INFO",
						key + ".epa.host = " + ns.propertiesFile.getProperty((String) key + ".epa.host").toString());
			if (ns.propertiesFile.containsKey(key + ".epa.port"))
				LOG("PropertiesVerifier", "INFO",
						key + ".epa.port = " + ns.propertiesFile.getProperty((String) key + ".epa.port").toString());
			if (ns.propertiesFile.containsKey(key + ".epa.data.port"))
				LOG("PropertiesVerifier", "INFO",
						key + ".epa.data.port = " + ns.propertiesFile.getProperty((String) key + ".epa.data.port").toString());
			if (ns.propertiesFile.containsKey(key + ".delaytime"))
				LOG("PropertiesVerifier", "INFO",
						key + ".delaytime = " + ns.propertiesFile.getProperty((String) key + ".delaytime").toString());
			if (ns.propertiesFile.containsKey(key + ".report.static.freq"))
				LOG("PropertiesVerifier", "INFO", key + ".report.static.freq = "
						+ ns.propertiesFile.getProperty((String) key + ".report.static.freq").toString());
			if (ns.propertiesFile.containsKey(key + ".report.static.regex"))
				LOG("PropertiesVerifier", "INFO", key + ".report.static.regex = "
						+ ns.propertiesFile.getProperty((String) key + ".report.static.regex").toString());
			if (ns.propertiesFile.containsKey(key + ".filter.includeonly.regex"))
				LOG("PropertiesVerifier", "INFO", key + ".filter.includeonly.regex = "
						+ ns.propertiesFile.getProperty((String) key + ".filter.includeonly.regex").toString());
			if (ns.propertiesFile.containsKey(key + ".filter.excludeonly.regex"))
				LOG("PropertiesVerifier", "INFO", key + ".filter.excludeonly.regex = "
						+ ns.propertiesFile.getProperty((String) key + ".filter.excludeonly.regex").toString());
		}
		// setup each server object
		Runnable[] servers = new Runnable[((String) ns.propertiesFile.get("nginx.server.list")).split(",").length];
		i = 0;
		for (String key : ((String) ns.propertiesFile.get("nginx.server.list")).split(",")) {
			final String k = key;
			final NginxStatus tmpNginxStatus = ns;
			final String MetricLocation = "nginx|";
			servers[i++] = new Runnable() {
				@Override
				public void run() {
					LOG("NginxStatus", "DEBUG", "Starting monitoring tasks for " + k);
					// get nginx JSON and parse into jsonObject
					JSONObject jsonObject = null;
					try {
						String s = GetNginxJSON.callURL(tmpNginxStatus.propertiesFile.getProperty(k + ".statusURL"));
						try {
							jsonObject = (JSONObject) new JSONParser().parse(s);
						} catch (ParseException e) {
							LOG("NginxStatus", "ERROR", "JSON object from NGINX server " + k + " is invalid.");
							return;
						}
					} catch (RuntimeException e) {
						LOG("NginxStatus", "ERROR",
								k + ".statusURL is invalid or the nginx server running on that server/port is down.");
						return;
					}
					// convert the nginx JSON to EPA metrics (JSON format)
					Json2EPAMetrics j2em = new Json2EPAMetrics(
							MetricLocation
									+ tmpNginxStatus.propertiesFile.getProperty(k + ".statusURL").replaceAll(":", "_"),
							jsonObject);
					// look at the exclude filter and update the object
					int countRemovedMetrics = j2em.removeMetrics(
							MetricLocation
									+ tmpNginxStatus.propertiesFile.getProperty(k + ".statusURL").replaceAll(":", "_"),
							tmpNginxStatus.propertiesFile.getProperty(k + ".filter.exclude.regex", ""));
					// add additional metrics like delaytime between pulls from nginx
					j2em.addMetric("IntCounter",
							MetricLocation
									+ tmpNginxStatus.propertiesFile.getProperty(k + ".statusURL").replaceAll(":", "_")
									+ "|NginxStatus Info:reporting interval (s)",
							Integer.valueOf(tmpNginxStatus.propertiesFile.getProperty(k + ".delaytime", "15")));
					j2em.addMetric("IntCounter",
							MetricLocation
									+ tmpNginxStatus.propertiesFile.getProperty(k + ".statusURL").replaceAll(":", "_")
									+ "|NginxStatus Info:number of excluded metrics",
							countRemovedMetrics);
					// send the JSON to EPAgent
					try {
						SendMetrics.sendMetric(j2em.eson, tmpNginxStatus.propertiesFile.getProperty(k + ".epa.host"),
								Integer.valueOf(tmpNginxStatus.propertiesFile.getProperty(k + ".epa.port")));
					} catch (NumberFormatException | IOException e) {
						LOG("NginxStatus", "WARN", "Problem sending " + k + " data to EPAgent.\n\t" + e.getMessage());
					}
					LOG("NginxStatus", "DEBUG", j2em.eson.toString());
					LOG("NginxStatus", "DEBUG", "Finished monitoring tasks for " + k);
				}
			};
		}

		// start monitoring
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(servers.length*2);
		i = 0;
		for (Runnable r : servers) {
			// schedule monitoring based on delaytime
			String value = ns.propertiesFile
					.getProperty(ns.propertiesFile.getProperty("nginx.server.list").split(",")[i] + ".delaytime", "15");
			if ((Integer.valueOf(value) % 15 == 0) && (Integer.valueOf(value) >= 15))
				executor.scheduleAtFixedRate(r, 0, Integer.valueOf(value), TimeUnit.SECONDS);
			else
				executor.scheduleAtFixedRate(r, 0, 60, TimeUnit.SECONDS);
			LOG("NginxStatus", "INFO",
					"Monitoring started for " + ns.propertiesFile.getProperty("nginx.server.list").split(",")[i++]);
		}
		
		// send tt for each server
		Runnable[] tt = new Runnable[((String) ns.propertiesFile.get("nginx.server.list")).split(",").length];
		i = 0;
		for (String key : ((String) ns.propertiesFile.get("nginx.server.list")).split(",")) {
			final String k = key;
			final NginxStatus tmpNginxStatus = ns;
			tt[i++] = new Runnable() {
				@Override
				public void run() {
					String host = tmpNginxStatus.propertiesFile.getProperty(k + ".epa.host");
					int port = Integer.valueOf(tmpNginxStatus.propertiesFile.getProperty(k + ".epa.data.port"));
					String alias = k;
					String resource = tmpNginxStatus.propertiesFile.getProperty(k + ".statusURL").replaceAll(":", "_");
					try {
						try {
							EPAgentTraceReporter.execute(host, port, alias, resource);
						} catch (UnknownHostException u) {
							LOG("NginxStatus.TT", "ERROR", "Known host:port [" + host + ":" + port + "]");
						}
					} catch (IOException e) {
						LOG("NginxStatus.TT", "ERROR", "Problem with socket connection on [" + host + ":" + port + "]");
					}
				}
			};
		}
		// start sending tt every 30 minutes
		ScheduledExecutorService ttexecutor = Executors.newScheduledThreadPool(tt.length);
		for (Runnable r : tt) {
			ttexecutor.scheduleAtFixedRate(r, 0, 5, TimeUnit.SECONDS);
			LOG("NginxStatus.TT", "INFO",
					"Started GENERIC BUSINESS SEGMENT generation starting, pings will happen 30 minutes apart.");
		}
	}
}
