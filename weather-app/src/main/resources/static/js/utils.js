/**
 * utils.js
 * Small shared helpers used across weather.js, favorites.js, history.js,
 * and ai-assistant.js. No dependencies, no globals beyond the `WeatherApp`
 * namespace object to avoid polluting window.
 */
window.WeatherApp = window.WeatherApp || {};

WeatherApp.utils = (function () {

  /** Maps an OpenWeatherMap-style icon code to a Bootstrap Icons class. */
  function iconFor(code) {
    if (!code) return 'bi-cloud';
    const map = {
      '01d': 'bi-sun-fill', '01n': 'bi-moon-stars-fill',
      '02d': 'bi-cloud-sun-fill', '02n': 'bi-cloud-moon-fill',
      '03d': 'bi-cloud-fill', '03n': 'bi-cloud-fill',
      '04d': 'bi-clouds-fill', '04n': 'bi-clouds-fill',
      '09d': 'bi-cloud-drizzle-fill', '09n': 'bi-cloud-drizzle-fill',
      '10d': 'bi-cloud-rain-fill', '10n': 'bi-cloud-rain-fill',
      '11d': 'bi-cloud-lightning-rain-fill', '11n': 'bi-cloud-lightning-rain-fill',
      '13d': 'bi-cloud-snow-fill', '13n': 'bi-cloud-snow-fill',
      '50d': 'bi-cloud-haze2-fill', '50n': 'bi-cloud-haze2-fill'
    };
    return map[code] || 'bi-cloud-sun-fill';
  }

  /** Maps a condition string to a hero background variant key used by CSS [data-condition]. */
  function conditionKey(condition, iconCode) {
    if (!condition) return 'default';
    const c = condition.toLowerCase();
    const isNight = iconCode && iconCode.endsWith('n');
    if (c.includes('clear')) return isNight ? 'clear-night' : 'clear';
    if (c.includes('rain') || c.includes('drizzle')) return 'rain';
    if (c.includes('thunder')) return 'thunderstorm';
    if (c.includes('cloud')) return 'clouds';
    if (c.includes('snow')) return 'snow';
    return 'default';
  }

  function formatTemp(value) {
    if (value === null || value === undefined || isNaN(value)) return '--';
    return Math.round(value) + '°';
  }

  function formatTime(isoOrDate) {
    if (!isoOrDate) return '--';
    const d = new Date(isoOrDate);
    return d.toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' });
  }

  function formatHour(isoOrDate) {
    if (!isoOrDate) return '--';
    const d = new Date(isoOrDate);
    return d.toLocaleTimeString([], { hour: 'numeric' });
  }

  function formatDayLabel(dateStr, index) {
    if (index === 0) return 'Today';
    const d = new Date(dateStr);
    return d.toLocaleDateString([], { weekday: 'short' });
  }

  function formatDate(dateStr) {
    const d = new Date(dateStr);
    return d.toLocaleDateString([], { month: 'short', day: 'numeric' });
  }

  /** Basic debounce for the search-suggestions typeahead. */
  function debounce(fn, delay) {
    let timer = null;
    return function (...args) {
      clearTimeout(timer);
      timer = setTimeout(() => fn.apply(this, args), delay);
    };
  }

  /** Thin fetch wrapper: JSON in, JSON out, throws with server message on failure. */
  async function apiFetch(url, options = {}) {
    const response = await fetch(url, {
      headers: { 'Content-Type': 'application/json' },
      ...options
    });
    if (!response.ok) {
      let message = 'Something went wrong. Please try again.';
      try {
        const body = await response.json();
        if (body && body.message) message = body.message;
      } catch (_) { /* ignore parse failure, use default message */ }
      throw new Error(message);
    }
    if (response.status === 204) return null;
    return response.json();
  }

  function escapeHtml(str) {
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
  }

  return { iconFor, conditionKey, formatTemp, formatTime, formatHour, formatDayLabel, formatDate, debounce, apiFetch, escapeHtml };
})();
