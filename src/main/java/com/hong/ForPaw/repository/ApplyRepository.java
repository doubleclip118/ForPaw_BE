package com.hong.ForPaw.repository;

import com.hong.ForPaw.domain.Apply.Apply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplyRepository extends JpaRepository<Apply, Long> {

    List<Apply> findByUserId(Long userId);

    Optional<Apply> findByUserIdAndAnimalId(Long userId, Long animalId);

    Optional<Apply> findByUserIdAndId(Long userId, Long applyId);
}