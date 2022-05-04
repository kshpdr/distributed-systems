import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;





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
    public final static int DATASENTREAD = 9;
    public final static int MESSAGESENT = 10;
    public final static int QUITSENT = 11;
    public final static int HELPSENT = 12;

    private int state;
    private int previousState;
    private ByteBuffer byteBuffer;

    public State(){
        this.state = CONNECTED;
        this.previousState = CONNECTED;
        this.byteBuffer = ByteBuffer.allocate(8192);
    }

    public int getState(){
        return this.state;
    }

    public int getPreviousState(){
        return previousState;
    }

    public void setState(int state){
        if(previousState != State.HELPSENT){
            previousState = this.state;
        }

        this.state = state;

    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }
}

public class Server {


    public final static String WELCOMEMESSAGE = "220 ";
    public final static String OKMESSAGE = "250 ";
    public final static String CLOSINGMESSAGE = "221 ";
    public final static String STARTMAILINPUTMESSAGE = "354 ";
    public final static String HELPMESSAGE =
            "214 " +
            "HELO: initiate MAIL\n" +
            "MAIL FROM:<sender@example.org> : provide sender-address\n" +
            "RCPT TO<receiver@example.com> : provide receiver-address\n" +
            "DATA: initiate writing\n" +
            "QUIT: close the connection\n";


    public static void main(String[] args) throws IOException {

        //initialisiere Selektor, Server-Socket
        Selector selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();

        //Binde Server-Socket and "localhost" in Port 8192
        serverSocket.bind(new InetSocketAddress("localhost", 8192));
        serverSocket.configureBlocking(false);


        //Registriere Server-Socket auf Selektor und höre auf das Event: "OP_ACCEPT"
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


                    //Speichere den Status jedes Keys ab, um zu unterscheiden
                    //State state = new State();
                    //key.attach(state);

                }


                //gelesenes aus dem Buffer
                //Nachrichten gesendet vom client
                if(key.isReadable()){
                    //System.out.println("isReadable");

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
                    if(s.contains("HELP")){
                        state.setState(State.HELPSENT);
                    }

                    if (state.getState() == State.RECEIVEDWELCOME) {

                        System.out.println("Message received: " + s);


                        //HELO
                        //Bestätigung HELO Client
                        if (s.contains("HELO")) {
                            state.setState(State.HELOSENT);
                            System.out.println("Received HELO...Setting state to HELOSENT " + s);
                        }
                    }

                    if (state.getState() == State.HELOREAD){
                            if (s.contains("MAIL FROM")){
                                state.setState(State.MAILFROMSENT);
                                System.out.println("Received MAIL FROM...Setting state to MAILFROMSENT " + s);
                            }
                    }

                    if (state.getState() == State.MAILFROMREAD){
                        if (s.contains("RCPT TO")){
                            state.setState(State.RCPTTOSENT);
                            System.out.println("Received RCPT TO...Setting state to RCPTTO " + s);
                        }
                    }

                    if (state.getState() == State.RCPTTOREAD){
                        if (s.contains("DATA")){
                            state.setState(State.DATASENT);
                            System.out.println("Received DATASENT...Setting state to DATA " + s);
                        }
                    }

                }


                if(key.isWritable()){
                    //System.out.println("isWritable");


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
                    if(state.getState() == State.CONNECTED){ //CONNECTED:

                        //Willkommensnachricht wird nur einmal pro Client geschickt
                        sendMessage(channel, state.getByteBuffer(), WELCOMEMESSAGE+ "\r\n");
                        state.setState(State.RECEIVEDWELCOME);
                    }


                    if(state.getState() == State.HELPSENT){
                        sendMessage(channel, state.getByteBuffer(), HELPMESSAGE + "\r\n");
                        state.setState(state.getPreviousState());
                    }


                    if(state.getState() == State.HELOSENT){ //HELO:
                        // Server bestätigt Client mit dem String 250
                        sendMessage(channel, state.getByteBuffer(), OKMESSAGE + "\r\n");
                        // OKMessage muss nur einmal gesendet werden
                        state.setState(State.HELOREAD);
                    }

                    //System.exit(0);
                    if(state.getState() == State.MAILFROMSENT){
                        sendMessage(channel, state.getByteBuffer(), OKMESSAGE + "\r\n");
                        state.setState(State.MAILFROMREAD);
                    }

                    if(state.getState() == State.RCPTTOSENT){
                        sendMessage(channel, state.getByteBuffer(), OKMESSAGE + "\r\n");
                        state.setState(State.RCPTTOREAD);
                    }

                    if(state.getState() == State.DATASENT){
                        sendMessage(channel, state.getByteBuffer(),  STARTMAILINPUTMESSAGE+ "\r\n");
                        state.setState(State.DATASENTREAD);
                    }
                }


                iter.remove();
            }
        }
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
