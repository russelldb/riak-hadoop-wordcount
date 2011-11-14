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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.basho.riak.client.cap.DefaultResolver;
import com.basho.riak.client.convert.JSONConverter;
import com.basho.riak.client.query.indexes.BinIndex;
import com.basho.riak.client.raw.query.indexes.BinValueQuery;
import com.basho.riak.hadoop.config.RiakConfig;
import com.basho.riak.hadoop.config.RiakPBLocation;
import com.basho.riak.hadoop.keylisters.SecondaryIndexesKeyLister;

public class RiakWordCount extends Configured implements Tool {

    /**
     * The map class of WordCount.
     */
    public static class TokenCounterMapper extends RiakMapper<Chapter, Text, IntWritable> {

        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

        public TokenCounterMapper() {
            // set up with Converter/Resolver instances
            super(new JSONConverter<Chapter>(Chapter.class, ""), new DefaultResolver<Chapter>());
        }

        public void map(BucketKey key, Chapter value, Context context) throws IOException, InterruptedException {
            String[] splits = new String[] {};
            if (value != null && value.getText() != null) {
                splits = value.getText().split("[\\s\\.,\\?!;:\"]");
            }

            for (String s : splits) {
                word.set(s.trim().toLowerCase());
                context.write(word, one);
            }
        }
    }

    /**
     * The reducer class of WordCount
     */
    public static class TokenCounterReducer extends Reducer<Text, IntWritable, Text, WordCountResult> {
        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException,
                InterruptedException {
            int sum = 0;

            for (IntWritable value : values) {
                sum += value.get();
            }

            String riakKey = key.toString();

            if (sum > 1 && !"".equals(riakKey)) { // drop any words that only
                                                  // show up once
                context.write(key, new WordCountResult(riakKey, sum));
            }
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
        String[] keys = new String[10000];

        for (int i = 0; i < 10000; i++) {
            keys[i] = String.valueOf(i + 1000);
        }
        Configuration conf = getConf();
        conf = RiakConfig.setKeyLister(conf,
                                       new SecondaryIndexesKeyLister(new BinValueQuery(BinIndex.named("author"),
                                                                                       "wordcount", "Mark Twain")));
        conf = RiakConfig.addLocation(conf, new RiakPBLocation("33.33.33.10", 8087));
        conf = RiakConfig.addLocation(conf, new RiakPBLocation("33.33.33.11", 8087));
        conf = RiakConfig.addLocation(conf, new RiakPBLocation("33.33.33.12", 8087));
        conf = RiakConfig.addLocation(conf, new RiakPBLocation("33.33.33.13", 8087));
        conf = RiakConfig.setOutputBucket(conf, "wordcount_out");
        conf = RiakConfig.setHadoopClusterSize(conf, 4);

        Job job = new Job(conf, "Riak-WordCount");

        job.setJarByClass(RiakWordCount.class);

        job.setInputFormatClass(RiakInputFormat.class);
        job.setMapperClass(TokenCounterMapper.class);

        job.setReducerClass(TokenCounterReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setOutputFormatClass(RiakOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(WordCountResult.class);

        job.setNumReduceTasks(4);

        job.submit();
        return job.waitForCompletion(true) ? 0 : 1;
    }
}
