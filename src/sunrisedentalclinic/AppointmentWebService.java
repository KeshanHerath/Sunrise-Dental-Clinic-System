package sunrisedentalclinic;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class AppointmentWebService {

    public static void main(String[] args) {

        try {
            HttpServer server =
                    HttpServer.create(new InetSocketAddress(8080), 0);

            server.createContext(
                    "/appointment",
                    AppointmentWebService::handleAppointment
            );

            server.setExecutor(null);
            server.start();

            System.out.println(
                    "Sunrise Dental Clinic Web Service is running."
            );

            System.out.println(
                    "Open: http://localhost:8080/appointment?number=003"
            );

        } catch (IOException e) {

            System.out.println(
                    "Could not start the web service."
            );

            e.printStackTrace();
        }
    }

    private static void handleAppointment(
            HttpExchange exchange) throws IOException {

        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("GET")) {

            sendResponse(
                    exchange,
                    405,
                    "{\"error\":\"Method not allowed\"}"
            );

            return;
        }

        String query =
                exchange.getRequestURI().getRawQuery();

        String appointmentNumber =
                getParameter(query, "number");

        if (appointmentNumber == null ||
                appointmentNumber.trim().isEmpty()) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Appointment number is required\"}"
            );

            return;
        }

        String[] details =
                AppointmentDAO.findAppointment(
                        appointmentNumber.trim());

        if (details == null) {

            sendResponse(
                    exchange,
                    404,
                    "{\"error\":\"Appointment not found\"}"
            );

            return;
        }

        String response =
                "{\n" +
                "  \"appointmentNumber\": \"" +
                escapeJson(details[0]) + "\",\n" +

                "  \"patientName\": \"" +
                escapeJson(details[1]) + "\",\n" +

                "  \"address\": \"" +
                escapeJson(details[2]) + "\",\n" +

                "  \"contactNumber\": \"" +
                escapeJson(details[3]) + "\",\n" +

                "  \"dentistName\": \"" +
                escapeJson(details[4]) + "\",\n" +

                "  \"treatmentType\": \"" +
                escapeJson(details[5]) + "\",\n" +

                "  \"appointmentDate\": \"" +
                escapeJson(details[6]) + "\",\n" +

                "  \"appointmentTime\": \"" +
                escapeJson(details[7]) + "\",\n" +

                "  \"status\": \"" +
                escapeJson(details[8]) + "\"\n" +
                "}";

        sendResponse(exchange, 200, response);
    }

    private static String getParameter(
            String query,
            String key) {

        if (query == null) {
            return null;
        }

        String[] pairs = query.split("&");

        for (String pair : pairs) {

            String[] parts = pair.split("=", 2);

            if (parts.length == 2) {

                String name =
                        URLDecoder.decode(
                                parts[0],
                                StandardCharsets.UTF_8);

                String value =
                        URLDecoder.decode(
                                parts[1],
                                StandardCharsets.UTF_8);

                if (name.equals(key)) {
                    return value;
                }
            }
        }

        return null;
    }

    private static void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String response) throws IOException {

        byte[] data =
                response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

        exchange.sendResponseHeaders(
                statusCode,
                data.length
        );

        try (OutputStream output =
                     exchange.getResponseBody()) {

            output.write(data);
        }
    }

    private static String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
