const DEFAULT_HEADERS = {
  'Content-Type': 'application/json',
  Accept: 'application/json'
};

async function apiRequest(url, options = {}) {
  const settings = {
    method: 'GET',
    credentials: 'include',
    headers: DEFAULT_HEADERS,
    ...options,
    headers: {
      ...DEFAULT_HEADERS,
      ...(options.headers || {})
    }
  };

  const response = await fetch(url, settings);
  const contentType = response.headers.get('content-type') || '';
  let payload = null;

  if (contentType.includes('application/json')) {
    payload = await response.json();
  } else {
    const text = await response.text();
    payload = { message: text };
  }

  if (!response.ok) {
    const error = new Error(payload?.message || 'Yêu cầu thất bại');
    error.status = response.status;
    error.payload = payload;
    throw error;
  }

  return payload;
}

function serializeForm(form) {
  const formData = new FormData(form);
  const data = {};
  formData.forEach((value, key) => {
    if (data[key]) {
      if (!Array.isArray(data[key])) {
        data[key] = [data[key]];
      }
      data[key].push(value);
    } else {
      data[key] = value;
    }
  });
  return data;
}

function setFeedback(container, type, message) {
  if (!container) return;
  container.textContent = message;
  container.style.display = 'block';
  container.classList.remove('alert-success', 'alert-error');
  container.classList.add(type === 'success' ? 'alert-success' : 'alert-error');
}

function clearFeedback(container) {
  if (!container) return;
  container.textContent = '';
  container.style.display = 'none';
  container.classList.remove('alert-success', 'alert-error');
}

window.EVSwapApi = {
  request: apiRequest,
  serializeForm,
  setFeedback,
  clearFeedback
};
