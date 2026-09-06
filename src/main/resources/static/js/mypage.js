/*
 * 마이페이지 · 담당 6
 *
 * SC-04 화면. 구역마다 요청이 따로 있음.
 */

async function loadProfile() {
    try {
        // 내 정보 요청
        const data = await api.get('/api/members/me');
        // 내 정보 DOM 할당
        const profile = document.getElementById('profile');
        // 내 정보 표시
        profile.innerHTML = `
            <div>이메일 &nbsp; ${escapeHtml(data.email)}</div>
            <div>별명 &nbsp; <b>${escapeHtml(data.nickname)}</b></div>
            <div>가입일 &nbsp; ${formatDate(data.createdAt)}</div>
        `;
    } catch (e) {
        // 토큰이 없는 경우 로그인 화면으로 이동
        if (e.status === 401) {
            location.href = '/login.html';
        }
    }
}

async function loadMyStudies() {
    try {
        // 내 모집글 목록 요청
        const data = await api.get('/api/members/me/studies');
        // 내 모집글 DOM 할당
        const myStudies = document.getElementById('my-studies');
        // 건수 표시
        document.getElementById('study-count').textContent = `${data.length}건`;
        // 한 건도 없는 경우
        if (data.length === 0) {
            // 빈 화면 문구 표시
            myStudies.innerHTML = '<div class="empty">등록된 모집글이 없습니다</div>';
        // 내 모집글이 있는 경우
        } else {
            // 내 모집글 목록 표시
            myStudies.innerHTML = data.map(item => `
                <div class="${item.status === 'CLOSED' ? 'item closed' : 'item'}">
                    <div class="item-title"><a href="/study.html?id=${item.id}">${escapeHtml(item.title)}</a> ${badge(item.status)}</div>
                    <span class="item-meta">${item.acceptedCount} / ${item.capacity}명</span>
                </div>
            `).join('');
        }
    } catch (e) {
        console.error("내 모집글 불러오기 실패", e);
    }
}

async function loadMyApplications() {
    try {
        // 내 신청 목록 요청
        const data = await api.get('/api/members/me/applications');
        // 내 신청 DOM 할당
        const myApplications = document.getElementById('my-applications');
        // 건수 표시
        document.getElementById('application-count').textContent = `${data.length}건`;
        // 한 건도 없는 경우
        if (data.length === 0) {
            // 빈 화면 문구 표시
            myApplications.innerHTML = '<div class="empty">신청 내역이 없습니다</div>';
        // 내 신청이 있는 경우
        } else {
            // 내 신청 목록 표시
            myApplications.innerHTML = data.map(item => `
                <div class="item">
                    <div>
                        <div class="item-title"><a href="/study.html?id=${item.studyPostId}">${escapeHtml(item.studyPostTitle)}</a> ${badge(item.status)}</div>
                        <div class="item-meta"><span>${escapeHtml(item.message)}</span></div>
                    </div>
                    <span class="item-meta">${shortDate(item.createdAt)}</span>
                </div>
            `).join('');
        }
    } catch (e) {
        console.error("내 신청 불러오기 실패", e);
    }
}

// 가입 시점에서 날짜 정보만 분리하는 메서드
function formatDate(value) {
    if (!value) {
        return '-';
    }
    return value.substring(0, 10);
}

document.addEventListener('DOMContentLoaded', async () => {
    if (!requireLogin()) return;
    await loadProfile();
    await loadMyStudies();
    await loadMyApplications();
});
