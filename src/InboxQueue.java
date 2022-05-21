import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class InboxQueue implements Runnable{


    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_RESET = "\u001B[0m";
    public String path;


    BlockingQueue<ExternalMessage> externalMessages = null;
    BlockingQueue<InternalMessage> internalMessages = null;
    private String name;

    public InboxQueue(BlockingQueue externalMessages, BlockingQueue internalMessages, String name){
        this.externalMessages = externalMessages;
        this.internalMessages = internalMessages;
        this.name = name;
        path = "logs/" + name + ".txt";
    }


    public BlockingQueue<ExternalMessage> getExternalMessages(){
        return externalMessages;
    }

    public BlockingQueue<InternalMessage> getInternalMessages(){
        return internalMessages;
    }


    //TODO:
    @Override
    public void run() {


        while(true){

            //printExternalMessages();
            //printInternalMessages();
            if(internalMessages.peek() != null){
                try {
                    writeLogFile(internalMessages.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }




            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            /*
            try {
                Message msg = this.externalMessages.take();
                System.out.println("got: " + msg.getMessage() + " (" + name + ")");
            }
            catch (InterruptedException e){
                System.out.println("Thread" + " (" + name + ")" + "stopped");
            }
             */
        }
    }

    public void writeLogFile(InternalMessage msg){
        try {
            FileWriter writer = new FileWriter(path, true);
            writer.write("Received message: " + msg.getMessage() + " with payload: " + msg.getAttachment().get(0) + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    //for debug
    public void printExternalMessages(){

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
        System.out.print(name + "(External): ");
        for(Message msg : externalMessages){
            String output = color + "" + msg.getMessage() + ", " + ANSI_RESET;
            System.out.print(output);
        }
        System.out.println("");
    }

    public void printInternalMessages(){

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
        System.out.print(name + " (Internal): ");
        for(Message msg : internalMessages){
            String output = color + "" + msg.getMessage() + ", " + ANSI_RESET;
            System.out.print(output);
        }
        System.out.println("");
    }


}
