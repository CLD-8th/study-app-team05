package com.example.study.application;

import com.example.study.application.dto.ApplicationRequest;
import com.example.study.application.dto.ApplicationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 신청 표현 계층.
 *
 * 대상 모집글에 종속된 주소와 신청 자체를 가리키는 주소가 섞여 있어
 * 묶음 주소를 클래스에 두지 않음.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApplicationController {

    private final ApplicationService applicationService;

    /*
     * TODO 33 · 신청과 취소 주소
     *
     * 기능        POST /api/studies/{studyId}/applications 와
     *             DELETE /api/applications/{id} 를 만듦
     *             신청은 201 · 취소는 204
     * 활용메소드  ApplicationService.apply()    TODO 31 · 같은 담당
     *             ApplicationService.cancel()   TODO 32 · 같은 담당
     * 반환형태    ApplicationResponse · 취소는 없음
     * 동작결과    EP-07 · EP-08
     */

    /*
     * TODO 46 · 신청 처리 주소 셋
     *
     * 기능        GET /api/studies/{studyId}/applications
     *             PATCH /api/applications/{id}/accept
     *             PATCH /api/applications/{id}/reject 를 만듦
     * 활용메소드  ApplicationService.findByStudy()   TODO 42 · 같은 담당
     *             ApplicationService.accept()       TODO 43 · 같은 담당
     *             ApplicationService.reject()       TODO 44 · 같은 담당
     * 반환형태    List<ApplicationResponse> · ApplicationResponse
     * 동작결과    EP-09 · EP-10 · EP-11
     */

    @GetMapping("/studies/{studyId}/applications")
    public ResponseEntity<List<ApplicationResponse>> findByStudy(
            @PathVariable Long studyId,
            @AuthenticationPrincipal Long memberId
    ) {
        List<ApplicationResponse> responses = applicationService.findByStudy(studyId, memberId);

        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/applications/{id}/accept")
    public ResponseEntity<ApplicationResponse> accept(
            @PathVariable Long id,
            @AuthenticationPrincipal Long memberId
    ) {
        ApplicationResponse response = applicationService.accept(id, memberId);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/applications/{id}/reject")
    public ResponseEntity<ApplicationResponse> reject(
            @PathVariable Long id,
            @AuthenticationPrincipal Long memberId
    ) {
        ApplicationResponse response = applicationService.reject(id, memberId);

        return ResponseEntity.ok(response);
    }
}
