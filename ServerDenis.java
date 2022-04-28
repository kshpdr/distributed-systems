import java.net.InetSocketAddress;
import java.nio.*;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

// For my purposes, will delete it then
public class ServerDenis {
    ServerDenis(InetSocketAddress listenAddress) {
        // start event loop
        while (true) {
            try {
                // register server socket with selector for listening
                Selector selector = Selector.open();
                ServerSocketChannel servSock = ServerSocketChannel.open();
                servSock.configureBlocking(false);
                servSock.register(selector, SelectionKey.OP_ACCEPT);
                servSock.socket().bind(listenAddress);

                selector.selectNow();
                if (selector.select() == 0) continue;

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();

                while (iter.hasNext()){
                    SelectionKey key = iter.next();
                    // code here
                    if (key.isAcceptable()){
                        //
                    }
                    if (key.isReadable()){
                        ByteBuffer buf = ByteBuffer.allocate(1024);
                        SocketChannel channel = key.channel();
                        channel.read();
                        buf.flip();
                        ///
                    }


                    iter.remove();
                }
                selector.keys.clear();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new ServerDenis(new InetSocketAddress("localhost", 8080));
    }
}
