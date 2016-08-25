package com.openenergi.flex.persistence;

import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class MemoryPersisterTest {


    @Test
    public void testPut() {
       MemoryPersister mp = new MemoryPersister(3);
        try {
            mp.put("asdf", 1L, false);
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
            mp.put("asdf", 1L, false);
            mp.put("bsdf", 2L, false);
            mp.put("csdf", 3L, false);
            mp.put("dsdf", 4L, false); //first item gets evicted
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
            mp.put("asdf", 4L, false);
            mp.put("bsdf", 3L, false);
            mp.put("csdf", 2L, false);
        } catch (PersisterFullException e) {
            e.printStackTrace();
        }


        try {
            mp.put("dsdf", 1L, false); //this item never gets inserted
        } catch (PersisterFullException expected) {
        }
        assertEquals(3L, (long) mp.size());
        assertEquals("asdf", mp.peekLock().data);
    }

    @Test
    public void testDelete(){
        MemoryPersister mp = new MemoryPersister(3);
        try {
            mp.put("asdf", 1L, false);
            mp.put("bsdf", 2L, false);
        } catch (PersisterFullException e) {
            fail("Should not throw");
        }

        TokenizedObject to = mp.peekLock();
        mp.delete(to.token);
        assertEquals(1L, (long) mp.size());
        assertEquals("asdf", mp.peekLock().data);
    }

    @Test
    public void testLocking(){
        MemoryPersister mp = new MemoryPersister(3);
        Long token1 = -1L;
        Long token2 = -1L;
        try {
            token1 = mp.put("asdf", 1L, false);
            token2 = mp.put("bsdf", 2L, true);
        } catch (PersisterFullException e) {
            fail("Should not throw");
        }
        TokenizedObject to = mp.peekLock();
        assertEquals(to.data, "asdf");

        try {
            mp.peekLock();
        } catch (NoSuchElementException expected){
        }

        mp.release(token2);

        try {
            to = mp.peekLock();
            assertEquals(to.data, "bsdf");
        } catch (NoSuchElementException ex){
            fail("Should not throw");
        }


    }


}
