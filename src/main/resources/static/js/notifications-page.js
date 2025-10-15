const markButtons = document.querySelectorAll('[data-action="mark-read"]');

markButtons.forEach((button) => {
    button.addEventListener('click', async () => {
        const notificationId = button.getAttribute('data-mark-read');
        if (!notificationId) {
            return;
        }

        button.disabled = true;
        button.textContent = 'Đang cập nhật...';

        try {
            const response = await fetch(`/api/notifications/${notificationId}/read`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error('Không thể đánh dấu đã đọc');
            }

            const card = button.closest('.notification-card');
            if (card) {
                card.classList.add('notification-card--read');
            }
            button.remove();
        } catch (error) {
            button.disabled = false;
            button.textContent = 'Đánh dấu đã đọc';
            window.alert('Không thể cập nhật trạng thái thông báo. Vui lòng thử lại.');
        }
    });
});
