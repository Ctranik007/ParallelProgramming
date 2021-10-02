import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        long m = System.currentTimeMillis();

        List<Thread> threadList = new ArrayList<>();

        Map<String, Set<String>> invertedIndexMap = new HashMap<>();
        Files.find(Paths.get("D:\\7 семестр\\параллельное\\spring-framework-main"), 999, (p, bfa) -> bfa.isRegularFile()
                && p.getFileName().toString().matches(".*\\.java")).forEach(x -> {

            RegexThread thread = new RegexThread(x, invertedIndexMap);

            thread.start();
            threadList.add(thread);
        });

        for (Thread f : threadList) {
            f.join();
        }
        invertedIndexMap.keySet().forEach(k -> System.out.println(k + " базовый для " + invertedIndexMap.get(k)));
        System.out.println("Количество классов и интерфейсов : " + invertedIndexMap.size());
        System.out.println("Время выполнения мс : " + (double) (System.currentTimeMillis() - m));
    }
}
// (((|public|final|abstract|private|static|protected)(\s+))?(class)(\s+)(\w+)(<.*>)?(\s+extends\s+)(\w+)?(<.*>)?(\s+implements\s+)?(.*)?(<.*>)?(\w*))
//(class\s+)(\w+)(<.*>)?(\s+extends\s+)(\w+)?