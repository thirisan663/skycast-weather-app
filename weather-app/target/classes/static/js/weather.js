/**
 * weather.js
 * Powers the dashboard (index page): search box + typeahead, "use my
 * location", and rendering of the current-weather hero, stat tiles,
 * hourly strip, and 7-day forecast. All data comes from /api/weather/*.
 */
(function () {
  const { iconFor, conditionKey, formatTemp, formatHour, formatDayLabel, formatDate, debounce, apiFetch, escapeHtml } = WeatherApp.utils;

  const els = {
    form: document.getElementById('citySearchForm'),
    input: document.getElementById('citySearchInput'),
    suggestions: document.getElementById('searchSuggestions'),
    locateBtn: document.getElementById('locateMeBtn'),
    dashboard: document.getElementById('dashboardContent'),
    loading: document.getElementById('dashboardLoading'),
    empty: document.getElementById('dashboardEmpty'),
    errorBox: document.getElementById('dashboardError'),
    errorText: document.getElementById('dashboardErrorText'),

    heroCard: document.getElementById('heroCard'),
    heroLocation: document.getElementById('heroLocation'),
    heroUpdated: document.getElementById('heroUpdated'),
    heroTemp: document.getElementById('heroTemp'),
    heroCondition: document.getElementById('heroCondition'),
    heroFeelsLike: document.getElementById('heroFeelsLike'),
    heroIcon: document.getElementById('heroIcon'),
    heroMax: document.getElementById('heroMax'),
    heroMin: document.getElementById('heroMin'),
    favoriteBtn: document.getElementById('addFavoriteBtn'),

    statHumidity: document.getElementById('statHumidity'),
    statWind: document.getElementById('statWind'),
    statWindSub: document.getElementById('statWindSub'),
    statPressure: document.getElementById('statPressure'),
    statVisibility: document.getElementById('statVisibility'),
    statUv: document.getElementById('statUv'),
    statSunrise: document.getElementById('statSunrise'),
    statSunset: document.getElementById('statSunset'),

    hourlyStrip: document.getElementById('hourlyStrip'),
    dailyList: document.getElementById('dailyList'),
  };

  let currentCity = null; // { name, country, latitude, longitude } - shared with ai-assistant.js

  function showState(state) {
    // state: 'loading' | 'content' | 'empty' | 'error'
    if (els.loading) els.loading.classList.toggle('d-none', state !== 'loading');
    if (els.dashboard) els.dashboard.classList.toggle('d-none', state !== 'content');
    if (els.empty) els.empty.classList.toggle('d-none', state !== 'empty');
    if (els.errorBox) els.errorBox.classList.toggle('d-none', state !== 'error');
  }

  function renderHero(current) {
    if (!els.heroCard) return;
    els.heroCard.setAttribute('data-condition', conditionKey(current.condition, current.iconCode));
    els.heroLocation.textContent = [current.cityName, current.country].filter(Boolean).join(', ');
    els.heroUpdated.textContent = 'Updated ' + new Date().toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' });
    els.heroTemp.textContent = formatTemp(current.temperature);
    els.heroCondition.textContent = current.conditionDetail || current.condition || '--';
    els.heroFeelsLike.textContent = 'Feels like ' + formatTemp(current.feelsLike);
    els.heroIcon.className = 'bi ' + iconFor(current.iconCode);
    els.heroMax.textContent = formatTemp(current.tempMax);
    els.heroMin.textContent = formatTemp(current.tempMin);
  }

  function renderStats(current) {
    if (els.statHumidity) els.statHumidity.textContent = (current.humidity ?? '--') + '%';
    if (els.statWind) els.statWind.textContent = (current.windSpeed ?? '--') + ' km/h';
    if (els.statWindSub) els.statWindSub.textContent = current.windDirectionCompass || '';
    if (els.statPressure) els.statPressure.textContent = (current.pressure ?? '--') + ' hPa';
    if (els.statVisibility) els.statVisibility.textContent = (current.visibility != null ? current.visibility.toFixed(1) : '--') + ' km';
    if (els.statUv) els.statUv.textContent = current.uvIndex != null ? current.uvIndex : 'N/A';
    if (els.statSunrise) els.statSunrise.textContent = current.sunrise ? WeatherApp.utils.formatTime(current.sunrise) : '--';
    if (els.statSunset) els.statSunset.textContent = current.sunset ? WeatherApp.utils.formatTime(current.sunset) : '--';
  }

  function renderHourly(hourly) {
    if (!els.hourlyStrip) return;
    if (!hourly || hourly.length === 0) {
      els.hourlyStrip.innerHTML = '<p class="text-muted-custom small mb-0">Hourly forecast unavailable.</p>';
      return;
    }
    els.hourlyStrip.innerHTML = hourly.map(h => `
      <div class="hourly-card solid-card">
        <span class="hourly-time">${formatHour(h.time)}</span>
        <i class="bi ${iconFor(h.iconCode)} hourly-icon"></i>
        <span class="hourly-temp">${formatTemp(h.temperature)}</span>
        <span class="hourly-pop"><i class="bi bi-droplet-fill"></i>${h.precipitationChance ?? 0}%</span>
      </div>
    `).join('');
  }

  function renderDaily(daily) {
    if (!els.dailyList) return;
    if (!daily || daily.length === 0) {
      els.dailyList.innerHTML = '<p class="text-muted-custom small mb-0">7-day forecast unavailable.</p>';
      return;
    }
    const globalMax = Math.max(...daily.map(d => d.tempMax ?? 0));
    const globalMin = Math.min(...daily.map(d => d.tempMin ?? 0));
    const range = Math.max(1, globalMax - globalMin);

    els.dailyList.innerHTML = daily.map((d, i) => {
      const widthPct = Math.round((((d.tempMax ?? globalMin) - globalMin) / range) * 100);
      return `
      <div class="daily-row">
        <div>
          <div class="daily-day">${formatDayLabel(d.date, i)}</div>
          <div class="daily-date">${formatDate(d.date)}</div>
        </div>
        <div class="daily-icon-condition">
          <i class="bi ${iconFor(d.iconCode)}"></i>
          <span>${escapeHtml(d.condition || '')}</span>
        </div>
        <div class="daily-pop"><i class="bi bi-droplet-fill me-1"></i>${d.precipitationChance ?? 0}%</div>
        <div class="daily-temps">
          <span class="daily-temp-min">${formatTemp(d.tempMin)}</span>
          <span class="daily-temp-bar"><span style="width:${Math.max(15, widthPct)}%"></span></span>
          <span class="daily-temp-max">${formatTemp(d.tempMax)}</span>
        </div>
      </div>`;
    }).join('');
  }

  function render(weatherResponse) {
    const current = weatherResponse.current;
    currentCity = {
      name: current.cityName,
      country: current.country,
      latitude: current.latitude,
      longitude: current.longitude
    };
    renderHero(current);
    renderStats(current);
    renderHourly(weatherResponse.hourly);
    renderDaily(weatherResponse.daily);
    showState('content');

    // Let ai-assistant.js know a city is now active, so it can ground its answers.
    document.dispatchEvent(new CustomEvent('skycast:city-selected', { detail: currentCity }));

    if (els.favoriteBtn) els.favoriteBtn.classList.remove('d-none');
  }

  async function searchCity(query) {
    if (!query || !query.trim()) return;
    showState('loading');
    try {
      const result = await apiFetch('/api/weather/search', {
        method: 'POST',
        body: JSON.stringify({ query: query.trim() })
      });
      render(result);
    } catch (err) {
      els.errorText.textContent = err.message;
      showState('error');
    }
  }

  async function searchByCoordinates(lat, lon, label) {
    showState('loading');
    try {
      const params = new URLSearchParams({ lat, lon });
      if (label) params.set('cityName', label);
      const result = await apiFetch('/api/weather/coordinates?' + params.toString());
      render(result);
    } catch (err) {
      els.errorText.textContent = err.message;
      showState('error');
    }
  }

  // ---------------- Search form ----------------
  if (els.form) {
    els.form.addEventListener('submit', (e) => {
      e.preventDefault();
      searchCity(els.input.value);
      hideSuggestions();
    });
  }

  // ---------------- Typeahead ----------------
  function hideSuggestions() {
    if (els.suggestions) {
      els.suggestions.classList.remove('show');
      els.suggestions.innerHTML = '';
    }
  }

  const fetchSuggestions = debounce(async function (query) {
    if (!query || query.trim().length < 2) {
      hideSuggestions();
      return;
    }
    try {
      const results = await apiFetch('/api/weather/suggest?query=' + encodeURIComponent(query));
      if (!results || results.length === 0) {
        hideSuggestions();
        return;
      }
      els.suggestions.innerHTML = results.map(c => `
        <div class="search-suggestion-item" data-lat="${c.latitude}" data-lon="${c.longitude}" data-name="${escapeHtml(c.name)}">
          <i class="bi bi-geo-alt-fill"></i>
          <span>${escapeHtml(c.name)}${c.state ? ', ' + escapeHtml(c.state) : ''}</span>
          <small>${escapeHtml(c.country || '')}</small>
        </div>
      `).join('');
      els.suggestions.classList.add('show');
    } catch (_) {
      hideSuggestions();
    }
  }, 350);

  if (els.input) {
    els.input.addEventListener('input', (e) => fetchSuggestions(e.target.value));
    els.input.addEventListener('blur', () => setTimeout(hideSuggestions, 150));
  }

  if (els.suggestions) {
    els.suggestions.addEventListener('mousedown', (e) => {
      const item = e.target.closest('.search-suggestion-item');
      if (!item) return;
      const lat = parseFloat(item.dataset.lat);
      const lon = parseFloat(item.dataset.lon);
      els.input.value = item.dataset.name;
      hideSuggestions();
      searchByCoordinates(lat, lon, item.dataset.name);
    });
  }

  // ---------------- Use my location ----------------
  if (els.locateBtn) {
    els.locateBtn.addEventListener('click', () => {
      if (!navigator.geolocation) {
        els.errorText.textContent = 'Geolocation is not supported by your browser.';
        showState('error');
        return;
      }
      showState('loading');
      navigator.geolocation.getCurrentPosition(
        (pos) => searchByCoordinates(pos.coords.latitude, pos.coords.longitude),
        () => {
          els.errorText.textContent = 'Location access was denied. Try searching for a city instead.';
          showState('error');
        }
      );
    });
  }

  // ---------------- Add to favorites ----------------
  if (els.favoriteBtn) {
    els.favoriteBtn.addEventListener('click', async () => {
      if (!currentCity) return;
      els.favoriteBtn.disabled = true;
      try {
        await apiFetch('/api/favorites', {
          method: 'POST',
          body: JSON.stringify({
            cityName: currentCity.name,
            country: currentCity.country,
            latitude: currentCity.latitude,
            longitude: currentCity.longitude
          })
        });
        els.favoriteBtn.innerHTML = '<i class="bi bi-star-fill me-1"></i>Saved';
      } catch (_) {
        els.favoriteBtn.disabled = false;
      }
    });
  }

  // ---------------- Initial load: default city so dashboard isn't empty ----------------
  document.addEventListener('DOMContentLoaded', () => {
    if (els.dashboard) {
      // A sensible default so first-time visitors see a populated dashboard.
      searchByCoordinates(51.5072, -0.1276, 'London');
    }
  });
})();
