package lk.ijse.dep12.server;

import lk.ijse.dep12.shared.to.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerAppInitializer {
    public static File dbFile;
    private static final List<Socket> CLIENT_LIST = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws IOException {
        dbFile = new File("db.txt");
        if (!dbFile.exists()) dbFile.createNewFile();

        try (ServerSocket serverSocket = new ServerSocket(5050)) {
            while (true) {
                Socket localSocket = serverSocket.accept();
                CLIENT_LIST.add(localSocket);
                //  sendCurrentMSG(localSocket);
                new Thread(() -> {
                    try {
                        try (ObjectInputStream ois = new ObjectInputStream(localSocket.getInputStream());
                             ObjectOutputStream ooss = new ObjectOutputStream(new FileOutputStream(dbFile))) {
                            Message messageObject = (Message) ois.readObject();
                            ooss.writeObject(messageObject);
                            broadcastMSG(messageObject);
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }
        }
    }

    private static void broadcastMSG(Message msgOBJ) {
        System.out.println("CLint list broadcast");
        new Thread(() -> {
            for (Socket client : CLIENT_LIST) {
                if (client.isConnected()) {
                    try {

                        try (FileInputStream fis = new FileInputStream(dbFile);
                             ObjectInputStream ois = new ObjectInputStream(fis)) {
                           // System.out.println(client.toString());
                            var oos = new ObjectOutputStream(client.getOutputStream());

                            Message msgObj = (Message) ois.readObject();
                            oos.writeObject(msgObj);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
    }


    private static void sendCurrentMSG(Socket client) throws IOException {
        if (dbFile.length() == 0) {
            //load default image if db has no data
            new ObjectOutputStream(client.getOutputStream()).writeObject(imageViewToByteArray());
            return;
        }
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dbFile))){

            Message msgObj = (Message) ois.readObject();

            new ObjectOutputStream(client.getOutputStream()).writeObject(msgObj);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] imageViewToByteArray() throws IOException {
        File imageFile = new File("/home/ghost/Pictures/peakpx (2).jpg");
        try (FileInputStream fis = new FileInputStream(imageFile);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            return bos.toByteArray();
        }
    }

}