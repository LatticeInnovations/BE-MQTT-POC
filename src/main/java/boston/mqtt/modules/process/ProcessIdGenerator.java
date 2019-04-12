package boston.mqtt.modules.process;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import boston.mqtt.conn.manager.DBConnection;
import boston.mqtt.constants.Constants;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProcessIdGenerator {

	public static String generate() {
		final Connection con = DBConnection.getInstance().getConnection();
		PreparedStatement statement = null;
		ResultSet rs = null;
		String generatedId = null;
		try {
			statement = con.prepareStatement("select max(process_id) from process_master");
			rs = statement.executeQuery();
			if (rs.first() && rs.getString(1) != null) {
				generatedId = Constants.PROCESS_PREFIX
						+ String.format("%03d", Long.parseLong(rs.getString(1).substring(3)) + 1);
			} else {
				generatedId = Constants.PROCESS_PREFIX + String.format("%03d", new Long(1));
			}
		} catch (SQLException e) {
			log.error(Constants.EXCEPTION, e);
		} finally {
			DBConnection.getInstance().closeConnection(con, statement);
		}
		return generatedId;
	}
}
