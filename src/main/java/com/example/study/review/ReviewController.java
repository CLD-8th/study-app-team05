package com.example.study.review;

import com.example.study.review.dto.ReviewRequest;
import com.example.study.review.dto.ReviewResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/api/studies/{studyId}/reviews")
    public List<ReviewResponse> findByStudy(@PathVariable Long studyId) {
        return reviewService.findByStudy(studyId);
    }

    @PostMapping("/api/studies/{studyId}/reviews")
    public ResponseEntity<ReviewResponse> create(@PathVariable Long studyId,
                                                 @Valid @RequestBody ReviewRequest request,
                                                 @AuthenticationPrincipal Long memberId) {
        return ResponseEntity.status(201)
                .body(reviewService.create(studyId, request.content(), request.rating(), memberId));
    }

    @DeleteMapping("/api/reviews/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal Long memberId) {
        reviewService.delete(id, memberId);
        return ResponseEntity.noContent().build();
    }
}
