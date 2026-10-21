package com.example.library.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.library.entity.Loan;

public interface LoanRepository extends JpaRepository<Loan, UUID> {

	// UUIDs have no meaningful order, so the list is ordered by request time (then id, for a stable tie-break).
	List<Loan> findByMemberIdOrderByRequestedAtAscIdAsc(Long memberId);

}
