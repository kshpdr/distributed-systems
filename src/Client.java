import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

public class Client {
    public static void main(String[] args) throws IOException {
        SocketChannel client = SocketChannel.open(new InetSocketAddress("localhost", 8192));
        ByteBuffer byteBuffer = ByteBuffer.allocate(256);
        String message = "Hello Server";
        Charset messageCharset = null;

        /*
        try {
            messageCharset = Charset.forName("US-ASCII");
        }catch (UnsupportedCharsetException e){
            e.printStackTrace();
        }

        byte []bytes = message.getBytes(messageCharset);
        byteBuffer.put(bytes);
        */

        byteBuffer = ByteBuffer.wrap(message.getBytes());

        try {
            client.write(byteBuffer);
            byteBuffer.clear();
            System.out.println("Message sent");
        }catch (IOException e){
            e.printStackTrace();
        }

        client.close();
    }
}
