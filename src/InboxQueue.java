import java.util.concurrent.BlockingQueue;

public class InboxQueue  implements Runnable{
    BlockingQueue<String> blockingQueue = null;

    public InboxQueue(BlockingQueue blockingQueue){
        this.blockingQueue = blockingQueue;
    }


    //TODO:
    @Override
    public void run() {
        while(true){
            try {
                String element = this.blockingQueue.take();
                System.out.println("got: " + element);
            }
            catch (InterruptedException e){
                System.out.println("Thread stopped");
            }
        }
    }
}
