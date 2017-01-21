import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * Author: Sven & Jeroen
 * Date created: 17-11-2016
 */
public class Server {

    private static final int SERVER_PORT = 25565;

    static User server;
    static String serverName;
    private static String serverColour;

    private Map<User, ClientThread> clients;

    private Semaphore userAccess;

    // initialize the client map
    private Server() {
        server = new User("ChatServer", "BLACK");
        serverName = server.getUsername();
        serverColour = server.getColour();
        userAccess = new Semaphore(0);
        clients = new HashMap<>();
    }

    // start the server when the program starts
    public static void main(String Args[]) {
        new Server().run();
    }

    private void run() {
        try {
            // open the socket on the port
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);

            // success!
            System.out.println("Server started on port: " + SERVER_PORT);

            //noinspection InfiniteLoopStatement
            while (true) {
                // wait for a client to connect
                final Socket clientSocket = serverSocket.accept();
                // create a new client thread and start it
                new ClientThread(clientSocket, this).start();
                System.out.println("Client connected.");
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Forward a message to all other clients
     *
     * @param from    the client that send the message
     * @param message the message that was send
     */
    void forward(User from, String message) {

        try {
            userAccess.acquire();
            // iterate through all users
            // if the user is not the one that send the message
            clients.values().stream().filter(ct -> ct.getUser() != from).forEach(ct -> {
                final Socket clientSocket = ct.getSocket();
                try {
                    final OutputStream os = clientSocket.getOutputStream();
                    final PrintWriter writer = new PrintWriter(os);

                    final JSONObject json = new JSONObject();
                    json.put("message", message);
                    json.put("username", from.getUsername());
                    json.put("colour", from.getColour());

                    writer.println(json.toString());
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            userAccess.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Whisper something to a certain client
     *
     * @param username the username of the client the whisper is send to
     * @param message  the message that is whispered
     * @param thread   the client that tries to whisper
     * @param user     the client that tries to whisper
     */
    void whisper(String username, String message, ClientThread thread, User user) {

        try {
            userAccess.acquire();
            //loop a Map
            for (ClientThread ct : clients.values()) {
                if (ct.getUser().getUsername().equalsIgnoreCase(username)) {
                    try {
                        final OutputStream os = ct.getSocket().getOutputStream();
                        final PrintWriter writer = new PrintWriter(os);

                        final JSONObject json = new JSONObject();
                        json.put("whisper", message);
                        json.put("from", user.getUsername());
                        json.put("colour", user.getColour());

                        writer.println(json.toString());
                        writer.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    userAccess.release();
                    return;
                }
            }

            // send an error message
            thread.sendError("User was not found.");
            userAccess.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * Sends a doing message
     *
     * @param action the action
     * @param from   the user
     */
    void me(String action, User from) {
        final JSONObject json = new JSONObject();

        json.put("me", from.getUsername() + " " + action);
        json.put("colour", from.getColour());
        sendToAll(json);
    }

    /**
     * Send a message that a certain client has changed their colour
     *
     * @param user      the user that changed their colour
     * @param oldColour the old colour
     */
    void changedColour(User user, String oldColour) {
        final JSONObject json = new JSONObject();

        json.put("me", "[" + serverName + "]: " + user.getUsername() + " changed their colour to " + user.getColour().toUpperCase());
        json.put("colour", serverColour);
        sendToAll(json);
    }

    /**
     * Send a message that a certain client has changed their username
     *
     * @param user        the user that changed their colour
     * @param oldUsername the old username
     */
    void changedUsername(User user, String oldUsername) {
        final JSONObject json = new JSONObject();

        json.put("me", "[" + serverName + "]: " + oldUsername + " changed their username to " + user.getUsername());
        json.put("colour", serverColour);
        sendToAll(json);
    }

    /**
     * Save a client connection by a user
     *
     * @param user   the user that is connected on that thread
     * @param thread the thread the user is connected on
     */
    void connect(User user, ClientThread thread) {
        try {
            userAccess.acquire();
            if (!user.getUsername().isEmpty()) clients.put(user, thread);
            forward(server, user.getUsername() + " joined the server.");
            userAccess.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if a username already exists
     *
     * @param username the username to check
     */
    boolean checkUsernameExist(final String username) {

        // not allowed to have the same name as the server
        if (username.equals(serverName)) return true;

        try {
            userAccess.acquire();
            // iterate through all clients
            for (ClientThread ct : clients.values()) {
                if (ct.getUser().getUsername().equalsIgnoreCase(username)) {
                    userAccess.release();
                    return true;
                }
            }
            userAccess.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Remove a client from the map
     *
     * @param user the client to remove
     */
    void removeClient(User user) throws IOException {
        if (user != null) {
            try {
                userAccess.acquire();
                System.err.println("User with username '" + user.getUsername() + "' disconnected.");
                clients.get(user).getSocket().close();
                clients.remove(user);
                userAccess.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Send a certain json message to all clients
     *
     * @param json the message to send
     */
    private void sendToAll(JSONObject json) {

        try {
            userAccess.acquire();
            // iterate through all users
            clients.values().forEach(ct -> {
                sendTo(json, ct);
            });
            userAccess.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a list of all users to a certain client
     *
     * @param ct the client to send the list to
     */
    void sendList(ClientThread ct) {

        try {
            userAccess.acquire();

            final JSONObject userList = new JSONObject();
            final JSONArray json = new JSONArray();
            for (User u : clients.keySet()) {
                final JSONObject user = new JSONObject();
                user.put("username", u.getUsername());
                user.put("colour", u.getColour());
                json.put(user);
            }

            userList.put("userlist", json);
            sendTo(userList, ct);

            userAccess.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a message to a certain client
     *
     * @param json what to send
     * @param ct   to whom
     */
    private void sendTo(JSONObject json, ClientThread ct) {
        final Socket clientSocket = ct.getSocket();
        try {
            final OutputStream os = clientSocket.getOutputStream();
            final PrintWriter writer = new PrintWriter(os);

            writer.println(json.toString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}