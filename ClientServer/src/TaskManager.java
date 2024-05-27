import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TaskManager {
    private final ExecutorService executorService;
    private final Lock answersFileLock = new ReentrantLock();
    private final Lock resultsFileLock = new ReentrantLock();
    private final Condition answersFileCondition = answersFileLock.newCondition();
    private final Condition resultsFileCondition = resultsFileLock.newCondition();
    private boolean isAnswersFileAccessed = false;
    private boolean isResultsFileAccessed = false;
    String answersFilePath = "src\\bazaOdpowiedzi.txt";
    String resultsFilePath = "src\\wyniki.txt";

    public TaskManager(int numberOfThreads) {
        this.executorService = Executors.newFixedThreadPool(numberOfThreads);
    }

    public FutureTask<Pair<String, String>> submitTask(Callable<Pair<String, String>> task) {
        FutureTask<Pair<String, String>> futureTask = new FutureTask<>(task) {
            @Override
            protected void done() {
                try {
                    Pair<String, String> result = get();
                    save(answersFilePath, result.getFirst(), answersFileLock, answersFileCondition, "answers");
                    save(resultsFilePath, result.getSecond(), resultsFileLock, resultsFileCondition, "results");
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println("Interruption/Execution exception occurred");
                } catch (CancellationException cancel) {
                    System.out.println("Thread was cancelled");
                }
            }
        };

        executorService.submit(futureTask);
        return futureTask;
    }

    private void save(String file, String data, Lock lock, Condition condition, String accessType) {
        lock.lock();
        try {
            boolean isAccessed = accessType.equals("answers") ? isAnswersFileAccessed : isResultsFileAccessed;

            if (isAccessed) {
                condition.await();
            }

            if (accessType.equals("answers")) {
                isAnswersFileAccessed = true;
            } else {
                isResultsFileAccessed = true;
            }

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
                bw.write(data);
                bw.newLine();
            }
        } catch (InterruptedException | FileNotFoundException ie) {
            System.out.println("Thread interrupted while waiting for the lock.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (accessType.equals("answers")) {
                isAnswersFileAccessed = false;
            } else {
                isResultsFileAccessed = false;
            }
            condition.signal();
            lock.unlock();
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }
}