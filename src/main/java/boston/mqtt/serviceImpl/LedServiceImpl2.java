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
public class LedServiceImpl2 {

	private static final Logger logger = LogManager.getLogger(LedServiceImpl2.class);

	private static StringBuilder queryBatch = null;

	private static int count = 0;

	public static void sqlQueryCounter(LedModelProto ledModelProto) throws SQLException {
		if (count == 0) {
			queryBatch = new StringBuilder("insert into led_details_2(counter,client_id) values");
		}
		if (count < 99) {
			queryBatch.append("('" + ledModelProto.getCounter() + "','" + ledModelProto.getClientId() + "'),");
			count++;
		} else {
			queryBatch.append("('" + ledModelProto.getCounter() + "','" + ledModelProto.getClientId() + "'),");
			queryBatch.setLength(queryBatch.length() - 1);
			saveMessage(queryBatch.toString());
			count = 0;
		}
	}

	public static void saveMessage(String queryBatch) {
		final Connection con = DBConnection.getInstance().getConnection();
		PreparedStatement ps = null;
		try {
			con.setAutoCommit(false);
			int result = 0;
			ps = con.prepareStatement(queryBatch.toString());
			logger.info(ps.toString());
			result = ps.executeUpdate();
			if (result == 1) {
				con.commit();
				logger.info("Saved to db successfully");
			} else {
				logger.info("error");
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