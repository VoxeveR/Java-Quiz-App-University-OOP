import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

public class DatabaseHandler {
    private final static String DBURL = "jdbc:mysql://127.0.0.1:3306/";
    private final static String DBUSER = "root";
    private final static String DBPASS = "";
    private final static String DBDRIVER = "com.mysql.cj.jdbc.Driver";
    private Connection connection;
    private static Statement statement;

    public DatabaseHandler(String databaseName) {
        try {
            if(loadDriver()) System.out.println("Driver is OK!");
            else System.exit(1);

            connection = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
            statement = createStatement(connection);

            if(executeUpdateCr(("USE " + databaseName + ";")) == 0){
                System.out.println("Baza wybrana");
            } else{
                System.out.println("Baza nie istnieje, tworzymy nową bazę o nazwie " + databaseName);
                if(executeUpdateCr("CREATE DATABASE " + databaseName  + ";") == 1){
                    System.out.println("Baza utworzona!");
                } else {
                    System.out.println("Baza nieutworzona!");
                    System.exit(5);
                }
                if(executeUpdateCr("USE " +databaseName + ";") == 0){
                    System.out.println("Baza wybrana!");
                    buildDatabase();
                } else {
                    System.out.println("Baza niewybrana!");
                }
            }

            System.out.println("Connection created succesfully.");
        } catch (SQLException e) {
            System.err.println("Nie można połączyć z bazą danych.");
            System.exit(4);
        }
    }

    private void buildDatabase (){
        try {
            System.out.println("Tworzę strukturę bazy danych!");
            statement.executeUpdate("CREATE TABLE Users ( user_id int(4) PRIMARY KEY AUTO_INCREMENT, name " +
                    "varchar(20) NOT NULL, surname varchar(20) NOT NULL);");
            System.out.println("Utworzono tabelę użytkownik.");
            statement.executeUpdate("CREATE TABLE Tests ( test_id int(4) PRIMARY KEY AUTO_INCREMENT, name " +
                    "varchar(100) NOT NULL, description TEXT NOT NULL, questionCount INT(3) NOT NULL);");
            System.out.println("Utworzono tabelę testy.");
            statement.executeUpdate("CREATE TABLE Results (results_id int(4) PRIMARY KEY AUTO_INCREMENT, " +
                    "test_id int(4) NOT NULL, user_id int(4) NOT NULL, result int(3), CONSTRAINT FOREIGN KEY (test_id) " +
                    "REFERENCES Tests(test_id), CONSTRAINT FOREIGN KEY (user_id) REFERENCES Users(user_id));");
            System.out.println("Utworzono tabelę wyniki.");
            statement.executeUpdate("CREATE TABLE Questions (question_id int(4) PRIMARY KEY AUTO_INCREMENT, " +
                    "question TEXT NOT NULL, correct_answer varchar(1) NOT NULL, test_id int(4) NOT NULL, CONSTRAINT " +
                    "FOREIGN KEY (test_id) REFERENCES Tests(test_id));");
            System.out.println("Utworzono tabelę pytania.");
            statement.executeUpdate("CREATE TABLE userAnswers ( useranswer_id int(4) PRIMARY KEY AUTO_INCREMENT, " +
                    "user_id int(4) NOT NULL, question_id int(4) NOT NULL, answer varchar(1), CONSTRAINT FOREIGN KEY " +
                    "(user_id) REFERENCES Users(user_id), CONSTRAINT FOREIGN KEY (question_id) " +
                    "REFERENCES Questions(question_id));");
            System.out.println("Utworzono tabelę odpowiedzi użytkownika.");
            statement.executeUpdate("CREATE TABLE Answers (answer_id int(4) PRIMARY KEY AUTO_INCREMENT, " +
                    "question_id int(4) NOT NULL, answer varchar(200) NOT NULL, CONSTRAINT FOREIGN KEY (question_id) " +
                    "REFERENCES Questions(ssquestion_id));");
            System.out.println("Utworzono tabelę odpowiedzi.");
            loadExampleData();
        } catch (SQLException sql) {
            System.err.println("Błąd w trakcie tworzenia tabel!");
        }
    }

    private void loadExampleData(){
        System.out.println("Rozpoczynam wprowadzanie przykładowych danych.");
        try{
            BufferedReader reader = new BufferedReader(new FileReader("src\\bazaPytan.txt"));
            int numberOfQuestions = Integer.parseInt(reader.readLine());
            String testName = reader.readLine();
            String testDescription = reader.readLine();

            statement.executeUpdate("INSERT INTO TESTS (name, description, questionCount) VALUES ( \"" + testName +
                    "\",\"" + testDescription + "\",\"" + numberOfQuestions + "\");", Statement.RETURN_GENERATED_KEYS);
            ResultSet res = statement.getGeneratedKeys();
            res.next();
            int testID = res.getInt(1);

            String question;
            ArrayList<String> answers = new ArrayList<>();
            String correctAnswer;
            for(int i = 0; i < numberOfQuestions; i++){
                question = reader.readLine();
                for(int j = 0; j < 4; j++){
                    answers.add(reader.readLine());
                }
                correctAnswer = reader.readLine();
                statement.executeUpdate("INSERT INTO Questions (question, correct_answer, test_id) VALUES (\"" +
                        question + "\",\"" + correctAnswer +"\",\"" + testID +"\");", Statement.RETURN_GENERATED_KEYS);
                res = statement.getGeneratedKeys();
                res.next();
                int questionID = res.getInt(1);

                for(int k = 0; k < 4; k++){
                    statement.executeUpdate("INSERT INTO Answers (question_id, answer) VALUES (\"" + questionID +
                            "\",\"" + answers.getFirst() +"\");");
                    answers.removeFirst();
                }
            }

            System.out.println("Wprowadzono przykładowe dane.");
        } catch(FileNotFoundException e){
            System.err.println("Bład podczas otwierania pliku: ");
        } catch(IOException ioException){
            System.err.println("Bład w trakcie odczytywania pliku: " + ioException);
        } catch (SQLException sqlException){
            System.err.println("Bład w trakcie wprowadzania danych: " + sqlException);
        }

    }

    private static Statement createStatement(Connection connection){
        try {
            return connection.createStatement();
        } catch (SQLException e) {
            System.err.println("Bład przy tworzeniu obiektu Statement!");
            System.exit(2);
        }
        return null;
    }
    private static boolean loadDriver(){
        try {
            Class.forName(DBDRIVER);
            return true;
        } catch (ClassNotFoundException e) {
            System.err.println("Sterownik DBDriver nie został załadowany pomyślnie");
            System.exit(1);
        }
        return false;
    }
    public void closeDatabaseConnection(){
        System.out.println("Zamykanie połączenia z bazą danych.");
        try {
            connection.close();
            statement.close();
        } catch (SQLException e) {
            System.err.println("Nie udało się zamknąć połączenia z bazą danych.");
            System.exit(3);
        }
        System.out.println("Połączenie zamknięte pomyślnie");
    }

    private static int executeUpdateCr(String sql){
        try {
            return statement.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("Błąd! Zapytanie nie zostało wykonane.");
        }
        return -1;
    }

    public ResultSet executeUpdate(String sql){
        try {
            statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            return statement.getGeneratedKeys();
        } catch (SQLException e) {
            System.err.println("Błąd! Zapytanie nie zostało wykonane.");
        }
        return null;
    }

    public ResultSet executeQuery(String sql){
        try {
            return statement.executeQuery(sql);
        } catch (SQLException e) {
            System.err.println("Błąd! Zapytanie nie zostało wykonane.");
        }
        return null;
    }
}
