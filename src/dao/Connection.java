package dao;

import java.sql.DriverManager;
import java.sql.SQLException;

public class Connection {
    private static Connection instance;
    private java.sql.Connection connection;

    private Connection() { 
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost/NoteTakingApp","root","");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Problem connecting to the database: " + e.getMessage());
        }
    }

    public static Connection getInstance() {
        if (instance == null)
            instance = new Connection();
        return instance;
    }

    public java.sql.Connection getConnection() {
        return connection;
    }
}
