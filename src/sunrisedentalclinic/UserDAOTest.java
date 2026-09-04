package sunrisedentalclinic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class UserDAOTest {

    @Test
    void validLoginShouldReturnTrue() {

        boolean result =
                UserDAO.validateLogin("admin", "Admin@123");

        assertTrue(result);
    }

    @Test
    void invalidLoginShouldReturnFalse() {

        boolean result =
                UserDAO.validateLogin("admin", "wrong123");

        assertFalse(result);
    }
}