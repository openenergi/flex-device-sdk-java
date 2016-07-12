/*
 * Copyright (c) 2016 Open Energi. All rights reserved.
 *
 * MIT License Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the ""Software""),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.openenergi.flex.persistence;


import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Persists messages in memory in an ordered hashmap.
 */
public class MemoryPersister implements Persister {
    private static AtomicLong tokenCounter = new AtomicLong();
    private Integer size;
    public AtomicLong counter = new AtomicLong(0L); //total number of objects ever stored
    private ConcurrentSkipListSet<TokenizedObject> list;

    public Long put(Object data, Long priority, Boolean acquireLock) throws PersisterFullException {
        //warning: size() is not constant time with this data structure
        if (this.list.size() >= this.size){
            if (priority > this.list.first().priority) {
                this.truncate();
            } else {
                throw new PersisterFullException("Buffer full"); //TODO(mbironneau): Log this instead of silently dropping it
            }
        }
        this.counter.incrementAndGet();
        Long token = MemoryPersister.tokenCounter.getAndIncrement();
        this.list.add(new TokenizedObject(token, data, priority, acquireLock));
        return token;
    }

    /**
     * Truncates the list so that it has one free slot.
     */
    private void truncate() {
        while (this.list.size() >= this.size) {
            this.list.remove(this.list.first());
        }
    }

    /**
     * Returns the number of items currently persisted.
     * @return
     */
    public Integer size() {
        return this.list.size();
    }


    public TokenizedObject peekLock()  throws NoSuchElementException{
        Iterator<TokenizedObject> it = this.list.descendingIterator();
        while (it.hasNext()){
            TokenizedObject to = it.next();
            if (to.tryAcquire()){
                return to;
            }
        }
        //if we got this far then there are no eligible objects. for consistency
        //we should throw.
        throw new NoSuchElementException();
    }

    @Override
    public TokenizedObject getByToken(Long token) throws NoSuchElementException {
        Iterator<TokenizedObject> it = this.list.descendingIterator();
        while (it.hasNext()){
            TokenizedObject to = it.next();
            if (to.token == token){
                return to;
            }
        }
        //if we got this far then there are no eligible objects. for consistency
        //we should throw.
        throw new NoSuchElementException();
    }


    public void delete(Long token) {
        //This method looks like it is O(N). However, peek() returns elements from the
        //head of the list, so by iterating in descending order we are actually quite
        //likely to hit the desired element without iterating through more than a few
        //items.

        Iterator<TokenizedObject> it = this.list.descendingIterator();

        it.forEachRemaining((TokenizedObject to) -> {
            if (to.token == token) {
                this.list.remove(to);
                return;
            }
        });
    }

    @Override
    public void release(Long token) {
        //This method looks like it is O(N). However, peek() returns elements from the
        //head of the list, so by iterating in descending order we are actually quite
        //likely to hit the desired element without iterating through more than a few
        //items.

        Iterator<TokenizedObject> it = this.list.descendingIterator();

        it.forEachRemaining((TokenizedObject to) -> {
            if (to.token == token) {
                to.release();
                return;
            }
        });
    }

    /**
     * Construct a new in-memory persister with maximum capacity.
     * @param size Maximum capacity (in number of messages).
     * @throws IllegalArgumentException Thrown if size is zero.
     */
    public MemoryPersister(Integer size) throws IllegalArgumentException{
        if (size == 0) throw new IllegalArgumentException("Size should be at least 1");
        this.list = new ConcurrentSkipListSet<>(new Comparator<TokenizedObject>() {
            public int compare(TokenizedObject o1, TokenizedObject o2) {
                return Long.compare(o1.priority, o2.priority);
            }
        });
        this.size = size;
    }

}
