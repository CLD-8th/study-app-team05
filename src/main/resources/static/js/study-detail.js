/*
 * 모집글 상세 구획 · 담당 2
 *
 * StudyPage.study 를 읽어 표시함. 자료를 직접 조회하지 않음.
 * 이 구획이 그려져야 담당 3 · 4 · 5 가 모집자 여부를 판단할 수 있음.
 */

StudyPage.register(async function renderDetail() {
    const study = StudyPage.study;
    const box = document.getElementById('study-detail');
    const isOwner = StudyPage.isOwner();
    const isRecruiting = study.status === 'RECRUITING';

    let buttons = '';
    if (isOwner) {
        buttons = '<div class="actions">';
        if (isRecruiting) {
            buttons += '<button id="edit">수정</button>';
            buttons += '<button class="primary" id="close">모집 마감</button>';
        }
        buttons += '<button class="danger" id="remove">삭제</button>';
        buttons += '</div>';
    }

    box.innerHTML =
        '<div class="card-head">' +
        '<div class="card-title" style="font-size:19px;">' + escapeHtml(study.title) + '</div>' +
        badge(study.status) +
        '</div>' +
        '<div class="item-meta" style="margin-bottom:12px;">' +
        '<span>' + escapeHtml(study.writerNickname) + '</span>' +
        '<span>' + study.acceptedCount + ' / ' + study.capacity + '명</span>' +
        '<span>~ ' + shortDate(study.deadline) + '</span>' +
        '<span>' + dateTime(study.createdAt) + '</span>' +
        '</div>' +
        '<div style="font-size:13px; line-height:1.7; white-space:pre-wrap;">' +
        escapeHtml(study.content) + '</div>' +
        buttons;

    box.classList.remove('hidden');

    if (!isOwner) return;

    if (isRecruiting) {
        document.getElementById('edit').addEventListener('click', () => {
            location.href = '/form.html?id=' + study.id;
        });
        document.getElementById('close').addEventListener('click', async () => {
            if (!confirm('모집을 마감하시겠습니까?')) return;
            await api.patch('/api/studies/' + study.id + '/close');
            await StudyPage.reload();
        });
    }

    document.getElementById('remove').addEventListener('click', async () => {
        if (!confirm('삭제하시겠습니까?')) return;
        await api.del('/api/studies/' + study.id);
        location.href = '/index.html';
    });
});