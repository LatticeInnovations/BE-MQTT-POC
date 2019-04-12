package boston.mqtt.config;

import java.sql.Timestamp;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import boston.mqtt.constants.Constants;
import boston.mqtt.model.AmsProcessProto.AmsProcess;
import boston.mqtt.model.AmsSyncAckProto.AmsSyncAck;
import boston.mqtt.model.SyncRequestProto.SyncRequest;
import boston.mqtt.modules.process.ProcessSyncLog;
import boston.mqtt.modules.process.ProcessDAO;
import boston.mqtt.modules.schedule.ScheduleDAO;
import boston.mqtt.modules.user.UserDAO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public final class MqttMessageHandler {

	private MqttMessageHandler() {
	}

	public static void arrivedMessage(String topic, MqttMessage message) {
		try {
			switch (topic) {
			case "sync/users/request":
				log.info("get users request received...");
				SyncRequest userSyncRequest = SyncRequest.parseFrom(message.getPayload());
				UserDAO.getUsersService(userSyncRequest.getClientId());
				break;
			case "sync/ack/users":
				AmsSyncAck amsUserSyncAck = AmsSyncAck.parseFrom(message.getPayload());
				if (amsUserSyncAck.getReceived()) {
					UserDAO.saveAmsUserSyncLog(amsUserSyncAck.getClientId(), new Timestamp(System.currentTimeMillis()));
				} else {
					log.error("User(s) not synced.");
				}
				break;
			case "sync/schedules/request":
				log.info("get schedules request received...");
				SyncRequest schSyncRequest = SyncRequest.parseFrom(message.getPayload());
				ScheduleDAO.getSchedulesService(schSyncRequest.getClientId());
				break;
			case "sync/ack/schedules":
				AmsSyncAck amsSyncAck = AmsSyncAck.parseFrom(message.getPayload());
				if (amsSyncAck.getReceived()) {
					ScheduleDAO.saveAmsScheduleSyncLog(amsSyncAck.getClientId(),
							new Timestamp(System.currentTimeMillis()));
				} else {
					log.error("Schedule(s) not synced.");
				}
				break;
			case "sync/process":
				log.info("process received from ams...");
				AmsProcess receivedProcess = AmsProcess.parseFrom(message.getPayload());
				ProcessDAO.getProcessService(receivedProcess);
				break;
			case "sync/ack/process":
				AmsSyncAck processSyncAck = AmsSyncAck.parseFrom(message.getPayload());
				if (processSyncAck.getReceived()) {
					ProcessSyncLog.saveAmsProcessSyncLog(processSyncAck.getClientId(),
							new Timestamp(System.currentTimeMillis()));
				} else {
					log.error("Process(es) not synced.");
					//TODO
				}
				break;
			default:
				log.error("Topic " + topic + " not found.");
				break;
			}
		} catch (Exception e) {
			log.error(Constants.EXCEPTION, e);
		}
	}

}
