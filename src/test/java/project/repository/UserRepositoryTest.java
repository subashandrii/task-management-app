package project.repository;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import project.model.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    
    @Test
    @DisplayName("Find user by username")
    @Sql(scripts = "classpath:database/add-users-to-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/remove-users-from-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findUserByUsername_UsernameExists_ReturnsUser() {
        String username = "alice123";
        Optional<User> user = userRepository.findByUsername(username);

        Assertions.assertNotNull(user);
        Assertions.assertEquals(2L, user.get().getId());
    }
    
    @Test
    @DisplayName("Find user by non-existent username")
    @Sql(scripts = "classpath:database/add-users-to-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/remove-users-from-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findUserByUsername_UsernameDoesNotExist_ReturnsEmptyOptional() {
        String username = "alice";
        Optional<User> user = userRepository.findByUsername(username);

        Assertions.assertTrue(user.isEmpty());
    }
    
    @Test
    @DisplayName("Find user by email")
    @Sql(scripts = "classpath:database/add-users-to-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/remove-users-from-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findUserByEmail_EmailExists_ReturnsUser() {
        String email = "john@email.com";
        Optional<User> user = userRepository.findByEmail(email);

        Assertions.assertNotNull(user);
        Assertions.assertEquals(3L, user.get().getId());
    }
    
    @Test
    @DisplayName("Find user by non-existent email")
    @Sql(scripts = "classpath:database/add-users-to-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/remove-users-from-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findUserByEmail_EmailDoesNotExist_ReturnsEmptyOptional() {
        String username = "john123@email.com";
        Optional<User> user = userRepository.findByUsername(username);

        Assertions.assertTrue(user.isEmpty());
    }

    @Test
    @DisplayName("Get user by username")
    @Sql(scripts = "classpath:database/add-users-to-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/remove-users-from-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getUserByUsername_UsernameExists_ReturnsUser() {
        String username = "bob123";
        User user = userRepository.getUserByUsername(username);

        Assertions.assertNotNull(user);
        Assertions.assertEquals(1L, user.getId());
    }

    @Test
    @DisplayName("Get user by non-existent username")
    @Sql(scripts = "classpath:database/add-users-to-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/remove-users-from-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getUserByUsername_UsernameDoesNotExist_ReturnsNull() {
        String username = "bob321";
        User user = userRepository.getUserByUsername(username);

        Assertions.assertNull(user);
    }
}
