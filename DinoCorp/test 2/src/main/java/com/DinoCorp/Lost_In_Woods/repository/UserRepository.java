package com.DinoCorp.Lost_In_Woods.repository;

import com.DinoCorp.Lost_In_Woods.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Keyed by the canonical (lowercased) username, so existsById / findById give
// case-insensitive lookups with no extra index.
@Repository
public interface UserRepository extends JpaRepository<User, String> {
}
