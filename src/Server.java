import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by sveno on 17-11-2016.
 */
public class Server {
    private static int SERVER_PORT = 1234;
    private ServerSocket serverSocket;
    private HashMap<String, ClientThread> hashmap = new HashMap();
    private PrintWriter writer;
    private OutputStream os;
    private String username;
    private String color;

    public void run() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Server started on port: " + SERVER_PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                ClientThread ct = new ClientThread(socket, this);
                ct.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String message){
        for (HashMap.Entry<String, ClientThread> entry : hashmap.entrySet())
        {
            Socket socket = entry.getValue().getSocket();
            try {
                os = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            writer = new PrintWriter(os);
            writer.println(message);
            writer.flush();
        }
    }

    public void put(String username, ClientThread thread){
        hashmap.put(username, thread);
    }

    public static void main(String Args[]) {
        new Server().run();
    }
}
