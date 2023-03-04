import java.util.Random;

public class ExternalMessage extends Message{


    private int payload;
    private String message;

    public ExternalMessage(String message) {
        super(message);
        Random rand = new Random();
        payload = rand.nextInt();
    }


    public int getPayload(){
        return payload;
    }


}
