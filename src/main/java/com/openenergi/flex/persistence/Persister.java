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

import java.util.NoSuchElementException;

/**
 * An interface to a persistent data store for message tuples.
 */
public interface Persister {
    /**
     * Stores the object.
     * @param data The object to store
     * @param priority A number representing the priority of the entry for future eviction (the higher the more important)
     * @return The token of the object (can be used to delete it if the request is successful)
     */
    Long put(Object data, Long priority, Boolean acquireLock) throws PersisterFullException;

    /**
     * Returns the object in the store with the highest priority and locks it.
     */
    TokenizedObject peekLock() throws NoSuchElementException;

    TokenizedObject getByToken(Long token) throws NoSuchElementException;

    /**
     * Deletes the object with the given token.
     * @param token The token of the object
     */
    void delete(Long token);

    /**
     * Releases the object held at token for processing by other threads.
     * @param token The token of the object
     */
    void release(Long token);
}
