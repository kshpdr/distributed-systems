package cit;

/**
 * SMTP client for "Verteilte Systeme".
 * 
 * Author: Daniel Warneke <daniel.warneke@tu-berlin.de>
 * 
 */

import de.tu_berlin.cit.SMTPClientState;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Iterator;
import java.util.Set;


public class SMTPClient {

	//Encoding proposed by RFC 821
	private static Charset messageCharset = null;
	private static CharsetDecoder decoder = null;
	
	private static byte [] clientName = null;
	
	private static byte [] heloMsg = null;
	private static byte [] mailFromMsg = null;
	private static byte [] rcptToMsg = null;
	private static byte [] dataMsg = null;
	private static byte [] helpMsg = null;
	private static byte [] quitMsg = null;
	private static byte [] crnlMsg = null;
	
	
	private static byte [] dummyAddress1 = null;
	private static byte [] dummyAddress2 = null;
	private static byte [] dummyAddress3 = null;
	private static byte [] dummyAddress4 = null;
	
	private static byte [] messageChars = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', ' '};
	
	
	/**
	 * Converts request strings to byte arrays
	 */
	private static void initMessages() {
		
		heloMsg = new String("HELO ").getBytes(messageCharset);
		crnlMsg = new String("\r\n").getBytes(messageCharset);
		mailFromMsg = new String("MAIL FROM: ").getBytes(messageCharset);
		rcptToMsg = new String("RCPT TO: ").getBytes(messageCharset);
		dataMsg = new String("DATA").getBytes(messageCharset);
		quitMsg = new String("QUIT").getBytes(messageCharset);
		helpMsg = new String("HELP").getBytes(messageCharset);
	}
	
	/**
	 * Converts a set of dummy e-mail address strings to byte arrays
	 */
	private static void initDummyAddresses() {
		
		dummyAddress1 = new String("abc@def.edu").getBytes(messageCharset);
		dummyAddress2 = new String("ghi@jkl.com").getBytes(messageCharset);
		dummyAddress3 = new String("nmo@pqr.gov").getBytes(messageCharset);
		dummyAddress4 = new String("stu@vwx.de").getBytes(messageCharset);		
	}
	
	/**
	 * Decides whether help request is supposed to be sent next
	 * 
	 * @return <code>true</code> if help command is supposed to be sent next, <code>false</code> otherwise
	 */
	private static boolean sendHelpNext() {
		
		if(Math.random() < 0.2)
			return true;
		
		return false;
	}
	
	/**
	 * Generates a message consisting of a random sequence of characters with a length between 300 and 500 bytes
	 * 
	 * @return the generates message as a byte array
	 */
	private static byte [] generateRandomMessage() {
	
		int len = (int) (Math.random() * 200) + 300;
		byte [] endSequence = {'\r', '\n', '.' ,'\r' , '\n'};
		
		byte [] message = new byte[len+5];
		
		for(int i = 0; i < len; i++)
			message[i] = messageChars[(int) (Math.random() * messageChars.length)];
		
		for(int i = 0; i < endSequence.length; i++)
			message[len + i] = endSequence[i];
		
		return message;
	}
	
	/**
	 * Draws a previously defined dummy e-mail address from a set and returns it as a byte array
	 * 
	 * @return a random dummy e-mail address as a byte array
	 */
	private static byte [] getRandomEmailAddress() {
		
		int rand = (int) (Math.random() * 4);
		
		switch(rand) {
		case 0:
			return dummyAddress1;
		case 1:
			return dummyAddress2;
		case 2:
			return dummyAddress3;
		}
		
		return dummyAddress4;
	}
	
	/**
	 * Extracts an SMTP server response from a socket channel and writes it into a byte buffer
	 * 
	 * @param socketChannel the socket channel to read from
	 * @param buffer the buffer to write to 
	 * @return <code>true</code> if buffer already contains entire server response, <code>false</code> if further data is expected to follow
	 * @throws IOException
	 */
	private static boolean readCommandLine(SocketChannel socketChannel, ByteBuffer buffer) throws IOException {
		
		boolean foundHyphen = false;
		int pos = buffer.position();
		
		socketChannel.read(buffer);
		
		for(int i = pos; i < buffer.position(); i++) {
			
			if(buffer.get(i) == '-' && (i == 3))
			{
				foundHyphen = true;
			}
			
			if(buffer.get(i) == '\n') {
				if((i-1) >= 0 && buffer.get(i-1) == '\r') {
					if(foundHyphen) {
						foundHyphen = false;
					} else {
						buffer.flip();
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Outputs the content of a byte buffer for debugging purposes
	 * 
	 * @param buffer the buffer to read from
	 */
	private static void printBuffer(ByteBuffer buffer) {
		
		buffer.position(0);
		
		CharBuffer cb = null;
		try {
			cb = decoder.decode(buffer);
		} catch (CharacterCodingException e) {
			System.err.println("Cannot show buffer content. Character coding exception...");
			return;
		}
		
		System.out.println(cb);
	}
	
	/**
	 * Explains how to use this program
	 * 
	 */
	private static void printUsage() {
		
		System.err.println("Usage: java SMTPClient <address> <port>");
	}
	
	/**
	 * Retrieves the SMTP server's response code from a command line inside a byte buffer
	 * 
	 * @param buffer the buffer that contains the command line
	 * @return the response code extracted from the buffer, <code>-1</code> if no response code could be determined
	 */
	private static int getResponseCode(ByteBuffer buffer) {
		
		int blank = -1;
		
		for(int i = 0; i < buffer.limit(); i++) {
			if((buffer.get(i) == ' ') || buffer.get(i) == '-') {
				blank = i;
				break;
			}
		}
		
		if(blank == -1)
			return -1;
		
		//Convert byte string with ASCII characters to integer
		int code = -1;
		
		for(int i = 0; i < blank; i++) {
			int digit = buffer.get(i) - 48; //48 is offset in ASCII table
			int factor = 1;
			for(int j = (blank-i-2); j >= 0 ; j--)
				factor *= 10;
			
			if(code == -1)
				code = (digit * factor);
			else
				code += (digit * factor);
		}
		
		return code;
	}
	
	/**
	 * Generates a dummy e-mail message and attaches it to an SMTPClientState object
	 * 
	 * @param state the state object to attach the generated message to
	 */
	private static void generateMail(SMTPClientState state) {
		
		state.setFrom(getRandomEmailAddress());
		state.setTo(getRandomEmailAddress());
		state.setMessage(generateRandomMessage());
	}
	
	/**
	 * Sends the "HELO" command through the given channel
	 * 
	 * @param clientChannel the channel to send the command to
	 * @param buffer the buffer to store the command
	 * @throws IOException
	 */
	private static void sendHelo(SocketChannel clientChannel, ByteBuffer buffer) throws IOException {
		
		buffer.clear();
		
		buffer.put(heloMsg);
		buffer.put(clientName);
		buffer.put(crnlMsg);
		
		buffer.flip();
		
		clientChannel.write(buffer);
		
		buffer.clear();
	}
	
	/**
	 * Sends the "MAIL FROM" command through the given channel
	 * 
	 * @param clientChannel the channel to send the command to
	 * @param buffer the buffer to store the command
	 * @param mailFrom the byte array containing the sender's e-mail address
	 * @throws IOException
	 */
	private static void sendMailFrom(SocketChannel clientChannel, ByteBuffer buffer, byte [] mailFrom) throws IOException {
		
		buffer.clear();
		
		buffer.put(mailFromMsg);
		buffer.put(mailFrom);
		buffer.put(crnlMsg);
		
		buffer.flip();
		
		clientChannel.write(buffer);
		
		buffer.clear(); 
	}
	
	/**
	 * Sends the "RCPT TO" command through the given channel
	 * 
	 * @param clientChannel the channel to send the command to
	 * @param buffer the buffer to store the command
	 * @param rcptTo the byte array containing the recipient's e-mail address
	 * @throws IOException
	 */
	private static void sendRcptTo(SocketChannel clientChannel, ByteBuffer buffer, byte [] rcptTo) throws IOException {
		
		buffer.clear();
		
		buffer.put(rcptToMsg);
		buffer.put(rcptTo);
		buffer.put(crnlMsg);
		
		buffer.flip();
		
		clientChannel.write(buffer);
		
		buffer.clear();
	}
	
	/**
	 * Sends the "DATA" command through the given channel
	 * 
	 * @param clientChannel the channel to send the command to
	 * @param buffer the buffer to store the command
	 * @throws IOException
	 */
	private static void sendData(SocketChannel clientChannel, ByteBuffer buffer) throws IOException {
		
		buffer.clear();
		
		buffer.put(dataMsg);
		buffer.put(crnlMsg);
		
		buffer.flip();
		
		clientChannel.write(buffer);
		
		buffer.clear();
	}
	
	/**
	 * Sends the generated message through the given channel
	 * 
	 * @param clientChannel the channel to send the command to
	 * @param buffer the buffer to store the message
	 * @param message the message to be transfered
	 * @throws IOException
	 */
	private static void sendMessage(SocketChannel clientChannel, ByteBuffer buffer, byte [] message) throws IOException {
		
		buffer.clear();
		
		buffer.put(message);
		buffer.flip();
		
		clientChannel.write(buffer);
		
		buffer.clear();
	}
	
	/**
	 * Sends the "QUIT" command through the given channel
	 * 
	 * @param clientChannel the channel to send the command to
	 * @param buffer the buffer to store the command
	 * @throws IOException
	 */
	private static void sendQuit(SocketChannel clientChannel, ByteBuffer buffer) throws IOException {
		
		buffer.clear();
		
		buffer.put(quitMsg);
		buffer.put(crnlMsg);
		buffer.flip();
		
		clientChannel.write(buffer);
		
		buffer.clear();
	}
	
	/**
	 * Sends the "HELP" command through the given channel
	 * 
	 * @param clientChannel the channel to send the command to
	 * @param buffer the buffer to store the command
	 * @throws IOException
	 */
	private static void sendHelp(SocketChannel clientChannel, ByteBuffer buffer) throws IOException {
		
		buffer.clear();
		
		buffer.put(helpMsg);
		buffer.put(crnlMsg);
		buffer.flip();
		
		clientChannel.write(buffer);
		
		buffer.clear();
	}
	
	/**
	 * Prints the content of the given byte buffer, the last response code and exits the program
	 * 
	 * @param channel the channel to be closed before the program quits
	 * @param buffer the buffer content that could not be processed
	 * @param responseCode the retrieved response code
	 * @throws IOException
	 */
	private static void debugAndExit(SocketChannel channel, ByteBuffer buffer, int responseCode) throws IOException {
		
		printBuffer(buffer);
		System.err.print("Unexpected response code " + responseCode + ", exiting...");
		channel.close();
		System.exit(1);
		
	}
	
	/**
	 * Main program loop
	 * 
	 * @param args the parameters to start the program with
	 */
	public static void main(String [] args) {
		
		SocketChannel clientChannel = null;
		InetSocketAddress remoteAddress = null;
		Selector selector = null;
		
		try {
			messageCharset = Charset.forName("US-ASCII");
		} catch(UnsupportedCharsetException uce) {
			System.err.println("Cannot create charset for this application. Exiting...");
			System.exit(1);
		}
		
		decoder = messageCharset.newDecoder();
		initMessages();
		initDummyAddresses();
		
		try {
			clientName = java.net.InetAddress.getLocalHost().getHostName().getBytes(messageCharset);
		} catch (UnknownHostException e) {
			System.err.println("Cannot determine name of host. Exiting...");
			System.exit(1);
		}
		
		
		if(args.length != 2) {
			printUsage();
			System.exit(1);
		}
		
		try {
			remoteAddress = new InetSocketAddress(args[0], Integer.parseInt(args[1]));
		} catch(IllegalArgumentException e) {
			printUsage();
			System.exit(1);
		} catch(SecurityException e) {
			printUsage();
			System.exit(1);
		}
		
		System.out.println("Connecting to SMTP server " + args[0] + ":" + args[1]);
		
		try {
			selector = Selector.open();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		
		
		try {
			clientChannel = SocketChannel.open();
			clientChannel.configureBlocking(false);
			clientChannel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
			clientChannel.connect(remoteAddress);
			
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		while(true)
		{
			try {
				if(selector.select() == 0)
					continue;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Set<SelectionKey> selectedKeys = selector.selectedKeys();
			Iterator<SelectionKey> iter = selectedKeys.iterator();
			
			while(iter.hasNext()) {
				
				SelectionKey key = iter.next();
				
				/*System.out.println(key.toString());
				System.out.println(key.readyOps());*/
				
				try {
					
					if(key.isConnectable())
					{
						SocketChannel channel = (SocketChannel) key.channel();
						channel.finishConnect();
						SMTPClientState state = new SMTPClientState();
						generateMail(state);
						key.attach(state);
					}
					
					if(key.isReadable())
					{
						SMTPClientState state = (SMTPClientState) key.attachment();
						SocketChannel channel = (SocketChannel) key.channel();
						
						if(readCommandLine(channel, state.getByteBuffer()))
							continue;
						
						
						
						int responseCode = getResponseCode(state.getByteBuffer());
						System.out.println("Received response code: " + responseCode);
						printBuffer(state.getByteBuffer());
						
						if(state.getState() == SMTPClientState.HELPSENT)
						{
							if(responseCode == 214)
							{
								switch(state.getPreviousState()) {
								case SMTPClientState.CONNECTED:
									sendHelo(channel, state.getByteBuffer());
									state.setState(SMTPClientState.RECEIVEDWELCOME);
									break;
								case SMTPClientState.RECEIVEDWELCOME:
									sendMailFrom(channel, state.getByteBuffer(), state.getFrom());
									state.setState(SMTPClientState.MAILFROMSENT);
									break;
								case SMTPClientState.MAILFROMSENT:
									sendRcptTo(channel, state.getByteBuffer(), state.getTo());
									state.setState(SMTPClientState.RCPTTOSENT);
									break;
								case SMTPClientState.RCPTTOSENT:
									sendData(channel, state.getByteBuffer());
									state.setState(SMTPClientState.DATASENT);
									break;
								case SMTPClientState.MESSAGESENT:
									sendQuit(channel, state.getByteBuffer());
									state.setState(SMTPClientState.QUITSENT);
									break;
								}
							}
							else
							{
								debugAndExit(channel, state.getByteBuffer(), responseCode);
							}
							
							continue;
						}
						
						switch(state.getState())
						{
						case SMTPClientState.CONNECTED:
							if(responseCode == 220) {
								if(sendHelpNext()) {
									sendHelp(channel, state.getByteBuffer());
									state.setPreviousState(state.getState());
									state.setState(SMTPClientState.HELPSENT);									
								}
								else {
									sendHelo(channel, state.getByteBuffer());
									state.setState(SMTPClientState.RECEIVEDWELCOME);
								}
							} else {
								debugAndExit(channel, state.getByteBuffer(), responseCode);
							}
							break;
						case SMTPClientState.RECEIVEDWELCOME:
							if(responseCode == 250) {
								if(sendHelpNext()) {
									sendHelp(channel, state.getByteBuffer());
									state.setPreviousState(state.getState());
									state.setState(SMTPClientState.HELPSENT);									
								}
								else {
									sendMailFrom(channel, state.getByteBuffer(), state.getFrom());
									state.setState(SMTPClientState.MAILFROMSENT);
								}
							} else {
								debugAndExit(channel, state.getByteBuffer(), responseCode);
							}
							break;
						case SMTPClientState.MAILFROMSENT:
							if(responseCode == 250) {
								if(sendHelpNext()) {
									sendHelp(channel, state.getByteBuffer());
									state.setPreviousState(state.getState());
									state.setState(SMTPClientState.HELPSENT);									
								}
								else {
									sendRcptTo(channel, state.getByteBuffer(), state.getTo());
									state.setState(SMTPClientState.RCPTTOSENT);
								}
							} else {
								debugAndExit(channel, state.getByteBuffer(), responseCode);
							}
							break;
						case SMTPClientState.RCPTTOSENT:
							if(responseCode == 250) {
								if(sendHelpNext()) {
									sendHelp(channel, state.getByteBuffer());
									state.setPreviousState(state.getState());
									state.setState(SMTPClientState.HELPSENT);									
								}
								else {
									sendData(channel, state.getByteBuffer());
									state.setState(SMTPClientState.DATASENT);
								}
							} else {
								debugAndExit(channel, state.getByteBuffer(), responseCode);
							}
							break;
						case SMTPClientState.DATASENT:
							if(responseCode == 354) {
								sendMessage(channel, state.getByteBuffer(), state.getMessage());
								state.setState(SMTPClientState.MESSAGESENT);
							} else {
								debugAndExit(channel, state.getByteBuffer(), responseCode);
							}
							break;
						case SMTPClientState.MESSAGESENT:
							if(responseCode == 250) {
								if(sendHelpNext()) {
									sendHelp(channel, state.getByteBuffer());
									state.setPreviousState(state.getState());
									state.setState(SMTPClientState.HELPSENT);									
								}
								else {
									sendQuit(channel, state.getByteBuffer());
									state.setState(SMTPClientState.QUITSENT);
								}
							} else {
								debugAndExit(channel, state.getByteBuffer(), responseCode);
							}
							break;
						case SMTPClientState.QUITSENT:
							if(responseCode == 221) {
								if(!key.isValid())
									System.out.println("Connection closed by foreign host");
									key.cancel();
									key.channel().close();
									System.exit(0);
							} else {
								debugAndExit(channel, state.getByteBuffer(), responseCode);	
							}
							break;
							
						}
						
						
						
					}
					
				} catch(IOException ioe) {
					ioe.printStackTrace();
					System.exit(1);
				}
				
				iter.remove();
			}
		}
		
	}
	
}
