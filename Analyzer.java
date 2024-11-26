import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Analyzer {
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "SumByItem");
        job.setJarByClass(Analyzer.class);
        job.setMapperClass(SumMapper.class);
        job.setCombinerClass(SumReducer.class);
        job.setReducerClass(SumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

    public static class SumMapper extends Mapper<Object, Text, Text, IntWritable> {

        private Text itemName = new Text();
        private IntWritable calculatedValue = new IntWritable();

        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {
            String line = value.toString();
            String[] fields = line.split(",");

            // Skip the header
            if (fields[0].equals("Retailer")) {
                return;
            }

            try {
                String productName = fields[6].trim(); // Product name
                double pricePerUnit = Double.parseDouble(fields[7].trim());
                double unitsSold = Double.parseDouble(fields[8].trim());

                int calculated = (int) Math.round(pricePerUnit * unitsSold);
                itemName.set(productName);
                calculatedValue.set(calculated);
                context.write(itemName, calculatedValue);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                // Log and ignore malformed records
            }
        }
    }

    public static class SumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }

            result.set(sum);
            context.write(key, result);
        }
    }
}

