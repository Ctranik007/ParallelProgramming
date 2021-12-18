package hadoop;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.exit(-1);
        }
        FileUtils.deleteDirectory(new File("output"));

        Job job = Job.getInstance();
        job.setJarByClass(Main.class);
        job.setJobName("Hadoop");
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        job.setMapperClass(SearchBotMapper.class);
        job.setReducerClass(SearchBotReducer.class);
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
