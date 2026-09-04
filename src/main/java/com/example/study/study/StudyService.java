package com.example.study.study;

import com.example.study.application.ApplicationRepository;
import com.example.study.application.ApplicationStatus;
import com.example.study.application.dto.AcceptedCount;
import com.example.study.common.BusinessException;
import com.example.study.common.ErrorCode;
import com.example.study.member.Member;
import com.example.study.member.MemberService;
import com.example.study.study.dto.StudyDetailResponse;
import com.example.study.study.dto.StudyListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 모집글 업무 계층.
 *
 * 소유 관계와 상태 전이를 여기서 판단함.
 * 값의 형식과 범위는 요청 형태에서 이미 걸러짐.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyService {

    private final StudyPostRepository studyPostRepository;
    private final ApplicationRepository applicationRepository;
    private final MemberService memberService;

    @Transactional
    public StudyDetailResponse create(String title, String content, int capacity,
                                      LocalDate deadline, Long memberId) {
        Member writer = memberService.getMember(memberId);
        StudyPost saved = studyPostRepository.save(
                new StudyPost(title, content, capacity, deadline, writer));

        return StudyDetailResponse.of(saved, 0);
    }

    /**
     * 목록 조회.
     *
     * 수락 인원을 건마다 세면 조회 구문이 건수에 비례함.
     * 식별자 묶음을 한 번에 세어 붙임.
     */
    public Page<StudyListResponse> findAll(String keyword, StudyStatus status, Pageable pageable) {

        Page<StudyPost> studyPosts = studyPostRepository.search(keyword, status, pageable);

        List<Long> postIds = studyPosts.getContent().stream()
                .map(StudyPost::getId)
                .toList();

        Map<Long, Long> acceptedCounts = acceptedCounts(postIds);

        return studyPosts.map(studyPost ->
                StudyListResponse.of(
                        studyPost,
                        acceptedCounts.getOrDefault(studyPost.getId(), 0L)
                )
        );
    }

    public StudyDetailResponse findById(Long id) {
        StudyPost post = getWithWriter(id);
        long acceptedCount = countAccepted(id);

        return StudyDetailResponse.of(post, acceptedCount);
    }

    /**
     * 수정.
     *
     * 마감된 모집글은 수정하지 않음. 정원을 늘리면 자리가 있는데 신청이 막히고
     * 마감일을 바꿔도 상태가 그대로라 의미가 없음.
     *
     * 정원은 현재 수락 인원보다 작게 바꿀 수 없음.
     * 인원이 정원을 넘는 상태가 되며 되돌릴 방법이 없음.
     */
    @Transactional
    public StudyDetailResponse update(Long id, String title, String content, int capacity,
                                      LocalDate deadline, Long memberId) {
        StudyPost post = getWithWriter(id);

        if(!post.isWrittenBy(memberId)){
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if(!post.isRecruiting()){
            throw new BusinessException(ErrorCode.STUDY_CLOSED);
        }
        long acceptedCount = countAccepted(id);
        if(capacity < acceptedCount){
            throw new BusinessException(ErrorCode.CAPACITY_BELOW_ACCEPTED);
        }
        post.update(title, content, capacity, deadline);
        return StudyDetailResponse.of(post, acceptedCount);
    }

    @Transactional
    public void delete(Long id, Long memberId) {
        StudyPost post = getWithWriter(id);

        if(!post.isWrittenBy(memberId)){
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        studyPostRepository.delete(post);
    }

    /**
     * 마감.
     *
     * 대기 상태의 신청은 그대로 둠. 모집자가 개별로 처리함.
     */
    @Transactional
    public StudyDetailResponse close(Long id, Long memberId) {
    /*
     * TODO 25 · 모집 마감
     *
     * 기능        모집자 본인인지 → 모집 중인지 확인한 뒤 상태를 마감으로 바꿈
     *             대기 상태의 신청은 그대로 둠 · 모집자가 개별로 처리함
     * 활용메소드  StudyService.getWithWriter()   제공됨
     *             StudyPost.isWrittenBy()        엔티티 · 제공됨
     *             StudyPost.isRecruiting()       엔티티 · 제공됨
     *             StudyPost.close()              엔티티 · 제공됨
     * 반환형태    StudyDetailResponse
     * 동작결과    EP-06 · 상태가 CLOSED · 이미 마감이면 400 STUDY_CLOSED
     */
        throw new UnsupportedOperationException("TODO 25");
    }

    public List<StudyListResponse> findMine(Long memberId) {
    /*
     * TODO 63 · 내 모집글 조회
     *
     * 기능        토큰에서 온 식별자로 내 모집글을 최신순으로 조회함
     *             목록과 마찬가지로 수락 인원을 한 번에 세어 붙임
     * 활용메소드  StudyPostRepository 의 내 모집글 규약   TODO 61 · 같은 담당
     *             StudyService.acceptedCounts()          같은 클래스 · 제공됨
     *             StudyListResponse.of()                 제공됨
     * 반환형태    List<StudyListResponse>
     * 동작결과    EP-16 · 내가 등록한 것만 최신순으로 나옴
     */
        throw new UnsupportedOperationException("TODO 63");
    }

    public StudyPost getWithWriter(Long id) {
        // 제공 · 담당 3 · 4 · 5 도 이 메서드를 씀.
        return studyPostRepository.findWithWriterById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "모집글 부재"));
    }

    private long countAccepted(Long studyPostId) {
        return applicationRepository.countByStudyPostIdAndStatus(studyPostId, ApplicationStatus.ACCEPTED);
    }

    private Map<Long, Long> acceptedCounts(List<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return applicationRepository.countAcceptedByStudyPostIds(ids, ApplicationStatus.ACCEPTED)
                .stream()
                .collect(Collectors.toMap(AcceptedCount::studyPostId, AcceptedCount::count));
    }
}
