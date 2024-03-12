package com.ambience.TransactionsService.model.repository;

import com.ambience.TransactionsService.model.entity.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<UsersEntity, Object> {
    // You can define custom methods here
}