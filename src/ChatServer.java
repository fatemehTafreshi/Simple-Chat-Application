import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ChatServer {

    private static ConcurrentMap<UUID, ChatClientRunnable> clients;
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        final int portNumber = 4444;

        try {
            serverSocket = new ServerSocket(portNumber);
            acceptClients();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void acceptClients() {
        clients = new ConcurrentHashMap<>();
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("New Client Connected");
                UUID id = UUID.randomUUID();

                ChatClientRunnable client = new ChatClientRunnable(socket, id);
                Thread thread = new Thread(client);
                thread.start();
                clients.put(id, client);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class ChatClientRunnable implements Runnable {
        private final Socket socket;
        private final UUID id;
        PrintWriter out;

        public ChatClientRunnable(Socket socket, UUID id) {
            this.socket = socket;
            this.id = id;
        }

        public PrintWriter getOut() {
            return out;
        }

        @Override
        public void run() {
            try (BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));) {

                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

                while (!socket.isClosed()) {
                    if (in.ready()) {
                        String message = in.readLine();
                        System.out.println("Message from client " + message);
                        for (UUID clientId : clients.keySet()) {
                            if (clientId.equals(id)) continue;
                            clients.get(clientId).getOut().println("Message from client " + message);
                        }
                    }

                    if (userIn.ready()) {
                        String line = userIn.readLine();
                        for (UUID clientId : clients.keySet()) {
                            clients.get(clientId).getOut().println("Message from server : " + line);
                        }
                        System.out.println("Message Sent");
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }finally {
                out.close();
            }


        }
    }
}
