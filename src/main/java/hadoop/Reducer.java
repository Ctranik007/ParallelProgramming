package hadoop;

import org.apache.hadoop.io.Text;
import java.io.IOException;

public class Reducer extends org.apache.hadoop.mapreduce.Reducer<Text, Text, Text, Text> {
    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) {
        try {
            StringBuilder result = new StringBuilder();
            for (Text value : values) {
                result.append(value.toString()).append(", ");
            }
            context.write(key, new Text("is base to [ " + result + " ]"));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
