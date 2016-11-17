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

    public ClientThread(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        server.put("1",this);
        System.out.println("Client connected");
        try {
            os = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        writer = new PrintWriter(os);
        writer.println("Welkom bij de server");
        writer.flush();


    }

    public void run() {
        while (socket.isConnected()) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                String clientData = "";
                JSONObject object= new JSONObject();
                clientData = reader.readLine();
                if (clientData != "") {
                    server.send(clientData);
                    System.out.println(clientData);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 1. Wacht op berichten van de client.
            // 2. Stuur berichten van de clients door naar de andere
            // clients. (Broadcast)
        }
    }

    public Socket getSocket() {
        return socket;
    }
}
