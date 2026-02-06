package com.example.skillseeker_be.repository;

import com.example.skillseeker_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
