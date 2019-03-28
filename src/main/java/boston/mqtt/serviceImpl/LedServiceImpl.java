package boston.mqtt.serviceImpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import boston.mqtt.conn.manager.DBConnection;
import boston.mqtt.model.LedProto.LedModelProto;

@Component
public final class LedServiceImpl {

	private static final Logger logger = LogManager.getLogger(LedServiceImpl.class);

	private static StringBuilder queryBatch = null;

	private static int count = 0;

	public static void sqlQueryCounter(LedModelProto ledModelProto) throws SQLException {

		if (count == 0) {
			queryBatch = new StringBuilder("insert into led_details(counter,client_id) values");
		}
		if (count < 49) {
			queryBatch.append("('" + ledModelProto.getCounter() + "','" + ledModelProto.getClientId() + "'),");
			count++;
		} else {
			queryBatch.append("('" + ledModelProto.getCounter() + "','" + ledModelProto.getClientId() + "'),");
			queryBatch.setLength(queryBatch.length() - 1);
			saveMessage(queryBatch.toString());
			count = 0;
		}
	}

	public static void saveMessage(String queryBatch) throws SQLException {
		final Connection con = DBConnection.getInstance().getConnection();
		PreparedStatement ps = null;
		int result = 0;
		try {
			con.setAutoCommit(false);
			ps = con.prepareStatement(queryBatch);
			result = ps.executeUpdate(queryBatch);
			if (result > 0) {
				con.commit();
				logger.info("Saved to db successfully, ROWS_COUNT: " + result);
			}
		} catch (Exception e) {
			try {
				con.rollback();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			e.printStackTrace();
		} finally {
			DBConnection.getInstance().closeConnection(con, ps);
		}
	}
}