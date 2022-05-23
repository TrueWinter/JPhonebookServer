function createTableHeader(...headings) {
    var tr = document.createElement('tr');

    for (var i = 0; i < headings.length; i++) {
        var th = document.createElement('th');
        if (headings[i] instanceof Element) {
            th.appendChild(headings[i]);
        } else {
            th.innerText = headings[i];
        }
        tr.appendChild(th);
    }

    return tr;
}

function createTableRow(...data) {
    var tr = document.createElement('tr');

    for (var i = 0; i < data.length; i++) {
        var td = document.createElement('td');
        if (data[i] instanceof Element) {
            td.appendChild(data[i]);
        } else {
            td.innerText = data[i];
        }
        tr.appendChild(td);
    }

    return tr;
}

function createLink(link, text) {
    var a = document.createElement('a');
    a.href = link;
    a.innerText = text;

    return a;
}