document.addEventListener("DOMContentLoaded", function() {
    var triggerTabList = [].slice.call(document.querySelectorAll('#myTab a'))
    triggerTabList.forEach(function (triggerEl) {
        var tabTrigger = new bootstrap.Tab(triggerEl)

        triggerEl.addEventListener('click', function (event) {
            event.preventDefault()
            tabTrigger.show()
        })
    })
});

function enrollBank(bankName) {
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/kyc';

    const hiddenField = document.createElement('input');
    hiddenField.type = 'hidden';
    hiddenField.name = 'bank';
    hiddenField.value = bankName;

    form.appendChild(hiddenField);

    document.body.appendChild(form);
    form.submit();
}
