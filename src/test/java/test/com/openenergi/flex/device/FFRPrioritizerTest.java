package com.openenergi.flex.device;

import static org.junit.Assert.*;

import com.openenergi.flex.message.Reading;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.google.gson.JsonSyntaxException;


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
                .setType(Reading.Type.POWER)
                .setValue(1.23)
                .setEntity("l1")
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
