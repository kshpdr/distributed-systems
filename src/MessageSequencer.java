import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MessageSequencer implements Runnable{


    private BlockingQueue<Message> internalMessages;
    ArrayList<InboxQueue> inboxQueues = null;

    public MessageSequencer(BlockingQueue<Message> internalMessages, ArrayList<InboxQueue> inboxQueues){
        this.internalMessages = internalMessages;
        this.inboxQueues = inboxQueues;
    }

    //TODO:
    //message sequencer continuously polls the inbox queues of all the threads
    public void poll() throws InterruptedException {
        try{
            for (InboxQueue inboxQueue : inboxQueues) {
                while (!inboxQueue.getExternalMessages().isEmpty()) {
                    Message message = inboxQueue.getExternalMessages().take();
                    internalMessages.put(message);
                }
            }
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
    }



    //TODO:
    //message sequencer sends all messages to all other threads
    public void forward(Message message){
        try{
            for (InboxQueue inboxQueue : inboxQueues) {
                InternalMessage internalMessage = new InternalMessage(message.getMessage(), null);
                inboxQueue.getInternalMessages().put(internalMessage);
            }
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    //TODO:
    @Override
    public void run() {
        while(true){
            try {
                poll();
                while(!internalMessages.isEmpty()){
                    forward(internalMessages.take());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
