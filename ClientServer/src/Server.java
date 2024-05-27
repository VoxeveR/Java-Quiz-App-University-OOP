import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.*;

public class Server {
    private Socket socket = null;
    private final ServerSocket serverSocket;
    private final TaskManager taskManager;

    public Server(ServerSocket serverSocket, int numberOfThreads) {
        this.serverSocket = serverSocket;
        this.taskManager = new TaskManager(numberOfThreads);
    }

    public void startServer() {
        while(!serverSocket.isClosed()){
            try {
                socket = serverSocket.accept();
                System.out.println("New client connected");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            ClientHandler clientHandler = new ClientHandler(socket);
            taskManager.submitTask(clientHandler);
        }
    }
    public void closeServer() throws IOException {
        serverSocket.close();
        taskManager.shutdown();
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(2137);
        Server server = new Server(serverSocket, 250);
        server.startServer();
    }


}

class ClientHandler implements Callable<Pair<String, String>> {
    private Socket socket = null;
    private BufferedReader buffInput;
    private BufferedWriter buffOutput;
    private Boolean initial = true;
    File bazaPytan = new File("src\\bazaPytan.txt");
    private String name;
    private String surname;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.buffInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.buffOutput = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch(IOException o){
            closeClientHandler();
        }
    }

    @Override
    public Pair<String,String> call() {
        StringBuilder userAnswers = new StringBuilder();
        String userScore = "";
        int result = 0;
        int questionNumber = 0;
        try {
            Scanner fileReader = new Scanner(bazaPytan);
            String questionCount = fileReader.nextLine();
            sendToClient(questionCount);

            while (fileReader.hasNextLine()) {
                try {
                    if (initial) {
                        name = buffInput.readLine();
                        surname = buffInput.readLine();
                        for (int i = 0; i < 2; i++) {
                            if (fileReader.hasNextLine()) {
                                sendToClient(fileReader.nextLine());
                            }
                        }
                        userAnswers.append(name).append(" ").append(surname).append(": ");
                        initial = false;
                    }

                    for (int i = 0; i < 5; i++) {
                        if (fileReader.hasNextLine()) {
                            sendToClient(fileReader.nextLine());
                        }
                    }
                    socket.setSoTimeout(30000);
                    if (fileReader.hasNextLine()) {
                        questionNumber++;
                        String correctAnswer = fileReader.nextLine();
                        String clientMessage = buffInput.readLine();
                        System.out.println(questionNumber + ". " + surname + ": " + clientMessage);

                        userAnswers.append(questionNumber).append(".").append(clientMessage).append(" ");
                        if (clientMessage.equalsIgnoreCase(correctAnswer)) result++;
                    } else {
                        break;
                    }
                } catch (SocketTimeoutException ste) {
                    userAnswers.append(questionNumber).append(".X ");

                    System.out.println("Czas uzytkownika " + name + " " + surname + " na udzielenie odpowiedzi " +
                            "na pytanie nr." + questionNumber + " minął");
                    sendToClient("TIMEOUT");
                    this.socket.setSoTimeout(0);
                    buffInput.readLine();
                }
            }
        } catch (IOException e) {
            closeClientHandler();
        } finally {
            closeClientHandler();
        }
        userScore = (name + " " + surname + ": " + result);
        return new Pair<>(userAnswers.toString(), userScore);
    }

    public void sendToClient(String data){
        try {
            buffOutput.write(data);
            buffOutput.newLine();
            buffOutput.flush();
        } catch (IOException e) {
            System.out.println("Lost connection to a client, couldn't send data.");
        }
    }

    private void closeClientHandler(){
        try {
            if (socket != null) socket.close();
            if (buffInput != null) buffInput.close();
            if (buffOutput != null) buffOutput.close();
        } catch (IOException e) {
            System.out.println("Already closed!");
        }
    }
}
