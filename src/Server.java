import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Iterator;
import java.util.Set;

public class Server {
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
                    client.register(selector, SelectionKey.OP_READ);
                }

                if(key.isReadable()){

                    //maximale empfangen Byte-Anzahl
                    ByteBuffer buf = ByteBuffer.allocate(1024);

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

                    String s = charBuffer.toString();

                    //TODO: Hier nur eine Idee wie mans umsetzen könnte
                    switch (s){
                        case "HELO":
                            break;
                        case "MAIL FROM":
                            break;
                        case "RCPT TO":
                            break;
                        case "DATA":
                            break;
                        case "HELP":
                            break;
                        case "QUIT":
                            break;
                    }
                }

                iter.remove();
            }
        }
    }

    //ZUR TESTZWECKEN

    static int count = 0;
    //Führe main Methode aus
    static void start() throws IOException {
        System.out.println("Server has started");
        count++;


        main(null);

    }


}
