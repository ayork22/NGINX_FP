package com.apm.nginx;


//import java.util.Iterator;

//import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ParseJSON {

	//Currently this is parsing JSON from a local file
//	private static final String filePath = "/Users/yoral01/Desktop/NGINX_JSON.txt";
	
	GetNginxJSON myJsonObject = new GetNginxJSON();
	
	public static void main(String[] args) {
	String NginxURL = "http://192.168.128.170:8080/status";	
		
		try {
			
			// read the json file
//			FileReader reader = new FileReader(filePath);

//			GetNginxJSON myJsonObject = new GetNginxJSON();
//			myJsonObject.callURL(sb);
			
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(GetNginxJSON.callURL(NginxURL));

			// get a String from the JSON object
			String hostname = (String) jsonObject.get("address");
			System.out.println("Hostname: " + hostname);

			// get a number from the JSON object
			long id =  (long) jsonObject.get("version");
			System.out.println("The version is: " + id);

			long pid =  (long) jsonObject.get("pid");
			System.out.println("PID =: " + pid);

			//Use this when getting vertex that is embedded within another JSON Object
			JSONObject connections = (JSONObject) jsonObject.get("connections");
			long accepted = (long) connections.get("accepted");
			System.out.println("Connections Accecpted =: " + accepted);

//			//Use this when getting vertex that is embedded within another JSON Object
//			JSONObject server_zones = (JSONObject) jsonObject.get("server_zones");
//			long processing = (long) server_zones.get("processing");
//			System.out.println("Processing =: " + processing);



		} catch (ParseException ex) {
			ex.printStackTrace();
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}

	}

}