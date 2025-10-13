package intern.lp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OrderServiceApplicationTests {

	// 🔴 VULNERABLE CODE - Semgrep should detect this
	@Test
	void testSQLInjection_Vulnerable() throws SQLException {
		String userInput = "1'; DROP TABLE users; --";

		Connection conn = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "Root1234");

		// 🔴 SQL INJECTION - String concatenation
		String query = "SELECT * FROM users WHERE id = '" + userInput + "'";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);

		while (rs.next()) {
			System.out.println(rs.getString("username"));
		}

		rs.close();
		stmt.close();
		conn.close();
	}

	// 🔴 VULNERABLE CODE - Direct user input in SQL
	@Test
	void testSQLInjection_Vulnerable2() throws SQLException {
		String username = "admin' OR '1'='1";
		String password = "password";

		Connection conn = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");

		// 🔴 SQL INJECTION in login query
		String query = "SELECT * FROM users WHERE username = '" + username +
				"' AND password = '" + password + "'";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);

		if (rs.next()) {
			System.out.println("Login successful - VULNERABLE TO SQL INJECTION");
		}

		rs.close();
		stmt.close();
		conn.close();
	}

	// 🔴 VULNERABLE CODE - Order by with user input
	@Test
	void testSQLInjection_OrderBy() throws SQLException {
		String userInput = "id; DROP TABLE products; --";

		Connection conn = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");

		// 🔴 SQL INJECTION in ORDER BY clause
		String query = "SELECT * FROM products ORDER BY " + userInput;
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);

		while (rs.next()) {
			System.out.println(rs.getString("name"));
		}

		rs.close();
		stmt.close();
		conn.close();
	}

	// 🔴 VULNERABLE CODE - Multiple concatenations
	@Test
	void testSQLInjection_MultipleParams() throws SQLException {
		String category = "electronics' OR '1'='1";
		String price = "100";

		Connection conn = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");

		// 🔴 SQL INJECTION with multiple parameters
		String query = "SELECT * FROM products WHERE category = '" + category +
				"' AND price < " + price;
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);

		while (rs.next()) {
			System.out.println(rs.getString("name"));
		}

		rs.close();
		stmt.close();
		conn.close();
	}

	// 🔴 VULNERABLE CODE - Using string formatting
	@Test
	void testSQLInjection_StringFormat() throws SQLException {
		String userId = "1'; UPDATE users SET admin=true WHERE id=1; --";

		Connection conn = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");

		// 🔴 SQL INJECTION using String.format
		String query = String.format("SELECT * FROM users WHERE id = '%s'", userId);
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);

		while (rs.next()) {
			System.out.println(rs.getString("username"));
		}

		rs.close();
		stmt.close();
		conn.close();
	}

	// 🔴 VULNERABLE CODE - Using StringBuilder
	@Test
	void testSQLInjection_StringBuilder() throws SQLException {
		String searchTerm = "test'; DELETE FROM logs; --";

		Connection conn = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");

		// 🔴 SQL INJECTION using StringBuilder
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM logs WHERE message LIKE '%");
		sb.append(searchTerm);
		sb.append("%'");

		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sb.toString());

		while (rs.next()) {
			System.out.println(rs.getString("message"));
		}

		rs.close();
		stmt.close();
		conn.close();
	}

	// 🔴 VULNERABLE CODE - Complex query with multiple injections
	@Test
	void testSQLInjection_ComplexQuery() throws SQLException {
		String id = "1";
		String name = "John' OR '1'='1";
		String email = "test@example.com";

		Connection conn = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");

		// 🔴 MULTIPLE SQL INJECTIONS in complex query
		String query = "INSERT INTO users (id, name, email) VALUES (" + id +
				", '" + name + "', '" + email + "')";
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(query);

		stmt.close();
		conn.close();
	}

	// 🔴 VULNERABLE CODE - UNION based injection
	@Test
	void testSQLInjection_Union() throws SQLException {
		String userInput = "1' UNION SELECT username, password FROM users --";

		Connection conn = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");

		// 🔴 UNION SQL INJECTION
		String query = "SELECT name, description FROM products WHERE id = '" + userInput + "'";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);

		while (rs.next()) {
			System.out.println("Username: " + rs.getString(1) + ", Password: " + rs.getString(2));
		}

		rs.close();
		stmt.close();
		conn.close();
	}

	// 🔴 VULNERABLE CODE - Batch SQL injection
	@Test
	void testSQLInjection_Batch() throws SQLException {
		String ids = "1, 2, 3; DROP TABLE customers; --";

		Connection conn = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");

		// 🔴 BATCH SQL INJECTION
		String query = "DELETE FROM customers WHERE id IN (" + ids + ")";
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(query);

		stmt.close();
		conn.close();
	}

	// 🔴 VULNERABLE CODE - LIKE clause injection
	@Test
	void testSQLInjection_LikeClause() throws SQLException {
		String search = "%'; DROP TABLE products; --";

		Connection conn = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");

		// 🔴 SQL INJECTION in LIKE clause
		String query = "SELECT * FROM products WHERE name LIKE '" + search + "'";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);

		while (rs.next()) {
			System.out.println(rs.getString("name"));
		}

		rs.close();
		stmt.close();
		conn.close();
	}
}