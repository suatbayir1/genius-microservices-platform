package com.iotiq.unit.application.service;

import com.iotiq.api.dto.CreateUserRequestDTO;
import com.iotiq.application.service.UserCreateService;
import com.iotiq.domain.model.UserProfile;
import com.iotiq.domain.repository.UserRepository;
import com.iotiq.dto.UserProfileResponseDTO;
import com.iotiq.exception.UserAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserCreateServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserCreateService userCreateService;

    @Test
    void createUser_shouldReturnResponse_whenSuccess() {
        // given
        CreateUserRequestDTO requestDTO = CreateUserRequestDTO.builder()
                .authUuid(UUID.randomUUID())
                .userName("newUserName")
                .email("newUser@example.com")
                .build();

        UserProfile savedUser = UserProfile.builder()
                .uuid(UUID.randomUUID())
                .authUuid(requestDTO.authUuid())
                .userName(requestDTO.userName())
                .email(requestDTO.email())
                .build();

        UserProfileResponseDTO responseDTO = UserProfileResponseDTO.builder()
                .uuid(savedUser.getUuid())
                .userName(savedUser.getUserName())
                .email(savedUser.getEmail())
                .avatar(savedUser.getAvatar())
                .build();

        // when
        when(userRepository.existsByUserName(requestDTO.userName())).thenReturn(false);
        when(userRepository.existsByEmail(requestDTO.email())).thenReturn(false);
        when(userRepository.save(any(UserProfile.class))).thenReturn(savedUser);

        // then
        UserProfileResponseDTO result = userCreateService.createUser(requestDTO);

        assertEquals(responseDTO.getUuid(), result.getUuid());
        assertEquals(responseDTO.getUserName(), result.getUserName());
        assertEquals(responseDTO.getEmail(), result.getEmail());

        verify(userRepository).save(any(UserProfile.class));
    }

    @Test
    void createUser_shouldThrowException_whenUserAlreadyExists() {
        CreateUserRequestDTO requestDTO = new CreateUserRequestDTO(
                UUID.randomUUID(), "existingUser", "existing@example.com"
        );

        when(userRepository.existsByUserName(requestDTO.userName())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> {
            userCreateService.createUser(requestDTO);
        });

        verify(userRepository, never()).save(any());
    }
}
