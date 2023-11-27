package com.example.repo;

import com.example.domain.LargeFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LargeFileRepository extends JpaRepository<LargeFile, Long> {
}