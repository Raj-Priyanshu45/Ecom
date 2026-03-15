package com.ecom.trial.Service;

import org.springframework.stereotype.Service;

import com.ecom.trial.DTOs.RequestDTOs.RegisterRequest;
import com.ecom.trial.Enums.Role;
import com.ecom.trial.Models.User;
import com.ecom.trial.Repo.UserRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final KeycloakUserService keycloakUserService;
    private final UserRepo userRepo;
    
    @Transactional
    public void register(RegisterRequest req){

        String keycloakId = keycloakUserService.createUser(req);

        userRepo.save(
            User.builder()
                .keyCloakId(keycloakId)
                .email(req.email())
                .role(Role.CUSTOMER).build()
        );
    }
}
