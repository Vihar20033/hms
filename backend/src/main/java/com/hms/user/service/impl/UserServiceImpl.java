package com.hms.user.service.impl;

import com.hms.user.dto.UserResponseDTO;
import com.hms.user.entity.User;
import com.hms.user.exception.UserNotFoundException;
import com.hms.user.mapper.UserMapper;
import com.hms.user.repository.UserRepository;
import com.hms.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<UserResponseDTO> getSlice(int page, int size) {
        PageRequest request = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "username"));
        return userRepository.findAll(request).map(userMapper::toResponseDTO);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found", id.toString()));
        userRepository.delete(user);
    }
}
