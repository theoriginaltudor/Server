import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static String name;
    private static DataOutputStream outToServer;
    private static DataInputStream inFromServer;

    public static void main(String argv[]) throws Exception {
        join();
        communicate();
    }

    public static void join() {
        try {
            boolean b = true;
            while (b) {
                Socket clientSocket = new Socket("172.16.31.246", 9000/*"localhost", 5555*/);
                System.out.println("Choose a nickname: ");
                Scanner inFromUser = new Scanner(System.in);
                outToServer = new DataOutputStream(clientSocket.getOutputStream());
                inFromServer = new DataInputStream(clientSocket.getInputStream());
                String nickname = inFromUser.nextLine();
                outToServer.writeUTF("JOIN " + nickname +", localhost:5555");
                String fromServer = inFromServer.readUTF();
                if (fromServer.substring(0, 4).equals("J_OK")) {
                    name = nickname;
                    b = false;
                }
                System.out.println(fromServer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void communicate() {
        try {
            Scanner inFromUser = new Scanner(System.in);
            Thread receive = new Thread(() -> {
                try {
                    while (true) {
                        String fromServer = inFromServer.readUTF();
                        System.out.println(fromServer);

                    }
                } catch (Exception e) {
                    System.out.println("Problem in receive");
                    System.exit(1);
                }
            });
            Thread send = new Thread(() -> {
                try {
                    while (true) {
                        String text = inFromUser.nextLine();
                        if (text.equalsIgnoreCase("/QUIT")) {
                            outToServer.writeUTF("QUIT " + name);
                            return;
                        }
                        if (text.equalsIgnoreCase("/LIST")) {
                            outToServer.writeUTF("LIST");
                        } else {
                            outToServer.writeUTF("DATA " + name + ": " + text);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            Thread alive = new Thread(() -> {
                try {
                    while (true) {
                        Thread.sleep(59999);
                        outToServer.writeUTF("ALVE " + name);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            receive.start();
            send.start();
            alive.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}