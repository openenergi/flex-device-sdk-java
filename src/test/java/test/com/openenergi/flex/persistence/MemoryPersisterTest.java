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
        try {
            mp.put("asdf", 1L);
        } catch (PersisterFullException e) {
            fail("Should not throw");
        }
        assertEquals(1L, (long) mp.size());
        assertEquals("asdf", mp.peekLock().data);
    }

    @Test
    public void testCap(){
        MemoryPersister mp = new MemoryPersister(3);
        try {
            mp.put("asdf", 1L);
            mp.put("bsdf", 2L);
            mp.put("csdf", 3L);
            mp.put("dsdf", 4L); //first item gets evicted
        } catch (PersisterFullException e) {
            fail("Should not throw");
        }

        assertEquals(3L, (long) mp.size());
        assertEquals("dsdf", mp.peekLock().data);
    }

    @Test
    public void testCap2(){
        MemoryPersister mp = new MemoryPersister(3);
        try {
            mp.put("asdf", 4L);
            mp.put("bsdf", 3L);
            mp.put("csdf", 2L);
        } catch (PersisterFullException e) {
            e.printStackTrace();
        }


        try {
            mp.put("dsdf", 1L); //this item never gets inserted
        } catch (PersisterFullException expected) {
        }
        assertEquals(3L, (long) mp.size());
        assertEquals("asdf", mp.peekLock().data);
    }

    @Test
    public void testDelete(){
        MemoryPersister mp = new MemoryPersister(3);
        try {
            mp.put("asdf", 1L);
            mp.put("bsdf", 2L);
        } catch (PersisterFullException e) {
            fail("Should not throw");
        }

        TokenizedObject to = mp.peekLock();
        mp.delete(to.token);
        assertEquals(1L, (long) mp.size());
        assertEquals("asdf", mp.peekLock().data);
    }

}
