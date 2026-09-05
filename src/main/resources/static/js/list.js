/*
 * 모집글 목록 · 담당 1
 *
 * SC-01 모집글 목록 화면을 그림.
 */

let listPage = 0;
const listSize = 10;

function renderList(data) {
    // 리스트 DOM 할당
    const list = document.getElementById('list');
    if (!list) return;
    // 목록에 표시할 데이터 List
    const items = data.content;
    // 건수 표시
    document.getElementById('total').textContent = `${data.totalElements}건`;
    // 쪽 이동 표시
    renderPager(data);
    // 한 건도 없는 경우
    if (items.length === 0) {
        // 빈 화면 문구 표시
        list.innerHTML = '<div class="empty">등록된 모집글이 없습니다</div>';
        return;
    }
    // 목록 표시
    list.innerHTML = items.map(item => `
        <div class="${item.status === 'CLOSED' ? 'item closed' : 'item'}">
            <div>
                <div class="item-title"><a href="/study.html?id=${item.id}">${escapeHtml(item.title)}</a></div>
                <div class="item-meta">
                    <span>${escapeHtml(item.writerNickname)}</span>
                    <span>${item.acceptedCount} / ${item.capacity}명</span>
                </div>
            </div>
            <div class="item-meta">
                ${badge(item.status)}
                <span>${shortDate(item.createdAt)}</span>
            </div>
        </div>
    `).join('');
}

function renderPager(data) {
    // 쪽 표시 DOM 할당
    const area = document.getElementById('pager');
    if (!area) return;
    // 모집글이 한 건도 없거나 페이지가 한 개인 경우 쪽 표시 X
    if (!data.totalPages || data.totalPages <= 1) {
        area.innerHTML = '';
        return;
    }
    // 전체 쪽 수만큼 버튼 생성
    const buttons = [];
    for (let i = 0; i < data.totalPages; i++) {
        buttons.push(
            `<button class="${i === data.page ? 'current' : ''}" onclick="moveTo(${i})">${i + 1}</button>`
        );
    }
    area.innerHTML = buttons.join('');
}

async function loadList() {
    // 검색어 추출
    const keyword = document.getElementById('keyword').value.trim();
    // 선택된 상태 추출
    const status = document.getElementById('status').value;
    // 실패 안내 문구 DOM 할당
    const errorMessage = document.getElementById('load-error');
    // 리스트 DOM 할당
    const list = document.getElementById('list');
    // 쪽 표시 DOM 할당
    const pager = document.getElementById('pager');
    // 조회 조건 생성 - 검색어와 상태가 비어 있으면 뺌
    const params = {};
    params.page = listPage;
    params.size = listSize;
    if (keyword) {
        params.keyword = keyword;
    }
    if (status) {
        params.status = status;
    }
    // 조건과 함께 조회 요청
    try {
        // 쿼리스트링 생성
        const query = Object.keys(params).length ? '?' + new URLSearchParams(params).toString() : '';
        // 응답 저장
        const data = await api.get('/api/studies' + query);
        // 목록 표시
        renderList(data);
        // 실패 안내 문구 숨김
        errorMessage.classList.add('hidden');
    } catch (e) {
        // 요청 실패 시 실패 안내 문구 표시
        errorMessage.classList.remove('hidden');
        // 요청 실패 시 목록과 쪽 표시를 숨김
        list.innerHTML = '';
        pager.innerHTML = '';
    }
}

// listPage를 변경하고 새로운 페이지를 로드하는 메서드
function moveTo(page) {
    listPage = page;
    loadList();
}

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('search').addEventListener('click', () => { listPage = 0; loadList(); });
    document.getElementById('retry').addEventListener('click', loadList);
    document.getElementById('create').addEventListener('click', () => location.href = '/form.html');

    // 등록 단추는 로그인한 경우에만 보임.
    if (auth.loggedIn) {
        document.getElementById('create').classList.remove('hidden');
    }
    loadList();
});
