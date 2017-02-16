package chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.*;

public class ChatServer implements Runnable {
    static final int PORT_NUMBER = 1488;
    static final String COMMAND_ASK_NAME = "jkbdjehbfewbfwibfilw";
    private static final int EXPECTED_USER_QUANTITY = 100;
    private static final int FIRST_USER_ID = 1000;
    private static final long REFRESH_TIME = 100;//ms

    private final Map<Integer, ObjectInputStream> fromUsers = new ConcurrentHashMap<>();
    private final Map<Integer, ObjectOutputStream> toUsers = new ConcurrentHashMap<>();
    private final Map<Integer, String> usernames = new ConcurrentHashMap<>();
    private ExecutorService executorService = Executors.newFixedThreadPool(EXPECTED_USER_QUANTITY / 10 + 1);

    private final BlockingQueue<Message> nonSystemMessages = new PriorityBlockingQueue<>();
    private final BlockingQueue<Message> systemMessages = new PriorityBlockingQueue<>();

    private boolean serverRunning = true;

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new ChatServer());
    }

    @Override
    public void run() {
        ServerSocket serverSocket = startServer();
        System.out.println("server started");
        int usersIdCount = FIRST_USER_ID;
        ScheduledExecutorService services = startServices();
        System.out.println("services started");

        if (serverSocket != null) {
            while (serverRunning) {
                waitUserAndConnect(serverSocket, usersIdCount);
                usersIdCount++;
            }
        }
        services.shutdown();
    }

    private ServerSocket startServer() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT_NUMBER);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return serverSocket;
    }

    private ScheduledExecutorService startServices() {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(3);
        executorService.scheduleWithFixedDelay(new InboxService(), REFRESH_TIME, REFRESH_TIME, TimeUnit.MILLISECONDS);
        executorService.scheduleWithFixedDelay(new OutboxService(), REFRESH_TIME, REFRESH_TIME, TimeUnit.MILLISECONDS);
        executorService.scheduleWithFixedDelay(new SystemRequestService(), REFRESH_TIME, REFRESH_TIME, TimeUnit.MILLISECONDS);
        return  executorService;
    }

    private void waitUserAndConnect(ServerSocket serverSocket, int userId) {
        try {
            Socket socket = serverSocket.accept();
            executorService.submit(new UserIntroduceService(socket, userId));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class UserIntroduceService implements Runnable {
        private final Socket userSocket;
        private final int userId;

        private ObjectOutputStream oos;
        private ObjectInputStream ois;

        UserIntroduceService(Socket socket, int userId) {
            this.userSocket = socket;
            this.userId = userId;
        }

        @Override
        public void run() {
            openStreams();
            registerUser();
        }

        private void openStreams() {
            try {
                oos = new ObjectOutputStream(userSocket.getOutputStream());
                ois = new ObjectInputStream(userSocket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void registerUser(){
            String username = "unnamed";
            ChatServer.this.usernames.put(userId, username);
            ChatServer.this.fromUsers.put(userId, ois);
            ChatServer.this.toUsers.put(userId, oos);
            Message message = new Message(ChatRoom.SERVER,
                    String.format("WELCOME to chat, %s, to change username, type \"--changename 'newName'\"%n", username),
                    null, Message.MessageType.NORMAL);
            try {
                nonSystemMessages.put(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class InboxService implements Runnable {
        @Override
        public void run() {
            if (fromUsers.isEmpty()) {
                return;
            }
            for (Map.Entry<Integer, ObjectInputStream> user : fromUsers.entrySet()) {
                try {
                    boolean hasNewMessages = user.getValue().readBoolean();
                    while (hasNewMessages){
                        System.out.println(user);
                        Message message = receiveMessage(user.getValue());
                        message.setSentBy(usernames.get(user.getKey()));
                        if (message.type == Message.MessageType.SYSTEM) {
                            ChatServer.this.systemMessages.put(message);
                        } else {
                            ChatServer.this.nonSystemMessages.put(message);
                        }
                        System.out.println(message);
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private Message receiveMessage(ObjectInputStream from) {
            Message message = null;
            try {
                message = (Message) from.readObject();
            } catch (SocketException ex) {
                //TODO close socket
                ex.printStackTrace();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return message;
        }
    }

    private class OutboxService implements Runnable {
        @Override
        public void run() {
            if(toUsers.isEmpty() || nonSystemMessages.isEmpty()){
                return;
            }

            Message message = nextMessage();

            if (message == null) {
                return;
            }

            Integer senderId = message.getSenderId();
            System.out.println(senderId);
            for (Map.Entry<Integer, ObjectOutputStream> userConnection : toUsers.entrySet()) {
                if (!userConnection.getKey().equals(senderId)) {
                    sendMessage(userConnection.getValue(), message);
                }
            }
        }

        private Message nextMessage() {
            Message message = null;
            try {
                message = nonSystemMessages.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return message;
        }

        private void sendMessage(ObjectOutputStream to, Message message) {
            try {
                to.writeObject(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class SystemRequestService implements Runnable {
        @Override
        public void run() {
            if(systemMessages.isEmpty()){
                return;
            }
            try {
                processRequest(systemMessages.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void processRequest(Message message) {
            if(!isSystem(message)){
                return;
            }
            if(message.getContent().startsWith("--changename ")){
                changeName(message);
            }
        }

        private void changeName(Message message) {
            String newName = message.getContent().replaceFirst("--changename ", "");
            if(newName.length() > 8){
                newName = newName.substring(0, 7);
            }
            usernames.put(message.getSenderId(), newName);
            System.out.println("changed name to " + newName);
        }

        private boolean isSystem(Message message) {
            return message.type == Message.MessageType.SYSTEM &&
                    message.getContent().startsWith("--");
        }
    }
}
