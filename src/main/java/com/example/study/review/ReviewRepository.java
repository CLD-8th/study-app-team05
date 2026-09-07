package com.example.study.review;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 후기 목록.
     *
     * 작성자를 함께 가져와 목록 건수만큼 조회가 늘어나지 않게 함.
     */
    @EntityGraph(attributePaths = {"writer"})
    List<Review> findByStudyPostIdOrderByIdAsc(Long studyPostId);

    boolean existsByStudyPostIdAndWriterId(Long studyPostId, Long writerId);
}
