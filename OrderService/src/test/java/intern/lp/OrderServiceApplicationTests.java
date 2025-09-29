package intern.lp;

import java.beans.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OrderServiceApplicationTests {

	@Test
	void testSQLInjection() throws SQLException {
		String orderid = "1 ";

		Connection conn = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "Root1234");

		String query = "SELECT * FROM orders WHERE id = '" + orderid + "'";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);

		System.out.println("Executed query: " + query);

	}

}
