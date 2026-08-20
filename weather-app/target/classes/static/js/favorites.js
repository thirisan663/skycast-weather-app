/**
 * favorites.js
 * Loads and renders the user's pinned cities on the /favorites page,
 * each with a live mini weather snapshot, and handles removal.
 */
(function () {
  const { iconFor, formatTemp, apiFetch, escapeHtml } = WeatherApp.utils;

  const list = document.getElementById('favoritesList');
  const loading = document.getElementById('favoritesLoading');
  const empty = document.getElementById('favoritesEmpty');
  const errorBox = document.getElementById('favoritesError');

  if (!list) return;

  function cardHtml(fav) {
    const city = fav.city;
    return `
      <div class="col-12 col-md-6 col-xl-4">
        <div class="favorite-card solid-card hoverable h-100" data-favorite-id="${fav.favoriteId}">
          <div class="favorite-card-icon"><i class="bi ${iconFor(fav.iconCode)}"></i></div>
          <div class="flex-grow-1 min-w-0">
            <p class="favorite-city-name text-truncate">${escapeHtml(city.name)}</p>
            <p class="favorite-city-meta mb-1">${escapeHtml(city.country || '')}</p>
            <p class="fw-bold mb-0">${fav.currentTemperature != null ? formatTemp(fav.currentTemperature) : '--'}
              <span class="fw-normal text-muted-custom small">${escapeHtml(fav.currentCondition || '')}</span>
            </p>
          </div>
          <button class="favorite-remove-btn" title="Remove from favorites" aria-label="Remove favorite">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>
      </div>`;
  }

  async function loadFavorites() {
    loading.classList.remove('d-none');
    empty.classList.add('d-none');
    errorBox.classList.add('d-none');
    list.innerHTML = '';

    try {
      const favorites = await apiFetch('/api/favorites');
      loading.classList.add('d-none');
      if (!favorites || favorites.length === 0) {
        empty.classList.remove('d-none');
        return;
      }
      list.innerHTML = favorites.map(cardHtml).join('');
    } catch (err) {
      loading.classList.add('d-none');
      errorBox.textContent = err.message;
      errorBox.classList.remove('d-none');
    }
  }

  list.addEventListener('click', async (e) => {
    const btn = e.target.closest('.favorite-remove-btn');
    if (!btn) return;
    const card = btn.closest('[data-favorite-id]');
    const id = card.dataset.favoriteId;
    btn.disabled = true;
    try {
      await apiFetch('/api/favorites/' + id, { method: 'DELETE' });
      card.closest('.col-12').remove();
      if (!list.children.length) empty.classList.remove('d-none');
    } catch (_) {
      btn.disabled = false;
    }
  });

  document.addEventListener('DOMContentLoaded', loadFavorites);
})();
