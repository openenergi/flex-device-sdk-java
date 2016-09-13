package com.openenergi.flex.device;

import java.io.IOException;

import org.junit.Test;

import com.openenergi.flex.message.Event;
import com.openenergi.flex.message.MessageContext;

import static org.junit.Assert.assertEquals;


public class DeviceTest {
	

	@Test
	public void testConnect() {
		BasicClient client = new BasicClient("oeiot.azure-devices.net", "d1000000", "EM/3FdzxAxxExOktcF311DNcm8pCWHpLLrHZpmTx+p4=");
		try {
			client.connect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testSendMessage() {
		BasicClient client = new BasicClient("oeiot.azure-devices.net", "d1000000", "EM/3FdzxAxxExOktcF311DNcm8pCWHpLLrHZpmTx+p4=");
		try {
			client.connect();
			MessageContext ctx = new MessageContext();
			ctx.setData(1L);
			Event e = new Event.Builder()
			.withValue("testing")
			.withLevel(Event.Level.DEBUG)
			.withEntity("l1")
			.withCustomType("test").build();
			client.onPublish((MessageContext pub) ->  assertEquals(pub.getData(), 1L));
			client.publish(e, ctx);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
}
