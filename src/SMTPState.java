import java.nio.ByteBuffer;

class SMTPClientState {

    public final static int CONNECTED = 0;
    public final static int RECEIVEDWELCOME = 1;
    public final static int MAILFROMSENT = 2;
    public final static int RCPTTOSENT = 3;
    public final static int DATASENT = 4;
    public final static int MESSAGESENT = 5;
    public final static int QUITSENT = 6;
    public final static int HELPSENT = 7;

    private int state;
    private int previousState;
    private ByteBuffer buffer;
    private byte [] from;
    private byte [] to;
    private byte [] message;

    public SMTPClientState() {
        this.state = CONNECTED;
        this.buffer = ByteBuffer.allocate(8192);
    }

    public int getState() {
        return this.state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public ByteBuffer getByteBuffer() {
        return this.buffer;
    }

    public byte[] getFrom() {
        return from;
    }

    public void setFrom(byte[] from) {
        this.from = from;
    }

    public byte[] getTo() {
        return to;
    }

    public void setTo(byte[] to) {
        this.to = to;
    }

    public byte[] getMessage() {
        return message;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    public int getPreviousState() {
        return previousState;
    }

    public void setPreviousState(int previousState) {
        this.previousState = previousState;
    }
}
