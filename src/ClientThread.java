import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

/**
 * Created by sveno on 17-11-2016.
 */
public class ClientThread extends Thread {
    private Socket socket;
    private OutputStream os;
    private Server server;
    private PrintWriter writer;
    private String username;
    private String color;

    public ClientThread(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        System.out.println("Client connected");
    }

    public void run() {
        while (socket.isConnected()) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                String clientData = "";

                clientData = reader.readLine();
                System.out.println(clientData);
                if (clientData != "") {
                    JSONObject object = new JSONObject(clientData);
                    if(object.has("username")) {
                        username = object.getString("username");
                        server.put(username, this);
                    }
                    if(object.has("colour")){
                        color = object.getString("colour");
                    }
                    if(object.has("message")){
                        server.send(object.getString("message"), username, color);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public String getUsername() {
        return username;
    }

    public String getColor() {
        return color;
    }
}
