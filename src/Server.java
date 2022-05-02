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
    public final static int MAILFROMSENT = 2;
    public final static int RCPTTOSENT = 3;
    public final static int DATASENT = 4;
    public final static int MESSAGESENT = 5;
    public final static int QUITSENT = 6;
    public final static int HELPSENT = 7;

    private int state;
    private ByteBuffer byteBuffer;

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
}

public class Server {


    public final static String WELCOMEMESSAGE = "220";
    public final static String OKMESSAGE = "250";
    public final static String CLOSINGMESSAGE = "221";
    public final static String STARTMAILINPUTMESSAGE = "354";


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


                if(key.isAcceptable()){
                    ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                    SocketChannel client = serverSocketChannel.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);


                    //Speichere den Status jedes Keys ab, um zu unterscheiden
                    State state = new State();
                    key.attach(state);

                }

                //aus
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
                        sendMessage(channel, state.getByteBuffer(), WELCOMEMESSAGE+ "\r\n");
                        //Willkommensnachricht wird nur einmal pro Client geschickt

                    }



                    //Just EXIT for now, continue working on later
                    //System.exit(0);

                    //TODO: HELO, MAIL FROM, RCPT TO, DATA, QUIT, HELP missing
                    //...

                    //HELO
                    if(state.getState() == State.RECEIVEDWELCOME){
                        // Server bestätigt Client Name mit dem String 250
                        sendMessage(channel, state.getByteBuffer(), OKMESSAGE + "\r\n");
                        // OKMessage muss nur einmal gesendet werden
                        state.setState(State.MAILFROMSENT);
                    }
                   continue;
                   // System.exit(0);


                }


                //gelesenes aus dem Buffer
                //Nachrichten gesendet vom client
                if(key.isReadable()){
                    //System.out.println("isReadable")
                    State state = null;

                    //Wenn key schon ein Status bekommen hatte, dann speichere diesen, ansonsten gib dem key einen neuen Status
                    if(key.attachment() != null) {
                        state = (State) key.attachment();
                    }else{
                        state = new State();
                        key.attach(state);
                    }


                    //maximale empfangen Byte-Anzahl
                    ByteBuffer buf = ByteBuffer.allocate(8192);

                    SocketChannel channel = (SocketChannel) key.channel();

                    channel.read(buf);
                    //System.out.println("Test");
                    buf.flip();


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
                        charBuffer = decoder.decode(buf);
                    }catch (CharacterCodingException e){
                        e.printStackTrace();
                    }

                    String s = charBuffer.toString();



                    System.out.println("Message received: " + s);

                    //HELO

                    //Bestätigung HALO Client
                    if(s.contains("HELO ")) {
                        state.setState(State.RECEIVEDWELCOME);
                        System.out.println("250" + s);
                    }


                    //Just EXIT for now, continue working on later
                    //System.exit(0);
                    continue;
                    //TODO: HELO, MAIL FROM, RCPT TO, DATA, QUIT, HELP missing
                    //...

                }

                iter.remove();
            }
        }
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
            //System.out.print("Sent: ");
            //printBuffer(byteBuffer);
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
