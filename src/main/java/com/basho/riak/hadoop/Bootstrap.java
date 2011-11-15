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

import static java.lang.System.out;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.basho.riak.client.IRiakClient;
import com.basho.riak.client.RiakException;
import com.basho.riak.client.RiakFactory;
import com.basho.riak.client.bucket.Bucket;
import com.basho.riak.client.cap.UnresolvedConflictException;
import com.basho.riak.client.convert.ConversionException;
import com.basho.riak.client.raw.Transport;
import com.basho.riak.client.raw.config.Configuration;
import com.basho.riak.client.raw.http.HTTPClientConfig;
import com.basho.riak.client.raw.pbc.PBClientConfig;

/**
 * This class loads the data Huck Finn novel into your Riak cluster, a key/value
 * per object. Keyed on chapter name. After you've run this, you can run the
 * {@link RiakWordCount} example.
 * <p>
 * You can call it from the command line, like
 * </p>
 * <p><code>
 * mvn exec:java -Dexec.mainClass="com.basho.riak.hadoop.Bootstrap"
 * -Dexec.classpathScope=runtime -Dexec.args="[pb|http PB_HOST|RIAK_URL]"
 * </code></p>
 * <p><code>
 * e.g. mvn exec:java -Dexec.mainClass="com.basho.riak.hadoop.Bootstrap"
 * -Dexec.classpath Scope=runtime
 * </code></p>
 * <p><code>
 * mvn exec:java -Dexec.mainClass="com.basho.riak.hadoop.Bootstrap"
 * -Dexec.classpath Scope=runtime -Dexec.args="pb 127.0.0.1:8087"
 * </code></p>
 * <p><code>
 * mvn exec:java -Dexec.mainClass="com.basho.riak.hadoop.Bootstrap"
 * -Dexec.classpath Scope=runtime -Dexec.args="http http://127.0.0.1:8098/riak"
 * </code></p>
 * If you don't specify a transport/host/url then it will use the pb interface
 * on 127.0.0.1:8081
 * 
 * Or just call it with plain old <code>java -cp <b>*ALL THE JARS*</b></code>
 * com.basho.riak.hadoop.Bootstrap
 * 
 * @author russell
 * 
 */
public class Bootstrap {
    private static final String AUTHOR = "Mark Twain";
    private static final String BOOK = "Adventures of Huckleberry Finn";
    private static final String BUCKET = "wordcount";
    private static final String HUCK_FIN = "huck_fin.txt";
    private static final int PORT = 8081;
    private static final Charset CHARSET = Charset.forName("UTF8");
    private static final CharsetDecoder DECODER = CHARSET.newDecoder();
    private static final Pattern START_PATTERN = Pattern.compile("\\*\\*\\* START.*\\*\\*\\*");
    private static final Pattern CHAPTER_PATTERN = Pattern.compile("CHAPTER\\s+([IVXLCDMTHEAST\\p{Blank}]+)[\\.|\r|\n]");
    private static final Pattern END_PATTERN = Pattern.compile("\\*\\*\\* END");

    private final Bucket bucket;

    /**
     * @param conf
     * @throws RiakException
     */
    private Bootstrap(Configuration conf) throws IOException, RiakException {
        IRiakClient client = RiakFactory.newClient(conf);
        bucket = client.fetchBucket(BUCKET).execute();
    }

    public static final void main(String[] args) {
        Configuration conf = null;
        if (args.length == 0) {
            conf = new PBClientConfig.Builder().withPort(PORT).build();
        } else if (args.length != 2) {
            printUsage();
        } else {
            try {
                Transport t = Transport.valueOf(args[0].toUpperCase());
                switch (t) {
                case PB:
                    String[] hp = args[1].split(":");
                    conf = new PBClientConfig.Builder().withHost(hp[0]).withPort(Integer.parseInt(hp[1])).build();
                    break;
                case HTTP:
                    conf = new HTTPClientConfig.Builder().withUrl(args[1]).build();
                    break;
                default:
                    throw new IllegalArgumentException(t.toString());
                }

            } catch (IllegalArgumentException e) {
                System.err.println("Unknown transport option '" + args[0] + "'");
                printUsage();
                System.exit(1);
            }
        }
        try {
            Bootstrap bootstrap = new Bootstrap(conf);
            bootstrap.loadData();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    /**
     * reads the huck_fin.txt file, splits it, loads the splits
     */
    private void loadData() throws IOException, RiakException {
        FileInputStream fis = new FileInputStream(HUCK_FIN);
        FileChannel channel = fis.getChannel();

        int fileSize = (int) channel.size();
        MappedByteBuffer byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);
        CharBuffer charBuffer = DECODER.decode(byteBuffer);

        Matcher chapterMatcher = CHAPTER_PATTERN.matcher(charBuffer);
        Matcher startMatcher = START_PATTERN.matcher(charBuffer);
        Matcher endMatcher = END_PATTERN.matcher(charBuffer);

        boolean match = startMatcher.find() && chapterMatcher.find();
        int from = startMatcher.end();
        endMatcher.find();
        int end = endMatcher.start();

        while (match) {
            String key = chapterMatcher.group(1).replaceAll("\\s", "");
            match = chapterMatcher.find();
            int to;

            if (!match) {
                to = end;
            } else {
                to = chapterMatcher.start();
            }
            store(key, charBuffer.subSequence(from, to).toString());
            from = to;
        }
    }

    /**
     * @param key
     * @param value
     * @throws IOException
     * @throws ConversionException
     * @throws UnresolvedConflictException
     * @throws KRiakRetryFailedException
     */
    private void store(String key, String value) throws IOException, RiakException {
        Chapter c = new Chapter(AUTHOR, BOOK, key, value);
        bucket.store(key, c).execute();
    }

    /**
     * Display usage message
     */
    private static void printUsage() {
        StringBuilder b = new StringBuilder("Usage: ");
        b.append("mvn exec:java -Dexec.mainClass=\"com.basho.riak.hadoop.Bootstrap\" -Dexec.classpathScope=runtime -Dexec.args=\"[pb|http pb_host:port|riak_url]\"");
        out.println(b);
    }
}
