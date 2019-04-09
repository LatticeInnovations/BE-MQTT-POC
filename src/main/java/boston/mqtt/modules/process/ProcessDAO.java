package boston.mqtt.modules.process;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import boston.mqtt.config.MqttUtil;
import boston.mqtt.config.PublishResponse;
import boston.mqtt.conn.manager.DBConnection;
import boston.mqtt.constants.Constants;
import boston.mqtt.model.AmsProcessProto.AmsProcess;
import boston.mqtt.model.PlatformProcessProto.PlatformProcess;
import boston.mqtt.model.PlatformProcessProto.PlatformProcess.ProcessDetails;
import boston.mqtt.model.PlatformProcessProto.PlatformProcess.ProcessIds;
import boston.mqtt.model.ResponseMessageProto.ResponseMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public final class ProcessDAO {

	private static final String PROPERTIES_FILE_NAME = "/query.properties";
	static Properties properties = new Properties();

	@PostConstruct
	void getProperty() throws IOException {
		properties.load(ProcessDAO.class.getResourceAsStream(PROPERTIES_FILE_NAME));
	}

	public static void getProcessService(AmsProcess receivedProcess) {
		final Connection con = DBConnection.getInstance().getConnection();
		PreparedStatement getProcessStatement = null;
		PreparedStatement existingAmsProcessSync = null;
		ResultSet amsResultSets = null;
		ResultSet resultSet = null;
		boolean published = true;
		try {
			long clientId = receivedProcess.getClientId();
			if (receivedProcess.getProcessCount() == 0) {
				// case: Process list is empty
				con.setAutoCommit(false);
				existingAmsProcessSync = con.prepareStatement(properties.getProperty("QUERY_FETCH_EXISTING_AMS_PROCESS_SYNC_LOG"));
				existingAmsProcessSync.setLong(1, clientId);
				amsResultSets = existingAmsProcessSync.executeQuery();
				if (amsResultSets.first()) {
					con.commit();
					// ams process sync log exists
					getProcessStatement = con.prepareStatement(properties.getProperty("QUERY_FETCH_UNSYNCED_PROCESS"));
					getProcessStatement.setTimestamp(1, amsResultSets.getTimestamp(Constants.LAST_SYNCED_ON));
				} else {
					con.commit();
					// does not exists
					getProcessStatement = con.prepareStatement(properties.getProperty("QUERY_FETCH_ALL_PROCESS"));
				}
				resultSet = getProcessStatement.executeQuery();
				if (resultSet.first()) {
					resultSet.beforeFirst();
					PlatformProcess.Builder processList = PlatformProcess.newBuilder();
					while (resultSet.next()) {
						ProcessDetails processDetails = ProcessDetails.newBuilder()
							.setProcessId(resultSet.getString(Constants.COLUMN_PROCESS_ID))
							.setTitle(resultSet.getString("title"))
							.setIsActive(resultSet.getBoolean("is_active"))
							.setIsModified(resultSet.getBoolean("is_modified"))
							.setUpdatedOn(resultSet.getString("updated_on"))
							.build();
						processList.addProcessDetails(processDetails);
							}
					processList.addProcessIds(ProcessIds.newBuilder().build());
					// publish process list to the ams client
					published = ProcessPublishUtil.mqttPublishProcess(MqttUtil.mqttAsyncClient, processList.build().toByteArray(), clientId);
					}
				 else {
					// process(es) already synced
					 published = PublishResponse.mqttPublishMessage(MqttUtil.mqttAsyncClient, ResponseMessage.newBuilder()
							 .setMessage("Process list already synced to device.")
							 .build()
							 .toByteArray(), clientId, "process/");
				}
				if (published) {
					log.info("Process(es) published to ams successfully..");
				} else {
					log.info(Constants.SOMETHING_WENT_WRONG);
				}
			} else {
				//case: Process list is not empty
			}
		}
		catch (Exception e) {
			try {
				con.rollback();
			} catch (SQLException e2) {
				log.error(Constants.EXCEPTION, e2);
			}
			log.error(Constants.EXCEPTION, e);
		}
	finally {
		DBConnection.getInstance().closeConnection(con, existingAmsProcessSync);
		DBConnection.getInstance().closeConnection(con, getProcessStatement);
	//	DBConnection.getInstance().closeConnection(con, existingAmsUserSync);
	}	
	}
	
	
}