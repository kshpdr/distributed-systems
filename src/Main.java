import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {



    public static void main(String[] args) {


        Thread messageSequencer = new Thread(new MessageSequencer());
        messageSequencer.start();

        int threadsAmount = Integer.parseInt("5");
        ArrayList<Thread> threads = new ArrayList<>();
        ArrayList<InboxQueue> inboxQueues = new ArrayList<>();

        //create inboxes and threads
        for (int i = 0; i < threadsAmount; i++){
            InboxQueue inbox = new InboxQueue(new ArrayBlockingQueue<>(256), "T" + i);
            inboxQueues.add(inbox);
            Thread thread = new Thread(inbox);
            threads.add(thread);
        }

        // middleware between messageGenerator and threads:
        // from one side messages generator put messages in the queue, from other side threads read them
        BlockingQueue<Message> externalMessages = new ArrayBlockingQueue<Message>(1024);
        MessageGenerator msgGenerator = new MessageGenerator(externalMessages, inboxQueues);
        Thread messageGenerator = new Thread(msgGenerator);
        messageGenerator.start();

        startAllThreads(threads);


    }

    private static void startAllThreads(ArrayList<Thread> threads) {
        for(Thread thread : threads){
            thread.start();
        }
    }
}
