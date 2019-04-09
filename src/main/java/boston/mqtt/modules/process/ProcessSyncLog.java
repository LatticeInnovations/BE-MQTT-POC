package boston.mqtt.modules.process;

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

import boston.mqtt.conn.manager.DBConnection;
import boston.mqtt.constants.Constants;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public final class ProcessSyncLog {

	private static final String PROPERTIES_FILE_NAME = "/query.properties";
	static Properties properties = new Properties();

	@PostConstruct
	void getProperty() throws IOException {
		properties.load(ProcessSyncLog.class.getResourceAsStream(PROPERTIES_FILE_NAME));
	}

	public static void saveAmsProcessSyncLog(long clientId, Timestamp timestamp) {
		final Connection con = DBConnection.getInstance().getConnection();
		PreparedStatement updateSyncLogStmt = null;
		PreparedStatement updateAmsSyncFlag = null;
		PreparedStatement existingAmsProcessSync = null;
		ResultSet amsResultSets = null;
		int result = 0;
		try {
			con.setAutoCommit(false);
			existingAmsProcessSync = con.prepareStatement(properties.getProperty("QUERY_FETCH_EXISTING_AMS_PROCESS_SYNC_LOG"));
			existingAmsProcessSync.setLong(1, clientId);
			amsResultSets = existingAmsProcessSync.executeQuery();
			if (amsResultSets.first()) {
				// update existing record
				updateSyncLogStmt = con.prepareStatement(properties.getProperty("UPDATE_AMS_PROCESS_SYNC_LOG"));
				updateSyncLogStmt.setTimestamp(1, timestamp);
				updateSyncLogStmt.setLong(2, clientId);
			} else {
				// new insert in ams_process_sync table
				updateSyncLogStmt = con.prepareStatement(properties.getProperty("INSERT_AMS_PROCESS_SYNC_LOG"));
				updateSyncLogStmt.setLong(1, clientId);
				updateSyncLogStmt.setTimestamp(2, timestamp);
			}
			result = updateSyncLogStmt.executeUpdate();
			if (result == 1) {
				con.commit();
				log.info("AMS process sync info successfully saved.... setting all other ams devices to unsynced...");
				result = 0;
				updateAmsSyncFlag = con.prepareStatement(properties.getProperty("UPDATE_AMS_PROCESS_SYNC_FLAG"));
				updateAmsSyncFlag.setLong(1, clientId);
				result = updateAmsSyncFlag.executeUpdate();
				if (result == 1) {
					log.info("Other AMS devices set to unsynced successfully...");
				} else {
					con.rollback();
					log.error("Ams sync log info not saved.");
				}
			} else {
				con.rollback();
				log.error("Ams sync log info not saved.");
			}
		} catch (Exception e) {
			try {
				con.rollback();
			} catch (SQLException e2) {
				log.error(Constants.EXCEPTION, e2);
			}
			log.error(Constants.EXCEPTION, e);
		} finally {
			DBConnection.getInstance().closeConnection(con, updateSyncLogStmt);
			DBConnection.getInstance().closeConnection(con, existingAmsProcessSync);
		}
	}
}