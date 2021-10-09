import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Instant start = Instant.now();

        List<Thread> threadList = new ArrayList<>();

        Map<String, Set<String>> invertedIndexMap = new HashMap<>();
        Files.find(Paths.get("D:\\7 семестр\\параллельное\\spring-framework-main"), 999, (p, bfa) -> bfa.isRegularFile()
                && p.getFileName().toString().matches(".*\\.java")).forEach(x -> {

            RegexThread thread = new RegexThread(x, invertedIndexMap);

            thread.start();
            threadList.add(thread);
        });

        for (Thread thread : threadList) {
            thread.join();
        }
        Instant stop = Instant.now();
        invertedIndexMap.keySet().forEach(k -> System.out.println(k + " базовый для " + invertedIndexMap.get(k)));
        System.out.println("Количество классов и интерфейсов : " + invertedIndexMap.size());

        Duration duration = Duration.between(start, stop);
        System.out.println(duration.toMillis());
        System.out.printf("%d H, %d M, %d S%n", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
    }
}
