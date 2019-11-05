package com.awsiot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTopic;
import com.awsiot.utils.SampleUtil;
import com.awsiot.utils.SampleUtil.KeyStorePasswordPair;

@RestController
public class AWSIoTController {
	
	private Boolean connected = false;
	private AWSIotMqttClient client;
	private String topicName = "remoteCar";
	private AWSIotQos qos = AWSIotQos.QOS0;
	
	@GetMapping("/command")
	public String getCommand(@RequestParam String command) throws AWSIotException, InterruptedException {
		if (!connected) {
			connectClient();
			subscribe();
			connected = true;
			System.out.println("connected now");
		}
		
		String payload = command;

		client.publish(topicName, qos, payload);
		String response = "Executed command: " + command;
		return response;
	}
	
	public void connectClient() throws AWSIotException {
		String clientEndpoint = "a1al3h316aw0ag-ats.iot.ap-south-1.amazonaws.com";       // replace <prefix> and <region> with your own
		String clientId = "remoteapi";                              // replace with your own client ID. Use unique client IDs for concurrent connections.
		String certificateFile = "1b92908738-certificate.pem.crt";                       // X.509 based certificate file
		String privateKeyFile = "1b92908738-private.pem.key";                        // PKCS#1 or PKCS#8 PEM encoded private key file

		// SampleUtil.java and its dependency PrivateKeyReader.java can be copied from the sample source code.
		// Alternatively, you could load key store directly from a file - see the example included in this README.
		KeyStorePasswordPair pair = SampleUtil.getKeyStorePasswordPair(certificateFile, privateKeyFile);
		client = new AWSIotMqttClient(clientEndpoint, clientId, pair.keyStore, pair.keyPassword);

		// optional parameters can be set before connect()
		client.connect();
	}

	public class MyTopic extends AWSIotTopic {
	    public MyTopic(String topic, AWSIotQos qos) {
	        super(topic, qos);
	    }

	    @Override
	    public void onMessage(AWSIotMessage message) {
	    	System.out.println("Received: " + message.getStringPayload());
	        // called when a message is received
	    }
	}
	
	public void subscribe() throws AWSIotException, InterruptedException {
		MyTopic topic = new MyTopic(topicName, qos);
		client.subscribe(topic);
		
		Thread.sleep(2000);
	}
	
}
