import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexThread extends Thread {
    Pattern pattern = Pattern.compile("(class|interface)(\\s+)(\\w+)(<.*>)?(\\s+extends(\\s+\\w+(,\\s+\\w+)*))((\\s+implements)(\\s+\\w+(,\\s+\\w+)*))*");
    Path x;
    Map<String, Set<String>> invertedIndexMap;

    RegexThread(Path x, Map<String, Set<String>> invertedIndexMap) {
        this.x = x;
        this.invertedIndexMap = invertedIndexMap;
    }

    @Override
    public void run() {
        try {
            Files.lines(x).forEach(
                    y -> {
                        Matcher matcher = pattern.matcher(y);
                        if (matcher.find()) {
                            Arrays.stream(matcher.group(6).split(",")).forEach(k -> {
                                Set<String> set = invertedIndexMap.getOrDefault(k, new HashSet<>());
                                set.add(matcher.group(1) + " " + matcher.group(3));
                                synchronized (this){
                                    invertedIndexMap.put(k, set);
                                }
                            });
                            if (matcher.group(10) != null) {
                                Arrays.stream(matcher.group(10).split(",")).forEach(k -> {
                                    Set<String> set = invertedIndexMap.getOrDefault(k, new HashSet<>());
                                    set.add(matcher.group(1) + " " + matcher.group(3));
                                    synchronized (this){
                                        invertedIndexMap.put(k, set);
                                    }
                                });
                            }
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}