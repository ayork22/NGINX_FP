package com.apm.nginx;

//EPAgentTraceReporter.java
//Reads traces from a xml file and sends to EPAgent on port 9000.
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class EPAgentTraceReporter {
private Socket sock;
public static void main(String[] args) throws Exception {
new EPAgentTraceReporter().execute();
}
private void execute() throws Exception {
String xmlTrace = readFile("/Users/yoral01/Desktop/Traces/TransactionTrace.xml", Charset.defaultCharset());
xmlTrace = xmlTrace.replaceAll(System.getProperty("line.separator"), " ");
System.out.println("Sending Transaction Trace: "+ xmlTrace);
while(true) {
sock = new Socket("127.0.0.1", 9000);
OutputStream out = sock.getOutputStream();
out.write(xmlTrace.getBytes());
out.close();
Thread.sleep(1000);
}
}
static String readFile(String path, Charset encoding) throws IOException{
byte[] encoded = Files.readAllBytes(Paths.get(path));
return new String(encoded, encoding);
}
}