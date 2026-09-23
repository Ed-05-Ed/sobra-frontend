package com.sobra.user.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sobra.user.model.User;

public interface UserRepository extends JpaRepository<User, UUID> {

    List<User> findAllByOrderByNameAsc();
}