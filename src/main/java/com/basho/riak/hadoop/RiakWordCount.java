/*
 * This file is provided to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.basho.riak.hadoop;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.basho.riak.client.IRiakObject;
import com.basho.riak.client.cap.DefaultResolver;
import com.basho.riak.client.cap.VClock;
import com.basho.riak.client.convert.ConversionException;
import com.basho.riak.client.convert.Converter;

public class RiakWordCount extends Configured implements Tool {

    /**
     * The map class of WordCount.
     */
    public static class TokenCounterMapper extends RiakMapper<String, Text, IntWritable> {

        /**
         * @param converter
         * @param resolver
         */
        public TokenCounterMapper() {
            super(new Converter<String>() {
                public IRiakObject fromDomain(String string, VClock vclock) throws ConversionException {
                    // NO -OP
                    return null;
                }

                public String toDomain(IRiakObject riakObject) throws ConversionException {
                    return riakObject.getValueAsString();
                }
            }, new DefaultResolver<String>());
        }

        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

        public void map(BucketKey key, String value, Context context) throws IOException, InterruptedException {
            final StringTokenizer tokenizer = new StringTokenizer(value);
            while (tokenizer.hasMoreTokens()) {
                word.set(tokenizer.nextToken());
                context.write(word, one);
            }
        }
    }

    /**
     * The reducer class of WordCount
     */
    public static class TokenCounterReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException,
                InterruptedException {
            int sum = 0;
            for (IntWritable value : values) {
                sum += value.get();
            }
            context.write(key, new IntWritable(sum));
        }
    }

    /**
     * The main entry point.
     */
    public static void main(String[] args) throws Exception {
        ToolRunner.run(new Configuration(), new RiakWordCount(), args);
        System.exit(0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.hadoop.util.Tool#run(java.lang.String[])
     */
    public int run(String[] args) throws Exception {
        Configuration conf = getConf();
        conf = RiakConfig.setBucket(conf, "wordCount");
        conf = RiakConfig.addLocation(conf, new RiakPBLocation("127.0.0.1", 8087));
        conf = RiakConfig.setHadoopClusterSize(conf, 1);
        Job job = new Job(conf, "WordCount");
        job.setJarByClass(RiakWordCount.class);
        job.setMapperClass(TokenCounterMapper.class);
        job.setReducerClass(TokenCounterReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        job.setInputFormatClass(RiakInputFormat.class);
        FileOutputFormat.setOutputPath(job, new Path("/tmp/word_count"));
        job.submit();
        return job.waitForCompletion(true) ? 0 : 1;
    }
}
