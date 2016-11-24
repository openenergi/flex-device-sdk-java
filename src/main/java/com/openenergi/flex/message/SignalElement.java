package com.openenergi.flex.message;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Bean encapsulating an element of a Signal<T>, regardless of what T is.
 */
public class SignalElement {
    private ZonedDateTime start;
    private List<SignalBatchListItem> values;

    public SignalElement(){}

    public SignalElement(ZonedDateTime start, List<SignalBatchListItem> values){
        this.start = start;
        this.values = values;
    }

    public ZonedDateTime getStart() {
        return start;
    }

    public void setStart(ZonedDateTime start) {
        this.start = start;
    }

    public List<SignalBatchListItem> getValues() {
        return values;
    }

    public void setValues(List<SignalBatchListItem> values) {
        this.values = values;
    }
}
