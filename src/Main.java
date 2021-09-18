import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) throws IOException {
        Pattern pattern = Pattern.compile("(class|interface)(\\s+)(\\w+)(<.*>)?(\\s+extends\\s+)(\\w+)");

        Map<String,Set<String>> invertedIndexMap = new HashMap<>();
        Files.find(Paths.get("C:\\Users\\pavel\\Downloads\\spring-framework-main\\spring-framework-main"), 999, (p, bfa) -> bfa.isRegularFile()
                && p.getFileName().toString().matches(".*\\.java")).forEach(x->{
                    try {
                        Files.lines(x).forEach(
                                y->{
                                    Matcher matcher = pattern.matcher(y);
                                    if (matcher.find()) {
                                        Set<String> set = invertedIndexMap.getOrDefault(matcher.group(6),new HashSet<>());
                                        set.add(matcher.group(1) + " " +matcher.group(3));
                                        invertedIndexMap.put(matcher.group(6),set);
                                    }

                                }
                        );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );

        invertedIndexMap.keySet().forEach(k-> System.out.println("Класс " + k + " базовый для " + invertedIndexMap.get(k)));
        System.out.println(invertedIndexMap.size());
    }
}
// (((|public|final|abstract|private|static|protected)(\s+))?(class)(\s+)(\w+)(<.*>)?(\s+extends\s+)(\w+)?(<.*>)?(\s+implements\s+)?(.*)?(<.*>)?(\w*))
//(class\s+)(\w+)(<.*>)?(\s+extends\s+)(\w+)?