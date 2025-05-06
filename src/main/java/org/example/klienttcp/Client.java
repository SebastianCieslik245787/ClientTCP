package org.example.klienttcp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {
    private final Socket clientSocket;
    private DataOutputStream outToServer;
    private BufferedReader inFromServer;
    private final ClientWindowController clientWindowController;

    public Client(ClientWindowController clientWindowController) {
        this.clientSocket = new Socket();
        this.clientWindowController = clientWindowController;
    }

    public boolean connect(String address, String port){
        try{
            int TIME_OUT = 3000;
            clientSocket.connect(new InetSocketAddress(address, Integer.parseInt(port)), TIME_OUT);
            this.outToServer = new DataOutputStream(clientSocket.getOutputStream());
            this.inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }
        catch (Exception e){
            return false;
        }
        return true;
    }

    public boolean disconnect(){
        try{
            this.clientSocket.close();
        }
        catch (Exception e){
            return false;
        }
        return true;
    }

    public boolean sendMessage(String message){
        try{
            outToServer.writeBytes(message + '\n');
        }
        catch (Exception e){
            return false;
        }
        return true;
    }

    public boolean readMessage(){
        try {
            if (inFromServer.ready())
                try {
                    String response = inFromServer.readLine();
                    clientWindowController.logMessage(" Message: \"" + response + "\" received from the server. (" + response.getBytes().length + " bytes)");
                } catch (IOException e) {
                    clientWindowController.logMessage(" Can not receive message from server");
                    return false;
                }
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
