/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

public class MetadataRest {

    @JsonAnySetter
    private SortedMap<String, List<MetadataValueRest>> map = new TreeMap();

    @JsonAnyGetter
    public SortedMap<String, List<MetadataValueRest>> getMap() {
        return map;
    }

    public void put(String key, MetadataValueRest... values) {
        map.put(key, Arrays.asList(values));
    }
}