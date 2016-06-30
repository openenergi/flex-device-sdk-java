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
import com.openenergi.flex.message.Message;
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
	private Consumer<Message> onPublishCallback;
	private Consumer<Signal<SignalPointItem>> onSignalCallback;
	private Consumer<Signal<SignalScheduleItem>> onScheduleSignalCallback;
	
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
	
	public void publish(Message msg){
		//TODO(mbironneau)
	}
	
	public void onPublish(Consumer<Message> callback){
		//TODO(mbironneau)
	}
	
	
	public void onSignal(Consumer<Signal<SignalPointItem>> callback){
		//TODO(mbironneau)
	}
	
	public void onScheduleSignal(Consumer<Signal<SignalScheduleItem>> callback){
		//TODO(mbironneau)
	}
	
	
	
	public void disableSubscription(){
		//TODO(mbironneau)
	}
	
	

}
