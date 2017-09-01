package com.openenergi.flex.message;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Bean encapsulating an element of a Signal<T>, regardless of what T is.
 */
public final class SignalElement
{
    private ZonedDateTime startAt;
    private List<SignalBatchListItem> values;

    public SignalElement(ZonedDateTime startAt, List<SignalBatchListItem> values)
    {
        this.startAt = startAt;
        this.values = values;
    }

    public ZonedDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(ZonedDateTime startAt) {
        this.startAt = startAt;
    }

    public List<SignalBatchListItem> getValues() {
        return values;
    }

    public void setValues(List<SignalBatchListItem> values) {
        this.values = values;
    }
}
