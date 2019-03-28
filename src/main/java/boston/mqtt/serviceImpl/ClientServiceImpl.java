package boston.mqtt.serviceImpl;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import boston.mqtt.conn.manager.DBConnection;

public class ClientServiceImpl {

	private static final Logger logger = LogManager.getLogger(ClientServiceImpl.class);

	public JSONObject saveClientToDB(JSONObject connObject) {
		final Connection con = DBConnection.getInstance().getConnection();
		JSONObject response = new JSONObject();
		PreparedStatement ps = null;
		int result = 0;
		try {
			String query = "insert into client(client_id, conn_ack, start_time) values(?,?,?)";
			ps = con.prepareStatement(query);
			ps.setString(1, connObject.getString("clientid"));
			ps.setInt(2, connObject.getInt("connack"));
			ps.setLong(3, connObject.getLong("ts"));
			logger.info(ps.toString());
			result = ps.executeUpdate();
			if (result == 1) {
				response.put("success", "done");
			} else {
				response.put("error", "error");			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		} finally {
			DBConnection.getInstance().closeConnection(con, ps);
		}
		return response;
	}

	public JSONObject updateClientEndTime(long endTime, String clientId) {
		final Connection con = DBConnection.getInstance().getConnection();
		JSONObject response = new JSONObject();
		PreparedStatement ps = null;
		int result = 0;
		try {
			String query = "UPDATE client SET end_time=? where clientid=? ORDER BY id DESC LIMIT 1";
			ps = con.prepareStatement(query);
			ps.setLong(1, endTime);
			ps.setString(2, clientId);
			logger.info(ps.toString());
			result = ps.executeUpdate();
			System.out.println(result);
			if (result == 1) {
				response.put("success", "done");
			} else {
				response.put("error", "error");			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		} finally {
			DBConnection.getInstance().closeConnection(con, ps);
		}
		return response;
	}
}
