package chat;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Comparator;
import java.util.concurrent.*;

public class ChatUser implements Runnable{
    static final int REFRESH_TIME = 10;



    private volatile String name = "username";
    private ObjectOutputStream toServer;
    private ObjectInputStream fromServer;
    private final Object sendingMonitor = new Object();
    private BufferedReader consoleReader;
    private Comparator<Message> messageComparator = new Comparator<Message>() {
        @Override
        public int compare(Message first, Message second) {
            return first.type != second.type ? first.type.ordinal() - second.type.ordinal()
                    : first.getCreated().compareTo(second.getCreated());
        }
    };
    private BlockingQueue<Message> inboxMessages = new PriorityBlockingQueue<>(5, messageComparator);
    private BlockingQueue<Message> outboxMessages = new PriorityBlockingQueue<>(5, messageComparator);

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new ChatUser());
        executorService.shutdown();
    }

    @Override
    public void run() {
        connectToChat();
        startServices();
        for (int i = 0; i < 1000000; i++) {//check conditions
            try {
                Thread.sleep(REFRESH_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
//        service.shutdown(); //close resources
    }

    private void parseAndSendString(String s) {
        Message message = null;
        if(s.matches("--[a-z]+ [a-zA-Z0-9]+")){
            message = new Message(null, s, ChatRoom.SERVER, Message.MessageType.SYSTEM);
            sendMessage(message);
        }if(s.matches("to [a-zA-Z0-9]+:")){
            String m = s.replaceAll("to [a-zA-Z0-9]+:", "");
            message = new Message(null, m.replaceFirst("to ",""), s.replaceFirst(m, ""), Message.MessageType.NORMAL);
            sendMessage(message);
        }else{
            message = new Message(null, s, ChatRoom.ALL, Message.MessageType.NORMAL);
            sendMessage(message);
        }
    }

    private void startServices() {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(4);
        service.scheduleWithFixedDelay(new InboxService(fromServer), REFRESH_TIME, REFRESH_TIME, TimeUnit.MILLISECONDS); //run inbox
        service.scheduleWithFixedDelay(new MessageProcessorService(), REFRESH_TIME, REFRESH_TIME, TimeUnit.MILLISECONDS); //run messageProcessor
        service.scheduleWithFixedDelay(new MessageSenderService(), REFRESH_TIME, REFRESH_TIME, TimeUnit.MILLISECONDS);
        service.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                parseAndSendString(readFromConsole());}
            },
                REFRESH_TIME, REFRESH_TIME, TimeUnit.MILLISECONDS);
    }

    private String readFromConsole() {
        if(consoleReader == null){
            consoleReader = new BufferedReader(new InputStreamReader(System.in));
        }
        try {
            return consoleReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "--error--";
    }



    private void connectToChat() {
        InetAddress inetAddress = null;
        Socket socket = null;

        try {
            inetAddress = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        if(inetAddress != null){
            try {
                socket = new Socket(inetAddress, ChatServer.PORT_NUMBER);
                toServer = new ObjectOutputStream(socket.getOutputStream());
                fromServer = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private boolean sendMessage(String recepient, String message){
        Message m = new Message(name, message, recepient, Message.MessageType.NORMAL);
        return sendMessage(m);
    }

    private boolean sendMessage(Message message){
        try {
            outboxMessages.put(message);
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }



    private class InboxService implements Runnable{
        private ObjectInputStream fromServer;

        InboxService(ObjectInputStream fromServer) {
            this.fromServer = fromServer;
        }

        @Override
        public void run() {
            receiveNewMessage();
        }

        private void receiveNewMessage() {
            try {
                Message message = (Message)fromServer.readObject();
                ChatUser.this.inboxMessages.put(message);
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class MessageProcessorService implements Runnable{
        private final BlockingQueue<Message> messages = ChatUser.this.inboxMessages;

        @Override
        public void run() {
            Message message = null;
            try {
                message = messages.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(message != null && message.type == Message.MessageType.SYSTEM){
                synchronized (ChatUser.this.sendingMonitor){
                    processSystemRequest(message);
                }
            }else{
                printMessageToConsole(message);
            }
        }

        private void processSystemRequest(Message message) {
            if(message.getContent().equals(ChatServer.COMMAND_ASK_NAME)){
                System.out.println("Server asks to introduce");
                ChatUser.this.name = ChatUser.this.readFromConsole();
                Message m = new Message(ChatUser.this.name, ChatServer.COMMAND_ASK_NAME, ChatRoom.SERVER, Message.MessageType.SYSTEM);
                ChatUser.this.sendMessage(m);
            }
        }

        private void printMessageToConsole(Message message) {
            System.out.printf("%s : %s%n", message.getSentBy(), message.getContent());
        }

    }

    private class MessageSenderService implements Runnable {
        @Override
        public void run() {
            try {
                if(!outboxMessages.isEmpty()){
                    toServer.writeBoolean(true);
                    Message m = outboxMessages.poll();
                    toServer.writeObject(m);
                    System.out.println(m);
                }else{
                    toServer.writeBoolean(false);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
