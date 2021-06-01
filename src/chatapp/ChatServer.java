package ChatApp;

import java.net.*;
import java.io.*;
import java.util.*;


/**
 * Implements the ChatServer
 * @author markus1728
 *
 */
public class ChatServer {

	public static int portNumber = 4448;
	private Vector<PrintWriter> clientWriters = new Vector<>();
	List<String> currentUsers = new ArrayList<String>();


	/**
	 * Start the server
	 */
	public void runServer() {
		try {
			ServerSocket serverSocket = new ServerSocket(portNumber);
			System.out.println("Server running on port " + portNumber);
			while(true) {
				try {
					Socket socket = serverSocket.accept();
					PrintWriter writer = new PrintWriter(socket.getOutputStream()); 
					clientWriters.add(writer);
					new Thread(new ClientManager(socket, writer)).start();
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}
		} catch (IOException e) {
			System.err.println("Error with port " + portNumber);
			e.printStackTrace();
		}
	}

	/**
	 * Send a message to all users
	 * @param msg
	 */
	private void broadcastMessage(String msg) {
		for(PrintWriter writer : clientWriters) {
			writer.println(msg);
			writer.flush();
		}
	}

	/**
	 * Manages the clients and the incoming and outgoing messages
	 * @author markus1728
	 *
	 */
	public class ClientManager implements Runnable {
		private PrintWriter writer;
		private BufferedReader reader;

		public ClientManager(Socket socket, PrintWriter writer) {
			this.writer = writer;
			try {
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream())
						);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			String message;

			try {
				while((message = reader.readLine()) != null) {
					String[] tokens = message.split(";");

					switch(tokens[0]) {
					case "joinMessage":
						broadcastMessage(message);
						break;
					case "addToList":
						currentUsers.add(tokens[1]);
						currentUsers.add(tokens[2]);
						String userListAdd = String.join(";", currentUsers); 
						broadcastMessage("userList;" + clientWriters.size() + ";" + userListAdd);
						break;
					case "leaveMessage":
						clientWriters.remove(this.writer);
						broadcastMessage(message);
						break;
					case "removeFromList":
						currentUsers.remove(tokens[1]);
						currentUsers.remove(tokens[2]);
						String userListRemove = String.join(";", currentUsers); 
						broadcastMessage("userList;" + clientWriters.size() + ";" + userListRemove);
						break;
					default: 
						broadcastMessage(message);						
						break;
					}
				}
			} catch (IOException e) {}
		}
	}

	public static void main(String[] args) {
		new ChatServer().runServer();
	}
}
