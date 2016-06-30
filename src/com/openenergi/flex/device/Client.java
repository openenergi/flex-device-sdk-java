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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Formatter;
import java.util.Locale;
import java.util.function.Consumer;

import com.microsoft.azure.iothub.DeviceClient;
import com.microsoft.azure.iothub.IotHubClientProtocol;
import com.microsoft.azure.iothub.IotHubEventCallback;
import com.microsoft.azure.iothub.IotHubStatusCode;
import com.openenergi.flex.message.Message;
import com.openenergi.flex.message.MessageContext;
import com.openenergi.flex.message.Signal;
import com.openenergi.flex.message.SignalPointItem;
import com.openenergi.flex.message.SignalScheduleItem;

public class Client {
	private static String connStr = "HostName=%s;DeviceId=%s;SharedAccessKey=%s";
	
	/**
	 * This determines what protocol to use. We recommend AMQPS whenever possible or 
	 * MQTT in constrained environments.
	 * 
	 * HTTPS is currently supported only for device-to-cloud messages. Support for 
	 * cloud-to-device messaging using HTTPS is on the roadmap.
	 * 
	 * @author mbironneau
	 *
	 */
	public enum Protocol {
		AMQPS(IotHubClientProtocol.AMQPS), 
		MQTT(IotHubClientProtocol.MQTT), 
		HTTPS(IotHubClientProtocol.HTTPS);
		
		IotHubClientProtocol value;
		
		private Protocol(IotHubClientProtocol value){
			this.value = value;
		}
	
	};
	private Protocol protocol = Protocol.AMQPS;
	private DeviceClient client;
	private Consumer<MessageContext> onPublishCallback;
	private Consumer<Signal<SignalPointItem>> onSignalCallback;
	private Consumer<Signal<SignalScheduleItem>> onScheduleSignalCallback;
	private Boolean subscribed = true;
	
	private class HubCallback implements IotHubEventCallback {
		private Consumer<MessageContext> callback;
		
		private HubCallback(Consumer<MessageContext> callback){
			this.callback = callback;
		}
		
		public void execute(IotHubStatusCode status, Object context) {
			if (this.callback==null)return;
			MessageContext ctx = (MessageContext)context;
			ctx.setStatus(status);
			this.callback.accept(ctx);
		}
		
	}
	
	/**
	 * Instantiates client using hub URL, device Id, and device key, using AMQPS protocol.
	 * 
	 * If you have not been given these parameters please contact Open Energi.
	 * 
	 * @param hubUrl URL to the hub (normally something.azure-devices.net).
	 * @param deviceId Id of the gateway device.
	 * @param deviceKey shared access key for the device. 
	 */
	public Client(String hubUrl, String deviceId, String deviceKey) throws IllegalArgumentException{
		try {
			this.client = new DeviceClient(String.format(Client.connStr, hubUrl, deviceId, deviceKey).toString(), this.protocol.value);
		} catch (URISyntaxException ex){
			throw new IllegalArgumentException("Invalid Hub Url");
		}
	}
	
	/**
	 * Connects to the IotHub.
	 * @throws IOException if the connection does not succeed.
	 */
	public void connect() throws IOException {
		this.client.open();
		
	
	}
	
	/**
	 * Sends the given message to the IoTHub. Delivery is not guaranteed - use 
	 * onPublish() to receive confirmation of delivery.
	 * 
	 * @param msg The message to publish.
	 */
	public void publish(Message msg){
	}
	
	/**
	 * Sends the given message to the IotHub using the given context as a notification
	 * parameter that is passed to callbacks (to track which messages have been successfully
	 * delivered to the hub).
	 *  
	 * @param msg The message
	 * @param context The context, passed to onPublish() callback when the message is delivered
	 */
	public void publish(Message msg, MessageContext context){
		this.client.sendEventAsync(new com.microsoft.azure.iothub.Message(msg.toString()), new HubCallback(this.onPublishCallback), context);
	}
	
	/**
	 * Sets the Lambda to invoke when a message is received. 
	 * 
	 * <pre>
	 * {@code
	 * (Message msg) -> System.out.println("Message with Id " + msg.id.toString() + " published!")
	 * }
	 * </pre>
	 * @param callback
	 */
	public void onPublish(Consumer<MessageContext> callback){
		this.onPublishCallback = callback;
	}
	
	/**
	 * Sets the Lambda to invoke when a message is received. 
	 * 
	 * <pre>
	 * {@code
	 * (Signal signal) -> System.out.println(signal)
	 * }
	 * </pre>
	 * @param callback
	 */
	public void onSignal(Consumer<Signal<SignalPointItem>> callback){
		this.onSignalCallback = callback;
	}
	
	/**
	 * Sets the Lambda to invoke when a message is received. 
	 * 
	 * <pre>
	 * {@code
	 * (ScheduleSignal signal) -> System.out.println(signal.getCurrentValue())
	 * }
	 * </pre>
	 * @param callback
	 */
	public void onScheduleSignal(Consumer<Signal<SignalScheduleItem>> callback){
		this.onScheduleSignalCallback = callback;
	}
	
	
	/**
	 * Unsubscribes the client from cloud-to-device messages (Signals). By default the subscription is enabled.
	 */
	public void disableSubscription(){
		this.subscribed = false;
	}
	
	/**
	 * Subscribes the client to cloud-to-device messages (Signals). By default the subscription is enabled.
	 */
	public void enableSubscription(){
		this.subscribed = true;
	}
	
	

}
