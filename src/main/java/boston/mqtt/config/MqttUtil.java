package boston.mqtt.config;

import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

/**
 * This class holds the MQTT methods to Connect, Publish & Subscribe to Broker
 *
 */
public class MqttUtil {

	private static final Logger logger = LogManager.getLogger(MqttUtil.class);

	private static final String PROPERTIES_FILE_NAME = "/mqtt.properties";
	static Properties props = new Properties();

	public static MqttAsyncClient mqttAsyncClient;
	
	public void mqttConnectAndSubscribe(String clientId) throws MqttException {
		final MemoryPersistence persistence = new MemoryPersistence();
		try {
			MqttUtil.props.load(MqttUtil.class.getResourceAsStream(PROPERTIES_FILE_NAME));
			MqttAsyncClient mqttClient = new MqttAsyncClient(props.getProperty("BROKER_URL"), clientId, persistence);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setAutomaticReconnect(true);
			connOpts.setCleanSession(true);
			connOpts.setKeepAliveInterval(10);
			connOpts.setConnectionTimeout(60);
			connOpts.setUserName("subscriber");
			connOpts.setPassword("subscriber".toCharArray());
			System.out.println(connOpts.toString());
			String[] serverURIarray = new String[] { props.getProperty("BROKER_URL") };
			connOpts.setServerURIs(serverURIarray);
			logger.info("About to connect to MQTT broker with the following parameters: - BROKER_URL=" + props.getProperty("BROKER_URL") + " CLIENT_ID=" + clientId);
			mqttClient.setCallback(new SimpleCallback());
			mqttClient.connect(connOpts, null, new IMqttActionListener() {
				@Override
				public void onSuccess(IMqttToken conToken) {
					logger.info("Connected to: " + mqttClient.getCurrentServerURI() + ", CLIENT_ID: " + mqttClient.getClientId());
					mqttAsyncClient = mqttClient;
					subscribe(mqttClient);
				}

				@Override
				public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
					logger.info("Failed to connect to: " + mqttClient.getServerURI());
					logger.info("Message: " + exception.getMessage());
					logger.info("Cause: " + exception.getCause());
					exception.printStackTrace();
				}
			});
			// mqttPublishMessageStream(mqttClient);

		} catch (MqttException | IOException e) {
			logger.error("msg: " + e.getMessage());
			logger.error("cause: " + e.getCause());
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void mqttConnect(String clientId) throws MqttException {
		final MemoryPersistence persistence = new MemoryPersistence();
		try {
			MqttUtil.props.load(MqttUtil.class.getResourceAsStream(PROPERTIES_FILE_NAME));
			MqttAsyncClient mqttClient = new MqttAsyncClient(props.getProperty("BROKER_URL"), clientId, persistence);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setAutomaticReconnect(true);
			connOpts.setCleanSession(false);
			connOpts.setKeepAliveInterval(10);
			connOpts.setConnectionTimeout(60);
			connOpts.setUserName("publishClient");
			connOpts.setPassword("publishClient".toCharArray());
			System.out.println(connOpts.toString());
			String[] serverURIarray = new String[] { props.getProperty("BROKER_URL") };
			connOpts.setServerURIs(serverURIarray);
			logger.info("About to connect to MQTT broker with the following parameters: - BROKER_URL=" + props.getProperty("BROKER_URL") + " CLIENT_ID=" + clientId);
			// mqttClient.setCallback(SimpleCallback.getInstance());
			mqttClient.connect(connOpts, null, new IMqttActionListener() {
				@Override
				public void onSuccess(IMqttToken conToken) {
					logger.info("Connected to: " + mqttClient.getCurrentServerURI() + ", CLIENT_ID: " + mqttClient.getClientId());
				}

				@Override
				public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
					logger.info("Failed to connect to: " + mqttClient.getServerURI());
					logger.info("Message: " + exception.getMessage());
					logger.info("Cause: " + exception.getCause());
					exception.printStackTrace();
				}
			});
			// mqttPublishMessageStream(mqttClient);

		} catch (MqttException | IOException e) {
			logger.error("msg: " + e.getMessage());
			logger.error("cause: " + e.getCause());
			e.printStackTrace();
			System.exit(-1);
		}
	}

	// Subscribe client to the topic with QoS level of 0
	public void subscribe(MqttAsyncClient mqttClient) {
		try {
			int subQoS = 0;
			String topic = props.getProperty("TOPIC_NAME");
			mqttClient.subscribe(topic, subQoS, null, new IMqttActionListener() {
				@Override
				public void onSuccess(IMqttToken asyncActionToken) {
					logger.info("Successfully Subscribed, client: {} to the topic: {}", mqttClient.getClientId(), topic);
				}
				
				@Override
				public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
					logger.error("Error subscribing: {}", asyncActionToken.getException());
				}
			});
			// String topic_2 = "$SYS/#";
			// logger.info("Subscribing client to topic: " + topic_2);
			// mqttClient.subscribe(topic_2, 1, null, new IMqttActionListener() {
			// @Override
			// public void onSuccess(IMqttToken asyncActionToken) {
			// logger.info("Subscribed client: " + mqttClient.getClientId() + " to the
			// topic: " + topic_2);
			// }
			//
			// @Override
			// public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
			// logger.error("Error subscribing: " + asyncActionToken.getException());
			// }
			// });
		} catch (MqttException ex) {
			logger.error("Exception whilst subscribing");
			ex.printStackTrace();
		}
	}

	public static void mqttPublish(MqttAsyncClient mqttClient, JSONObject jsonObject) {
		try {
			MqttMessage message = new MqttMessage(jsonObject.toString().getBytes());
			if (props.getProperty("QOS") != null) {
				message.setQos(Integer.parseInt(props.getProperty("QOS")));
				message.setRetained(false);
				// mqttClient.setCallback(new SimpleCallback());
				mqttClient.publish("web/iot/led/1", message);
				logger.info("Sent to client..");
				// try {
				// Thread.sleep(100);
				// } catch (InterruptedException e) {
				// e.printStackTrace();
				// }
			}
			// mqttClient.disconnect();
			// logger.info("Disconnected: " + mqttClient.getClientId());
		} catch (MqttException e) {
			logger.error("msg: " + e.getMessage());
			logger.error("cause: " + e.getCause());
			e.printStackTrace();
			System.exit(-1);
		}
	}

	// public void mqttPublishMessageStream(MqttAsyncClient mqttClient) {
	// /*
	// * Sample code to continually publish messages
	// */
	// Timer timer = new Timer();
	// timer.scheduleAtFixedRate(new TimerTask() {
	//
	// @Override
	// public void run() {
	// int counter = 1;
	// LedModelProto ledModel =
	// LedModelProto.newBuilder().setBlinkStatus(mqttClient.getClientId())
	// .setBlinkCounter(counter).build();
	// MqttMessage message = new MqttMessage(ledModel.toByteArray());
	// message.setQos(2);
	// try {
	// mqttClient.publish("iot/led", message);
	// System.out.println("buff count: "+ mqttClient.getBufferedMessageCount());
	// if (mqttClient.isConnected() && mqttClient.getBufferedMessageCount() > 0) {
	// System.out.println("hello");
	// }
	// logger.info("Message published from: " + mqttClient.getClientId());
	// counter++;
	// } catch (MqttException e) {
	// logger.error("msg: " + e.getMessage());
	// logger.error("cause: " + e.getCause());
	// e.printStackTrace();
	// System.exit(-1);
	// }
	// }
	// }, 2000, 2000); // Every 0.5 seconds
	// }
}