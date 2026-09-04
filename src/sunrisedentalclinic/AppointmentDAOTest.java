package sunrisedentalclinic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class AppointmentDAOTest {

    @Test
    void existingAppointmentShouldBeFound() {

        String[] result =
                AppointmentDAO.findAppointment("003");

        assertNotNull(result);
        assertEquals("003", result[0]);
    }

    @Test
    void invalidAppointmentShouldReturnNull() {

        String[] result =
                AppointmentDAO.findAppointment("999");

        assertNull(result);
    }
}
