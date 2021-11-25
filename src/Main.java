import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        Instant start = Instant.now();

        //очередь данных (100 размер), доступ к очереди для потоков, заблокированных при вставке или удалении, обрабатывается в порядке FIFO
        BlockingQueue<String> tasksQueue = new ArrayBlockingQueue<>(100, true);

        //очередь для результатов потоков
        BlockingQueue<Map<String, Set<String>>> poolMap = new LinkedBlockingQueue<>();

        //запускам 6 потоков
        for (int i = 0; i < 6; i++) {
            new Thread(new RegexProcessor(tasksQueue, poolMap)).start();
        }

        //раздаем задания для потоков
        Map(tasksQueue);

        //завершаем потоки с помощью poison pill
        for (int i = 0; i < 6; i++) {
            tasksQueue.put("stop");
        }

        //собираем информацию полученную от потоков в одну Map
        Map<String, Set<String>> invertedIndexMap  = Reducer(poolMap);
        Instant stop = Instant.now();

        invertedIndexMap .forEach((x, y) -> System.out.println(x + " базовый для " + y));

        System.out.println("Количество классов и интерфейсов : " + invertedIndexMap .size());

        Duration duration = Duration.between(start, stop);
        System.out.println(duration.toMillis());
        System.out.printf("%d H, %d M, %d S%n", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
    }

    //Метод для объединения результатов потоков
    private static Map<String, Set<String>> Reducer(BlockingQueue<Map<String, Set<String>>> maps) throws InterruptedException {
        Map<String, Set<String>> map = new HashMap<>();
        while (!maps.isEmpty()) {
            map = Stream.concat(map.entrySet().stream(), maps.take().entrySet().stream())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> Stream.concat(e1.stream(), e2.stream())
                                    .collect(Collectors.toSet())
                    ));
        }
        return map;
    }

    //Метод применяющий нужную функцию к каждому элементу списка tasks
    private static void Map(BlockingQueue<String> tasks) throws IOException {
        Files.find(Paths.get("G:\\учёба университет\\7 семестр\\параллельное\\spring-framework-main"), 999, (p, bfa) -> bfa.isRegularFile()
                && p.getFileName().toString().matches(".*\\.java")).forEach(x -> {
            try {
                tasks.put(x.toString());
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        });
    }
}

