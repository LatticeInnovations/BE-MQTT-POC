package boston.mqtt.config;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.googlecode.protobuf.format.JsonFormat;

import boston.mqtt.model.AmsSyncAckProto.AmsSyncAck;
import boston.mqtt.model.LedModel;
import boston.mqtt.model.LedProto.LedModelProto;
import boston.mqtt.model.UserSyncRequestProto.UserSyncRequest;
import boston.mqtt.modules.user.UserDAO;
import boston.mqtt.serviceImpl.LedServiceImpl;
import boston.mqtt.serviceImpl.LedServiceImpl2;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class MqttMessageHandler {

	// static StringBuilder queryBatch = null;
	//
	// static int count = 0;

	public static void arrivedMessage(String topic, MqttMessage message) {
		try {
			if (topic.contains("iot/led")) {
				log.info(new String(message.getPayload()));
				// if (topic.contains("/connected")) {
				// JSONObject connObject = new JSONObject(message.toString());
				// String clientId = connObject.getString("clientid");
				// log.info("Client '" + clientId + "' Connected...");
				// if (!clientId.equals("subscriber")) {
				// JSONObject savedClient = new ClientServiceImpl().saveClientToDB(connObject);
				// if (savedClient.has("success")) {
				// logger.info("Client persisted to db successfully...");
				// } else {
				// logger.info("Error in persisting Client '" + clientId + "' to db");
				// }
				// }
				// }
				// else {
				// JSONObject disconnObject = new JSONObject(message.toString());
				// String clientId = disconnObject.getString("clientid");
				// long endTime = disconnObject.getLong("ts");
				// log.info("Client Disconnected: " + clientId);
				// if (!clientId.equals("subscriber")) {
				// JSONObject updateClient = new
				// ClientServiceImpl().updateClientEndTime(endTime, clientId);
				// if (updateClient.has("success")) {
				// logger.info("Client update to db successfully...");
				// } else {
				// logger.info("Error in updating Client '" + clientId + "'");
				// }
				// }
				// }
			} else if (topic.contentEquals("sync/users/request")) {
				UserSyncRequest userSyncRequest = UserSyncRequest.parseFrom(message.getPayload());
				UserDAO.getUsersService(userSyncRequest.getClientId());
			} else if (topic.contentEquals("sync/users/ack")) {
				AmsSyncAck amsSyncAck = AmsSyncAck.parseFrom(message.getPayload());
				if (amsSyncAck.getReceived()) {
					UserDAO.saveAmsUserSyncLog(amsSyncAck.getClientId(), System.currentTimeMillis());
				}
				else {
					throw new RuntimeException("Something went wrong");
				}
			} else {
				LedModelProto ledModelProto = LedModelProto.parseFrom(message.getPayload());
				switch (topic) {
				case "iot/led/1":
					LedServiceImpl.sqlQueryCounter(ledModelProto);
					EventHandler.onEvent(new JsonFormat().printToString(ledModelProto));
					break;
				case "iot/led/2":
					LedServiceImpl2.sqlQueryCounter(ledModelProto);
					EventHandler.onEvent1(new LedModel(ledModelProto.getCounter(), ledModelProto.getClientId()));
					break;
				// case "iot/disconnect":
				// DeviceDisconnectProto deviceDisconnectProto =
				// DeviceDisconnectProto.parseFrom(message.getPayload());
				// logger.info("Device '" + deviceDisconnectProto.getClientId() + "'
				// disconnected at: "
				// + System.currentTimeMillis());
				// if (queryBatch.length() > 0) {
				// queryBatch.setLength(queryBatch.length() - 1);
				// new LedServiceImpl().saveMessage(queryBatch.toString());
				// count = 0;
				// }
				// break;
				default:
					log.error("Topic '" + topic + "' not found...");
					break;
				}
			}
		} catch (Exception e) {
			log.error("Exception At: ", e);
		}
	}

}
