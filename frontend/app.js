const logEl = document.getElementById('log');
const baseUrlInput = document.getElementById('baseUrl');

document.getElementById('clearLog').addEventListener('click', () => {
  logEl.innerHTML = '';
});

function logEntry(kind, title, detail) {
  const entry = document.createElement('div');
  entry.className = `log-entry ${kind}`;
  const time = new Date().toLocaleTimeString();
  entry.innerHTML = `<div class="meta">${time} — ${title}</div>${detail}`;
  logEl.prepend(entry);
}

async function callApi(method, path, body) {
  const baseUrl = baseUrlInput.value.replace(/\/$/, '');
  const url = `${baseUrl}${path}`;
  const options = {
    method,
    headers: { 'Content-Type': 'application/json' },
  };
  if (body !== undefined) {
    options.body = JSON.stringify(body);
  }

  logEntry('meta', `→ ${method} ${path}`, body ? `<pre>${JSON.stringify(body, null, 2)}</pre>` : '');

  try {
    const response = await fetch(url, options);
    const text = await response.text();
    let parsed;
    try {
      parsed = JSON.parse(text);
    } catch {
      parsed = text;
    }

    const kind = response.ok ? 'success' : 'error';
    logEntry(kind, `← ${response.status} ${method} ${path}`, `<pre>${JSON.stringify(parsed, null, 2)}</pre>`);
    return parsed;
  } catch (err) {
    logEntry('error', `✕ Network error on ${method} ${path}`, `<pre>${err.message}</pre>`);
    throw err;
  }
}

function formToObject(form, numericFields = []) {
  const data = Object.fromEntries(new FormData(form).entries());
  numericFields.forEach((field) => {
    if (data[field] !== undefined && data[field] !== '') {
      data[field] = Number(data[field]);
    }
  });
  return data;
}

document.getElementById('createCustomerForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const body = formToObject(e.target);
  await callApi('POST', '/api/v1/customers', body);
});

document.getElementById('createOrderForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const body = formToObject(e.target, ['customerId', 'quantity', 'price']);
  await callApi('POST', '/api/v1/orders', body);
});

document.getElementById('getOrdersForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const { customerId } = formToObject(e.target, ['customerId']);
  await callApi('GET', `/api/v1/customers/${customerId}/orders`);
});

document.getElementById('updateStatusForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const { orderId, status } = formToObject(e.target, ['orderId']);
  await callApi('PUT', `/api/v1/orders/${orderId}/status`, { status });
});
