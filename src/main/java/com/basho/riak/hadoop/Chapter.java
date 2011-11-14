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

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import com.basho.riak.client.convert.RiakIndex;

/**
 * Value type for the Huck Finn word count
 * 
 * @author russell
 */
@JsonIgnoreProperties(ignoreUnknown = true) public class Chapter {
    @RiakIndex(name="author")
    private final String author;
    private final String book;
    private final String chapter;
    private final String text;

    @JsonCreator public Chapter(@JsonProperty("author") String author, @JsonProperty("book") String book,
            @JsonProperty("chapter") String chapter, @JsonProperty("text") String text) {
        this.author = author;
        this.book = book;
        this.chapter = chapter;
        this.text = text;
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @return the book
     */
    public String getBook() {
        return book;
    }

    /**
     * @return the chapter
     */
    public String getChapter() {
        return chapter;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((author == null) ? 0 : author.hashCode());
        result = prime * result + ((book == null) ? 0 : book.hashCode());
        result = prime * result + ((chapter == null) ? 0 : chapter.hashCode());
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Chapter)) {
            return false;
        }
        Chapter other = (Chapter) obj;
        if (author == null) {
            if (other.author != null) {
                return false;
            }
        } else if (!author.equals(other.author)) {
            return false;
        }
        if (book == null) {
            if (other.book != null) {
                return false;
            }
        } else if (!book.equals(other.book)) {
            return false;
        }
        if (chapter == null) {
            if (other.chapter != null) {
                return false;
            }
        } else if (!chapter.equals(other.chapter)) {
            return false;
        }
        if (text == null) {
            if (other.text != null) {
                return false;
            }
        } else if (!text.equals(other.text)) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override public String toString() {
        return String.format("Chapter [author=%s, book=%s, chapter=%s, text=%s]", author, book, chapter, text);
    }
}
