import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

class ClientHandler implements Runnable {
    private Socket socket = null;
    private BufferedReader buffInput;
    private BufferedWriter buffOutput;
    private DatabaseHandler database;

    public ClientHandler(Socket socket, DatabaseHandler database) {
        try {
            this.socket = socket;
            this.database = database;
            this.buffInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.buffOutput = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch(IOException o){
            closeClientHandler();
        }
    }

    @Override
    public void run() {
        int testID = 1;
        int questionNumber = 0;
        int result = 0;
        int questionCount;
        int userID = 0;
        ResultSet res;

        try {
            res = database.executeQuery("SELECT * FROM tests WHERE test_id=" + testID);
            res.next();
            questionCount = res.getInt(4);
            sendToClient(String.valueOf(questionCount));

            String name = buffInput.readLine();
            String surname = buffInput.readLine();
            res = database.executeUpdate("INSERT INTO Users (name, surname) VALUES (\"" + name +"\",\"" + surname + "\");");
            res.next();
            userID = res.getInt(1);
            res = database.executeQuery("SELECT * FROM tests WHERE test_id=" + testID);
            res.next();
            sendToClient(res.getString(2));
            sendToClient(res.getString(3));

            ArrayList<String> questionArr = new ArrayList<>();
            ArrayList<String> answerArr = new ArrayList<>();
            ArrayList<String> correctAnswerArr = new ArrayList<>();
            ArrayList<String> questionIdArr = new ArrayList<>();

            res = database.executeQuery("SELECT questions.*, answer FROM questions LEFT JOIN answers ON answers.question_id = questions.question_id WHERE test_id=" + testID);
            for(int i = 0; i < questionCount; i++){
                res.next();
                questionIdArr.add(String.valueOf(res.getInt(1)));
                questionArr.add(res.getString(2));
                correctAnswerArr.add(res.getString(3));

                answerArr.add(res.getString(5));
                for(int j=0; j < 3; j++){
                    res.next();
                    answerArr.add(res.getString(5));
                }
            }

            for(int i = 0; i < questionCount; i++){
                try {
                    System.out.println(questionArr.getFirst());
                    sendToClient(questionArr.getFirst());
                    questionArr.removeFirst();

                    for(int j=0; j < 4; j++){
                        sendToClient(answerArr.getFirst());
                        answerArr.removeFirst();
                    }

                    socket.setSoTimeout(30000);

                    questionNumber++;
                    String clientMessage = buffInput.readLine();
                    System.out.println(questionNumber + ". " + surname + ": " + clientMessage);

                    if (clientMessage.equalsIgnoreCase(correctAnswerArr.getFirst())) result++;
                    database.executeUpdate("INSERT INTO useranswers (user_id, question_id, answer) VALUES ("
                            + userID + "," + questionIdArr.get(i) + ", '" + clientMessage + "');");
                    correctAnswerArr.removeFirst();
                } catch (SocketTimeoutException ste) {
                    String clientMessage = "X";
                    database.executeUpdate("INSERT INTO useranswers (user_id, question_id, answer) VALUES ("
                            + userID + "," + questionIdArr.get(i) + ", '" + clientMessage + "');");
                    System.out.println(questionIdArr.get(i));
                    System.out.println("Czas uzytkownika " + name + " " + surname + " na udzielenie odpowiedzi " +
                            "na pytanie nr." + questionNumber + " minął");
                    sendToClient("TIMEOUT");
                    this.socket.setSoTimeout(0);
                    buffInput.readLine();
                }
            }
        } catch (IOException | SQLException e) {
            closeClientHandler();
        } finally {
            closeClientHandler();
        }
        database.executeUpdate("INSERT INTO results (test_id, user_id, result) VALUES (" + testID + "," + userID + ", " + result + ");");
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
