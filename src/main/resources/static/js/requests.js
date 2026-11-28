/*
 * HemoLink - Create Blood Request page (Module 3.4).
 * Live "Donor availability" box: anonymous counts only (decision D-05),
 * fetched from GET /requests/donor-availability/summary.
 */
(function () {
    'use strict';

    const box = document.querySelector('[data-availability-url]');
    const form = document.querySelector('[data-availability-form]');
    if (!box || !form) {
        return;
    }

    const url = box.getAttribute('data-availability-url');
    const groupSelect = form.querySelector('#bloodGroup');
    const cityInput = form.querySelector('#city');

    const message = box.querySelector('[data-avail-message]');
    const numbers = box.querySelector('[data-avail-numbers]');
    const cityValue = box.querySelector('[data-avail-city]');
    const cityLabel = box.querySelector('[data-avail-city-label]');
    const allValue = box.querySelector('[data-avail-all]');
    const note = box.querySelector('[data-avail-note]');

    let timer = null;
    let latest = 0;

    function show(text) {
        message.textContent = text;
        message.hidden = false;
        numbers.hidden = true;
        note.hidden = true;
    }

    function donors(n) {
        return n === 1 ? '1 donor' : n + ' donors';
    }

    function render(data) {
        const hasCity = data.compatibleInCity !== null && data.compatibleInCity !== undefined;

        message.hidden = true;
        numbers.hidden = false;
        allValue.textContent = String(data.compatibleAll);

        if (hasCity) {
            cityValue.parentElement.hidden = false;
            cityValue.textContent = String(data.compatibleInCity);
            cityLabel.textContent = 'in ' + data.city;
        } else {
            cityValue.parentElement.hidden = true;
        }

        let text;
        if (!hasCity) {
            text = 'Enter a city to see donors who can be matched to this request.';
        } else if (data.compatibleInCity === 0) {
            text = 'No compatible donors are available in ' + data.city
                + ' right now. You can still submit: new donors are matched when you refresh.';
        } else {
            text = donors(data.compatibleInCity) + ' who can give to ' + data.bloodGroup
                + ' ' + (data.compatibleInCity === 1 ? 'is' : 'are') + ' available in ' + data.city + '.';
        }
        note.textContent = text;
        note.hidden = false;
    }

    function update() {
        const group = groupSelect ? groupSelect.value : '';
        const city = cityInput ? cityInput.value.trim() : '';

        if (!group) {
            show('Choose a blood group and city to see how many compatible donors are available.');
            return;
        }

        const request = ++latest;
        const params = new URLSearchParams({ bloodGroup: group });
        if (city) {
            params.set('city', city);
        }

        box.setAttribute('aria-busy', 'true');

        fetch(url + '?' + params.toString(), {
            headers: { 'Accept': 'application/json' },
            credentials: 'same-origin'
        })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('HTTP ' + response.status);
                }
                return response.json();
            })
            .then(function (data) {
                if (request === latest) {
                    render(data);
                }
            })
            .catch(function () {
                if (request === latest) {
                    show('Availability could not be loaded. You can still submit the request.');
                }
            })
            .finally(function () {
                if (request === latest) {
                    box.removeAttribute('aria-busy');
                }
            });
    }

    function schedule() {
        clearTimeout(timer);
        timer = setTimeout(update, 350);
    }

    if (groupSelect) {
        groupSelect.addEventListener('change', update);
    }
    if (cityInput) {
        cityInput.addEventListener('input', schedule);
    }

    // Values kept after a validation error: show counts straight away.
    update();
})();
