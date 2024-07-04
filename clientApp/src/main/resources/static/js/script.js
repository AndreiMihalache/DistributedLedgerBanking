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

function openFunctionModal(bank, channel) {
    document.getElementById('functionBankName').value = bank;
    document.getElementById('functionChannelName').value = channel;
    var functionModal = new bootstrap.Modal(document.getElementById('functionModal'));
    functionModal.show();
}

function loadFunctionForm() {
    var selectedFunction = document.getElementById('function').value;
    var parametersDiv = document.getElementById('functionParameters');
    parametersDiv.innerHTML = '';

    if (selectedFunction === 'createAsset') {
        parametersDiv.innerHTML += '<div class="form-group"><label for="assetID">Asset ID:</label><input type="text" id="assetID" name="assetID" class="form-control"></div>';
        parametersDiv.innerHTML += '<div class="form-group"><label for="color">Color:</label><input type="text" id="color" name="color" class="form-control"></div>';
        parametersDiv.innerHTML += '<div class="form-group"><label for="size">Size:</label><input type="text" id="size" name="size" class="form-control"></div>';
        parametersDiv.innerHTML += '<div class="form-group"><label for="owner">Owner:</label><input type="text" id="owner" name="owner" class="form-control"></div>';
        parametersDiv.innerHTML += '<div class="form-group"><label for="appraisedValue">Appraised Value:</label><input type="text" id="appraisedValue" name="appraisedValue" class="form-control"></div>';
    } else if (selectedFunction === 'readAsset') {
        parametersDiv.innerHTML += '<div class="form-group"><label for="assetID">Asset ID:</label><input type="text" id="assetID" name="assetID" class="form-control"></div>';
    }
}

function submitFunctionForm() {
    var functionForm = document.getElementById('functionForm');
    var selectedFunction = document.getElementById('function').value;

    if (selectedFunction === 'createAsset') {
        var bankName = document.getElementById('functionBankName').value;
        var channelName = document.getElementById('functionChannelName').value;
        var assetID = document.getElementById('assetID').value;
        var color = document.getElementById('color').value;
        var size = document.getElementById('size').value;
        var owner = document.getElementById('owner').value;
        var appraisedValue = document.getElementById('appraisedValue').value;

        fetch('/contracts/createAsset', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                bank: bankName,
                channel: channelName,
                assetID: assetID,
                color: color,
                size: size,
                owner: owner,
                appraisedValue: appraisedValue
            })
        })
            .then(response => response.text())
            .then(result => {
                alert('Asset created successfully: ' + result);
                var functionModal = bootstrap.Modal(document.getElementById('functionModal'));
                functionModal.hide();
            })
            .catch(error => {
                alert('Error creating asset: ' + error);
            });
    } else if (selectedFunction === 'readAsset') {
        var bankName = document.getElementById('functionBankName').value;
        var channelName = document.getElementById('functionChannelName').value;
        var assetID = document.getElementById('assetID').value;

        fetch(`/contracts/readAsset?bank=${bankName}&channel=${channelName}&assetID=${assetID}`)
            .then(response => response.json())
            .then(result => {
                var parametersForm = document.getElementById('functionParametersForm');
                parametersForm.innerHTML = ''; // Clear previous parameters
                parametersForm.innerHTML += '<div class="form-group"><label>Asset:</label><textarea class="form-control" readonly>' + JSON.stringify(result, null, 2) + '</textarea></div>';
                var parametersModal = new bootstrap.Modal(document.getElementById('functionParametersModal'));
                parametersModal.show();
            })
            .catch(error => {
                alert('Error reading asset: ' + error);
            });
    } else if (selectedFunction === 'readAllAssets') {
        var bankName = document.getElementById('functionBankName').value;
        var channelName = document.getElementById('functionChannelName').value;

        fetch(`/contracts/readAllAssets?bank=${bankName}&channel=${channelName}`)
            .then(response => response.json())
            .then(result => {
                var parametersForm = document.getElementById('functionParametersForm');
                parametersForm.innerHTML = ''; // Clear previous parameters
                parametersForm.innerHTML += '<div class="form-group"><label>Assets:</label><textarea class="form-control" readonly>' + JSON.stringify(result, null, 2) + '</textarea></div>';
                var parametersModal = new bootstrap.Modal(document.getElementById('functionParametersModal'));
                parametersModal.show();
            })
            .catch(error => {
                alert('Error reading assets: ' + error);
            });
    }
}