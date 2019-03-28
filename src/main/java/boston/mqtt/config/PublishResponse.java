package boston.mqtt.config;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class PublishResponse {

	private PublishResponse() {
	}

	public static boolean mqttPublishMessage(MqttAsyncClient mqttClient, byte[] response, String clientId) {
		try {
			MqttMessage message = new MqttMessage(response);
			message.setQos(0);
			message.setRetained(false);
			mqttClient.publish("response/".concat(clientId), message);
			return true;
		} catch (MqttException e) {
			log.error("Exception At: {}", e);
			return false;
		}
	}
}
