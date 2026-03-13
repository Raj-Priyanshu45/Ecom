package com.ecom.trial.Repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecom.trial.Models.User;

@Repository
public interface UserRepo extends JpaRepository<User, Integer>{
    
    Optional<User> findByKeyCloakId(String keyCloakId);
}