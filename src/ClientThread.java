import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

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
        try {
            final OutputStream os = socket.getOutputStream();
            final PrintWriter writer = new PrintWriter(os);

            final JSONObject json = new JSONObject();
            json.put("message", "Connected!");
            json.put("username", "Server");
            json.put("colour", "RED");

            writer.println(json.toString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        boolean connected = true;
        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while (connected) {
                try {
                    final String clientData = reader.readLine();

                    if (clientData != null && !clientData.isEmpty()) {
                        System.out.println("Received: " + clientData);
                        JSONObject clientJSON = new JSONObject();
                        try {
                            clientJSON = new JSONObject(clientData);
                        } catch (JSONException je) {
                            je.printStackTrace();
                            System.err.println("Received data not in JSON!");
                        }

                        // create a user the first time this client connects
                        if (user == null && clientJSON.has("username") && clientJSON.has("colour")) {
                            final String username = clientJSON.optString("username", "default");
                            final String colour = clientJSON.optString("colour", "BLACK");
                            final User temp = new User(username, colour);
                            if (!server.checkUsernameExist(temp)) {
                                user = temp;
                                server.connect(user, this);
                            } else
                                sendError("username-exists");
                            continue;
                        }

                        // if the user is already set, change the username
                        if (user != null && clientJSON.has("username")) {
                            final String username = clientJSON.optString("username", "default");
                            if (!server.checkUsernameExist(user))
                                user.setUsername(username);
                            else
                                sendError("username-exists");
                            continue;
                        }

                        // if the user is already set, change the user colour
                        if (user != null && clientJSON.has("colour")) {
                            final String colour = clientJSON.optString("colour", "BLACK");
                            user.setColour(colour);
                            continue;
                        }

                        // if we get a message, forward it
                        if (user != null && clientJSON.has("message")) {
                            server.forward(user, clientJSON.optString("message", ""));
                            continue;
                        }

                        // private message
                        if (user != null && clientJSON.has("to") && clientJSON.has("whisper")) {
                            server.whisper(clientJSON.optString("to", ""), clientJSON.optString("whisper", ""), socket, user);
                            continue;
                        }

                        // action message
                        if (user != null && clientJSON.has("me")) {
                            server.me(clientJSON.optString("me", ""), user);
                        }
                    }
                } catch (SocketException se) {
                    connected = false;
                    server.removeClient(user);
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

    private void sendError(final String error) {
        if (error != null && !error.isEmpty()) {
            try {
                final JSONObject json = new JSONObject();
                json.put("error", error);

                final PrintWriter writer = new PrintWriter(socket.getOutputStream());
                writer.println(json.toString());
                writer.flush();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
