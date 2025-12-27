package com.core.cashin.routing.repository;

import com.core.cashin.commons.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    Optional<Merchant> findByLoginIdAndSecretKey(String loginId, String secretKey);
}
