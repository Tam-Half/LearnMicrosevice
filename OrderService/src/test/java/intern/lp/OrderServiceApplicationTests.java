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

	@Test
	void testSQLInjection() throws SQLException {
		String orderid = "1";

		// Tạo connection với H2 in-memory database
		Connection conn = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "Root1234");

		try {
			// Tạo bảng orders để test
			Statement setupStmt = conn.createStatement();
			setupStmt.execute("CREATE TABLE IF NOT EXISTS orders (id INT, name VARCHAR(50))");
			setupStmt.execute("INSERT INTO orders VALUES (1, 'Order 1')");
			setupStmt.close();

			// 🔴 SQL INJECTION VULNERABILITY - Semgrep sẽ phát hiện
			String query = "SELECT * FROM orders WHERE id = '" + orderid + "'";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			System.out.println("Executed query: " + query);

			// In kết quả
			while (rs.next()) {
				System.out.println("Order ID: " + rs.getInt("id") +
						", Name: " + rs.getString("name"));
			}

			// Đóng resources
			rs.close();
			stmt.close();

		} finally {
			conn.close();
		}
	}

	@Test
	void testSQLInjection_WithMaliciousInput() throws SQLException {
		// Giả lập input độc hại
		String maliciousInput = "1' OR '1'='1";

		Connection conn = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "Root1234");

		try {
			Statement setupStmt = conn.createStatement();
			setupStmt.execute("CREATE TABLE IF NOT EXISTS orders (id INT, name VARCHAR(50))");
			setupStmt.execute("INSERT INTO orders VALUES (1, 'Order 1')");
			setupStmt.execute("INSERT INTO orders VALUES (2, 'Order 2')");
			setupStmt.close();

			String query = "SELECT * FROM orders WHERE id = '" + maliciousInput + "'";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			System.out.println("Malicious query: " + query);

			int count = 0;
			while (rs.next()) {
				count++;
				System.out.println("Order ID: " + rs.getInt("id"));
			}
			System.out.println("Total orders returned: " + count + " (Should be 1, but got all!)");

			rs.close();
			stmt.close();

		} finally {
			conn.close();
		}
	}
}