package sunrisedentalclinic;

import java.util.Scanner;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class SunriseDentalClinic {
	static ArrayList<String> appointments = new ArrayList<String>();
	
	static String currentUsername;
    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);

        int attempts = 0;
        boolean loginSuccessful = false;

        System.out.println("========================================");
        System.out.println("       SUNRISE DENTAL CLINIC");
        System.out.println("        APPOINTMENT SYSTEM");
        System.out.println("========================================");

        while (attempts < 3) {

            System.out.print("\nEnter Username: ");
            String username = input.nextLine();

            System.out.print("Enter Password: ");
            String password = input.nextLine();

            if (UserDAO.validateLogin(username, password)) {
            	
                loginSuccessful = true;
                currentUsername = username;
                   
                System.out.println("\nLogin Successful!");
                System.out.println("Welcome to Sunrise Dental Clinic.");

                break;

            } else {

                attempts++;

                System.out.println("\nInvalid username or password.");

                if (attempts < 3) {
                    System.out.println(
                        "Attempts remaining: " + (3 - attempts)
                    );
                }
            }
        }

        if (!loginSuccessful) {

            System.out.println("\nToo many failed attempts.");
            System.out.println("System will now close.");

        } else {

            showMainMenu(input);
        }

        input.close();
    }


    // Main menu
    public static void showMainMenu(Scanner input) {

        int choice;

        do {

            System.out.println("\n========================================");
            System.out.println("              MAIN MENU");
            System.out.println("========================================");
            System.out.println("1. Register New Appointment");
            System.out.println("2. Display Appointment Details");
            System.out.println("3. Calculate and Print Bill");
            System.out.println("4. Help");
            System.out.println("5. Exit");
            System.out.println("========================================");

            System.out.print("Enter your choice: ");

            choice = input.nextInt();
            input.nextLine();

            switch (choice) {

                case 1:
                	registerAppointment(input);
                    break;

                case 2:
                    displayAppointment(input);
                    break;

                case 3:
                    calculateBill(input);
                    break;

                case 4:
                    System.out.println(
                        "\nHelp selected."
                    );
                    break;

                case 5:
                    System.out.println(
                        "\nThank you for using Sunrise Dental Clinic."
                    );
                    System.out.println("System closed.");
                    break;

                default:
                    System.out.println(
                        "\nInvalid choice. Please enter 1-5."
                    );
            }

        } while (choice != 5);
    }
    
    private static void calculateBill(Scanner input) {

        System.out.println("\n========================================");
        System.out.println("          CALCULATE PATIENT BILL");
        System.out.println("========================================");

        System.out.print("Enter Appointment Number: ");
        String appointmentNumber = input.nextLine();

        double consultationFee = 2000.00;

        String[] bill = BillDAO.generateBill(
                appointmentNumber,
                consultationFee
        );

        if (bill == null) {
            System.out.println("Bill could not be generated.");
            return;
        }

        System.out.println("\n========================================");
        System.out.println("            PATIENT BILL");
        System.out.println("========================================");

        System.out.println("Appointment Number : " + bill[0]);
        System.out.println("Patient Name       : " + bill[1]);
        System.out.println("Treatment Type     : " + bill[2]);
        System.out.println("Consultation Fee   : Rs. " + bill[3]);
        System.out.println("Treatment Cost     : Rs. " + bill[4]);
        System.out.println("----------------------------------------");
        System.out.println("Total Amount       : Rs. " + bill[5]);

        System.out.println("========================================");
    }
    
    	

	public static void registerAppointment(Scanner input) {

        System.out.println("\n========================================");
        System.out.println("       REGISTER NEW APPOINTMENT");
        System.out.println("========================================");

        System.out.print("Appointment Number : ");
        String appointmentNumber = input.nextLine();

        System.out.print("Patient Name       : ");
        String patientName = input.nextLine();

        System.out.print("Address            : ");
        String address = input.nextLine();

        System.out.print("Contact Number     : ");
        String contactNumber = input.nextLine();

        System.out.print("Dentist Name       : ");
        String dentistName = input.nextLine();

        System.out.print("Treatment Type     : ");
        String treatmentType = input.nextLine();

        System.out.print("Appointment Date   : ");
        String appointmentDate = input.nextLine();

        System.out.print("Appointment Time   : ");
        String appointmentTime = input.nextLine();
        
        if (appointmentNumber.trim().isEmpty() ||
        	    patientName.trim().isEmpty() ||
        	    address.trim().isEmpty() ||
        	    contactNumber.trim().isEmpty() ||
        	    dentistName.trim().isEmpty() ||
        	    treatmentType.trim().isEmpty() ||
        	    appointmentDate.trim().isEmpty() ||
        	    appointmentTime.trim().isEmpty()) {

        	    System.out.println("\nAll fields are required.");
        	    System.out.println("Appointment could not be registered.");
        	    return;
        	}
        
        try {
            LocalDate.parse(appointmentDate);
            LocalTime.parse(appointmentTime);

        } catch (DateTimeParseException e) {

            System.out.println("\nInvalid date or time format.");
            System.out.println("Use date format: YYYY-MM-DD");
            System.out.println("Use time format: HH:MM");
            System.out.println("Appointment could not be registered.");
            return;
        }
        
        if (!contactNumber.matches("\\d{10}")) {
            System.out.println("\nInvalid contact number.");
            System.out.println("Contact number must contain exactly 10 digits.");
            System.out.println("Appointment could not be registered.");
            return;
        } 
        
        boolean saved = AppointmentDAO.saveAppointment(
                appointmentNumber,
                patientName,
                address,
                contactNumber,
                dentistName,
                treatmentType,
                appointmentDate,
                appointmentTime,
                currentUsername
        );

        if (!saved) {
            System.out.println("\nAppointment could not be registered.");
            return;
        }
        String appointmentData = appointmentNumber + "|" +
                patientName + "|" +
                address + "|" +
                contactNumber + "|" +
                dentistName + "|" +
                treatmentType + "|" +
                appointmentDate + "|" +
                appointmentTime;

        appointments.add(appointmentData);

        System.out.println("\n========================================");
        System.out.println("   APPOINTMENT REGISTERED SUCCESSFULLY");
        System.out.println("========================================");

        System.out.println("Appointment Number : " + appointmentNumber);
        System.out.println("Patient Name       : " + patientName);
        System.out.println("Address            : " + address);
        System.out.println("Contact Number     : " + contactNumber);
        System.out.println("Dentist Name       : " + dentistName);
        System.out.println("Treatment Type     : " + treatmentType);
        System.out.println("Appointment Date   : " + appointmentDate);
        System.out.println("Appointment Time   : " + appointmentTime);
    }
    
	private static void displayAppointment(Scanner input) {

	    System.out.println("\n========================================");
	    System.out.println("        DISPLAY APPOINTMENT DETAILS");
	    System.out.println("========================================");

	    System.out.print("Enter Appointment Number: ");
	    String searchNumber = input.nextLine();

	    String[] details = AppointmentDAO.findAppointment(searchNumber);

	    if (details == null) {
	        System.out.println("Appointment not found.");
	        return;
	    }

	    System.out.println("\nAppointment Found!");
	    System.out.println("Appointment Number : " + details[0]);
	    System.out.println("Patient Name       : " + details[1]);
	    System.out.println("Address            : " + details[2]);
	    System.out.println("Contact Number     : " + details[3]);
	    System.out.println("Dentist Name       : " + details[4]);
	    System.out.println("Treatment Type     : " + details[5]);
	    System.out.println("Appointment Date   : " + details[6]);
	    System.out.println("Appointment Time   : " + details[7]);
	    System.out.println("Status             : " + details[8]);
	}
}