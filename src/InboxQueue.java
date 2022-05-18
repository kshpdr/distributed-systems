import java.util.concurrent.BlockingQueue;

public class InboxQueue implements Runnable{


    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_RESET = "\u001B[0m";

    BlockingQueue<Message> blockingQueue = null;
    private String name;

    public InboxQueue(BlockingQueue blockingQueue, String name){
        this.blockingQueue = blockingQueue;
        this.name = name;
    }


    public BlockingQueue<Message> getInbox(){
        return blockingQueue;
    }


    //TODO:
    @Override
    public void run() {
        while(true){

            printInbox();

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            /*
            try {
                Message msg = this.blockingQueue.take();
                System.out.println("got: " + msg.getMessage() + " (" + name + ")");
            }
            catch (InterruptedException e){
                System.out.println("Thread" + " (" + name + ")" + "stopped");
            }

             */
        }
    }

    //for debug
    public void printInbox(){

        String color = "";
        switch (name){
            case "T0":
                color = ANSI_YELLOW;
                break;
            case "T1":
                color = ANSI_GREEN;
                break;
            case "T2":
                color = ANSI_PURPLE;
                break;
            case "T3":
                color = ANSI_BLUE;
                break;
            case "T4":
                color = ANSI_RED;
                break;
        }
        System.out.print(name + ": ");
        for(Message msg : blockingQueue){
            String output = color + "" + msg.getMessage() + ", " + ANSI_RESET;
            System.out.print(output);
        }
        System.out.println("");
    }
}
