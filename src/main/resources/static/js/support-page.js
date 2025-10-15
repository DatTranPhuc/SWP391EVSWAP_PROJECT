const form = document.querySelector('[data-support-form]');

if (form) {
    const message = form.querySelector('[data-support-message]');
    const driverId = form.getAttribute('data-driver-id');

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        if (message) {
            message.hidden = true;
        }

        const category = form.category.value;
        const comment = form.comment.value.trim();

        if (!driverId || !category || !comment) {
            if (message) {
                message.textContent = 'Vui lòng điền đầy đủ thông tin.';
                message.hidden = false;
            }
            return;
        }

        form.querySelector('button[type="submit"]').disabled = true;

        try {
            const response = await fetch('/api/support', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    driverId: Number(driverId),
                    staffId: null,
                    category,
                    comment
                })
            });

            if (!response.ok) {
                throw new Error('Không thể gửi yêu cầu');
            }

            form.reset();
            if (message) {
                message.textContent = 'Đã gửi yêu cầu hỗ trợ! Chúng tôi sẽ phản hồi sớm.';
                message.hidden = false;
            }

            setTimeout(() => window.location.reload(), 1200);
        } catch (error) {
            if (message) {
                message.textContent = 'Không thể gửi yêu cầu. Vui lòng thử lại sau.';
                message.hidden = false;
            }
        } finally {
            form.querySelector('button[type="submit"]').disabled = false;
        }
    });
}
