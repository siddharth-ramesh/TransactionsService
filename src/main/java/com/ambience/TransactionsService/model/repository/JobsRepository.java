package com.ambience.TransactionsService.model.repository;

import com.ambience.TransactionsService.model.entity.JobsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobsRepository extends JpaRepository<JobsEntity, Object> {
    // You can define custom methods here
}