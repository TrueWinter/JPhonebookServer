function loadDirectories(csrfToken) {
    var xhr = new XMLHttpRequest();
    xhr.open('GET', '/api/directories');

    xhr.onload = function() {
        var response = JSON.parse(this.responseText);

        if (!response.success) {
            showError(response.error);
            return;
        }

        var dataDiv = document.getElementById('apiData');
        var table = document.createElement('table');
        table.appendChild(createTableHeader('ID', 'Name', 'Edit', 'Contacts', 'Delete'));

        for (var i = 0; i < response.directories.length; i++) {
            var editLink = new PaddedElement(createLink(`/admin/directories/${response.directories[i].id}/edit`, 'Edit'), 16, 16);
            var contactsLink = new PaddedElement(createLink(`/admin/directories/${response.directories[i].id}/contacts`, 'Contacts'), 16, 16);
            var deleteButton = new PaddedElement(createDeleteForm(response.directories[i].id), 16, 16);
            var tr = createTableRow(
                    response.directories[i].id,
                    response.directories[i].name,
                    editLink,
                    contactsLink,
                    deleteButton
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

    function createDeleteForm(dirId) {
        let form = document.createElement('form');
        form.method = 'POST';
        // IMPORTANT: Do not use $\{id} here. Maven replaces it and everything I tried to get that to stop failed
        form.action = `/admin/directories/${dirId}/delete`;

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