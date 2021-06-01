package ChatApp;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import javax.swing.*;
import javax.swing.text.*;
import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;


/**
 * Implements a ChatClient
 * @author markus1728
 *
 */
public class ChatClient {
	private String hostName = "";
	private int portNumber = 0;
	private String userName = ""; 
	private String userNameColorCode = "";
	List<String> currentParticipants = new ArrayList<String>();

	private Socket client;
	private BufferedReader receiveMessageStream;
	private PrintStream sendMessageStream;

	private JFrame mainWindow;
	private JPanel panelLogin;
	private JPanel panelChat; 
	private JTextField userNameLoginField;
	private JTextField hostNameLoginField;
	private JTextField portNameLoginField;

	private JTextPane onlinePane;
	private JScrollPane scrollerOnlinePane;
	private JTextPane chatMessagesPane;
	private JScrollPane scrollerChatMessagesPane;
	private JScrollBar adjustScrollerChat;
	private JTextField sendMessagePane;

	private SimpleAttributeSet welcomeTextStyle;
	private SimpleAttributeSet joinTextStyle;
	private SimpleAttributeSet leaveTextStyle;
	private SimpleAttributeSet timeStampStyle;
	private SimpleAttributeSet userNameColorStyle;
	private SimpleAttributeSet chatTextStyle;
	private SimpleAttributeSet onlineLabelStyle;

	public void start() {
		loginGUI();
	}

	/**
	 * Creates the Login GUI
	 */
	private void loginGUI() {
		try {
			UIManager.setLookAndFeel(new FlatDarkLaf());
		} catch (Exception e) {
			e.printStackTrace();
		}

		mainWindow = new JFrame("Chat");
		mainWindow.setBounds(100, 100, 600, 460);
		mainWindow.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (userName != "") {
					sendMessageStream.println("leaveMessage;-> " + userName + " left the chat.");
					sendMessageStream.println("removeFromList;" + userName + ";" + userNameColorCode);
					sendMessageStream.flush();
				}
				System.exit(0);
			}
		});

		panelLogin = new JPanel();
		panelLogin.setLayout(null);

		//https://www.iconfinder.com/icons/171351/messages_chat_icon
		ImageIcon image = new ImageIcon("src/icons/chatIconBig.png");
		JLabel imageLabel = new JLabel();
		imageLabel.setIcon(image);
		imageLabel.setBounds(235, 50, 153, 140);

		JLabel userNameLabel = new JLabel("Username");
		userNameLabel.setBounds(270, 190, 153, 14);
		userNameLabel.setFont(new Font("Helvetica", Font.ITALIC, 13)); 

		userNameLoginField = new JTextField("");
		userNameLoginField.setBounds(220, 210, 160, 30);
		userNameLoginField.setHorizontalAlignment(JTextField.CENTER);
		userNameLoginField.addKeyListener(new KeyListenerLoginButton());

		JLabel hostNameLabel = new JLabel("Hostname");
		hostNameLabel.setBounds(270, 250, 153, 14);
		hostNameLabel.setFont(new Font("Helvetica", Font.ITALIC, 13)); 

		hostNameLoginField = new JTextField("");
		hostNameLoginField.setBounds(220, 270, 160, 30);
		hostNameLoginField.setText("localhost");
		hostNameLoginField.setHorizontalAlignment(JTextField.CENTER);
		hostNameLoginField.addKeyListener(new KeyListenerLoginButton());

		JLabel portNameLabel = new JLabel("Port");
		portNameLabel.setBounds(287, 310, 153, 14);
		portNameLabel.setFont(new Font("Helvetica", Font.ITALIC, 13)); 

		portNameLoginField = new JTextField("");
		portNameLoginField.setBounds(220, 330, 160, 30);
		portNameLoginField.setText("4448");
		portNameLoginField.setHorizontalAlignment(JTextField.CENTER);
		portNameLoginField.addKeyListener(new KeyListenerLoginButton());

		JButton loginButton = new JButton("Login");
		loginButton.setBounds(255, 378, 90, 30);
		loginButton.addActionListener(new LoginButtonListener());

		panelLogin.add(imageLabel);
		panelLogin.add(userNameLabel);
		panelLogin.add(userNameLoginField);
		panelLogin.add(hostNameLabel);
		panelLogin.add(hostNameLoginField);
		panelLogin.add(portNameLabel);
		panelLogin.add(portNameLoginField);
		panelLogin.add(loginButton);

		mainWindow.setContentPane(panelLogin);
		mainWindow.setVisible(true);
	}	


	/**
	 * Executes the login to the chat
	 */
	public void login() {
		String userNameCheck = userNameLoginField.getText();
		String hostNameCheck = hostNameLoginField.getText();
		String portNameCheck = portNameLoginField.getText();

		if(userNameCheck == null || userNameCheck.equals("") ) {
			JOptionPane.showMessageDialog(null, "Please insert a name");
		}
		else if(hostNameCheck == null || hostNameCheck.equals("") ) {
			JOptionPane.showMessageDialog(null, "Please insert a hostname");
		}
		else if(portNameCheck == null || portNameCheck.equals("") ) {
			JOptionPane.showMessageDialog(null, "Please insert a portnumber");
		}
		else {
			userName = userNameLoginField.getText();
			hostName = hostNameLoginField.getText();
			portNumber = Integer.valueOf(portNameLoginField.getText());
			chatGUI();
			connectToServer();
		}
	}

	public class LoginButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			login();
		}
	}

	public class KeyListenerLoginButton implements KeyListener {
		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_ENTER) {
				login();
			}
		}
		@Override public void keyReleased(KeyEvent e) {}
		@Override public void keyTyped(KeyEvent e) {}
	}

	/**
	 * Creates the chat GUI
	 */
	private void chatGUI () {
		panelChat = new JPanel();
		panelChat.setLayout(null);

		//https://www.iconfinder.com/icons/171351/messages_chat_icon
		ImageIcon image = new ImageIcon("src/icons/chatIconSmall.png");
		JLabel imageLabelChat = new JLabel();
		imageLabelChat.setIcon(image);
		imageLabelChat.setBounds(25, 7, 40, 40);

		JButton logoutButton = new JButton("Logout");
		logoutButton.setBounds(515, 15, 60, 25);
		logoutButton.addActionListener(new LogoutButtonListener());
		logoutButton.setBackground(new Color(60,63,65));
		logoutButton.setOpaque(true);
		logoutButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 0));

		onlinePane = new JTextPane();
		onlinePane.setBounds(25, 50, 135, 370);
		onlinePane.setEditable(false);

		scrollerOnlinePane = new JScrollPane(onlinePane);
		scrollerOnlinePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollerOnlinePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollerOnlinePane.setBounds(25, 50, 135, 370);

		chatMessagesPane = new JTextPane();
		chatMessagesPane.setBounds(165, 50, 410, 325);
		chatMessagesPane.setEditable(false);

		scrollerChatMessagesPane = new JScrollPane(chatMessagesPane);
		scrollerChatMessagesPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollerChatMessagesPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollerChatMessagesPane.setBounds(165, 50, 410, 325);

		sendMessagePane = new JTextField("Type a message and press enter...");
		sendMessagePane.setBounds(165, 380, 345, 40);
		sendMessagePane.addKeyListener(new KeyListenerSendButton());
		sendMessagePane.setForeground(new Color(187, 187, 187));
		sendMessagePane.setFont(new Font("Helvetica", Font.ITALIC, 13));  
		sendMessagePane.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				if (sendMessagePane.getText().equals("Type a message and press enter...")) {
					sendMessagePane.setText("");
					sendMessagePane.setFont(new Font("Helvetica", Font.PLAIN, 13)); 
				}
			}
			@Override
			public void focusLost(FocusEvent e) {
				if (sendMessagePane.getText().isEmpty()) {
					sendMessagePane.setFont(new Font("Helvetica", Font.ITALIC, 13)); 
					sendMessagePane.setText("Type a message and press enter...");
				}
			}
		});

		JButton sendButton = new JButton("Send");
		sendButton.setBounds(515, 380, 60, 40);
		sendButton.addActionListener(new SendButtonListener());

		panelChat.add(imageLabelChat);
		panelChat.add(logoutButton);
		panelChat.add(scrollerOnlinePane);
		panelChat.add(scrollerChatMessagesPane);
		panelChat.add(sendMessagePane);
		panelChat.add(sendButton);

		mainWindow.setContentPane(panelChat);
		mainWindow.revalidate();
	}

	/**
	 * Handles the sending of a message
	 */
	public void sendMessage() {
		String message = sendMessagePane.getText();
		sendMessagePane.setText("");
		sendMessagePane.requestFocus();

		if(message == null || message.equals("") || message.isEmpty() || message.trim().isEmpty()) {
			JOptionPane.showMessageDialog(null,"Please enter a message.");
		} else {
			String timeStampNow = new SimpleDateFormat("HH:mm").format(new Date());
			sendMessageStream.println("MSG;" + timeStampNow + ";" + userName + ";" + userNameColorCode + ";" + message);
			sendMessageStream.flush();
		}
	}

	public class SendButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			sendMessage();
		}
	}	

	public class KeyListenerSendButton implements KeyListener {
		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_ENTER) {
				sendMessage();
			}
		}
		@Override public void keyReleased(KeyEvent e) {}
		@Override public void keyTyped(KeyEvent e) {}
	}

	public class LogoutButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			sendMessageStream.println("leaveMessage;-> " + userName + " left the chat");
			sendMessageStream.println("removeFromList;" + userName + ";" + userNameColorCode);
			sendMessageStream.flush();
			mainWindow.setContentPane(panelLogin);
			mainWindow.revalidate();
		}	
	}

	/**
	 * Connects the client to the server 
	 */
	private void connectToServer() {
		try {
			client = new Socket(hostName, portNumber);
			System.out.println("Client connected to Server");

			sendMessageStream = new PrintStream(client.getOutputStream());
			receiveMessageStream = new BufferedReader(new InputStreamReader(client.getInputStream()));

			sendMessageStream.println("joinMessage;-> " + userName + " joined the chat");

			//create a random color for each username
			Random random = new Random();
			int nextInt = random.nextInt(0xffffff + 1);
			userNameColorCode = String.format("#%06x", nextInt);

			sendMessageStream.println("addToList;" + userName + ";" + userNameColorCode);
			sendMessageStream.flush();

			new Thread(new InputReader()).start();

			welcomeTextStyle = new SimpleAttributeSet();
			StyleConstants.setBold(welcomeTextStyle, true);
			StyleConstants.setForeground(welcomeTextStyle, Color.WHITE);
			StyleConstants.setFontSize(welcomeTextStyle, 14);

			StyledDocument doc = chatMessagesPane.getStyledDocument();
			try {
				doc.insertString(doc.getLength(), "Welcome to the chat :)" + "\n\n", welcomeTextStyle);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}

		} catch (IOException e) {
			System.out.println("Connection error");
			e.printStackTrace();
		}
	}

	/**
	 * Handles the incoming messages
	 * @author MarkusKaltenpoth
	 *
	 */
	public class InputReader implements Runnable {
		@Override
		public void run() {
			String message;

			joinTextStyle = new SimpleAttributeSet();
			StyleConstants.setItalic(joinTextStyle, true);
			StyleConstants.setForeground(joinTextStyle, Color.WHITE);
			StyleConstants.setFontSize(joinTextStyle, 11);

			leaveTextStyle = new SimpleAttributeSet();
			StyleConstants.setItalic(leaveTextStyle, true);
			StyleConstants.setForeground(leaveTextStyle, Color.decode("#FF6666"));
			StyleConstants.setFontSize(leaveTextStyle, 11);

			timeStampStyle = new SimpleAttributeSet();
			StyleConstants.setBold(timeStampStyle, false);
			StyleConstants.setFontSize(timeStampStyle, 11);

			chatTextStyle = new SimpleAttributeSet();
			StyleConstants.setBold(chatTextStyle, true);

			onlineLabelStyle = new SimpleAttributeSet();
			StyleConstants.setBold(onlineLabelStyle, true);
			StyleConstants.setUnderline(onlineLabelStyle, true);

			try {
				while((message = receiveMessageStream.readLine()) != null) {
					String[] tokens = message.split(";");
					StyledDocument chatDocument = chatMessagesPane.getStyledDocument();

					switch(tokens[0]) {
					case "joinMessage":
						try {
							chatDocument.insertString(chatDocument.getLength(), tokens[1] + "\n", joinTextStyle);
						} catch (BadLocationException e1) {
							e1.printStackTrace();
						}
						adjustScrollerChat = scrollerChatMessagesPane.getVerticalScrollBar();
						adjustScrollerChat.setValue(adjustScrollerChat.getMaximum());
						break;
					case "leaveMessage":
						try {
							chatDocument.insertString(chatDocument.getLength(), tokens[1] + "\n", leaveTextStyle);
						} catch (BadLocationException e1) {
							e1.printStackTrace();
						}
						adjustScrollerChat = scrollerChatMessagesPane.getVerticalScrollBar();
						adjustScrollerChat.setValue(adjustScrollerChat.getMaximum());
						break;
					case "userList":
						onlinePane.setText("");
						StyledDocument onlineDocument = onlinePane.getStyledDocument();
						try {
							onlineDocument.insertString(onlineDocument.getLength(), "Online" + " (" + tokens[1] + ") " + "\n", onlineLabelStyle);
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
						//add all online Users with their individual color to the onlinePane
						for (int i = 2; i < tokens.length; i=i+2) {
							try {
								SimpleAttributeSet styleForOnlineUser = new SimpleAttributeSet();
								StyleConstants.setBold(styleForOnlineUser, true);
								StyleConstants.setForeground(styleForOnlineUser, Color.decode(tokens[i+1]) );
								onlineDocument.insertString(onlineDocument.getLength(), tokens[i] + "\n", styleForOnlineUser);
							} catch (BadLocationException e) {
								e.printStackTrace();
							}
						}
						break;
					default: 
						//add a message to the chatPane
						try {
							userNameColorStyle = new SimpleAttributeSet();
							StyleConstants.setBold(userNameColorStyle, true);
							StyleConstants.setForeground(userNameColorStyle, Color.decode(tokens[3]));

							chatDocument.insertString(chatDocument.getLength(), tokens[1] + " ", timeStampStyle);
							chatDocument.insertString(chatDocument.getLength(), tokens[2] + ": ", userNameColorStyle);
							chatDocument.insertString(chatDocument.getLength(), tokens[4] + "\n", chatTextStyle);
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
						adjustScrollerChat = scrollerChatMessagesPane.getVerticalScrollBar();
						adjustScrollerChat.setValue(adjustScrollerChat.getMaximum());
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		new ChatClient().start();
	}	
}
