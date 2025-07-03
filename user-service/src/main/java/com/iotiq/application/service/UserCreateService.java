package com.iotiq.application.service;

import com.iotiq.api.dto.CreateUserRequestDTO;
import com.iotiq.dto.UserProfileResponseDTO;
import com.iotiq.application.usecase.UserCreateUseCase;
import com.iotiq.domain.model.UserProfile;
import com.iotiq.domain.repository.UserRepository;
import com.iotiq.exception.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCreateService implements UserCreateUseCase {
    private final UserRepository userRepository;

    @Override
    public UserProfileResponseDTO createUser(CreateUserRequestDTO dto) {
        if (userRepository.existsByUserName(dto.userName()) || userRepository.existsByEmail(dto.email())) {
            throw new UserAlreadyExistsException(dto.email(), dto.userName());
        }

        UserProfile user = UserProfile.builder()
                .authUuid(dto.authUuid())
                .userName(dto.userName())
                .email(dto.email())
                .build();

        user = userRepository.save(user);
        return UserProfileResponseDTO.builder()
                .uuid(user.getUuid())
                .authUuid(user.getAuthUuid())
                .userName(user.getUserName())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .build();
    }
}
