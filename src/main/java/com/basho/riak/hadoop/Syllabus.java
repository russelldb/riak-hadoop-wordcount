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

/**
 * Value type for the syllabus word count
 * 
 * @author russell
 */
@JsonIgnoreProperties(ignoreUnknown = true) public class Syllabus {

    private final String title;
    private final String id;
    private final String url;
    private final String dateAdded;
    private final String googleSnippet;

    /**
     * Create immutable instance
     * 
     * @param title
     * @param id
     * @param url
     * @param dateAdded
     * @param googleSnippet
     */
    @JsonCreator public Syllabus(@JsonProperty("title") String title, @JsonProperty("syllabi_id") String id,
            @JsonProperty("url") String url, @JsonProperty("date_added") String dateAdded,
            @JsonProperty("google_snippet") String googleSnippet) {
        this.title = title;
        this.id = id;
        this.url = url;
        this.dateAdded = dateAdded;
        this.googleSnippet = googleSnippet;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return the dateAdded
     */
    public String getDateAdded() {
        return dateAdded;
    }

    /**
     * @return the googleSnippet
     */
    public String getGoogleSnippet() {
        return googleSnippet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dateAdded == null) ? 0 : dateAdded.hashCode());
        result = prime * result + ((googleSnippet == null) ? 0 : googleSnippet.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        result = prime * result + ((url == null) ? 0 : url.hashCode());
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
        if (!(obj instanceof Syllabus)) {
            return false;
        }
        Syllabus other = (Syllabus) obj;
        if (dateAdded == null) {
            if (other.dateAdded != null) {
                return false;
            }
        } else if (!dateAdded.equals(other.dateAdded)) {
            return false;
        }
        if (googleSnippet == null) {
            if (other.googleSnippet != null) {
                return false;
            }
        } else if (!googleSnippet.equals(other.googleSnippet)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (title == null) {
            if (other.title != null) {
                return false;
            }
        } else if (!title.equals(other.title)) {
            return false;
        }
        if (url == null) {
            if (other.url != null) {
                return false;
            }
        } else if (!url.equals(other.url)) {
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
        return String.format("Syllabus [title=%s, id=%s, url=%s, dateAdded=%s, googleSnippet=%s]", title, id, url,
                             dateAdded, googleSnippet);
    }

}
