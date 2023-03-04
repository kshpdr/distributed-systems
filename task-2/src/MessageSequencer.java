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

    //message sequencer continuously polls the inbox queues of all the threads
    public void poll() throws InterruptedException {
        try{
            for (InboxQueue inboxQueue : inboxQueues) {
                while (!inboxQueue.getExternalMessages().isEmpty()) {
                    //System.out.println(inboxQueue.getExternalMessages());
                    ExternalMessage message = inboxQueue.getExternalMessages().take();
                    ArrayList<String> attachment = new ArrayList<>();
                    attachment.add(message.getPayload() + "");
                    InternalMessage internalMessage = new InternalMessage(message.getMessage(), attachment);
                    internalMessages.put(internalMessage);
                }
            }
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    //message sequencer sends all messages to all other threads
    public void forward(InternalMessage message){
        try{
            for (InboxQueue inboxQueue : inboxQueues) {
                InternalMessage internalMessage = new InternalMessage(message.getMessage(), message.getAttachment());
                inboxQueue.getInternalMessages().put(internalMessage);
            }
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while(true){
            try {
                poll();
                while(!internalMessages.isEmpty()){
                    forward((InternalMessage) internalMessages.take());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
