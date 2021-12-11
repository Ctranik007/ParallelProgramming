import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        Instant start = Instant.now();

        //очередь данных (100 размер), доступ к очереди для потоков, заблокированных при вставке или удалении, обрабатывается в порядке FIFO
        BlockingQueue<String> tasksQueue = new ArrayBlockingQueue<>(100, true);

        //очередь для результатов потоков
        BlockingQueue<Map<String, Set<String>>> poolResultsMap = new LinkedBlockingQueue<>(50);

        //запускам 6 потоков
        for (int i = 0; i < 6; i++) {
            new Thread(new RegexProcessor(tasksQueue, poolResultsMap)).start();
        }
        //создаем поток для объединения результатов потоков
        FutureTask<Map<String, Set<String>>> future = new FutureTask<>(new CleanResultQueue(poolResultsMap));
        new Thread(future).start();

        //раздаем задания для потоков
        Map(tasksQueue);

        //завершаем потоки с помощью poison pill
        for (int i = 0; i < 6; i++) {
            tasksQueue.put("stop");
        }

        //собираем информацию полученную от потоков в одну Map
        Map<String, Set<String>> invertedIndexMap = future.get();
        Instant stop = Instant.now();

        invertedIndexMap .forEach((x, y) -> System.out.println(x + " базовый для " + y));

        System.out.println("Количество классов и интерфейсов : " + invertedIndexMap .size());

        Duration duration = Duration.between(start, stop);
        System.out.println(duration.toMillis());
        System.out.printf("%d H, %d M, %d S%n", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
    }

    //Метод применяющий нужную функцию к каждому элементу списка tasks
    private static void Map(BlockingQueue<String> tasks) throws IOException {
        Files.find(Paths.get("G:\\учёба университет\\7 семестр\\параллельное\\лб7\\ParallelProgramming\\input"), 999, (p, bfa) -> bfa.isRegularFile()
                && p.getFileName().toString().matches(".*\\.java")).forEach(x -> {
            try {
                tasks.put(x.toString());
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        });
    }
}

