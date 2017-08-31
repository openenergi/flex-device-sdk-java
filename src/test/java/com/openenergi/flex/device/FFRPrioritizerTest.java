package com.openenergi.flex.device;

import com.openenergi.flex.message.Reading;
import org.junit.Test;

import static org.junit.Assert.assertTrue;


public class FFRPrioritizerTest {


    @Test
    public void testScoreTimestamp(){
        Reading e1 = new Reading.Builder()
                .withValue(1.23)
                .withEntity("l1")
                .atTime(12345L)
                .withCustomType("something").build();
        Reading e2 = new Reading.Builder()
                .withValue(1.23)
                .withEntity("l1")
                .atTime(12346L)
                .withCustomType("something").build();

        FFRPrioritizer p = new FFRPrioritizer();

        assertTrue(p.score(e2) > p.score(e1));
    }

    @Test
    public void testScoreFFR(){
        Reading e1 = new Reading.Builder()
                .withValue(1.23)
                .withEntity("l1")
                .withType(Reading.Type.POWER)
                .atTime(12345L).build();


        Reading e2 = new Reading.Builder()
                .withValue(1.23)
                .withEntity("l1")
                .atTime(12346L)
                .withCustomType("something").build();

        FFRPrioritizer p = new FFRPrioritizer();

        assertTrue(p.score(e1) > p.score(e2));
    }

}
