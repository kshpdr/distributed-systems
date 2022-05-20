import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

public class MessageGenerator implements Runnable{


    public final static String PATH = "external_messages.txt";
    BlockingQueue<ExternalMessage> externalMessages = null;
    ArrayList<InboxQueue> inboxQueues = null;

    public MessageGenerator(BlockingQueue externalMessages, ArrayList<InboxQueue> inboxQueues){
        this.externalMessages = externalMessages;
        this.inboxQueues = inboxQueues;
    }


    //write a txt file with num-amount of messages
    void generateRandomExternalMessages(int num, String path) throws IOException {
        try{
            FileWriter writer = new FileWriter(path);

            for(int i = 0; i < num; i++){
                writer.write("Message " + i + "\n");
            }
            writer.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }


    //put all the lines from a txt into a blockingqueue
    void fillBlockingQueue(BlockingQueue<ExternalMessage> messages, String path){
        try{
            File file = new File(path);
            Scanner reader = new Scanner(file);

            while (reader.hasNextLine()){
                String data = reader.nextLine();
                ExternalMessage msg = new ExternalMessage(data);
                messages.put(msg);
            }
        } catch (FileNotFoundException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        fillBlockingQueue(externalMessages, PATH);
        Random random = new Random();
        while(true){
            int r = random.nextInt(5);
            System.out.println("Sending external message to random thread... ");
            try {
                inboxQueues.get(r).getExternalMessages().put(externalMessages.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }
}
