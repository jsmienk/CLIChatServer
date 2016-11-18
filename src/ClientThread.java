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
                            final String username = clientJSON.optString("username", "");
                            final String colour = clientJSON.optString("colour", "BLACK");
                            final User temp = new User(username, colour);
                            if (!server.checkUsernameExist(username)) {
                                user = temp;
                                server.connect(user, this);

                                final JSONObject json = new JSONObject();
                                json.put("accept", "accept");

                                final PrintWriter writer = new PrintWriter(socket.getOutputStream());
                                writer.println(json.toString());
                                writer.flush();
                            } else
                                sendError("username-exists");
                            continue;
                        }

                        // if we get a message, forward it
                        if (user != null && clientJSON.has("message")) {
                            server.forward(user, clientJSON.optString("message", ""));
                            continue;
                        }

                        // if the user is already set, change the username
                        if (user != null && clientJSON.has("username")) {
                            final String username = clientJSON.optString("username", "");
                            if (!server.checkUsernameExist(username) && !user.getUsername().equals(username)) {
                                final String old = user.getUsername();
                                user.setUsername(username);
                                server.changedUsername(user, old);
                            } else
                                sendError("username-exists");
                            continue;
                        }

                        // if the user is already set, change the user colour
                        if (user != null && clientJSON.has("colour")) {
                            final String colour = clientJSON.optString("colour", "BLACK");
                            final String old = user.getColour();
                            user.setColour(colour);
                            server.changedColour(user, old);
                            continue;
                        }

                        // private message
                        if (user != null && clientJSON.has("to") && clientJSON.has("whisper")) {
                            final String username = clientJSON.optString("username", "");
                            if (server.checkUsernameExist(username))
                                server.whisper(username, clientJSON.optString("whisper", ""), socket, user);
                            else
                                sendError("whisper-to-noone");
                            continue;
                        }

                        // action message
                        if (user != null && clientJSON.has("me")) {
                            server.me(clientJSON.optString("me", ""), user);
                        }
                    } else throw new SocketException();
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

    /**
     * Send an error message to this client
     *
     * @param error the error message
     */
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
