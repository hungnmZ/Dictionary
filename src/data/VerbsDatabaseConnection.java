package data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

public class VerbsDatabaseConnection {
    public static Connection getConnection() {
        try {
            String path = new File("").getAbsolutePath();
            path = path.concat("/src/data/verbs.db");

            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:" + path);
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
}
