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

import java.util.concurrent.Semaphore;

/**
 * An object alongside a token that can later be used to delete it.
 */
public class TokenizedObject {
    Long token;
    Long priority;
    Object data;
    private Semaphore locker;

    public TokenizedObject(){}

    /**
     *
     * @param token Token by which to retrieve the object
     * @param data The data of the object
     * @param priority Priority of the data (higher is more)
     * @param locked Whether the object should be initialized in a locked state
     */
    public TokenizedObject(Long token, Object data, Long priority, Boolean locked){
        this.token = token;
        this.data = data;
        this.priority = priority;
        if (locked){
            this.locker = new Semaphore(0, true);
        } else {
            this.locker = new Semaphore(1, true);
        }
    }

    /**
     * Tries to lock the object from processing by other threads.
     */
    public Boolean tryAcquire(){
        return this.locker.tryAcquire();
    }


    /**
     * Releases the object for processing by other threads.
     */
    public void release(){
        this.locker.release();
    }
}
