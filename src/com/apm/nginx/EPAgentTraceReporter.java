package com.apm.nginx;

//EPAgentTraceReporter.java
//Reads traces from a xml file and sends to EPAgent on port 9000.
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class EPAgentTraceReporter {
	private static Socket sock;
	
	public static void execute(String host, int port, String alias, String resource) throws UnknownHostException, IOException {
		// {ALIAS} = component name
		// {RESOURCE} = resource name
		String tt = "<event resource=\"Business Segment|NginxApp|{ALIAS}BusinessTransaction\"\nComponentName=\"{ALIAS}BusinessTransaction\" ComponentType=\"Business Segment\"\nstartTime=\"\" duration=\"200\" offset=\"0\" >\n<calledComponent\nresource=\"Frontends|Apps|{ALIAS}|{RESOURCE}\"\nComponentName=\"{ALIAS}\" ComponentType=\"Frontends\" duration=\"200\" offset=\"0\">\n</calledComponent>\n</event>";
		tt = tt.replaceAll(System.getProperty("line.separator"), " ");
		tt = tt.replaceAll("\\{ALIAS\\}", alias).replaceAll("\\{RESOURCE\\}", resource);
		
		sock = new Socket(host, port);
		OutputStream out = sock.getOutputStream();
		out.write(tt.getBytes());
		out.close();
	}

	public static void main(String[] args) throws Exception {
//		new EPAgentTraceReporter().execute();
		EPAgentTraceReporter.execute("localhost", 8000, "alias", "resource");
	}

	@SuppressWarnings("unused")
	private void execute() throws Exception {
		String xmlTrace = readFile("resources/TransactionTrace.xml", Charset.defaultCharset());
		xmlTrace = xmlTrace.replaceAll(System.getProperty("line.separator"), " ");
		System.out.println("Sending Transaction Trace: " + xmlTrace);
		while (true) {
			sock = new Socket("127.0.0.1", 9000);
			OutputStream out = sock.getOutputStream();
			out.write(xmlTrace.getBytes());
			out.close();
			Thread.sleep(1000);
		}
	}

	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
}