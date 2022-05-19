import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class InboxQueueTest {

    @Test
    void writeLogFile() throws FileNotFoundException {
        InboxQueue inboxQueue = new InboxQueue(new ArrayBlockingQueue(10), "T1");

        inboxQueue.writeLogFile(new ExternalMessage("Hello World"));

        File file = new File(inboxQueue.path);
        Scanner sc = new Scanner(file);
        String data = "";
        while (sc.hasNextLine()){
            data = sc.nextLine();
        }
        sc.close();

        boolean bool = data.startsWith("Received message: Hello World at ");
        assertTrue(bool);

    }
}