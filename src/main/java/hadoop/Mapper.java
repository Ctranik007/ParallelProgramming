package hadoop;

import org.apache.hadoop.io.Text;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mapper extends org.apache.hadoop.mapreduce.Mapper<Object, Text, Text, Text> {

    @Override
    protected void map(Object key, Text value, Context context) {

        Pattern pattern = Pattern.compile("(class|interface)(\\s+)(\\w+)(<.*>)?(\\s+extends(\\s+\\w+(,\\s+\\w+)*))?((\\s+implements)(\\s+\\w+(,\\s+\\w+)*))*");
        Matcher matcher = pattern.matcher(value.toString());

        try {
            if (matcher.find()) {
                if (matcher.group(6) != null) {
                    for (String s : matcher.group(6).split(",")) {
                        context.write(new Text(s), new Text(matcher.group(3)));
                    }
                }
                if (matcher.group(10) != null) {
                    for (String s : matcher.group(10).split(",")) {
                        context.write(new Text(s), new Text(matcher.group(3)));
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
