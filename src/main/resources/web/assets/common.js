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

function handleScreenSize() {
    if (document.querySelector('.navbar') === null) return;

    let navItems = document.getElementsByClassName('navbar-item');

    if (window.innerWidth > 600) {
        document.querySelector('.navbar').style.minHeight = null;

        for (var i = 0; i < navItems.length; i++) {
            navItems[i].classList.remove('navbar-hidden');
            navItems[i].classList.remove('navbar-shown');
        }

        return;
    }

    document.querySelector('.navbar').style.minHeight = `${document.querySelector('.navbar-collapse').offsetHeight+4}px`
}

handleScreenSize();

window.addEventListener('resize', () => {
    handleScreenSize();
});

(function() {
    var navIsHidden = true;
    if (document.querySelector('.navbar-collapse')) {
        document.querySelector('.navbar-collapse').addEventListener('click', (e) => {
            e.preventDefault();
            console.log('click', navIsHidden);
            let navItems = document.getElementsByClassName('navbar-item');
            if (navIsHidden) {
                for (var i = 0; i < navItems.length; i++) {
                    navItems[i].classList.remove('navbar-hidden')
                    navItems[i].classList.add('navbar-shown');
                }
                navIsHidden = false;
            } else {
                for (var i = 0; i < navItems.length; i++) {
                    navItems[i].classList.add('navbar-hidden')
                    navItems[i].classList.remove('navbar-shown');
                }
                navIsHidden = true;
            }
        });
    }
})();