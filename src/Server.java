import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by sveno on 17-11-2016.
 */
public class Server {
    private static int SERVER_PORT = 1234;
    private ServerSocket serverSocket;

    public void run(){
        while(true) {
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
                System.out.println("Server started on port: " + SERVER_PORT);
                Socket socket = serverSocket.accept();
                ClientThread ct = new ClientThread(socket);
                ct.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String Args[]){
        new Server().run();
    }
}
