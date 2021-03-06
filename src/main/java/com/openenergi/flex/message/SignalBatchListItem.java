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

package com.openenergi.flex.message;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;

/**
 * This class represents a signal point that is part of an oe-vars batch.
 */
public final class SignalBatchListItem {

    @JsonProperty("variable")
    String subtype;
    Double value;
    public Double getValue() {
        return this.value;
    }
    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype){
        this.subtype = subtype;
    }

    public void setValue(Double value){
        this.value = value;
    }

    public SignalBatchListItem(){}

    public SignalBatchListItem(String subtype, Double value){
        this.subtype = subtype;
        this.value = value;
    }

}
