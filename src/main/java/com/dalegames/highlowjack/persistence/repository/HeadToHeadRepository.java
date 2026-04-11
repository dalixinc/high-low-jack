package com.dalegames.highlowjack.persistence.repository;

import com.dalegames.highlowjack.persistence.entity.HeadToHead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HeadToHeadRepository extends JpaRepository<HeadToHead, Long> {

    Optional<HeadToHead> findByPlayerAAndPlayerB(String playerA, String playerB);

    /** All records that include the given competitor (as either side). */
    @Query("SELECT h FROM HeadToHead h WHERE h.playerA = :name OR h.playerB = :name")
    List<HeadToHead> findAllByPlayer(@Param("name") String name);
}
