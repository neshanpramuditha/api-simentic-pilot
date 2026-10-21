import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

/**
 * Prints every row of the loans table as one JSON array (id, member_id, book_id, status, requested_at, issued_at,
 * returned_at). Ground truth for the final database state, independent of the semantic validator's code.
 * Run while the API is still up (it joins the API's AUTO_SERVER database):
 * java -cp h2-2.4.240.jar DumpLoans.java "jdbc:h2:file:<db path>;AUTO_SERVER=TRUE"
 */
public class DumpLoans {

	public static void main(String[] args) throws Exception {
		StringBuilder json = new StringBuilder("[");
		try (Connection c = DriverManager.getConnection(args[0], "sa", "");
				ResultSet rs = c.createStatement().executeQuery(
						"SELECT id, member_id, book_id, status, requested_at, issued_at, returned_at FROM loans ORDER BY requested_at, id")) {
			while (rs.next()) {
				if (json.length() > 1) {
					json.append(',');
				}
				json.append("{\"id\":\"").append(rs.getString(1)).append("\",\"memberId\":").append(rs.getLong(2))
						.append(",\"bookId\":").append(rs.getLong(3)).append(",\"status\":\"").append(rs.getString(4)).append('"');
				String[] names = { "requestedAt", "issuedAt", "returnedAt" };
				for (int i = 0; i < names.length; i++) {
					Object value = rs.getObject(5 + i);
					json.append(",\"").append(names[i]).append("\":").append(value == null ? "null" : "\"" + value + "\"");
				}
				json.append('}');
			}
		}
		System.out.println(json.append(']'));
	}

}
