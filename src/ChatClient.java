import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ChatClient {

    public static void main(String[] args) {
        final int portNumber = 4444;
        try (BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {

            Socket socket = new Socket(InetAddress.getLoopbackAddress(), portNumber);
            System.out.println("Please enter your name:");
            String name = userInput.readLine();
            System.out.println("Chat program started for " + name);
            ChatServerRunnable server = new ChatServerRunnable(socket, name);
            System.out.println("Please enter your messages:");
            Thread thread = new Thread(server);
            thread.start();
            thread.join();


        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private static class ChatServerRunnable implements Runnable {
        private Socket socket;
        private String name;

        public ChatServerRunnable(Socket socket, String name) {
            this.socket = socket;
            this.name = name;
        }

        @Override
        public void run() {
            try (PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 BufferedReader clientIn = new BufferedReader(new InputStreamReader(System.in));) {


                while (!socket.isClosed()) {

                    if (in.ready()) {
                        String message = in.readLine();
                        System.out.println(message);
                    }

                    if (clientIn.ready()) {
                        String line = clientIn.readLine();
                        out.println("\"" + name + "\"" + " : " + line);
                        System.out.println("Message Sent");
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        }
    }

}
