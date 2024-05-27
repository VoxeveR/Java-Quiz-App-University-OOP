import java.io.*;
import java.net.*;
import java.util.*;


public class Client {
    private Socket socket = null;
    private BufferedReader buffInput = null;
    private BufferedWriter buffOutput = null;
    private Boolean initial = true;

    public Client(Socket socket) throws IOException {
        this.socket = socket;
        this.buffInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.buffOutput = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        System.out.println("Succesfully connected to a server");
    }

    public void sendMessage() throws IOException {
            Scanner scanner = new Scanner(System.in);
            scanner.useDelimiter("");
            String line = "";
            int questionCount = Integer.parseInt(buffInput.readLine());
            while (questionCount > 0) {
                try {
                    if (initial) {

                        System.out.print("Name: ");
                        String name = scanner.nextLine();
                        sendToServer(name);

                        System.out.print("Surname: ");
                        String surname = scanner.nextLine();
                        sendToServer(surname);

                        for (int i = 0; i < 2; i++) {
                            String serverMessage = buffInput.readLine();
                            System.out.println(serverMessage);
                        }
                        initial = false;
                    }

                    for (int i = 0; i < 5; i++) {
                        String serverMessage = buffInput.readLine();
                        if (serverMessage.equals("TIMEOUT")){
                            System.out.println("Czas na udzielenie odpowiedzi minął!. Nie zostaną ci przyznane punkty!");
                            i--;
                        } else {
                            System.out.println(serverMessage);
                        }
                    }

                    line = scanner.nextLine().toUpperCase();
                    while (!((line.length() == 1) && (line.charAt(0) >= 'A') && (line.charAt(0) <= 'D'))) {
                        System.out.println("Wprowadź literę A, B, C lub D!");
                        line = scanner.nextLine().toUpperCase();
                    }
                    sendToServer(line);

                    questionCount--;
                } catch (IOException e) {
                    System.out.println("aha");
                 }
            }
        System.out.println("The test has ended. You'll receive ur results soon");
        closeClient();
        }

    public void sendToServer(String data){
        try {
            buffOutput.write(data);
            buffOutput.newLine();
            buffOutput.flush();
        } catch (IOException e) {
            System.out.println("Lost connection to a server, couldn't send data.");
        }
    }

    private void closeClient(){
        try {
            if (socket != null) socket.close();
            if (buffInput != null) buffInput.close();
            if (buffOutput != null) buffOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws IOException {
        Socket socket = new Socket("localhost", 2137);
        Client client = new Client(socket);
        client.sendMessage();
    }



}
