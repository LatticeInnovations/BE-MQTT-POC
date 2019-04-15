package boston.mqtt.config;

import java.sql.Timestamp;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import boston.mqtt.constants.Constants;
import boston.mqtt.model.AmsProcessProto.AmsProcess;
import boston.mqtt.model.AmsSyncAckProto.AmsSyncAck;
import boston.mqtt.model.ResponseMessageProto.ResponseMessage;
import boston.mqtt.model.SyncRequestProto.SyncRequest;
import boston.mqtt.modules.process.ProcessDAO;
import boston.mqtt.modules.process.ProcessSyncLog;
import boston.mqtt.modules.schedule.ScheduleDAO;
import boston.mqtt.modules.schedule.ScheduleSyncLog;
import boston.mqtt.modules.user.UserDAO;
import boston.mqtt.modules.user.UserSyncLog;
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
					UserSyncLog.saveAmsUserSyncLog(amsUserSyncAck.getClientId(),
							new Timestamp(System.currentTimeMillis()));
				} else {
					log.error("User(s) not synced.");
					PublishResponse.mqttPublishMessage(
							MqttUtil.mqttAsyncClient, ResponseMessage.newBuilder()
									.setMessage("Error in syncing user(s), please request again").build().toByteArray(),
							"error/sync/users/" + amsUserSyncAck.getClientId());
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
					ScheduleSyncLog.saveAmsScheduleSyncLog(amsSyncAck.getClientId(),
							new Timestamp(System.currentTimeMillis()));
				} else {
					log.error("Schedule(s) not synced.");
					PublishResponse.mqttPublishMessage(
							MqttUtil.mqttAsyncClient, ResponseMessage.newBuilder()
									.setMessage("Error in syncing schedule(s), please request again").build().toByteArray(),
							"error/sync/schedules/" + amsSyncAck.getClientId());
				}
				break;
			case "sync/process":
				log.info("process(es) received from ams...");
				AmsProcess receivedProcess = AmsProcess.parseFrom(message.getPayload());
				ProcessDAO.getProcessService(receivedProcess);
				break;
			case "sync/ack/process":
				AmsSyncAck processSyncAck = AmsSyncAck.parseFrom(message.getPayload());
				if (processSyncAck.getReceived() && processSyncAck.getClientId() != 0) {
					ProcessSyncLog.saveAmsProcessSyncLog(processSyncAck.getClientId(),
							new Timestamp(System.currentTimeMillis()));
					// check if all the ams devices are synced, if yes then change modified status
					// to 0 in process master
					if (ProcessSyncLog.countNotSyncedAmsDevices()) {
						// all ams devices have been synced, update modified status to 0 in process
						// master
						log.info(
								"All ams devices are synced, setting modified status to 0 for all processes on platform..");
						if (ProcessDAO.updateProcessModifiedStatus()) {
							// modified status changed to 0 for all processes on platform
							log.info("All ams devices successfully synced to platform.");
						}
					} else {
						log.info("All processes on platform already synced to ams devices");
					}
				} else {
					log.error("Process(es) not synced, Something went wrong...");
					PublishResponse.mqttPublishMessage(MqttUtil.mqttAsyncClient, ResponseMessage.newBuilder()
							.setMessage("Error in syncing process(es), please request again").build().toByteArray(),
							"error/sync/process/" + processSyncAck.getClientId());
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
