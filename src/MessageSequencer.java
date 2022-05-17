import java.util.LinkedList;
import java.util.Queue;

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

    }
}
