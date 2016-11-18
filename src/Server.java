import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: Sven & Jeroen
 * Date created: 17-11-2016
 */
public class Server {

    private static final int SERVER_PORT = 1234;

    private Map<User, ClientThread> clients;

    // initialize the client map
    private Server() {
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
     * @param from    the User that send the message
     * @param message the message that was send
     */
    void forward(User from, String message) {

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
    }

    /**
     * Save a client connection by a User
     *
     * @param user   the user that is connected on that thread
     * @param thread the thread the user is connected on
     */
    void connect(User user, ClientThread thread) {
        if (!user.getUsername().equals("default")) clients.put(user, thread);
    }
}
