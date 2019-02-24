import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class Server {
	
	// Global vars
	ServerSocket server;
	ArrayList<PrintWriter> clientWriter;
	
	static int SERVERPORT = 6000;
	static String sqlURL = "jdbc:mysql://localhost:3306/messages";
	static String sqlUser = "root";
	static String sqlPassw = "anonymous";
	
	// Main
	public static void main(String[] args) throws SQLException {
		Server s = new Server();
		
		if (s.runServer()) { s.listen(); } else {}
	}
	
	// Server
	public class Handler implements Runnable {
		Socket client;
		BufferedReader reader;
		
		public Handler(Socket client) {
			try {
				this.client = client;
				reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
			} catch (IOException e) {
				System.out.println("[-] Error reading from client: " + e.getMessage());
			}
		}
		
		@Override
		public void run() {
			String message;
			
			try {
				while ((message = reader.readLine()) != null) {
					System.out.println("[+] Received: " + message);
					sendToClients(message);
					insertToDB(message);
				}
			} catch (IOException e) {
				System.out.println("[-] Error receiving message: " + e.getMessage());
			}
		}
	}
	
	// Utilities
	public void listen() {
		while (true) {
			try {
				Socket client = server.accept();
				
				PrintWriter writer = new PrintWriter(client.getOutputStream());
				clientWriter.add(writer);
				
				Thread clientThread = new Thread(new Handler(client));
				clientThread.start();
			} catch (IOException e) {
				System.out.println("[-] Error accepting client: " + e.getMessage());
			}
		}
	}
	
	public boolean runServer() {
		try {
			server = new ServerSocket(SERVERPORT);
			System.out.println("[+] Server gestartet.");
			
			clientWriter = new ArrayList<PrintWriter>();
			return true;
		} catch (IOException e) {
			System.out.println("[-] Server couldn't start: " + e.getMessage());
			return false;
		}
	}
	
	public void sendToClients(String message) {
		Iterator<PrintWriter> i = clientWriter.iterator();
		
		while (i.hasNext()) {
			PrintWriter writer = (PrintWriter) i.next();
			writer.println(message);
			writer.flush();
		}
	}
	
	// Database
	public void insertToDB(String message) {		
		try {
			Connection conn = DriverManager.getConnection(sqlURL, sqlUser, sqlPassw);
			Statement sql = conn.createStatement();
			
			String sq1 = "insert into messages_sent " + " (date, message)" + " values ('" + new Date() + "', '" + message + "')";
			sql.executeUpdate(sq1);
			
			System.out.println("[+] Insert to DB complete.");			
		} catch (Exception e) {
			System.out.println("[-] Couldn't insert into DB: " + e.getMessage());
		}
	}
}
