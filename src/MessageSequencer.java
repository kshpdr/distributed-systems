import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MessageSequencer implements Runnable{


    private Queue<Message> messages;

    public MessageSequencer(){
        messages = new LinkedList<>();

    }

    //TODO:
    //message sequencer continuously polls the inbox queues of all the threads
    public int poll(){
        return 0;
    }



    //TODO:
    //message sequencer sends all messages to all other threads
    public int forward(){
        return 0;
    }

    //TODO:
    @Override
    public void run() {
        System.out.println("hi");
    }

    public static void main(String[] args) {
        Thread messageSequencer = new Thread(new MessageSequencer());
        messageSequencer.start();

        int threadsAmount = Integer.parseInt(args[0]);
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<String>(1024);

        Thread messageGenerator = new Thread(new MessageGenerator(blockingQueue));
        messageGenerator.start();

        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadsAmount; i++){
            Thread thread = new Thread(new InboxQueue(blockingQueue));
            thread.start();
            threads.add(thread);
        }
    }
}
