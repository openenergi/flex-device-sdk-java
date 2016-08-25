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


import com.fasterxml.jackson.core.JsonProcessingException;
import com.microsoft.azure.iothub.*;
import com.openenergi.flex.message.Message;
import com.openenergi.flex.message.MessageContext;
import com.openenergi.flex.message.Signal;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class BasicClient implements Client {
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
	private Consumer<Signal<?>> onSignalCallback;
	private Boolean subscribed = true;
	
	private Boolean connected = false;
	
	private class HubCallback implements IotHubEventCallback {
		private Consumer<MessageContext> callback;
		
		public HubCallback(Consumer<MessageContext> callback){
			this.callback = callback;
		}
		
		public void execute(IotHubStatusCode status, Object context) {
			if (this.callback==null)return;
			
			MessageContext ctx;
			
			if (context != null){
				ctx = (MessageContext)context;
			} else {
				ctx = new MessageContext();
			}
			ctx.setStatus(status);
			this.callback.accept(ctx);
		}
		
	}
	
	private class SignalCallback implements MessageCallback {
		private Consumer<Signal<?>> callback;
		
		public SignalCallback(Consumer<Signal<?>> callback){
			this.callback = callback;
		}

		public IotHubMessageResult execute(
				com.microsoft.azure.iothub.Message rawMessage, Object context) {
			try {
				Signal msg = (Signal) Message.deserialize(new String(rawMessage.getBytes(), StandardCharsets.UTF_8));
				if (this.callback != null){
					this.callback.accept(msg);
				} else {
					System.out.println("[NO CALLBACK} Received signal " + new String(rawMessage.getBytes(), StandardCharsets.UTF_8));
				}

				return IotHubMessageResult.COMPLETE;
			} catch (IOException ex){
				ex.printStackTrace();
				return IotHubMessageResult.ABANDON;
			}
			

		}
		
	}
	
	/**
	 * Instantiates basicClient using hub URL, device Id, and device key, using AMQPS protocol.
	 * 
	 * If you have not been given these parameters please contact Open Energi.
	 * 
	 * @param hubUrl URL to the hub (normally something.azure-devices.net).
	 * @param deviceId Id of the gateway device.
	 * @param deviceKey shared access key for the device. 
	 */
	public BasicClient(String hubUrl, String deviceId, String deviceKey) throws IllegalArgumentException{
		try {
			this.client = new DeviceClient(String.format(BasicClient.connStr, hubUrl, deviceId, deviceKey).toString(), this.protocol.value);
		} catch (URISyntaxException ex){
			throw new IllegalArgumentException("Invalid Hub Url");
		}
	}
	
	/**
	 * Connects to the IotHub.
	 * @throws IOException if the connection does not succeed.
	 */
	public void connect() throws IOException {
		if (this.connected) return;
		
		this.client.open();
		
		if (this.subscribed) this.client.setMessageCallback(new SignalCallback(this.onSignalCallback), null);
	}
	
	/**
	 * Disconnects from the IotHub. Idempotent.
	 */
	public void disconnect() {
		if (!this.connected) return;
		
		try {
			this.client.close();
		} catch (IOException ignored){
			//Ignore IOExceptions to make method idempotent
		} finally {
			this.connected = false;
		}
		
		
	}
	
	/**
	 * Sends the given message to the IoTHub. Delivery is not guaranteed - use 
	 * onPublish() to receive confirmation of delivery.
	 * 
	 * @param msg The message to publish.
	 */
	public void publish(Message msg){
		this.publish(msg, null);
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
	 * Sets the Lambda to invoke when a signal is received. 
	 * 
	 * <pre>
	 * {@code
	 * (Signal signal) -> System.out.println(signal)
	 * }
	 * </pre>
	 * @param callback
	 */
	public void onSignal(Consumer<Signal<?>> callback){
		this.onSignalCallback = callback;
	}
	

	
	
	/**
	 * Unsubscribes the basicClient from cloud-to-device messages (Signals). By default the subscription is enabled.
	 */
	public void disableSubscription(){
		this.subscribed = false;
	}
	
	/**
	 * Subscribes the basicClient to cloud-to-device messages (Signals). By default the subscription is enabled.
	 */
	public void enableSubscription(){
		this.subscribed = true;
	}
	
	

}
