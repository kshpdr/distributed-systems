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



        try {
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        }catch (Exception e) {
            e.printStackTrace();
        }



        while(true){
            if(selector.select() == 0){
                continue;
            }

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();


            while(iter.hasNext()){
                SelectionKey key = iter.next();


                //check ready set
                //...

                if(key.isAcceptable()){
                    ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

                    SocketChannel client = serverSocketChannel.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);
                }

                if(key.isReadable()){
                    ByteBuffer buf = ByteBuffer.allocate(1024);

                    SocketChannel channel = (SocketChannel) key.channel();

                    channel.read(buf);

                    buf.flip();

                    //Teste nur bisschen herum


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
                    System.out.println(s);
                    continue;
                }

                iter.remove();
            }
        }
    }


}
