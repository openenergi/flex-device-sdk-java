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

package com.openenergi.flex.message;

import com.microsoft.azure.iothub.IotHubStatusCode;

/**
 * This class contains application-level data for a message as well as a status code passed back from the IoTHub.
 * 
 * @author mbironneau
 *
 */
public class MessageContext {

	private Object data;
	private String status;
	
	
	public MessageContext(Object data){
		this.setData(data);
	}
	
	public MessageContext(){}
	
	public void setStatus(IotHubStatusCode status){
		switch (status){
		case BAD_FORMAT:
			this.status = "Internal error - message had incorrect format";
			break;
		case OK:
		case OK_EMPTY:
			this.status = "The message was successfully received";
			break;
		case UNAUTHORIZED:
			this.status = "Unauthorized - please try again or request a new device key";
			break;
		case TOO_MANY_DEVICES:
			this.status = "Please contact Open Energi quoting the error code TOO_MANY_DEVICES";
			break;
		case HUB_OR_DEVICE_ID_NOT_FOUND:
			this.status = "Either the Hub or Device Id was not found. Please check inputs.";
			break;
		case PRECONDITION_FAILED:
			this.status = "Please contact Open Energi quoting the error code PRECONDITION_FAILED";
			break;
		case REQUEST_ENTITY_TOO_LARGE:
			this.status = "Your message was too large. The limit is 256KB";
			break;
		case THROTTLED:
			this.status = "This device has been sending too many messages and the request was throttled. Please try again soon";
			break;
		case INTERNAL_SERVER_ERROR:
			this.status = "There was an unknown server-side error. If this happens frequently please contact Open Energi quoting the error code INTERNAL_SERVER_ERROR";
			break;
		case SERVER_BUSY:
			this.status = "The server is busy. Please try again soon. If this happens frequently please contact Open Energi quoting the error code SERVER_BUSY";
			break;
		case ERROR:
			this.status = "There was an uknown error. Please contact Open Energi quoting the error code ERROR";
			break;
		case MESSAGE_EXPIRED:
			this.status = "The message failed to deliver in a timely fashion. If this happens frequently please contact Open Energi quoting the error code MESSAGE_EXPIRED";
			break;
		}
	}
	
	public String getStatus(){
		return this.status;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
	
}
