package com.iotiq.unit.application.service;

import com.iotiq.application.service.GetUserService;
import com.iotiq.domain.model.UserProfile;
import com.iotiq.domain.repository.UserRepository;
import com.iotiq.dto.UserProfileResponseDTO;
import com.iotiq.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetUserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetUserService getUserService;

    @Test
    void getUserByUserName_shouldReturnResponse_whenUserExists() {
        // given
        String userName = "exampleUser";
        UUID uuid = UUID.randomUUID();
        String email = "user@example.com";

        UserProfile user = UserProfile.builder()
                .uuid(uuid)
                .userName(userName)
                .email(email)
                .authUuid(UUID.randomUUID())
                .build();

        UserProfileResponseDTO responseDTO = UserProfileResponseDTO.builder()
                .uuid(uuid)
                .userName(userName)
                .email(email)
                .build();

        // when
        when(userRepository.findByUserName(userName)).thenReturn(Optional.of(user));

        // then
        UserProfileResponseDTO result = getUserService.getUserByUserName(userName);

        assertEquals(responseDTO.getUuid(), result.getUuid());
        assertEquals(responseDTO.getUserName(), result.getUserName());
        assertEquals(responseDTO.getEmail(), result.getEmail());

        verify(userRepository).findByUserName(userName);
    }

    @Test
    void getUserByUserName_shouldThrowException_whenUserNotFound() {
        // given
        String userName = "nonExistingUser";

        // when
        when(userRepository.findByUserName(userName)).thenReturn(Optional.empty());

        // then
        assertThrows(UserNotFoundException.class, () -> {
            getUserService.getUserByUserName(userName);
        });

        verify(userRepository).findByUserName(userName);
    }
}
