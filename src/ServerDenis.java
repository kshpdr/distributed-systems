import de.tu_berlin.cit.SMTPClientState;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.*;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

class StateDenis {
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

    public StateDenis(){
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

public class ServerDenis {
    ServerDenis(InetSocketAddress listenAddress) throws IOException {
        // register server socket with selector for listening
        Selector selector = Selector.open();
        ServerSocketChannel servSock = ServerSocketChannel.open();
        servSock.configureBlocking(false);
        servSock.register(selector, SelectionKey.OP_ACCEPT);
        servSock.socket().bind(listenAddress);
        System.out.println("Listenin on port " + listenAddress.getPort());

        // start event loop
        while (true) {
            try {
                // select all keys
                selector.selectNow();

                if (selector.select() == 0) continue;

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();

                while (iter.hasNext()){
                    SelectionKey key = iter.next();
                    if (!key.isValid()) continue;

                    if (key.isAcceptable()){
                        SocketChannel channel = servSock.accept();
                        if (channel == null) continue;
                        channel.configureBlocking(false);
                        channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        System.out.println("Connection established");

                        StateDenis state = new StateDenis();
                        key.attach(state);
                    }
                    if(key.isReadable()) {
                        StateDenis state = (StateDenis) key.attachment();
                        SocketChannel channel = (SocketChannel) key.channel();

                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        channel.read(buffer);
                        buffer.flip();
                    }

                    if (key.isWritable()) {

                    }


                    iter.remove();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new ServerDenis(new InetSocketAddress("localhost", 8080));
    }
}
