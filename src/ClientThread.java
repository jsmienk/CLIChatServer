import java.net.Socket;

/**
 * Created by sveno on 17-11-2016.
 */
public class ClientThread extends Thread{
        private Socket socket;

        public ClientThread(Socket socket) {
            this.socket = socket;
                System.out.println("Client connected");
        }

        public void run() {
                while(true) {
                        // 1. Wacht op berichten van de client.
                        // 2. Stuur berichten van de clients door naar de andere
                        // clients. (Broadcast)
                }
        }
}
