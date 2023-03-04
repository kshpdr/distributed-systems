import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class InboxQueueTest {

    @Test
    void writeLogFile() throws FileNotFoundException {
        InboxQueue inboxQueue = new InboxQueue(new ArrayBlockingQueue(10), new ArrayBlockingQueue(10), "T1");

        ArrayList<String> attachment = new ArrayList<>();
        attachment.add("10");
        inboxQueue.writeLogFile(new InternalMessage("Hello World", attachment));

        File file = new File(inboxQueue.path);
        Scanner sc = new Scanner(file);
        String data = "";
        while (sc.hasNextLine()){
            data = sc.nextLine();
        }
        sc.close();

        boolean bool = data.startsWith("Received message: Hello World with payload: ");
        assertTrue(bool);

    }
}