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

function openConnectModal() {
    var connectModal = new bootstrap.Modal(document.getElementById('connectModal'));
    connectModal.show();
}

function submitConnectForm() {
    var connectForm = document.getElementById('connectForm');
    var loadingModal = new bootstrap.Modal(document.getElementById('loadingModal'));
    loadingModal.show();
    connectForm.submit();
}

function openEnrollModal() {
    var enrollModal = new bootstrap.Modal(document.getElementById('enrollModal'));
    enrollModal.show();
}

function submitEnrollForm() {
    var enrollForm = document.getElementById('enrollForm');
    enrollForm.submit();
}

function openExecuteModal(contract) {
    document.getElementById('contractName').value = contract;
    var executeModal = new bootstrap.Modal(document.getElementById('executeModal'));
    executeModal.show();
}

function submitExecuteForm() {
    var executeForm = document.getElementById('executeForm');
    executeForm.submit();
}
