package com.ecom.trial.Repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecom.trial.Models.User;


public interface UserRepo extends JpaRepository<User, Integer>{
    
    Optional<User> findByKeyCloakId(String keyCloakId);
}