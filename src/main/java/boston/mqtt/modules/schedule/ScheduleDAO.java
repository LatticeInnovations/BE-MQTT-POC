package boston.mqtt.modules.schedule;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import boston.mqtt.config.MqttUtil;
import boston.mqtt.config.PublishResponse;
import boston.mqtt.conn.manager.DBConnection;
import boston.mqtt.model.ResponseMessageProto.ResponseMessage;
import boston.mqtt.model.ScheduleSyncResponseProto.ScheduleSyncResponse;
import boston.mqtt.model.ScheduleSyncResponseProto.ScheduleSyncResponse.Schedule;
import boston.mqtt.model.ScheduleSyncResponseProto.ScheduleSyncResponse.Schedule.Operators;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public final class ScheduleDAO {

	private static final String PROPERTIES_FILE_NAME = "/query.properties";
	static Properties properties = new Properties();

	@PostConstruct
	void getProperty() throws IOException {
		properties.load(ScheduleDAO.class.getResourceAsStream(PROPERTIES_FILE_NAME));
	}

	public static void getSchedulesService(long clientId) {
		final Connection con = DBConnection.getInstance().getConnection();
		PreparedStatement getSchedulesStatement = null;
		PreparedStatement existingAmsUserSync = null;
		PreparedStatement getOperators = null;
		ResultSet amsResultSets = null;
		ResultSet resultSet = null;
		ResultSet operatorsResultSet = null;
		boolean published = true;
		try {
			con.setAutoCommit(false);
			existingAmsUserSync = con.prepareStatement(properties.getProperty("QUERY_FETCH_EXISTING_AMS_SCHEDULE_SYNC_LOG"));
			existingAmsUserSync.setLong(1, clientId);
			amsResultSets = existingAmsUserSync.executeQuery();
			if (amsResultSets.first()) {
				con.commit();
				// ams schedule sync log exists
				getSchedulesStatement = con.prepareStatement(properties.getProperty("QUERY_FETCH_UNSYNCED_SCHEDULES"));
				getSchedulesStatement.setLong(1, clientId);
				getSchedulesStatement.setTimestamp(2, amsResultSets.getTimestamp("last_synced_on"));
			} else {
				con.commit();
				// new ams device
				getSchedulesStatement = con.prepareStatement(properties.getProperty("QUERY_FETCH_ALL_SCHEDULES"));
				getSchedulesStatement.setLong(1, clientId);
			}
			log.info(getSchedulesStatement.toString());
			resultSet = getSchedulesStatement.executeQuery();
			if (resultSet.first()) {
				resultSet.beforeFirst();
				ScheduleSyncResponse.Builder schedulesList = ScheduleSyncResponse.newBuilder();
				while (resultSet.next()) {
						Schedule.Builder schedule = Schedule.newBuilder()
								.setScheduleId(resultSet.getLong("schedule_id"))
								.setSchStartDate(resultSet.getLong("start_date_time"))
								.setSchEndDate(resultSet.getLong("end_date_time"))
								.setAmsId(resultSet.getLong("ams_id"))
								.setProcessId(resultSet.getString("process_id"))
								.setProcessTitle(resultSet.getString("title"))	
								.setCreatedOn(resultSet.getString("created_on"))
								.setUpdatedOn(resultSet.getString("updated_on"));
						if (resultSet.getString("manager_id") != null) {
							schedule.setManagerId(resultSet.getLong("manager_id"));
							schedule.setManagerUsername(resultSet.getString("user_name"));
						}
						else {
							schedule.clearManagerId();
							schedule.clearManagerUsername();
						}
						getOperators = con.prepareStatement(properties.getProperty("QUERY_FETCH_USER_OPERATORS"));
						getOperators.setLong(1, resultSet.getLong("schedule_id"));
						operatorsResultSet = getOperators.executeQuery();
						if (operatorsResultSet.first()) {
							operatorsResultSet.beforeFirst();
							while (operatorsResultSet.next()) {
								Operators operator = Operators.newBuilder()
								.setUserId(operatorsResultSet.getLong("user_id"))
								.setUsername(operatorsResultSet.getString("user_name"))
								.build();
							schedule.addOperators(operator);
							}
						} else {
							schedule.addOperators(Operators.newBuilder().build()); 
						}
						schedulesList.addSchedule(schedule);
				}
				// publish schedule list to the ams client
				 published = SchedulePublishUtil.mqttPublishSchedules(MqttUtil.mqttAsyncClient, schedulesList.build().toByteArray(), clientId);
			} else {
				// schedule(s) already synced
				 published = PublishResponse.mqttPublishMessage(MqttUtil.mqttAsyncClient, ResponseMessage.newBuilder()
						 .setMessage("Schedule(s) already synced to device.")
						 .build()
						 .toByteArray(), clientId, "schedules/");
			}
			if (published) {
				log.info("Schedule(s) published to ams successfully..");
			} else {
				log.info("Something went wrong.");
			}
		} catch (Exception e) {
			try {
				con.rollback();
			} catch (SQLException e2) {
				log.error("Exception At: ", e2);
			}
			log.error("Exception At: ", e);
		} finally {
			DBConnection.getInstance().closeConnection(con, getOperators);
			DBConnection.getInstance().closeConnection(con, getSchedulesStatement);
			DBConnection.getInstance().closeConnection(con, existingAmsUserSync);
		}
	}

	public static void saveAmsScheduleSyncLog(long clientId, Timestamp timestamp) {
		final Connection con = DBConnection.getInstance().getConnection();
		PreparedStatement updateSyncLogStmt = null;
		PreparedStatement existingAmsScheduleSync = null;
		ResultSet amsResultSets = null;
		int result = 0;
		try {
			con.setAutoCommit(false);
			existingAmsScheduleSync = con.prepareStatement(properties.getProperty("QUERY_FETCH_EXISTING_AMS_SCHEDULE_SYNC_LOG"));
			existingAmsScheduleSync.setLong(1, clientId);
			amsResultSets = existingAmsScheduleSync.executeQuery();
			if (amsResultSets.first()) {
				// update existing record
				updateSyncLogStmt = con.prepareStatement(properties.getProperty("UPDATE_AMS_SCHEDULE_SYNC_LOG"));
				updateSyncLogStmt.setTimestamp(1, timestamp);
				updateSyncLogStmt.setLong(2, clientId);
			} else {
				// new insert in ams_schedule_sync table
				updateSyncLogStmt = con.prepareStatement(properties.getProperty("INSERT_AMS_SCHEDULE_SYNC_LOG"));
				updateSyncLogStmt.setLong(1, clientId);
				updateSyncLogStmt.setTimestamp(2, timestamp);
			}
			log.info(updateSyncLogStmt.toString());
			result = updateSyncLogStmt.executeUpdate();
			if (result == 1) {
				con.commit();
				log.info("AMS schedule sync info successfully saved.");
			} else {
				con.rollback();
				throw new RuntimeException("Ams sync log info not saved.");
			}
		} catch (Exception e) {
			try {
				con.rollback();
			} catch (SQLException e2) {
				log.error("Exception At: ", e2);
			}
			log.error("Exception At: ", e);
		} finally {
			DBConnection.getInstance().closeConnection(con, updateSyncLogStmt);
			DBConnection.getInstance().closeConnection(con, existingAmsScheduleSync);
		}
	}
}