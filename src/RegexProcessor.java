import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexProcessor implements Runnable {
    private final BlockingQueue<Map<String, Set<String>>> poolResultsMap;
    private final BlockingQueue<String> tasksQueue;
    private final String poison = "stop";

    public RegexProcessor(BlockingQueue<String> tasksQueue, BlockingQueue<Map<String, Set<String>>> poolResultsMap) {
        this.tasksQueue = tasksQueue;
        this.poolResultsMap = poolResultsMap;
    }

    @Override
    public void run() {
        try {
            String file;
            while (!((file = tasksQueue.take()).equals(poison))) {
                //в каждом потоке создаем свою HashMap
                Map<String, Set<String>> invertedIndexMap = new HashMap<>();
                Files.lines(Path.of(file)).forEach(
                        y -> {
                            Pattern pattern = Pattern.compile("(class|interface)(\\s+)(\\w+)(<.*>)?(\\s+extends(\\s+\\w+(,\\s+\\w+)*))((\\s+implements)(\\s+\\w+(,\\s+\\w+)*))*");
                            Matcher matcher = pattern.matcher(y);
                            if (matcher.find()) {
                                for (String s : matcher.group(6).split(",")) {
                                    Set<String> set = invertedIndexMap.getOrDefault(s, new HashSet<>());
                                    set.add(matcher.group(1) + " " + matcher.group(3));
                                    invertedIndexMap.put(s, set);

                                }
                                if (matcher.group(10) != null) {
                                    Arrays.stream(matcher.group(10).split(",")).forEach(k -> {
                                        Set<String> set = invertedIndexMap.getOrDefault(k, new HashSet<>());
                                        set.add(matcher.group(1) + " " + matcher.group(3));
                                        invertedIndexMap.put(k, set);
                                    });
                                }
                            }
                        });
                //добавляем HashMap текущего потока в общую очередь
                poolResultsMap.put(invertedIndexMap);
            }
            //метка что поток нужно завершить
            Map<String, Set<String>> poisonPill = new HashMap<>();
            poisonPill.put("poison", new HashSet<>());
            poolResultsMap.put(poisonPill);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

