package com.product.server.koi_control_application.repository;


import com.product.server.koi_control_application.model.KoiGrowthHistory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@RepositoryRestResource(exported = false)
public interface KoiGrowthHistoryRepository extends JpaRepository<KoiGrowthHistory, Integer> {
    Page<KoiGrowthHistory> findAllByKoiId(int koiId, Pageable pageable);
    List<KoiGrowthHistory> findAllByKoiId(int koiId);

    @Query("select k from KoiGrowthHistory k where k.koiId = ?1 order by k.createdAt DESC ")
    List<KoiGrowthHistory> findAllByKoiIdOrderByDate(int koiId);
}
