package boston.mqtt.modules.process;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import boston.mqtt.config.MqttUtil;
import boston.mqtt.config.PublishResponse;
import boston.mqtt.conn.manager.DBConnection;
import boston.mqtt.constants.Constants;
import boston.mqtt.model.AmsProcessProto.AmsProcess;
import boston.mqtt.model.AmsProcessProto.AmsProcess.AmsProcessDetails;
import boston.mqtt.model.PlatformProcessProto.PlatformProcess;
import boston.mqtt.model.PlatformProcessProto.PlatformProcess.Builder;
import boston.mqtt.model.PlatformProcessProto.PlatformProcess.PlatformProcessDetails;
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
		// loading query.properties
		properties.load(ProcessDAO.class.getResourceAsStream(PROPERTIES_FILE_NAME));
	}

	/** This is the front handler of this class */
	public static void getProcessService(AmsProcess receivedProcess) {
		boolean result = false;
		long clientId = receivedProcess.getClientId();
		if (receivedProcess.getAmsProcessDetailsList().isEmpty()) {
			// case: when process list is empty
			result = fetchProcessList(clientId);
		} else {
			// case: when process list is not empty
			result = updateProcessList(receivedProcess.getAmsProcessDetailsList(), clientId);
		}
		if (result) {
			log.info("Process response published to ams successfully..");
		} else {
			log.info("Error in publishing processes to ams...");
		}
	}

	/** This method fetch list of process(es) from process master table */
	private static boolean fetchProcessList(long clientId) {
		final Connection con = DBConnection.getInstance().getConnection();
		ResultSet amsResultSets = null;
		ResultSet resultSet = null;
		PreparedStatement getProcessStatement = null;
		PreparedStatement existingAmsProcessSync = null;
		try {
			con.setAutoCommit(false);
			existingAmsProcessSync = con
					.prepareStatement(properties.getProperty("QUERY_FETCH_EXISTING_AMS_PROCESS_SYNC_LOG"));
			existingAmsProcessSync.setLong(1, clientId);
			amsResultSets = existingAmsProcessSync.executeQuery();
			// checking if this clientId exists in ams_process_sync table
			if (amsResultSets.first()) {
				con.commit();
				// ams process sync log exists, fetching processes between last_synced_on and
				// current timestamp..
				getProcessStatement = con.prepareStatement(properties.getProperty("QUERY_FETCH_UNSYNCED_PROCESS"));
				getProcessStatement.setTimestamp(1, amsResultSets.getTimestamp(Constants.LAST_SYNCED_ON));
			} else {
				con.commit();
				// ams process log does not exists, fetching all processes..
				getProcessStatement = con.prepareStatement(properties.getProperty("QUERY_FETCH_ALL_PROCESS"));
			}
			resultSet = getProcessStatement.executeQuery();
			if (resultSet.first()) {
				resultSet.beforeFirst();
				// extracting resultSet and building process list
				PlatformProcess.Builder processList = resultToProcessBuilderMapper(resultSet);
				// adding an empty processIds map builder
				processList.addProcessIds(ProcessIds.newBuilder().build());
				// publishing to ams
				return publishProcessList(processList.build().toByteArray(), clientId);
			} else {
				// process(es) is/are already synced to device, sending message...
				return publishProcessResponseMessage(clientId, "Process list already synced to device.");
			}
		} catch (Exception e) {
			try {
				con.rollback();
			} catch (SQLException e2) {
				log.error(Constants.EXCEPTION, e2);
			}
			log.error(Constants.EXCEPTION, e);
		} finally {
			DBConnection.getInstance().closeConnection(con, existingAmsProcessSync);
			DBConnection.getInstance().closeConnection(con, getProcessStatement);
		}
		return false;
	}

	/** This method update & send response to ams device */
	private static boolean updateProcessList(List<AmsProcessDetails> amsProcessList, long clientId) {
		final Connection con = DBConnection.getInstance().getConnection();
		PreparedStatement existingAmsProcessSync = null;
		ResultSet processResultSets = null;
		PlatformProcess.Builder processList = PlatformProcess.newBuilder();
		int insertOrUpdate = 0;
//		List<String> updatedProcessIds = new ArrayList<>();
		StringBuilder updatedIds = new StringBuilder();
		try {
			con.setAutoCommit(false);
			for (AmsProcessDetails amsProcessDetails : amsProcessList) {
				String processId = amsProcessDetails.getProcessId();
				if (processId != null && !processId.isEmpty()) {
					// fetch process details from process master table
					existingAmsProcessSync = con
							.prepareStatement(properties.getProperty("QUERY_FETCH_EXISTING_PROCESS"));
					existingAmsProcessSync.setString(1, processId);
					processResultSets = existingAmsProcessSync.executeQuery();
					if (processResultSets.first()) {
						con.commit();
						if (amsProcessDetails.getModified() > processResultSets.getInt("is_modified")) {
							updateProcess(amsProcessDetails);
							insertOrUpdate++;
							updatedIds.append("'").append(processId).append("',");
							// updatedProcessIds.add(processId);
						} else if (amsProcessDetails.getModified() == processResultSets.getInt("is_modified")) {
							// check updated_on
							Timestamp amsProcessTimestamp = Timestamp.valueOf(amsProcessDetails.getUpdatedOn());
							if (amsProcessTimestamp.after(processResultSets.getTimestamp("updated_on"))) {
								updateProcess(amsProcessDetails);
								insertOrUpdate++;
								updatedIds.append("'").append(processId).append("',");
							}
						}
					} else {
						log.error("Process with id '" + processId + "' not found");
						return publishProcessResponseMessage(clientId, "Process with id '" + processId + "' not found");
					}
				} else {
					// insert into process master
					String returnedProcessId = insertProcess(amsProcessDetails);
					if (returnedProcessId != null) {
						processList.addProcessIds(PlatformProcess.ProcessIds.newBuilder()
								.setPid(amsProcessDetails.getPid()).setProcessId(returnedProcessId)).build();
					}
					insertOrUpdate++;
					// updatedProcessIds.add(insertedProcessId);
					updatedIds.append("'").append(returnedProcessId).append("',");
				}
			}
			if (insertOrUpdate > 0) {
				// update other ams to unsynced
				log.info("Setting all other ams devices to unsynced...");
				if (unsyncedOtherAmsDevices(clientId)) {
					log.info("All processes updated successfully... publishing message...");
					updatedIds.setLength(updatedIds.length() - 1);
					return getAllModifiedProcessAndPublish(updatedIds, processList, clientId);
				}
			}
		} catch (Exception e) {
			try {
				con.rollback();
			} catch (SQLException e2) {
				log.error(Constants.EXCEPTION, e2);
			}
			log.error(Constants.EXCEPTION, e);
		} finally {
			DBConnection.getInstance().closeConnection(con, existingAmsProcessSync);
		}
		return false;
	}

	/** This method maps resultSet to builder */
	private static Builder resultToProcessBuilderMapper(ResultSet resultSet) throws SQLException {
		PlatformProcess.Builder processList = PlatformProcess.newBuilder();
		while (resultSet.next()) {
			PlatformProcessDetails processDetails = PlatformProcessDetails.newBuilder()
					.setProcessId(resultSet.getString(Constants.COLUMN_PROCESS_ID))
					.setTitle(resultSet.getString("title")).setIsActive(resultSet.getBoolean("is_active"))
					.setModified(resultSet.getInt("is_modified")).setUpdatedOn(resultSet.getString("updated_on"))
					.build();
			processList.addPlatformProcessDetails(processDetails);
		}
		return processList;
	}

	/** This method publishes process list to the ams device */
	private static boolean publishProcessList(byte[] processListByteArray, long clientId) {
		return ProcessPublishUtil.mqttPublishProcess(MqttUtil.mqttAsyncClient, processListByteArray, clientId);
	}

	/** This method publishes response message to the ams device */
	private static boolean publishProcessResponseMessage(long clientId, String message) {
		return PublishResponse.mqttPublishMessage(MqttUtil.mqttAsyncClient,
				ResponseMessage.newBuilder().setMessage(message).build().toByteArray(), "response/process/" + clientId);
	}

	/** This method updates process details in process master */
	private static void updateProcess(AmsProcessDetails amsProcessDetails) {
		final Connection con = DBConnection.getInstance().getConnection();
		PreparedStatement updateProcess = null;
		int result = 0;
		try {
			con.setAutoCommit(false);
			updateProcess = con.prepareStatement(properties.getProperty("UPDATE_PROCESS_MASTER"));
			updateProcess.setString(1, amsProcessDetails.getProductName());
			updateProcess.setString(2, amsProcessDetails.getTitle());
			updateProcess.setBoolean(3, amsProcessDetails.getIsActive());
			updateProcess.setInt(4, amsProcessDetails.getModified());
			updateProcess.setTimestamp(5, Timestamp.valueOf(amsProcessDetails.getUpdatedOn()));
			updateProcess.setString(6, amsProcessDetails.getProcessId());
			result = updateProcess.executeUpdate();
			if (result == 1) {
				con.commit();
				log.info("Process updated successfully.. ID: " + amsProcessDetails.getProcessId());
			}
		} catch (Exception e) {
			try {
				con.rollback();
			} catch (SQLException e2) {
				log.error(Constants.EXCEPTION, e2);
			}
			log.error(Constants.EXCEPTION, e);
		} finally {
			DBConnection.getInstance().closeConnection(con, updateProcess);
		}
	}

	/** This method insert process details in process master */
	private static String insertProcess(AmsProcessDetails amsProcessDetails) {
		final Connection con = DBConnection.getInstance().getConnection();
		PreparedStatement insertProcess = null;
		int result = 0;
		try {
			con.setAutoCommit(false);
			insertProcess = con.prepareStatement(properties.getProperty("INSERT_PROCESS_MASTER"));
			String generatedProcessId = ProcessIdGenerator.generate();
			if (generatedProcessId != null) {
				insertProcess.setString(1, generatedProcessId);
				insertProcess.setString(2, amsProcessDetails.getProductName());
				insertProcess.setString(3, amsProcessDetails.getTitle());
				insertProcess.setBoolean(4, amsProcessDetails.getIsActive());
				insertProcess.setInt(5, amsProcessDetails.getModified());
				insertProcess.setTimestamp(6, Timestamp.valueOf(amsProcessDetails.getUpdatedOn()));
				result = insertProcess.executeUpdate();
				if (result == 1) {
					con.commit();
					log.info("Process created successfully..");
					return generatedProcessId;
				}
			}
		} catch (Exception e) {
			try {
				con.rollback();
			} catch (SQLException e2) {
				log.error(Constants.EXCEPTION, e2);
			}
			log.error(Constants.EXCEPTION, e);
		} finally {
			DBConnection.getInstance().closeConnection(con, insertProcess);
		}
		return null;
	}

	private static boolean unsyncedOtherAmsDevices(long clientId) {
		final Connection con = DBConnection.getInstance().getConnection();
		PreparedStatement updateAmsSyncFlag = null;
		int result = 0;
		try {
			updateAmsSyncFlag = con.prepareStatement(properties.getProperty("UPDATE_OTHER_AMS_PROCESS_SYNC_FLAG"));
			updateAmsSyncFlag.setLong(1, clientId);
			result = updateAmsSyncFlag.executeUpdate();
			if (result > 0) {
				log.info("Other AMS devices set to unsynced successfully.");
				return true;
			} else {
				log.info("Other AMS device(s) does not exists..");
				return true;
			}
		} catch (Exception e) {
			try {
				con.rollback();
			} catch (SQLException e2) {
				log.error(Constants.EXCEPTION, e2);
			}
			log.error(Constants.EXCEPTION, e);
		} finally {
			DBConnection.getInstance().closeConnection(con, updateAmsSyncFlag);
		}
		return false;
	}

	private static boolean getAllModifiedProcessAndPublish(StringBuilder updatedIds, Builder processList,
			long clientId) {
		final Connection con = DBConnection.getInstance().getConnection();
		PreparedStatement getModifiedProcess = null;
		ResultSet modifiedProcessResults = null;
		try {
			con.setAutoCommit(false);
			if (updatedIds.length() == 0) {
				// get all modified processes
				getModifiedProcess = con.prepareStatement(properties.getProperty("QUERY_FETCH_ALL_PROCESS"));
				System.out.println(updatedIds);
				modifiedProcessResults = getModifiedProcess.executeQuery();
				if (modifiedProcessResults.next()) {
					con.commit();
					modifiedProcessResults.beforeFirst();
					while (modifiedProcessResults.next()) {
						PlatformProcessDetails processDetails = PlatformProcessDetails.newBuilder()
								.setProcessId(modifiedProcessResults.getString(Constants.COLUMN_PROCESS_ID))
								.setTitle(modifiedProcessResults.getString("title"))
								.setIsActive(modifiedProcessResults.getBoolean("is_active"))
								.setModified(modifiedProcessResults.getInt("is_modified"))
								.setUpdatedOn(modifiedProcessResults.getString("updated_on")).build();
						processList.addPlatformProcessDetails(processDetails);
					}
				}
			} else {
				// get all modified processes held on platform's process master
				StringBuilder stringBuilder = new StringBuilder()
						.append("select * from process_master WHERE is_modified = 1 AND process_id NOT IN (")
						.append(updatedIds).append(")");
				getModifiedProcess = con.prepareStatement(stringBuilder.toString());
				System.out.println(updatedIds);
				// getModifiedProcess.setString(1, updatedIds);
				System.out.println(getModifiedProcess.toString());
				modifiedProcessResults = getModifiedProcess.executeQuery();
				System.out.println(modifiedProcessResults.getMetaData().getColumnCount());
				if (modifiedProcessResults.next()) {
					con.commit();
					modifiedProcessResults.beforeFirst();
					while (modifiedProcessResults.next()) {
						PlatformProcessDetails processDetails = PlatformProcessDetails.newBuilder()
								.setProcessId(modifiedProcessResults.getString(Constants.COLUMN_PROCESS_ID))
								.setTitle(modifiedProcessResults.getString("title"))
								.setIsActive(modifiedProcessResults.getBoolean("is_active"))
								.setModified(modifiedProcessResults.getInt("is_modified"))
								.setUpdatedOn(modifiedProcessResults.getString("updated_on")).build();
						processList.addPlatformProcessDetails(processDetails);
					}
				}
			}
			// publishing to ams
			return publishProcessList(processList.build().toByteArray(), clientId);
		} catch (Exception e) {
			try {
				con.rollback();
			} catch (SQLException e2) {
				log.error(Constants.EXCEPTION, e2);
			}
			log.error(Constants.EXCEPTION, e);
		} finally {
			DBConnection.getInstance().closeConnection(con, getModifiedProcess);
		}
		return false;
	}
}