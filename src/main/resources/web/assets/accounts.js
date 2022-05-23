var xhr = new XMLHttpRequest();
xhr.open('GET', '/api/accounts');

xhr.onload = function() {
    var response = JSON.parse(this.responseText);

    if (!response.success) {
        showError(response.error);
        return;
    }

    var dataDiv = document.getElementById('apiData');
    var table = document.createElement('table');
    table.appendChild(createTableHeader('ID', 'Username', 'Role', 'Active', 'Edit'));

    for (var i = 0; i < response.accounts.length; i++) {
        var tr = createTableRow(
                response.accounts[i].id,
                response.accounts[i].username,
                response.accounts[i].role,
                response.accounts[i].active,
                createLink(`/admin/accounts/${response.accounts[i].id}/edit`, 'Edit')
            );
        table.appendChild(tr);
    }

    dataDiv.appendChild(table);
};

xhr.onerror = function() {
    showError('An error occurred while requesting data from the API');
};

xhr.send();

function showError(error) {
    document.getElementById('apiError').innerText = error;
}