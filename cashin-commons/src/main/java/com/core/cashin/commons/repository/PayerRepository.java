package com.core.cashin.commons.repository;

import com.core.cashin.commons.entity.PayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayerRepository extends JpaRepository<PayerEntity, Long> {

    Optional<PayerEntity> findByDocument(String document);

}
