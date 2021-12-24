package spark;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import scala.Tuple2;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        //настройка имени и url
        SparkConf conf = new SparkConf().setAppName("SearchBot").setMaster("local[*]");

        //создание контекста
        JavaSparkContext ctx = new JavaSparkContext(conf);

        //возвращает путь и содержимое файла
        JavaPairRDD<String, String> files = ctx.wholeTextFiles(args[0]);

        //берем только значения
        JavaRDD<String> classTextValue = files.values();

        //применим функцию map ко всем элементам, а затем сгладив результаты.
        JavaPairRDD<String, String> mappingClasses = classTextValue.flatMapToPair((PairFlatMapFunction<String, String, String>) str -> map(str).iterator());

        //reducer
        reduce(mappingClasses);

        //останавливаем контекст
        ctx.close();
    }

    private static List<Tuple2<String, String>> map(String str) {
        Pattern pattern = Pattern.compile("(class|interface)(\\s+)(\\w+)(<.*>)?(\\s+extends(\\s+\\w+(,\\s+\\w+)*))?((\\s+implements)(\\s+\\w+(,\\s+\\w+)*))*");
        Matcher matcher = pattern.matcher(str);

        List<Tuple2<String, String>> result = new ArrayList<>();

        while (matcher.find()) {
            String className = matcher.group(3);
            List<String> list = new ArrayList<>();
            if (matcher.group(6) != null) {
                list.addAll(Arrays.asList(matcher.group(6).split(",")));
            }
            if (matcher.group(10) != null) {
                list.addAll(Arrays.asList(matcher.group(10).split(",")));
            }
            for (String item : list) {
                result.add(new Tuple2<>(item, className));
            }
        }
        return result;
    }

    private static void reduce(JavaPairRDD<String, String> mappingClasses) {
        //выполним свертку по ключам
        JavaPairRDD<String, String> reduceResult = mappingClasses.reduceByKey((Function2<String, String, String>) (c1, c2) -> c1 + ", " + c2);

        for (Tuple2<String, String> item : reduceResult.collect()) {
            System.out.println(item._1 + " : [" + item._2 + "]");
        }
    }
}
