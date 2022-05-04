import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.Iterator;
import java.util.Set;





class State {
    public final static int CONNECTED = 0;
    public final static int SENTWELCOME = 1;
    public final static int MAILFROMRECEIVED = 2;
    public final static int RCPTTORECEIVED = 3;
    public final static int DATARECEIVED = 4;
    public final static int MESSAGERECEIVED = 5;
    public final static int QUITSENT = 6;
    public final static int HELPRECEIVED = 7;

    private int state;
    private ByteBuffer byteBuffer;
    private int previousState;
    private byte [] from;
    private byte [] to;
    private byte [] message;
    private String messageString = "";

    public State(){
        this.state = CONNECTED;
        this.byteBuffer = ByteBuffer.allocate(8);
    }

    public int getState(){
        return this.state;
    }

    public void setState(int state){
        this.state = state;
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public byte[] getFrom() {
        return from;
    }

    public void setFrom(byte[] from) {
        this.from = from;
    }

    public byte[] getTo() {
        return to;
    }

    public void setTo(byte[] to) {
        this.to = to;
    }

    public byte[] getMessage() {
        return message;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    public void extendMessageString(String messageString) {this.messageString += messageString;}

    public int getPreviousState() {
        return previousState;
    }

    public void setPreviousState(int previousState) {
        this.previousState = previousState;
    }
}

public class Server {

    public final static String HELPESSAGE = "214 \r\n";
    public final static String WELCOMEMESSAGE = "220 \r\n";
    public final static String OKMESSAGE = "250 \r\n";
    public final static String CLOSINGMESSAGE = "221 \r\n";
    public final static String STARTMAILINPUTMESSAGE = "354 \r\n";


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

            //arbeite jedes key ab
            while(iter.hasNext()){
                SelectionKey key = iter.next();
//                System.out.println(key.toString());
//                System.out.println(key.interestOps());
//                System.out.println(key.readyOps());


                if(key.isAcceptable()){
                    ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                    SocketChannel client = serverSocketChannel.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);


                    //Speichere den Status jedes Keys ab, um zu unterscheiden
                    State state = new State();
                    key.attach(state);

                    System.out.println("Connection established");

                }

                if(key.isReadable()){
                    State state = null;
                    String s = read(key);
                    SocketChannel channel = (SocketChannel) key.channel();

                    System.out.println("Message received: " + s);
                    Charset messageCharset = Charset.forName("US-ASCII");

                    //TODO: HELO, MAIL FROM, RCPT TO, DATA, QUIT, HELP missing
                    if (s.startsWith("HELO")){
                        sendMessage(channel, state.getByteBuffer(), OKMESSAGE.getBytes(messageCharset));
                    }

                    if (s.startsWith("MAIL FROM")){
                        System.out.println(s.substring(11));
                        byte[] from = s.substring(11).getBytes(messageCharset);
                        state.setFrom(from);
                        state.setState(State.MAILFROMRECEIVED);

                        sendMessage(channel, state.getByteBuffer(), OKMESSAGE.getBytes(messageCharset));
                    }

                    if (s.startsWith("HELP")){
                        state.setState(State.HELPRECEIVED);
                        sendMessage(channel, state.getByteBuffer(), HELPESSAGE.getBytes(messageCharset));
                    }

                    if (s.startsWith("RCPT TO:")){
                        System.out.println(s.substring(9));
                        byte[] to = s.substring(9).getBytes(messageCharset);
                        state.setTo(to);
                        state.setState(State.RCPTTORECEIVED);

                        sendMessage(channel, state.getByteBuffer(), OKMESSAGE.getBytes(messageCharset));
                    }

                    if (s.startsWith("DATA")){
                        state.setState(State.DATARECEIVED);
                        sendMessage(channel, state.getByteBuffer(), STARTMAILINPUTMESSAGE.getBytes(messageCharset));
                    }

                    if (state.getState() == State.DATARECEIVED) {
                        if (s != "\n\r.\n\r"){
                            state.extendMessageString(s);
                        }
                        else if (s == "\n\r.\n\r") {
                            sendMessage(channel, state.getByteBuffer(), OKMESSAGE.getBytes(messageCharset));
                            state.setState(State.MESSAGERECEIVED);
                        }
                    }

                }

                if(key.isWritable()){
                    SocketChannel channel = (SocketChannel) key.channel();
                    State state = null;
                    String s = read(key);
                    Charset messageCharset = Charset.forName("US-ASCII");

                    //Wenn key schon ein Status bekommen hatte, dann speichere diesen, ansonsten gib dem key einen neuen Status
                    if(key.attachment() != null) {
                        state = (State) key.attachment();
                    }else{
                        state = new State();
                        key.attach(state);
                    }


                    //Wenn Client zum ersten mal connected, schicke eine Willkommensnachricht
                    if(state.getState() == State.CONNECTED){ //CONNECTED:
                        //TODO: Sende "220" an den Client
                        sendOpeningMessage(channel, state.getByteBuffer());
                        state.setState(State.SENTWELCOME);
                        getCommand(channel, state.getByteBuffer());
                    }

                    //TODO: HELO, MAIL FROM, RCPT TO, DATA, QUIT, HELP missing
                    if (s.startsWith("HELO")){
                        sendMessage(channel, state.getByteBuffer(), OKMESSAGE.getBytes(messageCharset));
                    }

                    if (s.startsWith("MAIL FROM")){
                        System.out.println(s.substring(11));
                        byte[] from = s.substring(11).getBytes(messageCharset);
                        state.setFrom(from);
                        state.setState(State.MAILFROMRECEIVED);

                        sendMessage(channel, state.getByteBuffer(), OKMESSAGE.getBytes(messageCharset));
                    }

                    if (s.startsWith("HELP")){
                        state.setState(State.HELPRECEIVED);
                        sendMessage(channel, state.getByteBuffer(), HELPESSAGE.getBytes(messageCharset));
                    }

                    if (s.startsWith("RCPT TO:")){
                        System.out.println(s.substring(9));
                        byte[] to = s.substring(9).getBytes(messageCharset);
                        state.setTo(to);
                        state.setState(State.RCPTTORECEIVED);

                        sendMessage(channel, state.getByteBuffer(), OKMESSAGE.getBytes(messageCharset));
                    }

                    if (s.startsWith("DATA")){
                        state.setState(State.DATARECEIVED);
                        sendMessage(channel, state.getByteBuffer(), STARTMAILINPUTMESSAGE.getBytes(messageCharset));
                    }

                    if (state.getState() == State.DATARECEIVED) {
                        if (s != "\n\r.\n\r"){
                            state.extendMessageString(s);
                        }
                        else if (s == "\n\r.\n\r") {
                            sendMessage(channel, state.getByteBuffer(), OKMESSAGE.getBytes(messageCharset));
                            state.setState(State.MESSAGERECEIVED);
                        }
                    }
                }

                iter.remove();
            }
        }
    }
    private static void getCommand(SocketChannel channel, ByteBuffer buffer) throws IOException {
        channel.read(buffer);
        printBuffer(buffer);
    }

    private static String read(SelectionKey key) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(8192);
        SocketChannel channel = (SocketChannel) key.channel();
        channel.read(buf);
        buf.flip();

        Charset messageCharset = null;
        try {
            messageCharset = Charset.forName("US-ASCII");
        }catch (UnsupportedCharsetException e){
            e.printStackTrace();
        }

        CharsetDecoder decoder = messageCharset.newDecoder();
        CharBuffer charBuffer = null;
        try {
            charBuffer = decoder.decode(buf);
        }catch (CharacterCodingException e){
            e.printStackTrace();
        }

        return charBuffer.toString();
    }

    private static void sendMessage(SocketChannel clientChannel, ByteBuffer buffer, byte [] message) throws IOException {

        buffer.clear();

        buffer.put(message);
        buffer.flip();

        clientChannel.write(buffer);

        buffer.clear();
    }


    private static void sendOpeningMessage(SocketChannel clientChannel, ByteBuffer byteBuffer) throws UnsupportedEncodingException {

        //Encode String zu Bytes
        try {
            byteBuffer.clear();
            byte[] bytes = WELCOMEMESSAGE.getBytes("US-ASCII");
            byteBuffer.put(bytes);
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }


        //Verschicke die Bytes
        try {
            System.out.print("Sent: ");
            printBuffer(byteBuffer);
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
