import java.io.*;
import java.net.*;
import java.util.*;

public class Server implements Runnable {
    private volatile static List<Person> persons = new ArrayList<>();
    private static ServerSocket welcomeSocket;
    private static Socket connectionSocket;

    public static void main(String argv[]) {
        try {
            welcomeSocket = new ServerSocket(5558);
            while (true) {
                connect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void connect() {
        try {
            connectionSocket = welcomeSocket.accept();
            System.out.println("Found request. Start thread for specific client.");
            new Thread(new Server()).start();
        } catch (IOException e) {
            System.out.println("Could not connect!!");
        }
    }

    private static void receiveMessage(Socket s, DataOutputStream o, DataInputStream i) {
        try {
            long first = 0, second;
            boolean b = true;
            while (b) {
                String message = i.readUTF();
                System.out.println(message);
                if (message.substring(0, 4).equals("JOIN")) {
                    first = Calendar.getInstance().getTimeInMillis();
                }
                switch (message.substring(0, 4)) {
                    case "JOIN":
                        for (Person p : persons) {
                            if (p.getName().equals(message.substring(5))) {
                                sendMessage("J_ERR Try again", o);
                                return;
                            }
                        }
                        persons.add(new Person(message.substring(5), o));
                        sendMessage("J_OK Welcome", o);
                        break;
                    case "DATA":
                        for (Person p : persons) {
                            sendMessage(message.substring(5), p.getStream());
                        }
                        break;
                    case "ALVE":
                        second = Calendar.getInstance().getTimeInMillis();
                        System.out.println("Second " + second + " first " + first);
                        if (second - first > 75000) {
                            for (Person p : persons) {
                                if (p.getName().equals(message.substring(5))) {
                                    persons.remove(p);
                                    s.close();
                                    System.out.println("Connection lost and person deleted from the list");
                                }
                            }
                            b = false;
                        } else {
                            first = second;
                        }
                        break;
                    case "LIST":
                        for (Person p : persons) {
                            sendMessage(p.getName() + " is online", o);
                        }
                        break;
                    case "QUIT":
                        for (Person p : persons) {
                            sendMessage(message, p.getStream());
                            if (p.getName().equals(message.substring(5))) {
                                persons.remove(p);
                                s.close();
                            }
                        }
                        b = false;
                        break;
                    default:
                        System.out.println("NU am trecut prin nici un case!");
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("Smth in the receiveMessage");
            try {
                s.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }

    private static void sendMessage(String s, DataOutputStream user) {
        try {
            user.writeUTF(s);
        } catch (Exception e) {
            System.out.println("Message failed to be sent");
        }
    }

    @Override
    public void run() {
        try {
            Socket socket = connectionSocket;
            DataInputStream inFromClient = new DataInputStream(socket.getInputStream());
            DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream());
            receiveMessage(socket, outToClient, inFromClient);

        } catch (Exception e) {
            System.out.println("Smth in the run method");
        }
    }
}