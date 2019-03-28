package boston.mqtt.conn.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class DBConnection {

	private static DBConnection dbConnection;
	private static HikariDataSource dataSource;

	private DBConnection() {
	}

	// singleton
	public static DBConnection getInstance() {
		if (dbConnection == null) {
			synchronized (DBConnection.class) {
				if (dbConnection == null) {
					dbConnection = new DBConnection();
				}
			}
		}
		return dbConnection;
	}

	private static HikariDataSource getDatasource() {
		if (dataSource == null) {
			HikariConfig hikariConfig = new HikariConfig();
			hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
			hikariConfig.setJdbcUrl("jdbc:mysql://localhost:3306/millipore_db?useSSL=false");
			hikariConfig.setUsername("root");
			// hikariConfig.setPassword(password);
			hikariConfig.setMaximumPoolSize(12);
			hikariConfig.setConnectionTestQuery("SELECT 1");
			hikariConfig.setPoolName("springHikariCP");
			hikariConfig.setAutoCommit(true);
			hikariConfig.setConnectionTimeout(20000);
			hikariConfig.setMinimumIdle(5);
			hikariConfig.setMaxLifetime(1200000);
			hikariConfig.setIdleTimeout(300000);
			dataSource = new HikariDataSource(hikariConfig);
		}
		return dataSource;
	}

	public Connection getConnection() {
		getDatasource();
		try {
			return dataSource.getConnection();
		} catch (SQLException e) {
			log.error("Connection failure...", e);
			return null;
		}
	}

	public static void closeDataSource() {
		log.info("Trying to close datasource.");
		try {
			if (!dataSource.isClosed()) {
				dataSource.close();
			}
		} catch (Exception e) {
		}
	}

	public void closeConnection(Connection con, PreparedStatement pmst) {
		try {
			if (!con.isClosed()) {
				con.close();
			}
			if (!pmst.isClosed()) {
				pmst.close();
			}
		} catch (Exception e) {
			log.info(e.getMessage());
		}
	}

	public void closeStatement(Statement stmt) {
		try {
			if (!stmt.isClosed()) {
				stmt.close();
			}
		} catch (Exception e) {
			log.info(e.getMessage());
		}
	}
}
