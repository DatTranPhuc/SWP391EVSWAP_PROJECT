(function () {
  if (!window.EVSwapApi) {
    console.warn('EVSwapApi helper is missing');
    return;
  }

  const { request, serializeForm, setFeedback, clearFeedback } = window.EVSwapApi;

  function initForm(form) {
    const endpoint = form.dataset.apiEndpoint;
    const method = form.dataset.apiMethod || 'POST';
    if (!endpoint) {
      return;
    }

    const feedbackId = form.dataset.feedbackTarget;
    const feedbackEl = feedbackId ? document.getElementById(feedbackId) : null;
    const prefill = (form.dataset.prefillQuery || '')
      .split(',')
      .map((field) => field.trim())
      .filter(Boolean);

    if (prefill.length > 0) {
      const params = new URLSearchParams(window.location.search);
      prefill.forEach((fieldName) => {
        const value = params.get(fieldName);
        if (value && form.elements[fieldName]) {
          form.elements[fieldName].value = value;
        }
      });
    }

    const queryParams = (form.dataset.queryParams || '')
      .split(',')
      .map((field) => field.trim())
      .filter(Boolean);

    form.addEventListener('submit', async (event) => {
      event.preventDefault();

      try {
        clearFeedback(feedbackEl);
        form.classList.add('is-loading');
        const payload = serializeForm(form);
        let url = endpoint;

        if (queryParams.length > 0) {
          const qp = new URLSearchParams();
          queryParams.forEach((field) => {
            if (payload[field]) {
              qp.set(field, payload[field]);
              delete payload[field];
            }
          });
          const queryString = qp.toString();
          if (queryString) {
            url = `${endpoint}?${queryString}`;
          }
        }

        const response = await request(url, {
          method,
          body: JSON.stringify(payload)
        });

        setFeedback(feedbackEl, 'success', response.message || 'Thao tác thành công');

        const next = response?.data?.next;
        if (next) {
          setTimeout(() => {
            window.location.href = next.startsWith('http') ? next : `${window.location.origin}${next}`;
          }, 600);
        }
      } catch (error) {
        const message = error?.payload?.message || error.message || 'Có lỗi xảy ra. Vui lòng thử lại.';
        setFeedback(feedbackEl, 'error', message);
      } finally {
        form.classList.remove('is-loading');
      }
    });
  }

  document.addEventListener('DOMContentLoaded', () => {
    document
      .querySelectorAll('form[data-api-endpoint]')
      .forEach(initForm);
  });
})();
