/**
 * history.js
 * Loads and renders the user's recent search history on /history,
 * and handles the "clear history" action.
 */
(function () {
  const { iconFor, formatTemp, apiFetch, escapeHtml } = WeatherApp.utils;

  const list = document.getElementById('historyList');
  const loading = document.getElementById('historyLoading');
  const empty = document.getElementById('historyEmpty');
  const errorBox = document.getElementById('historyError');
  const clearBtn = document.getElementById('clearHistoryBtn');

  if (!list) return;

  function relativeTime(iso) {
    const then = new Date(iso).getTime();
    const diffMin = Math.round((Date.now() - then) / 60000);
    if (diffMin < 1) return 'just now';
    if (diffMin < 60) return diffMin + 'm ago';
    const diffHr = Math.round(diffMin / 60);
    if (diffHr < 24) return diffHr + 'h ago';
    return Math.round(diffHr / 24) + 'd ago';
  }

  function rowHtml(item) {
    return `
      <div class="col-12 col-md-6">
        <div class="history-card solid-card h-100">
          <div class="history-card-icon"><i class="bi bi-clock-history"></i></div>
          <div class="flex-grow-1 min-w-0">
            <p class="history-city-name text-truncate">${escapeHtml(item.city.name)}${item.city.country ? ', ' + escapeHtml(item.city.country) : ''}</p>
            <p class="history-city-meta mb-0">
              ${item.temperatureAtSearch != null ? formatTemp(item.temperatureAtSearch) : '--'} ·
              ${escapeHtml(item.conditionAtSearch || '')} · ${relativeTime(item.searchedAt)}
            </p>
          </div>
        </div>
      </div>`;
  }

  async function loadHistory() {
    loading.classList.remove('d-none');
    empty.classList.add('d-none');
    errorBox.classList.add('d-none');
    list.innerHTML = '';

    try {
      const history = await apiFetch('/api/history');
      loading.classList.add('d-none');
      if (!history || history.length === 0) {
        empty.classList.remove('d-none');
        return;
      }
      list.innerHTML = history.map(rowHtml).join('');
    } catch (err) {
      loading.classList.add('d-none');
      errorBox.textContent = err.message;
      errorBox.classList.remove('d-none');
    }
  }

  if (clearBtn) {
    clearBtn.addEventListener('click', async () => {
      clearBtn.disabled = true;
      try {
        await apiFetch('/api/history', { method: 'DELETE' });
        await loadHistory();
      } finally {
        clearBtn.disabled = false;
      }
    });
  }

  document.addEventListener('DOMContentLoaded', loadHistory);
})();
