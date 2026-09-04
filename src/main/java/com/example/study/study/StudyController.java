package com.example.study.study;

import com.example.study.common.PageResponse;
import com.example.study.study.dto.StudyDetailResponse;
import com.example.study.study.dto.StudyListResponse;
import com.example.study.study.dto.StudyRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * 모집글 표현 계층.
 *
 * 예외를 잡지 않음. 전역 처리기가 받아 같은 형태로 변환함.
 * 모집자는 요청 본문이 아니라 토큰에서 확인함.
 */
@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;

    @GetMapping
    public PageResponse<StudyListResponse> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status
    ) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        StudyStatus studyStatus = status == null ? null : StudyStatus.valueOf(status);

        return PageResponse.of(studyService.findAll(keyword, studyStatus, pageable), data -> data);
    }

    @GetMapping("/{id}")
    public StudyDetailResponse findOne(@PathVariable Long id) {
        return studyService.findById(id);
    }

    @PostMapping
    public ResponseEntity<StudyDetailResponse> create(@Valid @RequestBody StudyRequest request,
                                                      @AuthenticationPrincipal Long memberId) {
        StudyDetailResponse created = studyService.create(
                request.title(), request.content(), request.capacity(), request.deadline(), memberId);

        return ResponseEntity.created(URI.create("/api/studies/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public StudyDetailResponse update(@PathVariable Long id, @Valid @RequestBody StudyRequest request,
                                      @AuthenticationPrincipal Long memberId) {
        return studyService.update(id, request.title(), request.content(),
                request.capacity(), request.deadline(), memberId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal Long memberId) {
        studyService.delete(id, memberId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/close")
    public StudyDetailResponse close(@PathVariable Long id, @AuthenticationPrincipal Long memberId) {
        return studyService.close(id, memberId);
    }
}
