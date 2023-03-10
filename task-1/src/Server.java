import java.io.*;
import java.net.InetSocketAddress;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.Random;
import java.nio.file.*;




class State {
    public final static int CONNECTED = 0;
    public final static int RECEIVEDWELCOME = 1;
    public final static int HELOSENT = 2;
    public final static int HELOREAD = 3;
    public final static int MAILFROMSENT = 4;
    public final static int MAILFROMREAD = 5;
    public final static int RCPTTOSENT = 6;
    public final static int RCPTTOREAD = 7;
    public final static int DATASENT = 8;
    public final static int DATAREAD = 9;
    public final static int MESSAGESENT = 10;
    public final static int MESSSAGEREAD = 11;
    public final static int QUITSENT = 12;
    public final static int DEAD = 13;
    public final static int HELPSENT = 14;

    private int state;
    private int previousState;
    private ByteBuffer byteBuffer;

    private String sender;
    private String receiver;
    private String message;

    public State(){
        this.state = CONNECTED;
        this.previousState = CONNECTED;
        this.byteBuffer = ByteBuffer.allocate(8192);
        this.sender = "";
        this.receiver = "";
        this.message = "";
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getState(){
        return this.state;
    }

    public int getPreviousState(){
        return previousState;
    }

    public void setState(int state){
        if(this.state != HELPSENT){
            previousState = this.state;
        }

        this.state = state;

    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public void superClear(){
        byteBuffer = ByteBuffer.allocate(8192);
    }
}

public class Server {


    public final static String WELCOMEMESSAGE = "220 ";
    public final static String OKMESSAGE = "250 ";
    public final static String CLOSINGMESSAGE = "221 ";
    public final static String STARTMAILINPUTMESSAGE = "354 ";
    public final static String ERRORMESSAGE = "503 bad sequence of commands";
    public final static String HELPMESSAGE =
            "214 \n" +
                    "HELO: initiate MAIL\n" +
                    "MAIL FROM:<sender@example.org> : provide sender-address\n" +
                    "RCPT TO:<receiver@example.com> : provide receiver-address\n" +
                    "DATA: initiate writing\n" +
                    "QUIT: close the connection\n";





    public static void main(String[] args) throws Exception {

        //initialisiere Selektor, Server-Socket
        Selector selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();

        //Binde Server-Socket and "localhost" in Port 8192
        serverSocket.bind(new InetSocketAddress("localhost", 8192));
        serverSocket.configureBlocking(false);


        //Registriere Server-Socket auf Selektor und h??re auf das Event: "OP_ACCEPT"
        try {
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        }catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }


        //Lasse den Server laufen
        while(true){


            //Fahre fort, wenn keine Elemente im Ready-Set sind
            if(selector.select() == 0){
                continue;
            }


            //Sammle alle keys
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();

            //arbeite jeden key ab
            while(iter.hasNext()){
                SelectionKey key = iter.next();


                if(key.isAcceptable()){
                    ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                    SocketChannel client = serverSocketChannel.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);


                }


                if(key.isReadable()){

                    SocketChannel channel = (SocketChannel) key.channel();
                    State state = null;

                    //Wenn key schon ein Status bekommen hatte, dann speichere diesen, ansonsten gib dem key einen neuen Status
                    if(key.attachment() != null) {
                        state = (State) key.attachment();
                    }else {
                        state = new State();
                        key.attach(state);
                    }


                    String s = readMessage(channel, state.getByteBuffer());
                    //                                         SMTPClient                 netcat                       SMTPClient                 netcat
                    boolean NOTQUITANDNOTHELP = !(s.startsWith("QUIT\r\n") || s.startsWith("QUIT\n") || s.startsWith("HELP\r\n") || s.startsWith("HELP\n"));

                    // LISTEN FOR HELO
                    if (state.getState() == State.RECEIVEDWELCOME) {

                        //System.out.println("Message received: " + s);

                        //Best??tigung HELO Client
                        if (s.startsWith("HELO")) {
                            state.setState(State.HELOSENT);
                            System.out.println("Received HELO...Setting state to HELOSENT " + s);

                        }else if(NOTQUITANDNOTHELP){
                            sendMessage(channel, state.getByteBuffer(), ERRORMESSAGE + "\r\n");
                            state.superClear();
                        }
                    }


                    // LISTEN FOR MAIL FROM: <address>
                    if(state.getState() == State.HELOREAD){
                        if (s.startsWith("MAIL FROM")){
                            state.setState(State.MAILFROMSENT);
                            System.out.println("Received MAIL FROM...Setting state to MAILFROMSENT " + s);

                            //save the sender-address
                            String content = getSenderContent(s);
                            state.setSender(content);
                        }else if(NOTQUITANDNOTHELP){
                            sendMessage(channel, state.getByteBuffer(), ERRORMESSAGE + "\r\n");
                            state.superClear();
                        }
                    }



                    // LISTEN FOR RCPT TO: <address>
                    if(state.getState() == State.MAILFROMREAD){
                        if (s.startsWith("RCPT TO")){
                            state.setState(State.RCPTTOSENT);
                            System.out.println("Received RCPT TO...Setting state to RCPTTO " + s);

                            //save the receiver-address
                            String content = getReceiverContent(s);
                            state.setReceiver(content);
                        }else if(NOTQUITANDNOTHELP){
                            sendMessage(channel, state.getByteBuffer(), ERRORMESSAGE + "\r\n");
                            state.superClear();
                        }
                    }


                    // LISTEN FOR DATA
                    if (state.getState() == State.RCPTTOREAD){
                        if (s.startsWith("DATA")){
                            state.setState(State.DATASENT);
                            System.out.println("Received DATASENT...Setting state to DATA " + s);
                        }else if(NOTQUITANDNOTHELP){
                            sendMessage(channel, state.getByteBuffer(), ERRORMESSAGE + "\r\n");
                            state.superClear();
                        }
                    }


                    // LISTEN FOR MESSAGE
                    if (state.getState() == State.DATAREAD){
                        System.out.println(s);

                        //checke nur ob nicht vielleicht ein HELP gesendet wurde
                        if(NOTQUITANDNOTHELP){
                            state.setState(State.MESSAGESENT);
                            //save the message

                            //System.out.println("Message saved");
                            state.setMessage(s);
                            state.superClear();
                        }



                    }


                    // LISTEN FOR HELP
                    //              SMTPClient                  netcat
                    if(s.startsWith("HELP\r\n") || s.startsWith("HELP\n")){
                        state.setState(State.HELPSENT);
                        System.out.println("Received HELP...Setting state to HELPSENT " + s);

                    }


                    // LISTEN FOR QUIT
                    //              SMTPClient                  netcat
                    if(s.startsWith("QUIT\r\n") || s.startsWith("QUIT\n")){
                        state.setState(State.QUITSENT);
                        System.out.println("Received QUIT...Setting state to QUITSENT " + s);

                    }



                }


                if(key.isWritable()){


                    SocketChannel channel = (SocketChannel) key.channel();
                    State state = null;

                    //Wenn key schon ein Status bekommen hatte, dann speichere diesen, ansonsten gib dem key einen neuen Status
                    if(key.attachment() != null) {
                        state = (State) key.attachment();
                    }else{
                        state = new State();
                        key.attach(state);
                    }


                    //Wenn Client zum ersten mal connected, schicke eine Willkommensnachricht
                    if(state.getState() == State.CONNECTED){ // CONNECTED:

                        //Willkommensnachricht wird nur einmal pro Client geschickt
                        sendMessage(channel, state.getByteBuffer(), WELCOMEMESSAGE+ "\r\n");
                        state.setState(State.RECEIVEDWELCOME);
                    }

                    // SEND HELP
                    if(state.getState() == State.HELPSENT){
                        sendMessage(channel, state.getByteBuffer(), HELPMESSAGE + "\r\n");

                        //nuke
                        state.superClear();
                        state.setState(state.getPreviousState());
                    }


                    // SEND OK AFTER HELO
                    if(state.getState() == State.HELOSENT){ // HELO:
                        // Server best??tigt Client mit dem String 250
                        sendMessage(channel, state.getByteBuffer(), OKMESSAGE + "\r\n");

                        state.setState(State.HELOREAD);
                    }

                    // SEND OK AFTER MAIL FROM
                    if(state.getState() == State.MAILFROMSENT){ // MAIL FROM:
                        sendMessage(channel, state.getByteBuffer(), OKMESSAGE + "\r\n");
                        state.setState(State.MAILFROMREAD);
                    }

                    // SEND OK AFTER RCPT TO
                    if(state.getState() == State.RCPTTOSENT){ // RCPT TO:
                        sendMessage(channel, state.getByteBuffer(), OKMESSAGE + "\r\n");
                        state.setState(State.RCPTTOREAD);
                    }

                    // SEND 354 AFTER DATA
                    if(state.getState() == State.DATASENT){ // DATA:
                        sendMessage(channel, state.getByteBuffer(),  STARTMAILINPUTMESSAGE+ "\r\n");
                        state.setState(State.DATAREAD);
                    }

                    // SEND OK AFTER MESSAGE
                    if(state.getState() == State.MESSAGESENT){ // DATA END:
                        sendMessage(channel, state.getByteBuffer(), OKMESSAGE + "\r\n");
                        state.setState(State.MESSSAGEREAD);
                        saveEmail(state.getSender(), state.getReceiver(), state.getMessage());
                    }

                    // SEND 214 AFTER QUIT
                    if(state.getState() == State.QUITSENT){ // DIE:
                        sendMessage(channel, state.getByteBuffer(), CLOSINGMESSAGE + "\r\n");
                        state.setState(State.DEAD);
                        channel.close();
                    }
                }


                iter.remove();
            }
        }
    }

    private static void saveEmail(String sender, String receiver, String message) throws Exception{
        // Random Zahl f??r Messenger ID
        Random rand = new Random();
        int messengerID = rand.nextInt(10000);
        String messageLastCharsDeleted = message.substring(0, message.length() - 5);

        try {
            Files.createDirectories(Paths.get("emails/" + receiver));

        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            File file = new File("emails/" + receiver  + "/" + sender + "_" + messengerID + ".txt");
            file.createNewFile();
        }catch (Exception e){
            e.printStackTrace();
        }


        int size = messageLastCharsDeleted.getBytes(StandardCharsets.US_ASCII).length;
        ByteBuffer buf = ByteBuffer.allocate(size);
        buf.put(messageLastCharsDeleted.getBytes(StandardCharsets.US_ASCII));
        buf.flip();


        FileOutputStream f;
        f = new FileOutputStream("emails/" + receiver  + "/" + sender + "_" + messengerID + ".txt");
        FileChannel ch = f.getChannel();

        ch.write(buf);

        ch.close();

        buf.clear();


        //removes the "." in the textfile
//        File originalFile = new File("emails/" + receiver  + "/" + sender + "_" + messengerID + ".txt");
//        File tmpFile = new File("tempFile.txt");
//
//        BufferedReader reader = new BufferedReader(new FileReader(originalFile));
//        BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile));
//
//        String lineToRemove = ".";
//        String currentLine;
//
//        while((currentLine = reader.readLine()) != null) {
//            String trimmedLine = currentLine.trim();
//            if(trimmedLine.equals(lineToRemove)) continue;
//            writer.write(currentLine);
//        }
//        writer.close();
//        reader.close();
//        tmpFile.renameTo(originalFile);
    }

    public static String getReceiverContent(String s) {
        String contentWithRN = "";
        String contentWithR = "";
        String content = "";
        String[] arrOfStrRN = s.split("RCPT TO: ");
        contentWithRN = arrOfStrRN[1];
        String[] arrOfStrR = contentWithRN.split("\n");
        contentWithR = arrOfStrR[0];
        String[] arrOfStr = contentWithR.split("\r");
        content = arrOfStr[0];



        return content;
    }

    public static String getSenderContent(String s) {
        String contentWithRN = "";
        String contentWithR = "";
        String content = "";
        String[] arrOfStrRN = s.split("MAIL FROM: ");
        contentWithRN = arrOfStrRN[1];
        String[] arrOfStrR = contentWithRN.split("\n");
        contentWithR = arrOfStrR[0];
        String[] arrOfStr = contentWithR.split("\r");
        content = arrOfStr[0];



        return content;
    }



    private static String readMessage(SocketChannel clientChannel, ByteBuffer byteBuffer) throws IOException {
        String s = "";

        clientChannel.read(byteBuffer);
        //System.out.println("Test");
        byteBuffer.flip();


        //Choose translation standard
        Charset messageCharset = null;
        try {
            messageCharset = Charset.forName("US-ASCII");
        }catch (UnsupportedCharsetException e){
            e.printStackTrace();
        }


        //Decode incoming message
        CharsetDecoder decoder = messageCharset.newDecoder();

        CharBuffer charBuffer = null;
        try {
            charBuffer = decoder.decode(byteBuffer);
        }catch (CharacterCodingException e){
            e.printStackTrace();
        }

        s = charBuffer.toString();

        return s;
    }

    private static void sendMessage(SocketChannel clientChannel, ByteBuffer byteBuffer, String message) throws UnsupportedEncodingException{
        try {
            byteBuffer.clear();
            byte[] bytes = message.getBytes("US-ASCII");
            byteBuffer.put(bytes);
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }

        //Verschicke die Bytes
        try {
            byteBuffer.flip();
            clientChannel.write(byteBuffer);
            //channel.close();
            byteBuffer.clear();
            //System.out.println("Message sent");
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    //Zu debugging-Zwecken
    private static void printBuffer(ByteBuffer buffer) {

        buffer.position(0);
        int formerLimit = buffer.limit();
        //find out limit
        int limit = 0;
        for(int i = 0; i < buffer.limit(); i++){
            if(buffer.get(i) == 0){
                limit = i;
                break;
            }
        }
        buffer.limit(limit);

        //Choose translation standard
        Charset messageCharset = null;
        try {
            messageCharset = Charset.forName("US-ASCII");
        }catch (UnsupportedCharsetException e){
            e.printStackTrace();
        }


        //Decode incoming message
        CharsetDecoder decoder = messageCharset.newDecoder();

        CharBuffer cb = null;
        try {
            cb = decoder.decode(buffer);
        } catch (CharacterCodingException e) {
            System.err.println("Cannot show buffer content. Character coding exception...");
            return;
        }

        System.out.println(cb);

        buffer.limit(formerLimit);
    }


}
