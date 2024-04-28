package lk.ijse.dep12.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.stage.FileChooser;
import lk.ijse.dep12.shared.to.Message;

import java.io.*;
import java.net.Socket;

public class MainViewController {

    public TextField txtImagePath;
    public Button btnAttachImage;
    public Button btnSendImage;
    public ScrollPane scrollpane;
    public TilePane tilepane;

    private String nickName;
    private Socket remoteSocket;
    public ObjectOutputStream oos;
    public ObjectInputStream ois;
    public ImageView img;
    private File imageFilePath;

    public void initialize() throws IOException, ClassNotFoundException {
        remoteSocket = new Socket("localhost", 5050);
        new Thread(() -> {
            try {
                ois = new ObjectInputStream(remoteSocket.getInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }).start();

        setChatUI();

    }


    public void initData(Socket remoteSocket, String nickName) {
        this.nickName = nickName;
        this.remoteSocket = remoteSocket;

    }

    private void setChatUI() {

        new Thread(() -> {
            try {
                ois = new ObjectInputStream(remoteSocket.getInputStream());
                while (true) {
                    Message messageObject = (Message) ois.readObject();
                    Platform.runLater(() -> {
                        byte[] messageImageData = messageObject.getImageData();
                        ImageView imgNew = byteArrayToImageView(messageImageData);
                        imgNew.setFitHeight(100);
                        imgNew.setFitWidth(100);
                        tilepane.getChildren().add(imgNew);
                    });
                }
            } catch (IOException e) {
                if (!remoteSocket.isClosed())
                    throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public void btnAttachImageOnAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image Files");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPEG Image", "*.jpeg", "*jpg"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        File file = fileChooser.showOpenDialog(btnAttachImage.getScene().getWindow());
        if (file == null) {
            txtImagePath.setText("No FIle Selected");
        } else {
            txtImagePath.setText(file.getAbsolutePath());
            imageFilePath = new File(txtImagePath.getText());
        }
    }

    public void btnSendImageOnAction(ActionEvent actionEvent) throws IOException {
        ImageView img = new ImageView(imageFilePath.toURI().toString());
        img.setFitWidth(100);
        img.setFitHeight(100);
        tilepane.getChildren().add(img);
        //System.out.println("path  " +imageFilePath.getPath());
        byte[] bytes = imageViewToByteArray(imageFilePath.getPath());
        send(bytes);

    }

    public void send(byte[] imgData) throws IOException {

        new Thread(() -> {
            try {
                oos = new ObjectOutputStream(remoteSocket.getOutputStream());
                Message msg = new Message(imgData);
                oos.writeObject(msg);
                oos.flush();
                System.out.println(msg.hashCode());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

    }

    public byte[] imageViewToByteArray(String imagePath) throws IOException {
        File imageFile = new File(imagePath);
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

    public static ImageView byteArrayToImageView(byte[] imageData) {
        Image image = new Image(new ByteArrayInputStream(imageData));
        ImageView imageView = new ImageView(image);
        return imageView;
    }
}
