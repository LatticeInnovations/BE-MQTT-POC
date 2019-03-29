package boston.mqtt.config;

import java.sql.Timestamp;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import boston.mqtt.model.AmsSyncAckProto.AmsSyncAck;
import boston.mqtt.model.SyncRequestProto.SyncRequest;
import boston.mqtt.modules.schedule.ScheduleDAO;
import boston.mqtt.modules.user.UserDAO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public final class MqttMessageHandler {

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
				log.info("get users request received...");
				SyncRequest userSyncRequest = SyncRequest.parseFrom(message.getPayload());
				UserDAO.getUsersService(userSyncRequest.getClientId());
			} else if (topic.contentEquals("sync/users/ack")) {
				AmsSyncAck amsSyncAck = AmsSyncAck.parseFrom(message.getPayload());
				if (amsSyncAck.getReceived()) {
					UserDAO.saveAmsUserSyncLog(amsSyncAck.getClientId(), new Timestamp(System.currentTimeMillis()));
				}
				else {
					throw new RuntimeException("Something went wrong");
				}
			}
			else if (topic.contentEquals("sync/schedules/request")) {
				log.info("get schedules request received...");
				SyncRequest schSyncRequest = SyncRequest.parseFrom(message.getPayload());
				ScheduleDAO.getSchedulesService(schSyncRequest.getClientId());
			}
			else if (topic.contentEquals("sync/schedules/ack")) {
				AmsSyncAck amsSyncAck = AmsSyncAck.parseFrom(message.getPayload());
				if (amsSyncAck.getReceived()) {
					ScheduleDAO.saveAmsScheduleSyncLog(amsSyncAck.getClientId(), new Timestamp(System.currentTimeMillis()));
				}
				else {
					throw new RuntimeException("Something went wrong");
				}
			}
			else {
				log.error("Topic "+topic+" not found.");
			}
		} catch (Exception e) {
			log.error("Exception At: ", e);
		}
	}

}
