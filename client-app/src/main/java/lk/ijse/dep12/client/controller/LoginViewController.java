package lk.ijse.dep12.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class LoginViewController {

    public Button btnLogin;
    public TextField txtHost;
    public TextField txtNickName;
    public TextField txtPort;

    public void btnLoginOnAction(ActionEvent event) {
        String nickName = txtNickName.getText();

        if (nickName.isBlank()) {
            txtNickName.requestFocus();
            txtNickName.selectAll();
            return;
        }

        try {
            Socket remoteSocket = new Socket("localhost", 5050);
            ((Stage) (btnLogin.getScene().getWindow())).close();
            Stage mainStage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
            mainStage.setScene(new Scene(fxmlLoader.load()));

            MainViewController controller = fxmlLoader.getController();
            controller.initData(remoteSocket, nickName);

            mainStage.setTitle("DEP 12 General Chat App");
            mainStage.show();
            mainStage.centerOnScreen();

            mainStage.setOnCloseRequest(e -> {
                try {
                    remoteSocket.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to connect with the server").show();
        }
    }

}
