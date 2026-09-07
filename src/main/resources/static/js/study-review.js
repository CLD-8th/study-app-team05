/*
 * 후기 구획 · 담당 5
 *
 * 마감된 뒤에 참여자만 작성 가능하며 한 번 쓰면 입력란을 두지 않음.
 * 참여자는 모집자와 수락된 신청자를 가리킴.
 */

StudyPage.register(async function renderReviews() {
    const study = StudyPage.study;
    const panel = document.getElementById('review-panel');
    panel.classList.remove('hidden');

    const list = await api.get('/api/studies/' + StudyPage.id + '/reviews');
    const written = auth.loggedIn && list.some(item => item.writerId === auth.memberId);
    const participant = StudyPage.isOwner() ||
        (StudyPage.myApplication !== null && StudyPage.myApplication.status === 'ACCEPTED');

    const writable = auth.loggedIn && study.status === 'CLOSED' && participant && !written;

    const form = writable
        ? '<div class="card" style="background:#fafbfc; margin-bottom:14px;">' +
        '  <div class="field" style="display:flex; gap:10px; align-items:center;">' +
        '    <label for="rating">평점</label>' +
        '    <select id="rating" style="width:90px;">' +
        '      <option>5</option><option>4</option><option>3</option>' +
        '      <option>2</option><option>1</option></select>' +
        '  </div>' +
        '  <div class="field"><textarea id="review-content" placeholder="후기를 남겨 주세요"></textarea></div>' +
        '  <div class="alert alert-error hidden" id="review-error"></div>' +
        '  <div class="actions"><button class="primary" id="write-review">등록</button></div>' +
        '</div>'
        : '';

    const rows = list.length === 0
        ? '<div class="empty">아직 후기가 없습니다</div>'
        : list.map(item =>
            '<div class="item">' +
            '  <div>' +
            '    <div class="item-title">' + escapeHtml(item.writerNickname) +
            '      <span style="color:#4f6ef0;">' + '★'.repeat(item.rating) + '</span></div>' +
            '    <div class="item-meta"><span>' + escapeHtml(item.content) + '</span></div>' +
            '  </div>' +
            '  <div class="item-meta"><span>' + dateTime(item.createdAt) + '</span>' +
            (auth.loggedIn && item.writerId === auth.memberId
                ? '<button class="danger" data-review="' + item.id + '">삭제</button>' : '') +
            '</div></div>').join('');

    panel.innerHTML = '<div class="card-head"><div class="card-title">후기</div></div>' + form + rows;

    if (writable) {
        document.getElementById('write-review').addEventListener('click', async () => {
            try {
                await api.post('/api/studies/' + StudyPage.id + '/reviews', {
                    content: document.getElementById('review-content').value.trim(),
                    rating: Number(document.getElementById('rating').value)
                });
                await StudyPage.reload();
            } catch (error) {
                const box = document.getElementById('review-error');
                if (!showFieldErrors(error, '')) showError(box, error);
            }
        });
    }

    panel.querySelectorAll('[data-review]').forEach(button =>
        button.addEventListener('click', async () => {
            if (!confirm('후기를 삭제하시겠습니까?')) return;
            await api.del('/api/reviews/' + button.dataset.review);
            await StudyPage.reload();
        }));
});
