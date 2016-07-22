package org.guavaberry.collections;

import org.guavaberry.collections.DefaultHashMap;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class DefaultHashMapTest {
    private final DefaultHashMap<String, String> map = new DefaultHashMap<>(() -> new String("default"));

    @Before
    public void setUp() throws Exception {
        map.put("world", "not-default");
    }

    @Test(expected = NullPointerException.class)
    public void testNullOperator() {
        new DefaultHashMap<String, String>(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullOperator2() {
        new DefaultHashMap<String, String>(null, 0, .7f);
    }

    @Test(expected = NullPointerException.class)
    public void testNullOperator3() {
        new DefaultHashMap<String, String>(null, 0);
    }

    @Test(expected = NullPointerException.class)
    public void testNullOperator4() {
        new DefaultHashMap<String, String>(null, new HashMap<>());
    }

    @Test
    public void testConstructors() {
        new DefaultHashMap<String, String>(()->"", 0, 0.7f);
        new DefaultHashMap<String, String>(()->"", 0);
        new DefaultHashMap<String, String>(()->"");
        new DefaultHashMap<String, String>(()->"", new HashMap<>());
    }

    @Test
    public void testGet() throws Exception {
        assertEquals("default", map.get("hello"));
    }

    @Test
    public void testGetEqualValues() throws Exception {
        assertEquals(map.get("hello1"), map.get("hello"));
    }

    @Test
    public void testGetOnNotSameValues() throws Exception {
        assertNotSame(map.get("hello1"), map.get("hello"));
    }

    @Test
    public void testGetOnSameKeys() throws Exception {
        assertSame(map.get("hello"), map.get("hello"));
    }

    @Test
    public void testGetOnExistingValue() throws Exception {
        assertEquals("not-default", map.get("world"));
    }

    @Test
    public void testGetOrDefaultOnExistingValue() throws Exception {
        assertEquals("not-default", map.getOrDefault("world", "new-default"));
    }

    @Test
    public void testGetOrDefaultOnNewValue() throws Exception {
        assertEquals("new-default", map.getOrDefault("!", "new-default"));
    }
}