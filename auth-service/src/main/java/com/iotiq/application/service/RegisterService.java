package com.iotiq.application.service;

import com.iotiq.api.dto.ApiResponse;
import com.iotiq.api.dto.CreateUserRequestDTO;
import com.iotiq.api.dto.RegisterRequestDTO;
import com.iotiq.api.dto.RegisterResponseDTO;
import com.iotiq.application.usecase.RegisterUseCase;
import com.iotiq.application.validator.RegisterValidator;
import com.iotiq.domain.model.Auth;
import com.iotiq.domain.repository.AuthRepository;
import com.iotiq.dto.UserProfileResponseDTO;
import com.iotiq.enums.UserRole;
import com.iotiq.exception.UserAlreadyExistsException;
import com.iotiq.infrastructure.client.UserClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterService implements RegisterUseCase {
    private final AuthRepository authRepository;
    private final UserClient userClient;
    private final PasswordEncoder passwordEncoder;
    private final RegisterValidator registerValidator;

    @Override
    @Transactional
    public RegisterResponseDTO register(RegisterRequestDTO dto) {
        registerValidator.validate(dto);

        Auth auth = Auth.builder()
                .userName(dto.getUserName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(UserRole.USER)
                .isActive(false)
                .build();

        auth = authRepository.save(auth);

        try {
            ResponseEntity<ApiResponse<UserProfileResponseDTO>> userProfileResponseDTO = userClient.createUser(CreateUserRequestDTO.builder()
                    .authUuid(auth.getUuid())
                    .email(auth.getEmail())
                    .userName(auth.getUserName())
                    .build());
        } catch (FeignException.Conflict exception) {
            throw new UserAlreadyExistsException(auth.getEmail(), auth.getUserName());
        } catch (FeignException exception) {
            throw new RuntimeException("User microservice error: " + exception.getMessage(), exception);
        }

        return RegisterResponseDTO.builder()
                .uuid(auth.getUuid())
                .email(auth.getEmail())
                .userName(auth.getUserName())
                .build();
    }
}
