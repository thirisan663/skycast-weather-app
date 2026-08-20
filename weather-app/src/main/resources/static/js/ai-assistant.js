/**
 * ai-assistant.js
 * Drives the AI Weather Assistant chat widget, present both as a side
 * panel on the dashboard and as the full-page /assistant view. Listens
 * for the 'skycast:city-selected' event dispatched by weather.js so its
 * questions are grounded in whatever city is currently on screen.
 */
(function () {
  const { apiFetch, escapeHtml } = WeatherApp.utils;

  const chatWindow = document.getElementById('aiChatWindow');
  const chatForm = document.getElementById('aiChatForm');
  const chatInput = document.getElementById('aiChatInput');
  const sendBtn = document.getElementById('aiSendBtn');
  const suggestedChips = document.querySelectorAll('.ai-suggested-chip');

  if (!chatWindow || !chatForm) return; // widget not present on this page

  let activeCity = null; // { name, country, latitude, longitude }

  document.addEventListener('skycast:city-selected', (e) => {
    activeCity = e.detail;
  });

  function appendBubble(role, html, { pending = false } = {}) {
    const row = document.createElement('div');
    row.className = 'chat-bubble-row ' + role;
    if (pending) row.dataset.pending = 'true';

    const avatar = document.createElement('div');
    avatar.className = 'chat-avatar-mini';
    avatar.innerHTML = role === 'user' ? '<i class="bi bi-person-fill"></i>' : '<i class="bi bi-stars"></i>';

    const bubble = document.createElement('div');
    bubble.className = 'chat-bubble';
    bubble.innerHTML = html;

    row.appendChild(avatar);
    row.appendChild(bubble);
    chatWindow.appendChild(row);
    chatWindow.scrollTop = chatWindow.scrollHeight;
    return row;
  }

  function appendTypingIndicator() {
    return appendBubble('assistant',
      '<div class="typing-indicator"><span></span><span></span><span></span></div>',
      { pending: true });
  }

  async function sendMessage(message) {
    if (!message || !message.trim()) return;

    appendBubble('user', escapeHtml(message));
    chatInput.value = '';
    sendBtn.disabled = true;
    const typingRow = appendTypingIndicator();

    try {
      const payload = { message: message.trim() };
      if (activeCity) {
        payload.cityName = activeCity.name;
        payload.latitude = activeCity.latitude;
        payload.longitude = activeCity.longitude;
      }
      const result = await apiFetch('/api/assistant/ask', {
        method: 'POST',
        body: JSON.stringify(payload)
      });
      typingRow.remove();
      appendBubble('assistant', escapeHtml(result.reply).replace(/\n/g, '<br>'));
    } catch (err) {
      typingRow.remove();
      appendBubble('assistant',
        '<span class="text-danger"><i class="bi bi-exclamation-triangle-fill me-1"></i>' +
        escapeHtml(err.message) + '</span>');
    } finally {
      sendBtn.disabled = false;
      chatInput.focus();
    }
  }

  chatForm.addEventListener('submit', (e) => {
    e.preventDefault();
    sendMessage(chatInput.value);
  });

  suggestedChips.forEach(chip => {
    chip.addEventListener('click', () => sendMessage(chip.dataset.question || chip.textContent.trim()));
  });
})();
