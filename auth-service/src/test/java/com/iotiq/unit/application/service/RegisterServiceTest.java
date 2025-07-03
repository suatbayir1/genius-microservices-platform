package com.iotiq.unit.application.service;

import com.iotiq.api.dto.CreateUserRequestDTO;
import com.iotiq.api.dto.RegisterRequestDTO;
import com.iotiq.api.dto.RegisterResponseDTO;
import com.iotiq.application.service.RegisterService;
import com.iotiq.application.validator.RegisterValidator;
import com.iotiq.domain.exception.PasswordNotMatchException;
import com.iotiq.domain.model.Auth;
import com.iotiq.domain.repository.AuthRepository;
import com.iotiq.exception.UserAlreadyExistsException;
import com.iotiq.infrastructure.client.UserClient;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegisterServiceTest {
    @Mock
    private AuthRepository authRepository;

    @Mock
    private UserClient userClient;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RegisterValidator registerValidator;

    @InjectMocks
    private RegisterService registerService;

    @Test
    void register_shouldReturnResponse_whenSuccess() {
        RegisterRequestDTO requestDTO = new RegisterRequestDTO(
                "user",
                "pass",
                "pass",
                "email@example.com");

        Auth auth = Auth.builder()
                .uuid(UUID.randomUUID())
                .userName("user")
                .email("email@example.com")
                .build();

        RegisterResponseDTO responseDTO = new RegisterResponseDTO(
                auth.getUuid(),
                auth.getUserName(),
                auth.getEmail(),
                auth.getRole()
        );

        doNothing().when(registerValidator).validate(requestDTO);
        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");
        when(authRepository.save(any(Auth.class))).thenReturn(auth);
        when(userClient.createUser(any(CreateUserRequestDTO.class)))
                .thenReturn(ResponseEntity.ok(null));

        RegisterResponseDTO response = registerService.register(requestDTO);

        assertNotNull(response);
        assertEquals(responseDTO.getUuid(), response.getUuid());
        verify(registerValidator).validate(requestDTO);
        verify(authRepository).save(any(Auth.class));
        verify(userClient).createUser(any(CreateUserRequestDTO.class));
    }

    @Test
    void register_shouldThrowPasswordNotMatchException_whenValidatorFails() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
                "user",
                "pass",
                "wrongPass",
                "email@example.com"
        );

        doThrow(new PasswordNotMatchException()).when(registerValidator).validate(dto);

        assertThrows(PasswordNotMatchException.class,  () -> registerService.register(dto));
        verify(registerValidator).validate(dto);
        verifyNoMoreInteractions(authRepository, userClient);
    }

    @Test
    void register_shouldThrowUserAlreadyExistsException_whenFeignConflict() {
        RegisterRequestDTO requestDTO = new RegisterRequestDTO(
                "user",
                "pass",
                "pass",
                "email@example.com"
        );

        Auth auth = Auth.builder()
                .uuid(UUID.randomUUID())
                .userName("user")
                .email("email@example.com")
                .build();

        doNothing().when(registerValidator).validate(requestDTO);
        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");
        when(authRepository.save(any(Auth.class))).thenReturn(auth);
        when(userClient.createUser(any(CreateUserRequestDTO.class))).thenThrow(FeignException.Conflict.class);

        assertThrows(UserAlreadyExistsException.class, () -> registerService.register(requestDTO));
    }

    @Test
    void register_shouldThrowRuntimeException_whenFeignException() {
        RegisterRequestDTO requestDTO = new RegisterRequestDTO(
                "user",
                "pass",
                "pass",
                "email@example.com"
        );

        Auth auth = Auth.builder()
                .uuid(UUID.randomUUID())
                .userName("user")
                .email("email@example.com")
                .build();

        doNothing().when(registerValidator).validate(requestDTO);
        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");
        when(authRepository.save(any(Auth.class))).thenReturn(auth);
        when(userClient.createUser(any(CreateUserRequestDTO.class))).thenThrow(mock(FeignException.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> registerService.register(requestDTO));
        assertTrue(exception.getMessage().contains("User microservice error"));
    }
}
