package boston.mqtt.modules.user;

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
import boston.mqtt.model.UserSyncResponseProto.UserSyncResponse;
import boston.mqtt.model.UserSyncResponseProto.UserSyncResponse.User;
import boston.mqtt.model.UserSyncResponseProto.UserSyncResponse.User.Manager;
import boston.mqtt.model.UserSyncResponseProto.UserSyncResponse.User.Roles;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public final class UserDAO {

	private static final String PROPERTIES_FILE_NAME = "/query.properties";
	static Properties properties = new Properties();

	@PostConstruct
	void getProperty() throws IOException {
		properties.load(UserDAO.class.getResourceAsStream(PROPERTIES_FILE_NAME));
	}

	public static void getUsersService(long clientId) {
		final Connection con = DBConnection.getInstance().getConnection();
		PreparedStatement getUsersStatement = null;
		PreparedStatement existingAmsUserSync = null;
		PreparedStatement getUserRoles = null;
		PreparedStatement getUserManager = null;
		ResultSet amsResultSets = null;
		ResultSet resultSet = null;
		ResultSet rolesResultSet = null;
		ResultSet managerResultSet = null;
		boolean published = true;
		try {
			con.setAutoCommit(false);
			existingAmsUserSync = con.prepareStatement(properties.getProperty("QUERY_FETCH_EXISTING_AMS_USER_SYNC_LOG"));
			existingAmsUserSync.setLong(1, clientId);
			amsResultSets = existingAmsUserSync.executeQuery();
			if (amsResultSets.first()) {
				con.commit();
				// ams user sync log exists
				getUsersStatement = con.prepareStatement(properties.getProperty("QUERY_FETCH_UNSYNCED_USERS"));
				getUsersStatement.setTimestamp(1, amsResultSets.getTimestamp("last_synced_on"));
			} else {
				con.commit();
				// new ams device
				getUsersStatement = con.prepareStatement(properties.getProperty("QUERY_FETCH_ALL_USERS"));
			}
			log.info(getUsersStatement.toString());
			resultSet = getUsersStatement.executeQuery();
			if (resultSet.first()) {
				resultSet.beforeFirst();
				UserSyncResponse.Builder usersList = UserSyncResponse.newBuilder();
				while (resultSet.next()) {
					getUserRoles = con.prepareStatement(properties.getProperty("QUERY_FETCH_USER_ROLES"));
					getUserRoles.setLong(1, resultSet.getLong("user_id"));
					rolesResultSet = getUserRoles.executeQuery();
					if (rolesResultSet.first()) {
						rolesResultSet.beforeFirst();
						User.Builder user = User.newBuilder()
								.setUserId(resultSet.getLong("user_id"))
								.setUsername(resultSet.getString("user_name"))
								.setFullname(resultSet.getString("full_name"))
								.setDepartment(resultSet.getString("department"))
								.setDesignation(resultSet.getString("designation"))
								.setEmail(resultSet.getString("email_id"))
								.setPhone(resultSet.getString("phone"))
								.setCountryCode(resultSet.getString("country_code"))
								.setPassword(resultSet.getString("password"))
								.setStatus(resultSet.getString("status"))
								.setCreatedOn(resultSet.getString("created_on"))
								.setUpdatedOn(resultSet.getString("updated_on"));
						long managerId = resultSet.getLong("manager_id");
						if (managerId > 0) {
							getUserManager = con.prepareStatement(properties.getProperty("QUERY_FETCH_USER_MANAGER"));
							getUserManager.setLong(1, managerId);
							managerResultSet = getUserManager.executeQuery();
							if (managerResultSet.first()) {
								Manager manager = Manager.newBuilder()
										.setUserId(managerResultSet.getLong("user_id"))
										.setUsername(managerResultSet.getString("user_name"))
										.setFullname(managerResultSet.getString("full_name"))
										.setEmail(managerResultSet.getString("email_id"))
										.setPhone(managerResultSet.getString("phone"))
										.setCountryCode(managerResultSet.getString("country_code"))
										.build();
								user.setManager(manager);
							} else {
								con.rollback();
								log.info("Error in fetching manager details...");
							}
						} else {
							user.setManager(Manager.newBuilder().build());
						}
						Roles.Builder roles = Roles.newBuilder();
						while (rolesResultSet.next()) {
							roles
							.setRoleId(rolesResultSet.getLong("role_id"))
							.setRoleDesc(rolesResultSet.getString("role_desc"));
						}
						user.addRoles(roles);
						usersList.addUser(user);
					} else {
						con.rollback();
						log.info("User roles not found.");
					}
				}
				// publish user list to the ams client
				 published = UserPublishUtil.mqttPublishUsers(MqttUtil.mqttAsyncClient, usersList.build().toByteArray(), clientId);
			} else {
				// user(s) already synced
				 published = PublishResponse.mqttPublishMessage(MqttUtil.mqttAsyncClient, ResponseMessage.newBuilder()
						 .setMessage("User(s) already synced to device.")
						 .build()
						 .toByteArray(), clientId, "users/");
			}
			if (published) {
				log.info("Users published to ams successfully..");
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
			DBConnection.getInstance().closeConnection(con, getUserRoles);
			DBConnection.getInstance().closeConnection(con, getUsersStatement);
			DBConnection.getInstance().closeConnection(con, existingAmsUserSync);
			DBConnection.getInstance().closeConnection(con, getUserManager);
		}
	}

	public static void saveAmsUserSyncLog(long clinetId, Timestamp timestamp) {
		final Connection con = DBConnection.getInstance().getConnection();
		PreparedStatement updateSyncLogStmt = null;
		PreparedStatement existingAmsUserSync = null;
		ResultSet amsResultSets = null;
		int result = 0;
		try {
			con.setAutoCommit(false);
			existingAmsUserSync = con.prepareStatement(properties.getProperty("QUERY_FETCH_EXISTING_AMS_USER_SYNC_LOG"));
			existingAmsUserSync.setLong(1, clinetId);
			amsResultSets = existingAmsUserSync.executeQuery();
			if (amsResultSets.first()) {
				// update existing record
				updateSyncLogStmt = con.prepareStatement(properties.getProperty("UPDATE_AMS_USER_SYNC_LOG"));
				updateSyncLogStmt.setTimestamp(1, timestamp);
				updateSyncLogStmt.setLong(2, clinetId);
			} else {
				// new insert in ams_user_sync table
				updateSyncLogStmt = con.prepareStatement(properties.getProperty("INSERT_AMS_USER_SYNC_LOG"));
				updateSyncLogStmt.setLong(1, clinetId);
				updateSyncLogStmt.setTimestamp(2, timestamp);
			}
			log.info(updateSyncLogStmt.toString());
			result = updateSyncLogStmt.executeUpdate();
			if (result == 1) {
				con.commit();
				log.info("AMS user sync info successfully saved.");
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
			DBConnection.getInstance().closeConnection(con, existingAmsUserSync);
		}
	}
}