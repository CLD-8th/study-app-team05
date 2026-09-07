package com.example.study.review;

import com.example.study.application.ApplicationRepository;
import com.example.study.application.ApplicationStatus;
import com.example.study.common.BusinessException;
import com.example.study.common.ErrorCode;
import com.example.study.member.Member;
import com.example.study.member.MemberService;
import com.example.study.review.dto.ReviewResponse;
import com.example.study.study.StudyPost;
import com.example.study.study.StudyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 후기 업무 계층.
 *
 * 참여자는 모집자와 수락된 신청자를 가리킴.
 * 모집자는 신청 절차를 거치지 않으므로 수락된 신청이 존재하지 않음.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ApplicationRepository applicationRepository;
    private final StudyService studyService;
    private final MemberService memberService;

    public List<ReviewResponse> findByStudy(Long studyPostId) {
        return reviewRepository.findByStudyPostIdOrderByIdAsc(studyPostId)
                .stream().map(ReviewResponse::from).toList();
    }

    /**
     * 후기 등록.
     *
     * 순서는 대상 확인 · 마감 여부 · 참여 여부 · 중복임.
     */
    @Transactional
    public ReviewResponse create(Long studyPostId, String content, int rating, Long memberId) {
        StudyPost post = studyService.getWithWriter(studyPostId);

        if (post.isRecruiting()) {
            throw new BusinessException(ErrorCode.STUDY_NOT_CLOSED, "마감되지 않은 스터디");
        }
        if (!isParticipant(post, memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "참여자만 후기 작성 가능");
        }
        if (reviewRepository.existsByStudyPostIdAndWriterId(studyPostId, memberId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_REVIEW, "이미 후기를 작성함");
        }

        Member writer = memberService.getMember(memberId);
        Review saved = reviewRepository.save(new Review(content, rating, post, writer));

        log.info("후기 등록: id={} study={}", saved.getId(), studyPostId);
        return ReviewResponse.from(saved);
    }

    /**
     * 후기 삭제.
     *
     * 모집자에게 삭제 권한을 주지 않음.
     * 낮은 평점을 지울 수 있게 되어 후기의 의미가 사라짐.
     */
    @Transactional
    public void delete(Long reviewId, Long memberId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "후기 부재"));

        if (!review.isWrittenBy(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "작성자만 삭제 가능");
        }
        reviewRepository.delete(review);
        log.info("후기 삭제: id={}", reviewId);
    }

    private boolean isParticipant(StudyPost post, Long memberId) {
        if (post.isWrittenBy(memberId)) {
            return true;
        }
        return applicationRepository.existsByStudyPostIdAndApplicantIdAndStatusIn(
                post.getId(), memberId, List.of(ApplicationStatus.ACCEPTED));
    }
}
