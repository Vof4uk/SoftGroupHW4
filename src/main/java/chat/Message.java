package chat;

import java.io.Serializable;
import java.util.Date;

class Message implements Serializable, Comparable<Message> {
    private Date created;
    private String content;
    private String sentBy;
    private String recepient;
    final MessageType type;
    private Integer senderId;

    public Message(String sentBy, String content, String recepient, MessageType type) {
        this.content = content;
        this.sentBy = sentBy;
        this.recepient = recepient;
        this.type = type;
        this.created = new Date();
    }

    @Override
    public int compareTo(Message m) {
        return this.created.compareTo(m.getCreated());
    }

    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    public String getRecepient() {
        return recepient;
    }

    public void setRecepient(String recepient) {
        this.recepient = recepient;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSentBy() {
        return sentBy;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }

    @Override
    public String toString() {
        return "Message{" +
                "sentBy='" + sentBy + '\'' +
                ", content='" + content + '\'' +
                ", to '" + recepient + '\'' +
                '}';
    }

    enum MessageType{
        NORMAL, SYSTEM
    }
}
