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

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import junit.framework.TestCase;

/**
 * @author russell
 *
 */
public class SyllabusTest extends TestCase {

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Test method for {@link com.basho.riak.hadoop.Syllabus#Syllabus(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
     */
    @Test public void testSyllabus() throws Exception {
        // can OM map it?
        String json = "{\"chnm_cache\":\"N\",\"title\":\"Syllabus for BIO 312 - General Ecology\",\"google_cached_size\":\"11k\",\"syllabi_id\":\"92090\",\"url\":\"http://bio.winona.msus.edu/delong/Ecology/Syllabus.html\",\"appearances\":\"2\",\"clicked_on\":\"0\",\"ip_of_last_click\":\"N\",\"date_added\":  \"2003-02-19\",\"chnm_visited\":\"N\",\"google_snippet\":\"Syllabus for General Ecology (BIO 312). Dr. Michael D. Delong 215E Pasteur Hall Telephone: 457-5484 e-mail: mdelong@winona.edu. Office ...\"}";
        Syllabus syllabus = new ObjectMapper().readValue(json, Syllabus.class);
        
        System.out.println(syllabus);
    }

}
