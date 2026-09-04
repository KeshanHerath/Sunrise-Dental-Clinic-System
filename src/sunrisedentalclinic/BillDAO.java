package sunrisedentalclinic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BillDAO {

    public static String[] generateBill(
            String appointmentNumber,
            double consultationFee) {

        String searchSql =
                "SELECT p.patient_name, " +
                "t.treatment_type, t.treatment_cost " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "JOIN treatments t ON a.treatment_id = t.treatment_id " +
                "WHERE a.appointment_number = ?";

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement searchStatement =
                     connection.prepareStatement(searchSql)) {

            searchStatement.setString(1, appointmentNumber);

            try (ResultSet result =
                     searchStatement.executeQuery()) {

                if (!result.next()) {
                    System.out.println("Appointment not found.");
                    return null;
                }

                String patientName =
                        result.getString("patient_name");

                String treatmentType =
                        result.getString("treatment_type");

                double treatmentCost =
                        result.getDouble("treatment_cost");

                double totalAmount =
                        consultationFee + treatmentCost;

                String billSql =
                        "INSERT INTO bills " +
                        "(appointment_number, consultation_fee, " +
                        "treatment_cost, total_amount) " +
                        "VALUES (?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "consultation_fee = ?, " +
                        "treatment_cost = ?, " +
                        "total_amount = ?";

                try (PreparedStatement billStatement =
                         connection.prepareStatement(billSql)) {

                    billStatement.setString(
                            1, appointmentNumber);

                    billStatement.setDouble(
                            2, consultationFee);

                    billStatement.setDouble(
                            3, treatmentCost);

                    billStatement.setDouble(
                            4, totalAmount);

                    billStatement.setDouble(
                            5, consultationFee);

                    billStatement.setDouble(
                            6, treatmentCost);

                    billStatement.setDouble(
                            7, totalAmount);

                    billStatement.executeUpdate();
                }

                return new String[] {
                        appointmentNumber,
                        patientName,
                        treatmentType,
                        String.format("%.2f", consultationFee),
                        String.format("%.2f", treatmentCost),
                        String.format("%.2f", totalAmount)
                };
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error while generating bill.");

            e.printStackTrace();

            return null;
        }
    }
}