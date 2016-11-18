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

                    if(user != null && clientJSON.has("username")){
                        final String username = clientJSON.optString("username", "default");
                        user.setUsername(username);
                        server.checkExists(user,this);
                    }

                    if(user != null && clientJSON.has("colour")){
                        final String colour = clientJSON.optString("colour", user.getColour());
                        user.setColour(colour);
                        try {
                            final OutputStream os = socket.getOutputStream();
                            final PrintWriter writer = new PrintWriter(os);

                            final JSONObject json = new JSONObject();
                            json.put("message", "Colour Changed to: "+colour);
                            json.put("username", "Server");
                            json.put("colour", "RED");

                            writer.println(json.toString());
                            writer.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    // set the user that is connected through this client
                    if (user == null && clientJSON.has("username") && clientJSON.has("colour")) {
                        final String username = clientJSON.optString("username", "default");
                        final String colour = clientJSON.optString("colour", "BLACK");
                        user = new User(username, colour);
                        server.checkExists(user,this);
                    }

                    // if the user is set and we get a message, forward it
                    if (user != null && clientJSON.has("message"))
                        server.forward(user, clientJSON.optString("message", ""));

                    if (user !=null && clientJSON.has("to") && clientJSON.has("whisper")){
                        server.whisper(clientJSON.getString("to"), clientJSON.getString("whisper"), socket,user);
                    }

                    if (user !=null && clientJSON.has("me")){
                        server.me(clientJSON.getString("me"), user);
                    }
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
