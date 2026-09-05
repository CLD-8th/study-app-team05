/*
 * 모집글 등록 · 수정 · 담당 1
 *
 * SC-03 화면. 주소에 id 가 없으면 등록, 있으면 수정임.
 */

const formId = param('id');

function defaultDeadline() {
    const date = new Date();
    date.setDate(date.getDate() + 7);
    return date.toISOString().substring(0, 10);
}

async function initForm() {
    // 로그인하지 않은 경우, 로그인 화면으로 이동
    requireLogin();
    // id가 있는 경우
    if (formId) {
        // 제목을 수정으로 변경
        document.getElementById('page-title').textContent = '모집글 수정';
        // 기존 값 채우기
        try {
            const data = await api.get('/api/studies/' + formId);
            document.getElementById('title').value = data.title;
            document.getElementById('content').value = data.content;
            document.getElementById('capacity').value = data.capacity;
            document.getElementById('deadline').value = data.deadline;
        } catch (e) {
            location.href = '/login.html';
        }
    // id가 없는 경우
    } else {
        // 마감일 기본 값 채우기
        document.getElementById('deadline').value = defaultDeadline();
    }
}

async function saveForm() {
    // 입력 값 추출
    const title = document.getElementById('title').value.trim();
    const content = document.getElementById('content').value.trim();
    const capacity = document.getElementById('capacity').value;
    const deadline = document.getElementById('deadline').value;
    const messageArea = document.getElementById('save-error');
    // Request Body 생성
    const body = { title, content, capacity, deadline };

    try {
        // id가 없는 경우
        if (!formId) {
            // 등록 요청
            const created = await api.post('/api/studies', body);
            // 상세 화면 이동
            location.href = '/study.html?id=' + created.id;
        // id가 있는 경우
        } else {
            // 수정 요청
            await api.put('/api/studies/' + formId, body);
            // 상세 화면으로 이동
            location.href = '/study.html?id=' + formId;
        }
    } catch (e) {
        // 항목별 사유 표시
        if (!showFieldErrors(e, '')) {
            // 항목별 사유가 없는 경우 하단에 안내
            showError(messageArea, e);
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('save').addEventListener('click', saveForm);
    document.getElementById('cancel').addEventListener('click', () => history.back());
    initForm();
});
