package com.hong.ForPaw.repository.Animal;

import com.hong.ForPaw.domain.Animal.Animal;
import com.hong.ForPaw.domain.Animal.AnimalType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnimalRepository extends JpaRepository<Animal, Long> {

    @Query("SELECT a FROM Animal a WHERE a.removedAt IS NULL")
    Page<Animal> findAll(Pageable pageable);

    @Query("SELECT a FROM Animal a WHERE (:category IS NULL OR a.category = :category) AND a.removedAt IS NULL")
    Page<Animal> findAllByCategory(@Param("category") AnimalType category, Pageable pageable);

    @Query("SELECT a FROM Animal a WHERE a.id = :id AND a.removedAt IS NULL")
    Optional<Animal> findById(@Param("id") Long id);

    @Query("SELECT a FROM Animal a WHERE a.id IN :ids AND a.removedAt IS NULL")
    List<Animal> findAllByIds(List<Long> ids);
    
    @Query("SELECT a FROM Animal a WHERE a.shelter.id = :careRegNo AND a.removedAt IS NULL")
    Page<Animal> findByShelterId(@Param("careRegNo") Long careRegNo, Pageable pageable);

    @Query("SELECT a FROM Animal a WHERE a.noticeEdt < NOW() AND a.removedAt IS NULL")
    List<Animal> findAllNoticeEnded();

    @Query("SELECT COUNT(a) > 0 FROM Animal a WHERE a.id = :animalId AND a.removedAt IS NULL")
    boolean existsById(@Param("animalId") Long animalId);

    @Modifying
    @Query("UPDATE Animal a SET a.inquiryNum = a.inquiryNum + 1 WHERE a.id = :animalId")
    void incrementInquiryNumById(@Param("animalId") Long animalId);

    @Modifying
    @Query("UPDATE Animal a SET a.inquiryNum = a.inquiryNum - 1 WHERE a.id = :animalId AND a.inquiryNum > 0")
    void decrementInquiryNumById(@Param("animalId") Long animalId);

    @Query("SELECT COUNT(a) FROM Animal a WHERE a.removedAt IS NULL")
    Long countAnimal();
}
