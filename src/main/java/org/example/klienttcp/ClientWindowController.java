package org.example.klienttcp;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Paint;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
 * Do napisania Clienta TCP wzorowałem się stroną https://systembash.com/a-simple-java-tcp-server-and-tcp-client/
 */

public class ClientWindowController {
    @FXML
    public TextArea addressField;
    @FXML
    public TextArea portField;
    @FXML
    public TextArea clientLogsField;
    @FXML
    public TextArea clientMessageField;
    @FXML
    public Button sendButton;
    @FXML
    public Button connectButton;
    @FXML
    public Button disconnectButton;
    @FXML
    public Label errorLabel;
    @FXML
    public Label serverStatus;

    private Socket clientSocket = null;
    private DataOutputStream outToServer = null;
    private BufferedReader inFromServer = null;

    private boolean isConnected = false;

    public void initialize() {
        sendButton.setOnAction(_ -> {
            if (isConnected) {
                if (validateInputMessage()) {
                    String message = clientMessageField.getText();
                    try {
                        outToServer.writeBytes(message + '\n');
                        clientMessageField.clear();
                        logMessage(getTime() + " Message: \"" + message + "\" has send to server. (" + message.getBytes().length + " bytes)");
                    } catch (IOException e) {
                        logMessage(getTime() + " Can not send message to server");
                        isConnected = false;
                        setConnectionToServerStatus(false);
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        if (inFromServer.ready())
                            try {
                                String response = inFromServer.readLine();
                                logMessage(getTime() + " Message: \"" + response + "\" received from the server. (" + response.getBytes().length + " bytes)");
                            } catch (IOException e) {
                                logMessage(getTime() + " Can not receive message from server");
                                isConnected = false;
                                setConnectionToServerStatus(false);
                            }
                    } catch (IOException e) {
                        logMessage(getTime() + " Server is not responding!");
                        try {
                            clientSocket.close();
                            logMessage(getTime() + " Client Socket closed!");
                            isConnected = false;
                            setConnectionToServerStatus(false);
                            toggleConnectionUI(false);
                        } catch (IOException ex) {
                            logMessage(getTime() + " Can not close connection with server!");
                        }
                    }
                }
            } else {
                logMessage(getTime() + " Can not send message to server because you are not connected!");
            }
        });
        /*
         * Na tej stronie znalazłem informacje co trzeba zrobić żeby po wpisaniu zlego ip serwera aplikacja nie czekała w nieskończoność na połączenie
         * https://www.baeldung.com/java-socket-connection-read-timeout
         */
        connectButton.setOnAction(_ -> {
            if (validatePort()) {
                if (validateAddress()) {
                    try {
                        clientSocket = new Socket();
                        clientSocket.connect(new InetSocketAddress(addressField.getText(), Integer.parseInt(portField.getText())), 3000);
                        outToServer = new DataOutputStream(clientSocket.getOutputStream());
                        inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        isConnected = true;
                        logMessage(getTime() + " Connected to server.");
                        toggleConnectionUI(true);
                        setConnectionToServerStatus(true);
                    } catch (IOException e) {
                        logMessage(getTime() + " Could not connect to server.");
                    }
                }
            }
        });

        disconnectButton.setOnAction(_ -> {
            try {
                clientSocket.close();
                isConnected = false;
                logMessage(getTime() + " Disconnected from server.");
                toggleConnectionUI(false);
                setConnectionToServerStatus(false);
            } catch (IOException e) {
                logMessage(getTime() + " Could not disconnect from server.");
            }
        });
    }

    /*
     * Do uzyskania aktualnego czasu inspirowałem się https://www.w3schools.com/java/java_date.asp
     */
    private String getTime() {
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("HH:mm:ss");
        return "[" + myDateObj.format(myFormatObj) + "]";
    }

    private void logMessage(String message) {
        clientLogsField.appendText(message + "\n");
    }

    private void toggleConnectionUI(Boolean connectState) {
        portField.setEditable(!connectState);
        addressField.setEditable(!connectState);

        connectButton.setDisable(connectState);
        connectButton.setVisible(!connectState);

        disconnectButton.setDisable(!connectState);
        disconnectButton.setVisible(connectState);
    }

    private Boolean validateInputMessage() {
        int messageSize = clientMessageField.getText().getBytes().length;
        if (messageSize == 0) {
            logMessage(getTime() + " Can not send empty message to server!");
            return false;
        }
        return true;
    }

    /*
     * Do testowaania i tworzenia regex'ów posłużyłem się https://regexr.com/
     */
    private Boolean validatePort() {
        if (portField.getText().isEmpty()) {
            errorLabel.setText("Port can not be empty");
            errorLabel.setVisible(true);
            return false;
        }

        String PORT_PATTERN = "^(6553[0-5]|655[0-2]\\d|65[0-4]\\d\\d|6[0-4]\\d\\d\\d|[0-5]?\\d?\\d?\\d?\\d)$";

        if (!portField.getText().matches(PORT_PATTERN)) {
            errorLabel.setText("Port must be a valid port number! (Possible ports are: 0-65535)");
            errorLabel.setVisible(true);
            return false;
        }
        errorLabel.setVisible(false);
        return true;
    }

    private Boolean validateAddress() {
        if (addressField.getText().isEmpty()) {
            errorLabel.setText("Address can not be empty");
            errorLabel.setVisible(true);
            return false;
        }

        String ADDRESS_PATTERN = "^(localhost|((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)))$";

        if (!addressField.getText().matches(ADDRESS_PATTERN)) {
            errorLabel.setText("Address must be a valid IP address! (Possible addresses are: [0-255].[0-255].[0-255].[0-255] or localhost)");
            errorLabel.setVisible(true);
            return false;
        }

        errorLabel.setVisible(false);
        return true;
    }

    private void setConnectionToServerStatus(boolean connectionStatus) {
        if (connectionStatus) {
            serverStatus.setText("Connected");
            serverStatus.setTextFill(Paint.valueOf("#058e2e"));
        } else {
            serverStatus.setText("Not Connected");
            serverStatus.setTextFill(Paint.valueOf("#e10202"));
        }
    }
}