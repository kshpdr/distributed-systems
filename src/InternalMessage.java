import java.util.ArrayList;

public class InternalMessage extends Message{


    private ArrayList<String> attachment;

    public InternalMessage(String message, ArrayList<String> attachment) {
        super(message);
        this.attachment = attachment;
    }


    public ArrayList<String> getAttachment(){
        return attachment;
    }

}
