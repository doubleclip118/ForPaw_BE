package com.hong.ForPaw.repository.Authentication;

import com.hong.ForPaw.domain.Authentication.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {
}
