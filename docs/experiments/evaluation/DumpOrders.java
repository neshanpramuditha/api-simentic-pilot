import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

/**
 * Prints every row of the orders table of an H2 file database as one JSON array (id, status, completed_at).
 * Run on a copy of a run's database: java -cp h2-2.4.240.jar DumpOrders.java <db file path without .mv.db>
 */
public class DumpOrders {

	public static void main(String[] args) throws Exception {
		String url = "jdbc:h2:file:" + args[0].replace('\\', '/') + ";ACCESS_MODE_DATA=r";
		StringBuilder json = new StringBuilder("[");
		try (Connection c = DriverManager.getConnection(url, "sa", "");
				ResultSet rs = c.createStatement().executeQuery("SELECT id, status, completed_at FROM orders ORDER BY id")) {
			while (rs.next()) {
				if (json.length() > 1) {
					json.append(',');
				}
				Object completedAt = rs.getObject(3);
				json.append("{\"id\":").append(rs.getLong(1)).append(",\"status\":\"").append(rs.getString(2))
						.append("\",\"completedAt\":").append(completedAt == null ? "null" : "\"" + completedAt + "\"").append('}');
			}
		}
		System.out.println(json.append(']'));
	}

}
