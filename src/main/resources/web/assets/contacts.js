function loadContacts(directory, csrfToken) {
    var xhr = new XMLHttpRequest();
    xhr.open('GET', `/api/directories/${directory}/contacts`);

    xhr.onload = function() {
        var response = JSON.parse(this.responseText);

        if (!response.success) {
            showError(response.error);
            return;
        }

        var dataDiv = document.getElementById('apiData');
        var table = document.createElement('table');
        table.appendChild(createTableHeader('ID', 'Name', 'Telephone', 'Mobile', 'Other', 'Group', 'Edit', 'Delete'));

        for (var i = 0; i < response.contacts.length; i++) {
            var tr = createTableRow(
                    response.contacts[i].id,
                    response.contacts[i].name,
                    response.contacts[i].telephone,
                    response.contacts[i].mobile,
                    response.contacts[i].other,
                    response.contacts[i].group,
                    createLink(`/admin/directories/${directory}/contacts/${response.contacts[i].id}/edit`, 'Edit'),
                    createDeleteForm(response.contacts[i].id)
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

    function createDeleteForm(contId) {
        let form = document.createElement('form');
        form.method = 'POST';
        // IMPORTANT: Do not use $\{id} here. Maven replaces it and everything I tried to get that to stop failed
        form.action = `/admin/directories/${directory}/contacts/${contId}/delete`;

        let csrf = document.createElement('input');
        csrf.type = 'hidden';
        csrf.name = 'csrf';
        csrf.value = csrfToken;

        let button = document.createElement('button');
        button.type = 'submit';
        button.className = 'button warn-button';
        button.innerText = 'Delete';

        form.appendChild(csrf);
        form.appendChild(button);

        return form;
    }
}