package com.openenergi.flex.message;

import java.util.List;

/**
 * The parameter passed to callbacks for the scheduler. This is the information that implementors should need to act on
 * Signal messages.
 */
public final class SignalCallbackItem {
    private String entity;
    private String type;
    private Double value;
    public static final String END_OF_SIGNAL = "END_OF_SIGNAL";

    public SignalCallbackItem(String entity, String type, Double value){
        this.entity = entity;
        this.type = type;
        this.value = value;
    }

    /**
     * Get the entities targeted by the signal.
     * @return The list of entities
     */
    public String getEntity() {
        return entity;
    }

    /**
     * Get the type of the Signal (eg. "oe-add")
     * @return The type of the signal
     */
    public String getType() {
        return type;
    }


    /**
     * Get the value of the variable/type that should be set (eg. set "oe-add" to 0.5)
     * @return The value
     */
    public Double getValue() {
        return value;
    }

}
