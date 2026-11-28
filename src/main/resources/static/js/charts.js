/*
 * HemoLink chart helper (Module 3.1). Styles Chart.js in the HemoLink
 * colours and loads trend data from the dashboard endpoints.
 *
 * Chart.js is a saved copy at /js/vendor/chart.umd.min.js (decision D-07).
 * If that file is missing, each chart shows a clear message instead.
 *
 * Markup:
 *   <div class="hl-chart-wrap" data-trend-url="/dashboard/trend">
 *       <canvas aria-label="..." role="img"></canvas>
 *       <div class="hl-chart-message">...</div>
 *   </div>
 *   Period tabs: <button class="hl-tab" data-period="WEEK" data-chart-target="id">
 *   Year select: <select data-year-for="id">
 *
 *   <div class="hl-donut-wrap"><canvas data-donut-labels='[...]' data-donut-values='[...]'></canvas></div>
 */
(() => {
    const COLORS = {
        requests: '#0b5d4f',
        matches: '#4fd1a5',
        grid: '#e8efed',
        text: '#52677a',
        donut: ['#0b5d4f', '#12ad7b', '#4fd1a5', '#9be7c8', '#087566', '#d97706', '#64748b', '#c81e35']
    };

    function unavailable(wrap, text) {
        wrap.classList.add('is-unavailable');
        const msg = wrap.querySelector('.hl-chart-message');
        if (msg && text) msg.textContent = text;
    }

    const chartJsMissing = typeof window.Chart === 'undefined';
    const MISSING_TEXT = 'Charts are unavailable: Chart.js has not been added to /static/js/vendor/.';

    if (!chartJsMissing) {
        Chart.defaults.font.family = 'Inter, ui-sans-serif, system-ui, -apple-system, "Segoe UI", sans-serif';
        Chart.defaults.color = COLORS.text;
    }

    // ---------- Trend line charts ----------
    const trendCharts = {};

    function renderTrend(wrap, data) {
        const canvas = wrap.querySelector('canvas');
        const id = wrap.id;
        const allZero = data.requests.every(v => v === 0) && data.matches.every(v => v === 0);

        if (trendCharts[id]) trendCharts[id].destroy();

        trendCharts[id] = new Chart(canvas, {
            type: 'line',
            data: {
                labels: data.labels,
                datasets: [
                    {
                        label: 'Requests', data: data.requests, borderColor: COLORS.requests,
                        backgroundColor: 'rgba(11, 93, 79, .10)', fill: true, tension: .4,
                        borderWidth: 2.5, pointRadius: 0, pointHoverRadius: 5
                    },
                    {
                        label: 'Matches', data: data.matches, borderColor: COLORS.matches,
                        backgroundColor: 'rgba(79, 209, 165, .12)', fill: true, tension: .4,
                        borderWidth: 2.5, pointRadius: 0, pointHoverRadius: 5
                    }
                ]
            },
            options: {
                responsive: true, maintainAspectRatio: false,
                interaction: { mode: 'index', intersect: false },
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: '#0b5d4f', padding: 10, cornerRadius: 10,
                        titleFont: { weight: '700' }, displayColors: true
                    }
                },
                scales: {
                    x: { grid: { display: false }, ticks: { maxRotation: 0, autoSkip: true, maxTicksLimit: 12 } },
                    y: { beginAtZero: true, grid: { color: COLORS.grid }, ticks: { precision: 0 }, suggestedMax: allZero ? 5 : undefined }
                }
            }
        });
        canvas.setAttribute('aria-label',
            'Trend chart: ' + data.requests.reduce((a, b) => a + b, 0) + ' requests and '
            + data.matches.reduce((a, b) => a + b, 0) + ' matches in the selected period.');
    }

    function loadTrend(wrap) {
        if (chartJsMissing) { unavailable(wrap, MISSING_TEXT); return; }
        const params = new URLSearchParams({ period: wrap.dataset.period || 'YEAR' });
        if (wrap.dataset.year) params.set('year', wrap.dataset.year);

        fetch(wrap.dataset.trendUrl + '?' + params, { headers: { Accept: 'application/json' } })
            .then(response => {
                if (!response.ok) throw new Error('HTTP ' + response.status);
                return response.json();
            })
            .then(data => { wrap.classList.remove('is-unavailable'); renderTrend(wrap, data); })
            .catch(() => unavailable(wrap, 'The chart could not be loaded. Please refresh the page.'));
    }

    document.querySelectorAll('.hl-chart-wrap[data-trend-url]').forEach(loadTrend);

    document.querySelectorAll('.hl-tab[data-period][data-chart-target]').forEach(tab => {
        tab.addEventListener('click', () => {
            const wrap = document.getElementById(tab.dataset.chartTarget);
            if (!wrap) return;
            document.querySelectorAll('.hl-tab[data-chart-target="' + tab.dataset.chartTarget + '"]')
                .forEach(t => t.setAttribute('aria-selected', String(t === tab)));
            wrap.dataset.period = tab.dataset.period;
            const yearSelect = document.querySelector('[data-year-for="' + wrap.id + '"]');
            if (yearSelect) yearSelect.disabled = tab.dataset.period !== 'YEAR';
            loadTrend(wrap);
        });
    });

    document.querySelectorAll('select[data-year-for]').forEach(select => {
        select.addEventListener('change', () => {
            const wrap = document.getElementById(select.dataset.yearFor);
            if (!wrap) return;
            wrap.dataset.year = select.value;
            loadTrend(wrap);
        });
    });

    // ---------- Donut charts ----------
    document.querySelectorAll('canvas[data-donut-values]').forEach(canvas => {
        const wrap = canvas.closest('.hl-donut-wrap, .hl-chart-wrap');
        if (chartJsMissing) { if (wrap) unavailable(wrap, MISSING_TEXT); return; }

        let labels = [], values = [];
        try {
            labels = JSON.parse(canvas.dataset.donutLabels || '[]');
            values = JSON.parse(canvas.dataset.donutValues || '[]');
        } catch (e) { return; }
        if (values.length === 0) return;

        new Chart(canvas, {
            type: 'doughnut',
            data: { labels, datasets: [{ data: values, backgroundColor: COLORS.donut, borderWidth: 2, borderColor: '#fff' }] },
            options: {
                responsive: true, maintainAspectRatio: false, cutout: '68%',
                plugins: { legend: { display: false }, tooltip: { backgroundColor: '#0b5d4f', cornerRadius: 10 } }
            }
        });
    });

    // Expose colours so legends in the page can match.
    window.HemoCharts = { colors: COLORS };
})();
