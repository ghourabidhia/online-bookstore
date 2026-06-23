package com.bnp.bookstore.repository;

import com.bnp.bookstore.entities.Role;
import com.bnp.bookstore.entities.User;
import com.bnp.bookstore.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired UserRepository userRepository;

    private User savedUser(String email) {
        User user = User.builder()
                .firstname("John").lastname("Doe")
                .email(email).password("hashed").role(Role.USER)
                .build();
        return userRepository.save(user);
    }

    @Test
    void findByEmail_existingEmail_returnsUser() {
        savedUser("john@test.com");

        Optional<User> result = userRepository.findByEmail("john@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john@test.com");
        assertThat(result.get().getFirstname()).isEqualTo("John");
    }

    @Test
    void findByEmail_unknownEmail_returnsEmpty() {
        Optional<User> result = userRepository.findByEmail("nobody@test.com");

        assertThat(result).isEmpty();
    }

    @Test
    void findByEmail_multipleUsers_returnsCorrectOne() {
        savedUser("alice@test.com");
        savedUser("bob@test.com");

        Optional<User> result = userRepository.findByEmail("alice@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("alice@test.com");
    }

    @Test
    void save_userPersistsWithGeneratedId() {
        User user = savedUser("new@test.com");

        assertThat(user.getId()).isNotNull();
        assertThat(userRepository.count()).isGreaterThan(0);
    }
}
