package sunrisedentalclinic;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;

public class AppointmentDAO {

    public static boolean saveAppointment(
            String appointmentNumber,
            String patientName,
            String address,
            String contactNumber,
            String dentistName,
            String treatmentType,
            String appointmentDate,
            String appointmentTime,
            String username) {

        Connection connection = null;

        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);

            // Find dentist ID
            int dentistId = findDentistId(connection, dentistName);

            // Find treatment ID
            int treatmentId = findTreatmentId(connection, treatmentType);

            // Find logged-in user ID
            int userId = findUserId(connection, username);

            if (dentistId == -1) {
                System.out.println("Dentist not found.");
                connection.rollback();
                return false;
            }

            if (treatmentId == -1) {
                System.out.println("Treatment not found.");
                connection.rollback();
                return false;
            }

            if (userId == -1) {
                System.out.println("User not found.");
                connection.rollback();
                return false;
            }

            // Insert patient
            String patientSql =
                    "INSERT INTO patients "
                  + "(patient_name, address, contact_number) "
                  + "VALUES (?, ?, ?)";

            int patientId;

            try (PreparedStatement patientStatement =
                    connection.prepareStatement(
                            patientSql,
                            Statement.RETURN_GENERATED_KEYS)) {

                patientStatement.setString(1, patientName);
                patientStatement.setString(2, address);
                patientStatement.setString(3, contactNumber);

                patientStatement.executeUpdate();

                try (ResultSet keys = patientStatement.getGeneratedKeys()) {

                    if (keys.next()) {
                        patientId = keys.getInt(1);
                    } else {
                        connection.rollback();
                        return false;
                    }
                }
            }

            // Insert appointment
            String appointmentSql =
                    "INSERT INTO appointments "
                  + "(appointment_number, patient_id, dentist_id, "
                  + "treatment_id, user_id, appointment_date, "
                  + "appointment_time, status) "
                  + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement appointmentStatement =
                    connection.prepareStatement(appointmentSql)) {

                appointmentStatement.setString(1, appointmentNumber);
                appointmentStatement.setInt(2, patientId);
                appointmentStatement.setInt(3, dentistId);
                appointmentStatement.setInt(4, treatmentId);
                appointmentStatement.setInt(5, userId);
                appointmentStatement.setDate(
                        6, Date.valueOf(appointmentDate));

                if (appointmentTime.length() == 5) {
                    appointmentTime = appointmentTime + ":00";
                }

                appointmentStatement.setTime(
                        7, Time.valueOf(appointmentTime));

                appointmentStatement.setString(8, "Scheduled");

                appointmentStatement.executeUpdate();
            }

            connection.commit();
            return true;

        } catch (SQLException | IllegalArgumentException e) {

            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    rollbackError.printStackTrace();
                }
            }

            System.out.println("Could not save appointment.");
            System.out.println(
                    "Check the appointment number, dentist, treatment, date and time.");

            return false;

        } finally {

            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static int findDentistId(
            Connection connection, String dentistName)
            throws SQLException {

        String sql =
                "SELECT dentist_id FROM dentists "
              + "WHERE dentist_name = ?";

        try (PreparedStatement statement =
                connection.prepareStatement(sql)) {

            statement.setString(1, dentistName);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return result.getInt("dentist_id");
                }
            }
        }

        return -1;
    }

    private static int findTreatmentId(
            Connection connection, String treatmentType)
            throws SQLException {

    	String sql =
    	        "SELECT treatment_id FROM treatments "
    	      + "WHERE LOWER(TRIM(treatment_type)) = LOWER(TRIM(?))";
    	
        try (PreparedStatement statement =
                connection.prepareStatement(sql)) {

        	statement.setString(1, treatmentType.trim());

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return result.getInt("treatment_id");
                }
            }
        }

        return -1;
    }

    private static int findUserId(
            Connection connection, String username)
            throws SQLException {

        String sql =
                "SELECT user_id FROM users WHERE username = ?";

        try (PreparedStatement statement =
                connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return result.getInt("user_id");
                }
            }
        }

        return -1;
    }
}