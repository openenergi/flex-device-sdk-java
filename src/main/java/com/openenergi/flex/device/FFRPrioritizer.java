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

package com.openenergi.flex.device;

import com.openenergi.flex.message.Event;
import com.openenergi.flex.message.Message;
import com.openenergi.flex.message.Reading;


/**
 * This prioritizes messages by their topic, type and timestamp.
 * Each message is initially given a priority equal to their timestamp, so that
 * newer messages get pushed first.
 *
 * FFR-related readings and events get a multiplier of 2. DEBUG and INFO level events
 * unrelated to FFR get a multiplier of 0.5.
 */
public class FFRPrioritizer implements Prioritizer {


    /**
     * Returns the priority score of the message - higher is better.
     * @param msg the Message
     * @return the Score
     */
    public Long score(Message msg) {

        switch (msg.getTopic()){
            case "readings":
                if (msg.getType() == Reading.Type.AVAILABILITY_FFR_HIGH.getValue()||
                        msg.getType() == Reading.Type.AVAILABILITY_FFR_LOW.toString()
                 || msg.getType() == Reading.Type.POWER.getValue()
                        ||msg.getType() == Reading.Type.RESPONSE_FFR_HIGH.getValue()
                || msg.getType() == Reading.Type.AVAILABILITY_FFR_LOW.getValue()){
                    return msg.getTimestamp()*2;
                } else {
                    return msg.getTimestamp();
                }
            case "events":
                if (msg.getType() == Event.Type.FFR_SWITCH_END.getValue()
                        || msg.getType() == Event.Type.FFR_SWITCH_START.getValue()) {
                    return msg.getTimestamp()*2;
                } else {
                    Event e = (Event) msg;
                    switch (e.getLevel()) {
                        case DEBUG:
                        case INFO:
                            return (Long) (msg.getTimestamp()/2);
                        default:
                            return msg.getTimestamp();
                    }
                }
            default:
                return msg.getTimestamp();
        }
    }


}
