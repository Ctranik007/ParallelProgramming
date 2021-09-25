import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) throws IOException {
        Instant start = Instant.now();
        Pattern pattern = Pattern.compile("(class|interface)(\\s+)(\\w+)(<.*>)?(\\s+extends(\\s+\\w+(,\\s+\\w+)*))((\\s+implements)(\\s+\\w+(,\\s+\\w+)*))*");

        Map<String, Set<String>> invertedIndexMap = new HashMap<>();
        Files.find(Paths.get("C:\\Users\\pavel\\Downloads\\spring-framework-main\\spring-framework-main"), 999, (p, bfa) -> bfa.isRegularFile()
                && p.getFileName().toString().matches(".*\\.java")).forEach(x -> {
                    try {
                        Files.lines(x).forEach(
                                y -> {
                                    Matcher matcher = pattern.matcher(y);
                                    if (matcher.find()) {
                                        Arrays.stream(matcher.group(6).split(",")).forEach(k -> {
                                            Set<String> set = invertedIndexMap.getOrDefault(k, new HashSet<>());
                                            set.add(matcher.group(1) + " " + matcher.group(3));
                                            invertedIndexMap.put(k, set);
                                        });
                                        if (matcher.group(10) != null) {
                                            Arrays.stream(matcher.group(10).split(",")).forEach(k -> {
                                                Set<String> set = invertedIndexMap.getOrDefault(k, new HashSet<>());
                                                set.add(matcher.group(1) + " " + matcher.group(3));
                                                invertedIndexMap.put(k, set);
                                            });
                                        }

                                    }

                                }
                        );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );

        invertedIndexMap.keySet().forEach(k -> System.out.println(k + " базовый для " + invertedIndexMap.get(k)));
        Instant stop = Instant.now();

        System.out.println("Количество классов и интерфейсов : " + invertedIndexMap.size());
        Duration duration = Duration.between(start, stop);
        System.out.println(duration.toMillis());
        System.out.println(String.format("%d H, %d M, %d S", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));
    }
}
// (((|public|final|abstract|private|static|protected)(\s+))?(class)(\s+)(\w+)(<.*>)?(\s+extends\s+)(\w+)?(<.*>)?(\s+implements\s+)?(.*)?(<.*>)?(\w*))
//(class\s+)(\w+)(<.*>)?(\s+extends\s+)(\w+)?