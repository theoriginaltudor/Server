import java.io.*;
import java.net.*;
import java.util.*;

public class Server implements Runnable {
    private volatile static List<Person> persons = new ArrayList<>();
    private static ServerSocket welcomeSocket;
    private static Socket connectionSocket;

    public static void main(String argv[]) {
        try {
            welcomeSocket = new ServerSocket(5555);
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
                if(message.length()>251)
                    return;
                if (message.substring(0, 4).equals("JOIN")) {
                    first = Calendar.getInstance().getTimeInMillis();
                }
                switch (message.substring(0, 4)) {
                    case "JOIN":
                        for (Person p : persons) {
                            if (p.getName().equals(message.substring(5, message.indexOf(",")))
                                    &&message.substring(5,message.indexOf(",")).length()<12) {
                                sendMessage("J_ERR Try again", o);
                                return;
                            }
                        }
                        persons.add(new Person(message.substring(5, message.indexOf(",")), o));
                        sendMessage("J_OK Welcome", o);
                        printList();
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
                        printList();
                        break;
                    case "QUIT":
                        for (int x = 0; x < persons.size(); x++) {
                            Person p = persons.get(x);
                            if (p.getName().equals(message.substring(5))) {
                                persons.remove(p);
                                p.getStream().close();
                            }
                        }
                        printList();
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
                for (Person p : persons) {
                    if (p.getStream().equals(o))
                        persons.remove(p);
                }
                printList();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }

    private static void printList() {
        for (Person pers : persons) {
            sendMessage("Users online",pers.getStream());
            for (Person p : persons) {
                sendMessage(p.getName(), pers.getStream());
            }
        }
    }

    private static void sendMessage(String s, DataOutputStream user) {
        try {
            user.writeUTF(s);
        } catch (Exception e) {
            e.printStackTrace();
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