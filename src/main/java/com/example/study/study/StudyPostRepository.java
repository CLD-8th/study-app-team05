package com.example.study.study;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StudyPostRepository extends JpaRepository<StudyPost, Long> {

    /**
     * 목록 조회.
     *
     * 검색어와 상태를 함께 받으며 값이 없으면 조건에서 제외됨.
     * 모집자를 함께 가져와 목록 건수만큼 조회가 늘어나지 않게 함.
     */
    @EntityGraph(attributePaths = {"writer"})
    @Query("""
            select p from StudyPost p
            where (:keyword is null or p.title like concat('%', :keyword, '%'))
              and (:status is null or p.status = :status)
            """)
    Page<StudyPost> search(String keyword, StudyStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"writer"})
    Optional<StudyPost> findWithWriterById(Long id);

    @EntityGraph(attributePaths = {"writer"})
    List<StudyPost> findByWriterIdOrderByIdDesc(Long writerId);
}