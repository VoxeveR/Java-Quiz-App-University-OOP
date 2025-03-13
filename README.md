# Quiz System (Java)

## Project Description

This project is a simple client-server application in Java that allows users to participate in quizzes. The application consists of a client that connects to the server, submits answers to questions, and receives results, as well as a server that handles multiple clients, manages the database, and processes the answers. The project uses a MySQL database to store tests, questions, answers, and results. There's also an additional version of project working on text files instead of database, though the main focus of this project is the database-based implementation.

### Main components of the project:
- **Client** - The application that connects to the server, submits answers, and receives questions and results.
- **Server** - The application that handles multiple clients, processes questions and answers, and manages the database connection.
- **Database Handler** - Responsible for connecting to the database, creating tables, and storing tests, questions, answers, and results.
- **Client Handler** - Manages each client, processes questions, checks answers, and stores results in the database.

## Requirements

- JDK 11 or newer
- MySQL or MariaDB
- A database with the appropriate tables (the database structure can be generated automatically via the application)

## How to Use

### 1. Running the Server

To run the server, you need to:
1. Set up MySQL and create the database (`exampleDatabase` or change the name in the code).
2. Ensure you have the correct database connection details (username, password).
3. Start the server:

```bash
javac Server.java
java Server
```

### 2. Running the Client
To run the client, simply execute:

```bash
javac Client.java
java Client
```
### 3. Database structure

![image](https://github.com/user-attachments/assets/dda968a2-a539-48a8-bcff-2826a5bf81d8)

The database should contain the following tables:

- **Users** - Stores user data (id, first name, last name).
- **Tests** - Stores information about quizzes (id, name, description, number of questions).
- **Results** - Stores user results for quizzes (id, test_id, user_id, score).
- **Questions** - Stores quiz questions (id, question, correct_answer, test_id).
- **Answers** - Stores possible answers to questions (id, question_id, answer).
- **userAnswers** - Stores user answers to questions (user_id, question_id, answer).
