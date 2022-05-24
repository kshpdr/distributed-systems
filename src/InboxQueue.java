import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

public class InboxQueue implements Runnable{


    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_RESET = "\u001B[0m";
    public String path;
    public int timestamp;
    public int STATE;


    BlockingQueue<ExternalMessage> externalMessages = null;

    //TODO: MAKE internalMessages synchronizable + ADD LAMPORT FEATURE
    BlockingQueue<InternalMessage> internalMessages = null;
    private String name;
    ArrayList<InboxQueue> inboxQueues;

    public InboxQueue(BlockingQueue externalMessages, BlockingQueue internalMessages, String name){
        this.externalMessages = externalMessages;
        this.internalMessages = internalMessages;
        this.name = name;
        path = "logs/" + name + ".txt";
        STATE = 0;
    }

    public InboxQueue(BlockingQueue externalMessages, BlockingQueue internalMessages, String name, ArrayList<InboxQueue> inboxQueues){
        this.externalMessages = externalMessages;
        this.internalMessages = internalMessages;
        this.name = name;
        path = "logs/" + name + ".txt";
        //init lamport timestamp
        timestamp = 1;
        STATE = 1;
    }


    public BlockingQueue<ExternalMessage> getExternalMessages(){
        return externalMessages;
    }

    public BlockingQueue<InternalMessage> getInternalMessages(){
        return internalMessages;
    }


    //TODO: MAKE forward-function: SEND EVERY MESSAGE IN INBOXQUEUE TO ALL OTHER THREADS
    public int forward(ExternalMessage msg){

        for(InboxQueue inboxQueue : inboxQueues){
            synchronized (inboxQueue.getInternalMessages()){
                //transform externalMessage into internalMessage with attachment: (payload, timestamp)
                ArrayList<String> attachment = new ArrayList<>();
                attachment.add(msg.getPayload() + "");
                attachment.add(timestamp + "");
                InternalMessage internalMessage = new InternalMessage(msg.getMessage(), attachment);


                //send internalMessage
                try {
                    inboxQueue.getInternalMessages().put(internalMessage);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return -1;
                }
                timestamp++;
            }
        }


        return 0;
    }


    @Override
    public void run() {


        while(true){

            if(STATE == 0){
                runMessageSequencerExample();
            }
            if(STATE == 1){
                runLamportExample();

            }

        }
    }

    private void runLamportExample(){
        while (true){

            if(externalMessages.peek() != null){
                try {
                    forward(externalMessages.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            synchronized (internalMessages){
                if(internalMessages.peek() != null){
                    try {
                        writeLogFile(internalMessages.take());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

    private void runMessageSequencerExample() {
        while (true){
            if(internalMessages.peek() != null){
                try {
                    writeLogFile(internalMessages.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }




            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void writeLogFile(InternalMessage msg){
        try {
            FileWriter writer = new FileWriter(path, true);
            if(msg.getAttachment().size() == 2){
                writer.write("Received message: " + msg.getMessage() + " with payload: " + msg.getAttachment().get(0) + " and timestamp: " + msg.getAttachment().get(1) + "\n");
            }else{
                writer.write("Received message: " + msg.getMessage() + " with payload: " + msg.getAttachment().get(0) + "\n");

            }
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
