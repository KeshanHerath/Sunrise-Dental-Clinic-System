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
            
            String numberCheckSql =
                    "SELECT appointment_number FROM appointments " +
                    "WHERE appointment_number = ?";

            try (PreparedStatement numberCheckStatement =
                    connection.prepareStatement(numberCheckSql)) {

                numberCheckStatement.setString(1, appointmentNumber);

                try (ResultSet numberResult =
                        numberCheckStatement.executeQuery()) {

                    if (numberResult.next()) {
                        System.out.println("Appointment number already exists.");
                        connection.rollback();
                        return false;
                    }
                }
            }
            
            String checkTime = appointmentTime;

            if (checkTime.length() == 5) {
                checkTime = checkTime + ":00";
            }

            String checkSql =
                    "SELECT appointment_number FROM appointments " +
                    "WHERE dentist_id = ? " +
                    "AND appointment_date = ? " +
                    "AND appointment_time = ?";

            try (PreparedStatement checkStatement =
                    connection.prepareStatement(checkSql)) {

                checkStatement.setInt(1, dentistId);
                checkStatement.setDate(
                        2, Date.valueOf(appointmentDate));
                checkStatement.setTime(
                        3, Time.valueOf(checkTime));

                try (ResultSet checkResult =
                        checkStatement.executeQuery()) {

                    if (checkResult.next()) {
                        System.out.println(
                                "This dentist is already booked at that date and time.");
                        connection.rollback();
                        return false;
                    }
                }
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
    public static String[] findAppointment(String appointmentNumber) {

        String sql =
                "SELECT a.appointment_number, " +
                "p.patient_name, p.address, p.contact_number, " +
                "d.dentist_name, t.treatment_type, " +
                "a.appointment_date, a.appointment_time, a.status " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "JOIN dentists d ON a.dentist_id = d.dentist_id " +
                "JOIN treatments t ON a.treatment_id = t.treatment_id " +
                "WHERE a.appointment_number = ?";

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, appointmentNumber);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    return new String[] {
                        result.getString("appointment_number"),
                        result.getString("patient_name"),
                        result.getString("address"),
                        result.getString("contact_number"),
                        result.getString("dentist_name"),
                        result.getString("treatment_type"),
                        result.getString("appointment_date"),
                        result.getString("appointment_time"),
                        result.getString("status")
                    };
                }
            }

        } catch (SQLException e) {
            System.out.println("Error searching for appointment.");
            e.printStackTrace();
        }

        return null;
    }
}