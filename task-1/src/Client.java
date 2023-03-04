import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

public class Client {
    public static void main(String[] args) throws IOException {
        SocketChannel client = SocketChannel.open(new InetSocketAddress("localhost", 8192));
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        String message = "Hello Server";
        Charset messageCharset = null;

        byte[] bytes = message.getBytes("US-ASCII");
        byteBuffer = ByteBuffer.wrap(bytes);

        try {
            client.write(byteBuffer);
            byteBuffer.clear();
            System.out.println("Message sent");
        }catch (IOException e){
            e.printStackTrace();
        }

        client.close();
    }



    //ZUR TESTZWECKEN

    static int count = 0;
    //Führe main Methode aus
    static void start() throws IOException {
        System.out.println("Client has started");
        count++;


        main(null);

    }
}
