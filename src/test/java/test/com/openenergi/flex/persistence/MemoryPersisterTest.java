package com.openenergi.flex.persistence;

import static org.junit.Assert.*;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.google.gson.JsonSyntaxException;


public class MemoryPersisterTest {


    @Test
    public void testPut() {
       MemoryPersister mp = new MemoryPersister(3);
        mp.put("asdf", 1L);
        assertEquals(1L, (long) mp.size());
        assertEquals("asdf", mp.peek().data);
    }

    @Test
    public void testCap(){
        MemoryPersister mp = new MemoryPersister(3);
        mp.put("asdf", 1L);
        mp.put("bsdf", 2L);
        mp.put("csdf", 3L);
        mp.put("dsdf", 4L); //first item gets evicted
        assertEquals(3L, (long) mp.size());
        assertEquals("dsdf", mp.peek().data);
    }

    @Test
    public void testCap2(){
        MemoryPersister mp = new MemoryPersister(3);
        mp.put("asdf", 4L);
        mp.put("bsdf", 3L);
        mp.put("csdf", 2L);
        mp.put("dsdf", 1L); //this item never gets inserted
        assertEquals(3L, (long) mp.size());
        assertEquals("asdf", mp.peek().data);
    }

    @Test
    public void testDelete(){
        MemoryPersister mp = new MemoryPersister(3);
        mp.put("asdf", 1L);
        mp.put("bsdf", 2L);
        TokenizedObject to = mp.peek();
        mp.delete(to.token);
        assertEquals(1L, (long) mp.size());
        assertEquals("asdf", mp.peek().data);
    }

}
