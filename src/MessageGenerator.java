import java.util.concurrent.BlockingQueue;

public class MessageGenerator implements Runnable{
    BlockingQueue<String> blockingQueue = null;

    public MessageGenerator(BlockingQueue blockingQueue){
        this.blockingQueue = blockingQueue;
    }

    @Override
    public void run() {
        while(true){
            try{
                String hello = "hello";
                blockingQueue.put(hello);
            }
            catch (InterruptedException e){
                System.out.println("Generator stopped");
            }
        }
    }
}
