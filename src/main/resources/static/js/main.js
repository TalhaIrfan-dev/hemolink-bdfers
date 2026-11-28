/*
 * HemoLink - shared page behaviour (Module 3.1).
 * Works without any library. Every feature is optional: if its markup is
 * not on the page, it does nothing.
 */
(() => {
    const body = document.body;

    // ---------- Mobile sidebar ----------
    const toggle = document.getElementById('hlMenuToggle');
    const overlay = document.getElementById('hlSidebarOverlay');
    const setSidebar = open => {
        body.classList.toggle('sidebar-open', open);
        if (toggle) toggle.setAttribute('aria-expanded', String(open));
    };
    if (toggle) toggle.addEventListener('click', () => setSidebar(!body.classList.contains('sidebar-open')));
    if (overlay) overlay.addEventListener('click', () => setSidebar(false));

    // ---------- Escape closes sidebar and user menu ----------
    document.addEventListener('keydown', event => {
        if (event.key !== 'Escape') return;
        setSidebar(false);
        document.querySelectorAll('details.hl-user-menu[open]').forEach(menu => {
            menu.removeAttribute('open');
            const summary = menu.querySelector('summary');
            if (summary) summary.focus();
        });
    });

    // ---------- User menu: close when clicking outside ----------
    document.addEventListener('click', event => {
        document.querySelectorAll('details.hl-user-menu[open]').forEach(menu => {
            if (!menu.contains(event.target)) menu.removeAttribute('open');
        });
    });

    // ---------- Quick search: filters sidebar links ----------
    const search = document.getElementById('hlQuickSearch');
    if (search) {
        search.addEventListener('input', () => {
            const query = search.value.trim().toLowerCase();
            document.querySelectorAll('.hl-nav-link[data-search]').forEach(link => {
                link.hidden = query.length > 0 && !link.dataset.search.toLowerCase().includes(query);
            });
            document.querySelectorAll('.hl-nav-label').forEach(label => {
                const nav = label.nextElementSibling;
                if (!query) { label.hidden = false; return; }
                if (nav && nav.classList.contains('hl-nav')) {
                    label.hidden = !Array.from(nav.querySelectorAll('.hl-nav-link')).some(l => !l.hidden);
                }
            });
        });
        search.addEventListener('keydown', event => {
            if (event.key !== 'Enter') return;
            const first = document.querySelector('.hl-nav-link[data-search]:not([hidden])');
            if (first) { event.preventDefault(); first.click(); }
        });
    }

    // ---------- Confirmation dialog for important actions ----------
    // <form data-confirm="Close this request?" data-confirm-button="Close request">
    const dialog = document.getElementById('hlConfirmDialog');
    if (dialog && typeof dialog.showModal === 'function') {
        const message = document.getElementById('hlConfirmMessage');
        const okButton = document.getElementById('hlConfirmOk');
        let pendingForm = null;

        document.addEventListener('submit', event => {
            const form = event.target;
            if (!(form instanceof HTMLFormElement) || !form.dataset.confirm) return;
            if (form.dataset.confirmed === 'true') return;

            // Let the browser show required-field messages first.
            if (!form.checkValidity()) return;

            event.preventDefault();
            pendingForm = form;
            message.textContent = form.dataset.confirm;
            okButton.textContent = form.dataset.confirmButton || 'Confirm';
            dialog.showModal();
        });

        dialog.addEventListener('close', () => {
            if (dialog.returnValue === 'confirm' && pendingForm) {
                pendingForm.dataset.confirmed = 'true';
                pendingForm.requestSubmit ? pendingForm.requestSubmit() : pendingForm.submit();
            }
            pendingForm = null;
        });
    }

    // ---------- Password show / hide ----------
    document.querySelectorAll('[data-toggle-password]').forEach(button => {
        button.addEventListener('click', () => {
            const input = document.getElementById(button.dataset.togglePassword);
            if (!input) return;
            const show = input.type === 'password';
            input.type = show ? 'text' : 'password';
            button.setAttribute('aria-pressed', String(show));
            button.setAttribute('aria-label', show ? 'Hide password' : 'Show password');
        });
    });

    // ---------- Donation report: units and date only when completed ----------
    document.querySelectorAll('[data-outcome-form]').forEach(form => {
        const select = form.querySelector('[name="outcome"]');
        const fields = form.querySelector('[data-completed-fields]');
        if (!select || !fields) return;
        const inputs = fields.querySelectorAll('input');
        const update = () => {
            const completed = select.value === 'COMPLETED';
            fields.hidden = !completed;
            inputs.forEach(input => {
                input.required = completed;
                if (!completed) input.value = '';
            });
        };
        select.addEventListener('change', update);
        update();
    });

    // ---------- Filter panels: collapsed on phones unless a filter is in use ----------
    if (window.matchMedia('(max-width: 640px)').matches && !window.location.search) {
        document.querySelectorAll('details[data-collapse-mobile]').forEach(d => { d.open = false; });
    }

    // ---------- Prevent double submission ----------
    document.addEventListener('submit', event => {
        const form = event.target;
        if (!(form instanceof HTMLFormElement) || event.defaultPrevented) return;
        if (form.method.toLowerCase() !== 'post') return;
        form.querySelectorAll('button[type="submit"]').forEach(btn => {
            setTimeout(() => { btn.disabled = true; }, 0);
        });
    });
})();
