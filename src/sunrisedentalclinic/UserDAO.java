package sunrisedentalclinic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public static boolean validateLogin(String username, String password) {

        String sql =
                "SELECT user_id FROM users "
              + "WHERE username = ? AND password = SHA2(?, 256)";

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, username);
            statement.setString(2, password);

            ResultSet result = statement.executeQuery();

            return result.next();

        } catch (SQLException e) {
            System.out.println("Login database error.");
            e.printStackTrace();
            return false;
        }
    }
}