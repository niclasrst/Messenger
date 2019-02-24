import java.io.*;
import java.awt.*;

import javax.swing.*;

import java.awt.event.*;
import java.net.Socket;

public class Client {
	
	// Global vars
	static JFrame frame;
	static JPanel panel;
	static JTextArea textArea;
	static JTextField textField;
	static JButton buttonSendMsg;
	static JTextField nameField;
	
	static Socket client;
	static PrintWriter writer;
	static BufferedReader reader;
	
	static int PORT = 6000;
	static String SERVER = "127.0.0.1";
	
	static int key = (int) Math.round(Math.random() * 20);
	
	// Main
	public static void main(String[] args) {		
		Client c = new Client();
		c.GUI();
	}
	
	// GraphicalUserInterface
	public void GUI() {
		frame = new JFrame("Messenger");
		frame.setSize(800, 600);
		
		panel = new JPanel();
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		
		textField = new JTextField(38);
		textField.addKeyListener(new enterPressed());
		
		buttonSendMsg = new JButton("Send");
		buttonSendMsg.addActionListener(new sendPressed());
		
		nameField = new JTextField(10);
		
		JScrollPane scrollPaneMsg = new JScrollPane(textArea);
		scrollPaneMsg.setPreferredSize(new Dimension(700, 500));
		scrollPaneMsg.setMinimumSize(new Dimension(700, 500));
		scrollPaneMsg.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPaneMsg.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		if (!connectToServer()) { frame.setTitle("Messenger | Nicht Verbunden"); } else { frame.setTitle("Messenger | Verbunden"); }
		
		Thread t = new Thread(new serverListener());
		t.start();
		
		panel.add(scrollPaneMsg);
		panel.add(nameField);
		panel.add(textField);
		panel.add(buttonSendMsg);
		
		frame.getContentPane().add(BorderLayout.CENTER, panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	// Utilities
	public static boolean connectToServer() {
		try {
			client = new Socket(SERVER, PORT);
			reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
			writer = new PrintWriter(client.getOutputStream());
			System.out.println("[+] Verbindung zu Server hergestellt.");
			return true;
		} catch (Exception e) {
			System.out.println("[-] Error connecting to Server: " + e.getMessage());
			return false;
		}
	}
	
	public void sendMessageToServer() {		
		if (!textField.getText().isEmpty() && !nameField.getText().isEmpty()) {
			writer.print(encryption.encrypt(nameField.getText() + ": " + textField.getText() + "\n"));
			writer.flush();
			System.out.println(encryption.encrypt(nameField.getText() + ": " + textField.getText() + "\n"));
		}
		
		textField.setText("");
		textField.requestFocus();
	}
	
	// Event Listener
	public class enterPressed implements KeyListener {

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				sendMessageToServer();
			}
		}
		
		@Override
		public void keyTyped(KeyEvent e) {}
		
		@Override
		public void keyReleased(KeyEvent e) {}
	}
	
	public class sendPressed implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			sendMessageToServer();
		}
	}
	
	public class serverListener implements Runnable {
		@Override
		public void run() {
			String message;
			
			try {
				while ((message = reader.readLine()) != null) {
					appendToTextArea(encryption.decrypt(message));
					textArea.setCaretPosition(textArea.getText().length());
				}
			} catch (IOException e) {
				System.out.println("[-] Error receiving Message: " + e.getMessage());
			}
		}
	}
	
	// Utilities
	public void appendToTextArea(String msg) {
		textArea.append(msg + "\n");
	}
	
	// Encryption
	public static class encryption {
		static String alphabet = "abcdefghijklmnopqrstuvwxyz";
				
		public static String encrypt(String msg) {
			char ch;
			String newMsg = "";
			
			for(int i = 0; i < msg.length(); ++i){
				ch = msg.charAt(i);
				
				if (ch >= 'a' && ch <= 'z') {
		            ch = (char)(ch + key);
		            
		            if (ch > 'z') { ch = (char)(ch - 'z' + 'a' - 1); }
		            
		            newMsg += ch;
		            
		        } else if (ch >= 'A' && ch <= 'Z') {
		            ch = (char)(ch + key);
		            
		            if (ch > 'Z') { ch = (char)(ch - 'Z' + 'A' - 1); }
		            
		            newMsg += ch;
		        } else {
		        	newMsg += ch;
		        }
			}
						
			return newMsg;
		}
		
		public static String decrypt(String msg) {
			String newMsg = "";
			char ch;
			
			for (int i = 0; i < msg.length(); ++i){
				ch = msg.charAt(i);
				
				if (ch >= 'a' && ch <= 'z') {
		            ch = (char)(ch - key);
		            
		            if (ch < 'a') { ch = (char)(ch + 'z' - 'a' + 1); }
		            
		            newMsg += ch;
		            
		        } else if (ch >= 'A' && ch <= 'Z') {
		            ch = (char)(ch - key);
		            
		            if (ch < 'A') { ch = (char)(ch + 'Z' - 'A' + 1); }
		            
		            newMsg += ch;
		            
		        } else {
		        	newMsg += ch;
		        }
			}
			
			return newMsg;
		}
	}
}
