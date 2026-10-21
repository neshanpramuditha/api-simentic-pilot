import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

/**
 * Prints every row of event_registration as one JSON array. Ground truth for the final database state, independent of
 * the semantic validator's code. Run while the API is up (joins its AUTO_SERVER database):
 * java -cp h2-2.4.240.jar DumpRegistrations.java "jdbc:h2:file:<db path>;AUTO_SERVER=TRUE"
 */
public class DumpRegistrations {

	public static void main(String[] args) throws Exception {
		String[] text = { "registration_id", "attendee_ref", "event_code", "state", "cancellation_reason" };
		String[] times = { "requested_at", "confirmed_at", "checked_in_at", "cancelled_at" };
		StringBuilder json = new StringBuilder("[");
		try (Connection c = DriverManager.getConnection(args[0], "sa", "");
				ResultSet rs = c.createStatement().executeQuery("SELECT * FROM event_registration ORDER BY requested_at, registration_id")) {
			while (rs.next()) {
				json.append(json.length() > 1 ? "," : "").append("{\"seats\":").append(rs.getInt("seats"));
				for (String col : text) {
					String v = rs.getString(col);
					json.append(",\"").append(col).append("\":").append(v == null ? "null" : "\"" + v.replace("\"", "\\\"") + "\"");
				}
				for (String col : times) {
					Object v = rs.getObject(col);
					json.append(",\"").append(col).append("\":").append(v == null ? "null" : "\"" + v + "\"");
				}
				json.append('}');
			}
		}
		System.out.println(json.append(']'));
	}

}
