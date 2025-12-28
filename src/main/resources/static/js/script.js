
/*    Animated Counters (Dashboard)    */
// числата се въртят от 0 до крайната стойност плавно
document.addEventListener("DOMContentLoaded", () => {
    const counters = document.querySelectorAll('.stat-number');
    counters.forEach(counter => {
        const target = +counter.getAttribute('data-target');
        const duration = 3000; // ms
        const increment = target / (duration / 16); // 60fps

        let current = 0;
        const updateCount = () => {
            current += increment;
            if (current < target) {
                counter.innerText = Math.ceil(current);
                requestAnimationFrame(updateCount);
            } else {
                counter.innerText = target;
            }
        };
        updateCount();
    });
});

/*    Active Link Handling (Navigation)   */
// oцветява бутона в менюто, който отговаря на текущата страница
document.addEventListener("DOMContentLoaded", function () {
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.nav-link');

    navLinks.forEach(link => {
        link.classList.remove('active');
    });

    navLinks.forEach(link => {
        const href = link.getAttribute('href');
        if (href === '/' && currentPath === '/') {
            link.classList.add('active');
        }
        else if (href !== '/' && currentPath.startsWith(href)) {
            link.classList.add('active');
        }
    });
});

// Employee Management Modal Logic
document.addEventListener('DOMContentLoaded', function () {
    const manageButtons = document.querySelectorAll('.btn-manage-employees');
    const modalElement = document.getElementById('manageEmployeesModal');

    // Guard клауза - скрипт, който се зарежда на всяка страница
    // ако сме на страница, в която модалът не съществува, този ред спира изпълнението,
    // за да не гърми конзолата с грешки от сорта "Cannot read property of null"
    if (!modalElement) return;

    const modal = new bootstrap.Modal(modalElement);
    const modalTitle = document.getElementById('manageModalTitle');
    const employeeList = document.getElementById('manageEmployeeList');
    const availableSelect = document.getElementById('availableEmployeeSelect');
    const assignForm = document.getElementById('assignEmployeeForm');
    const createForm = document.getElementById('createEmployeeForm');
    let currentCompanyId = null;

    // при клик на "Manage" се взима ID-то на фирмата от бутона,
    // зареждат се данните от сървъра и чак тогава се показва модала
    manageButtons.forEach(btn => {
        btn.addEventListener('click', function () {
            currentCompanyId = this.getAttribute('data-company-id');
            loadCompanyData(currentCompanyId);
            modal.show();
        });
    });

    if (assignForm) {
        assignForm.addEventListener('submit', function (e) {
            e.preventDefault();
            const employeeId = availableSelect.value;
            if (!employeeId || !currentCompanyId) return;

            // POST заявка за назначаване на съществуващ служител
            fetch(`/companies/${currentCompanyId}/employees/${employeeId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
                .then(response => {
                    if (response.ok) {
                        loadCompanyData(currentCompanyId); // Reload data
                        // Switch to list tab
                        const triggerEl = document.querySelector('#manageTabs button[data-bs-target="#list-pane"]');
                        bootstrap.Tab.getInstance(triggerEl).show();
                    } else {
                        alert('Failed to assign employee.');
                    }
                })
                .catch(error => console.error('Error:', error));
        });
    }

    if (createForm) {
        createForm.addEventListener('submit', function (e) {
            e.preventDefault();
            if (!currentCompanyId) return;

            const formData = new FormData(createForm);
            const data = {
                firstName: formData.get('firstName'),
                lastName: formData.get('lastName')
            };

            fetch(`/companies/${currentCompanyId}/employees/create`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            })
                .then(response => {
                    if (response.ok) {
                        createForm.reset();
                        loadCompanyData(currentCompanyId); // Reload data
                        // Switch to list tab
                        const triggerEl = document.querySelector('#manageTabs button[data-bs-target="#list-pane"]');
                        const tabInstance = new bootstrap.Tab(triggerEl);
                        tabInstance.show();
                    } else {
                        alert('Failed to create employee.');
                    }
                })
                .catch(error => console.error('Error:', error));
        });
    }

    // централна функция за зареждане.
    // извиква се при отваряне на модала И при всяко действие (Assign/Create/Fire),
    // за да сме сигурни, че се вижда актуалното състояние на базата данни
    function loadCompanyData(companyId) {
        fetch(`/companies/${companyId}/data`)
            .then(response => response.json())
            .then(data => {
                modalTitle.textContent = `Manage Employees for ${data.company.name}`;
                renderEmployeeList(data.employees);
                renderAvailableOptions(data.availableEmployees);
            })
            .catch(error => console.error('Error loading data:', error));
    }

    // динамично генерира HTML редове за всеки служител и закача event listener
    // на бутона "Fire", за да може да бъде уволнен веднага при натискане
    function renderEmployeeList(employees) {
        employeeList.innerHTML = '';
        if (employees.length === 0) {
            employeeList.innerHTML = '<li class="list-group-item text-muted">No employees assigned.</li>';
            return;
        }

        // динамично създаване на HTML (DOM Manipulation).
        // За всеки служител правим <li> елемент с име и бутон за уволнение.
        employees.forEach(emp => {
            const li = document.createElement('li');
            li.className = 'list-group-item d-flex justify-content-between align-items-center mb-2';
            li.innerHTML = `
                <span>${emp.firstName} ${emp.lastName}</span>
                <button class="btn btn-outline-danger btn-sm btn-fire" data-id="${emp.id}" title="Remove Employee">
                    <i class="fas fa-user-minus"></i> Fire
                </button>
            `;
            employeeList.appendChild(li);
        });

        // добавя event listeners за бутона Fire
        document.querySelectorAll('.btn-fire').forEach(btn => {
            btn.addEventListener('click', function () {
                const empId = this.getAttribute('data-id');
                if (confirm('Are you sure you want to unassign this employee?')) {
                    fireEmployee(currentCompanyId, empId);
                }
            });
        });
    }

    // попълва падащото меню (Dropdown) само със служители,
    // които все още НЕ работят в тази фирма (Available Employees).
    function renderAvailableOptions(employees) {
        availableSelect.innerHTML = '<option value="" selected disabled>Select an employee...</option>';
        employees.forEach(emp => {
            const option = document.createElement('option');
            option.value = emp.id;
            option.textContent = `${emp.firstName} ${emp.lastName}`;
            availableSelect.appendChild(option);
        });
    }

    function fireEmployee(companyId, empId) {
        // изпраща DELETE заявка към сървъра
        fetch(`/companies/${companyId}/employees/${empId}`, {
            method: 'DELETE'
        })
            .then(response => {
                if (response.ok) {
                    loadCompanyData(companyId); // Reload data
                } else {
                    alert('Failed to remove employee.');
                }
            })
            .catch(error => console.error('Error:', error));
    }
});
