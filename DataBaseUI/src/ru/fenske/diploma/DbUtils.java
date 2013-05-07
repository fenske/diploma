package ru.fenske.diploma;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DbUtils {
	public static void insertDocument(Document doc) throws ClassNotFoundException, SQLException {
		String url = "jdbc:postgresql://localhost:5432/enterprise";;
		Properties properties = new Properties();
		properties.setProperty("user", "postgres");
		properties.setProperty("password", "1234");
		Class.forName("org.postgresql.Driver");
		Connection connection = DriverManager.getConnection(url, properties);
		try {
			Statement insert = connection.createStatement();
			insert.executeUpdate("insert into documents (name, author, uri) values ('"+doc.getName()+"','"+doc.getAuthor()+"','"+doc.getUri() + "')");					
		} finally {
			connection.close();
		}
	}	
}
