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
                new Thread(() -> {
                    try {
                        InputStream is = localSocket.getInputStream();
                        ObjectInputStream ois = new ObjectInputStream(is);

                        while (true) {
                            Message msgOBJ = (Message) ois.readObject();
                            try (FileOutputStream fos = new FileOutputStream(dbFile);
                                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                                oos.writeObject(msgOBJ);
                            }
                            broadcastMSG();
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }
        }
    }

    private static void broadcastMSG() {

        new Thread(() -> {
            for (Socket client : CLIENT_LIST) {
                if (client.isConnected()) {
                    try {

                        try (FileInputStream fis = new FileInputStream(dbFile);
                             ObjectInputStream ois = new ObjectInputStream(fis)) {

                            var oos = new ObjectOutputStream(client.getOutputStream());
                            Message msgObj = (Message) ois.readObject();
                            oos.writeObject(msgObj);
                            System.out.println(msgObj.hashCode());
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
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