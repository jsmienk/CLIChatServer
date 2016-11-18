import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

/**
 * Author: Sven & Jeroen
 * Date created: 17-11-2016
 */
class ClientThread extends Thread {

    private Server server;

    // the socket connected to this client
    private Socket socket;

    private User user;

    ClientThread(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while (socket.isConnected()) {
                final String clientData = reader.readLine();

                System.out.println("Received: " + clientData);

                if (clientData != null && !clientData.isEmpty()) {

                    JSONObject clientJSON = new JSONObject();
                    try {
                        clientJSON = new JSONObject(clientData);
                    } catch (JSONException je) {
                        je.printStackTrace();
                        System.err.println("Received data not in JSON!");
                    }

                    // set the user that is connected through this client
                    if (user == null && clientJSON.has("username") && clientJSON.has("colour")) {
                        final String username = clientJSON.optString("username", "default");
                        final String colour = clientJSON.optString("colour", "BLACK");
                        user = new User(username, colour);
                        server.connect(user, this);
                    }

                    // if the user is set and we get a message, forward it
                    if (user != null && clientJSON.has("message"))
                        server.forward(user, clientJSON.optString("message", ""));
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    User getUser() {
        return user;
    }

    Socket getSocket() {
        return socket;
    }
}
