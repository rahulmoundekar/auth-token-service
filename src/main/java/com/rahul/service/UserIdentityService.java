package com.rahul.service;

import com.rahul.entity.User;
import com.rahul.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserIdentityService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User requireUser(
            UUID userId
    ) {

        return userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );
    }
}