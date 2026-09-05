package sunrisedentalclinic;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AppointmentWebServiceClient {

    public static void main(String[] args) {

        try {

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                        "http://localhost:8080/appointment?number=003"))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString());

            System.out.println(
                    "SUNRISE DENTAL CLINIC WEB SERVICE CLIENT");

            System.out.println(
                    "========================================");

            System.out.println(
                    "HTTP Status: " + response.statusCode());

            System.out.println("\nAppointment Data:");

            System.out.println(response.body());

        } catch (Exception e) {

            System.out.println(
                    "Could not connect to the web service.");

            e.printStackTrace();
        }
    }
}
