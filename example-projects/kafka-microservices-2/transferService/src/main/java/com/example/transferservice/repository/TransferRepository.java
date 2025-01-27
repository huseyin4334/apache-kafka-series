package com.example.transferservice.repository;

import com.example.transferservice.model.TransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<TransferEntity, String> {
}
