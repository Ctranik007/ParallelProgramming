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
        Pattern pattern = Pattern.compile("(class\\s+)(\\w+)(<.*>)?(\\s+extends\\s+)(\\w+)");

        Map<String,Set<String>> invertedIndexMap = new HashMap<>();
        Files.find(Paths.get("G:\\study\\7 семестр\\параллельное\\ParallelProgramming\\лб2\\spring-framework-main"), 999, (p, bfa) -> bfa.isRegularFile()
                && p.getFileName().toString().matches(".*\\.java")).forEach(x->{
                    try {
                        Files.lines(x).forEach(
                                y->{
                                    Matcher m = pattern.matcher(y);
                                    if (m.find()) {
                                        Set<String> set = invertedIndexMap.getOrDefault(m.group(5),new HashSet<>());
                                        set.add(m.group(2));
                                        invertedIndexMap.put(m.group(5),set);
                                    }
                                }
                        );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );

        invertedIndexMap.keySet().forEach(k-> System.out.println("Class " + k + " is base for " + invertedIndexMap.get(k)));
        System.out.println(invertedIndexMap.size());
    }
}
// (((|public|final|abstract|private|static|protected)(\s+))?(class)(\s+)(\w+)(<.*>)?(\s+extends\s+)(\w+)?(<.*>)?(\s+implements\s+)?(.*)?(<.*>)?(\w*))
//(class\s+)(\w+)(<.*>)?(\s+extends\s+)(\w+)?