(() => {
    const body = document.body;
    const toggle = document.getElementById('hlMenuToggle');
    const overlay = document.getElementById('hlSidebarOverlay');
    const closeSidebar = () => body.classList.remove('sidebar-open');

    if (toggle) {
        toggle.addEventListener('click', () => body.classList.toggle('sidebar-open'));
    }
    if (overlay) overlay.addEventListener('click', closeSidebar);
    document.addEventListener('keydown', event => {
        if (event.key === 'Escape') closeSidebar();
    });

    const search = document.getElementById('hlQuickSearch');
    if (search) {
        search.addEventListener('input', () => {
            const query = search.value.trim().toLowerCase();
            document.querySelectorAll('.hl-nav-link[data-search]').forEach(link => {
                link.hidden = query.length > 0 && !link.dataset.search.toLowerCase().includes(query);
            });
            document.querySelectorAll('.hl-nav-label, .hl-sidebar-divider').forEach(element => {
                if (!query) {
                    element.hidden = false;
                    return;
                }
                const nav = element.nextElementSibling;
                if (nav && nav.classList.contains('hl-nav')) {
                    element.hidden = !Array.from(nav.querySelectorAll('.hl-nav-link')).some(link => !link.hidden);
                } else if (element.classList.contains('hl-sidebar-divider')) {
                    element.hidden = true;
                }
            });
        });
    }
})();
