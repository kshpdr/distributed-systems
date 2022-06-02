import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {
    public static int threadsAmount;

    public static void main(String[] args) {
        // parse number of threads via command line
        threadsAmount = Integer.parseInt(args[0]);
        int STATE = 1;

        //STATE = 0: run with message Sequencer
        if(STATE == 0){
            runMessageSequencerExample(args);
        }
        //STATE = 1: run with lamport design
        if(STATE == 1){
            runLamportDesignExample(args);
        }
    }

    private static void runLamportDesignExample(String[] args) {
        ArrayList<Thread> threads = new ArrayList<>();
        ArrayList<InboxQueue> inboxQueues = new ArrayList<>();

        //create inboxes and threads
        for (int i = 0; i < threadsAmount; i++){
            InboxQueue inbox = new InboxQueue(new ArrayBlockingQueue<>(256), new ArrayBlockingQueue<>(256), "T" + i, inboxQueues);
            inboxQueues.add(inbox);
            Thread thread = new Thread(inbox);
            threads.add(thread);
        }
        updateInboxQueues(inboxQueues);

        // middleware between messageGenerator and threads:
        // from one side messages generator put messages in the queue, from other side threads read them
        BlockingQueue<Message> externalMessages = new ArrayBlockingQueue<Message>(1024);
        MessageGenerator msgGenerator = new MessageGenerator(externalMessages, inboxQueues);
        Thread messageGenerator = new Thread(msgGenerator);
        messageGenerator.start();
        startAllThreads(threads);
    }

    private static void updateInboxQueues(ArrayList<InboxQueue> inboxQueues) {
        for(InboxQueue inboxQueue : inboxQueues){
            inboxQueue.inboxQueues = inboxQueues;
        }
    }

    private static void runMessageSequencerExample(String[]args) {
        ArrayList<Thread> threadList = new ArrayList<>();
        ArrayList<InboxQueue> inboxQueues = new ArrayList<>();

        //create inboxes and threads
        for (int i = 0; i < threadsAmount; i++){
            InboxQueue inbox = new InboxQueue(new ArrayBlockingQueue<>(256), new ArrayBlockingQueue<>(256), "T" + i);
            inboxQueues.add(inbox);
            Thread thread = new Thread(inbox);
            threadList.add(thread);
        }
        //start message sequencer
        BlockingQueue<Message> internalMessages = new ArrayBlockingQueue<Message>(1024);
        MessageSequencer msgSequencer = new MessageSequencer(internalMessages, inboxQueues);
        Thread messageSequencer = new Thread(msgSequencer);
        messageSequencer.start();

        // middleware between messageGenerator and threads:
        // from one side messages generator put messages in the queue, from other side threads read them
        BlockingQueue<Message> externalMessages = new ArrayBlockingQueue<Message>(1024);
        MessageGenerator msgGenerator = new MessageGenerator(externalMessages, inboxQueues);
        Thread messageGenerator = new Thread(msgGenerator);
        messageGenerator.start();
        startAllThreads(threadList);
    }
    private static void startAllThreads(ArrayList<Thread> threads) {
        for(Thread thread : threads){
            thread.start();
        }
    }
}
