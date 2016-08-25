package com.openenergi.flex.device;

import com.openenergi.flex.message.Reading;
import org.junit.Test;

import static org.junit.Assert.assertTrue;


public class FFRPrioritizerTest {


    @Test
    public void testScoreTimestamp(){
        Reading e1 = (Reading) new Reading()
                .setValue(1.23)
                .setEntity("l1")
                .setTimestamp(12345L)
                .setType("something");
        Reading e2 = (Reading) new Reading()
                .setValue(1.23)
                .setEntity("l1")
                .setTimestamp(12346L)
                .setType("something");

        FFRPrioritizer p = new FFRPrioritizer();

        assertTrue(p.score(e2) > p.score(e1));
    }

    @Test
    public void testScoreFFR(){
        Reading e1 = (Reading) new Reading()
                .setValue(1.23)
                .setEntity("l1")
                .setType(Reading.Type.POWER.getValue())
                .setTimestamp(12345L);


        Reading e2 = (Reading) new Reading()
                .setValue(1.23)
                .setEntity("l1")
                .setTimestamp(12346L)
                .setType("something");

        FFRPrioritizer p = new FFRPrioritizer();

        assertTrue(p.score(e1) > p.score(e2));
    }

}
