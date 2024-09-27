package com.product.server.koi_control_application.repository;


import com.product.server.koi_control_application.model.KoiGrowthHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
@RepositoryRestResource(exported = false)
@Transactional
public interface KoiGrowthHistoryRepository extends JpaRepository<KoiGrowthHistory, Integer> {
}
