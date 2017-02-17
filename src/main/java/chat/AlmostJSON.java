package chat;

import java.io.Serializable;

public class AlmostJSON implements Serializable{
    static final int REQUEST_NEW_MESSAGES_CODE = 123456;
    static final int OFFER_NEW_MESSAGES_CODE = 123458;
    static final int NO_NEW_MESSAGES_CODE = 6678898;
    static final int MESSAGES_IN_PACKAGE = 5;


    private Message[] messages;
    private int code;

    public AlmostJSON(int code, Message... messages) {
        this.messages = messages;
        this.code = code;
    }

    public Message[] getMessages() {
        return messages;
    }

    public void setMessages(Message[] messages) {
        this.messages = messages;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
