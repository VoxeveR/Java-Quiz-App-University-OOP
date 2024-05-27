import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Server {
    private Socket socket = null;
    private final ServerSocket serverSocket;
    private final ExecutorService executorService;
    DatabaseHandler database;

    public Server(ServerSocket serverSocket, int numberOfThreads) {
        this.serverSocket = serverSocket;
        this.executorService = Executors.newFixedThreadPool(numberOfThreads);
    }
    public void startServer() {
        while(!serverSocket.isClosed()){
            try {
                socket = serverSocket.accept();
                System.out.println("New client connected");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            database = new DatabaseHandler("exampleDatabase");

            ClientHandler clientHandler = new ClientHandler(socket, database);
            executorService.submit(clientHandler);
        }

        database.closeDatabaseConnection();
    }
    public void closeServer() throws IOException {
        serverSocket.close();
        database.closeDatabaseConnection();
        executorService.shutdown();
    }
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(2137);
        Server server = new Server(serverSocket, 250);
        server.startServer();
    }
}