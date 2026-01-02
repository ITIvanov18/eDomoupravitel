/* global bootstrap */

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
                        loadCompanyData(currentCompanyId);
                        const triggerEl = document.querySelector('#manageTabs button[data-bs-target="#list-pane"]');
                        // @ts-ignore
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
                lastName: formData.get('lastName'),
                phoneNumber: formData.get('phoneNumber')
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
            .then(async response => {
                if (response.ok) {
                    loadCompanyData(companyId); // Reload data
                } else {
                    const errorMessage = await response.text();
                    alert(errorMessage || 'Failed to remove employee.');
                }
            })
            .catch(error => console.error('Error:', error));
    }
});

// Handle Edit Employee Modal
document.addEventListener('DOMContentLoaded', function () {
    const editModal = document.getElementById('editEmployeeModal');
    if (editModal) {
        editModal.addEventListener('show.bs.modal', function (event) {
            const button = event.relatedTarget;
            const id = button.getAttribute('data-id');
            const firstName = button.getAttribute('data-firstname');
            const lastName = button.getAttribute('data-lastname');
            const phoneNumber = button.getAttribute('data-phone');
            const companyId = button.getAttribute('data-company-id');

            const form = document.getElementById('editEmployeeForm');
            form.action = '/employees/edit/' + id;

            document.getElementById('editFirstName').value = firstName;
            document.getElementById('editLastName').value = lastName;
            document.getElementById('editPhoneNumber').value = phoneNumber;
            document.getElementById('editCompanyId').value = companyId;
        });
    }
});


// Open Edit Company Modal
window.openEditCompanyModal = function (id) {
    const modalElement = document.getElementById('editCompanyModal');
    const modal = new bootstrap.Modal(modalElement);
    const form = document.getElementById('editCompanyForm');
    const nameInput = document.getElementById('editCompanyName');

    // Fetch company data
    fetch(`/companies/${id}/data`)
        .then(response => response.json())
        .then(data => {
            if (data && data.company) {
                nameInput.value = data.company.name;
                document.getElementById('editTaxPerSqM').value = data.company.taxPerSqM;
                document.getElementById('editElevatorTax').value = data.company.elevatorTax;
                document.getElementById('editPetTax').value = data.company.petTax;
                form.action = `/companies/edit/${id}`;
                modal.show();
            } else {
                alert('Failed to load company data.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('An error occurred while loading data.');
        });
};

// Handle Pay Fee Modal (Apartments List)
document.addEventListener('DOMContentLoaded', function () {
    const payModal = document.getElementById('payModal');
    if (payModal) {
        payModal.addEventListener('show.bs.modal', function (event) {
            const button = event.relatedTarget;
            const apartmentId = button.getAttribute('data-apartment-id');
            const amount = button.getAttribute('data-amount');

            const idInput = payModal.querySelector('#payApartmentId');
            const amountInput = payModal.querySelector('#payAmount');

            idInput.value = apartmentId;
            if (amount) {
                amountInput.value = amount;
            }
        });
    }
});

// Open Edit Building Modal
window.openEditBuildingModal = function (id) {
    const modalElement = document.getElementById('editBuildingModal');
    // @ts-ignore
    const modal = new bootstrap.Modal(modalElement);
    const form = document.getElementById('editBuildingForm');

    // Inputs
    const address = document.getElementById('editAddress');
    const floors = document.getElementById('editFloors');
    const apts = document.getElementById('editNumberOfApartments');
    const area = document.getElementById('editArea');
    const common = document.getElementById('editCommonPartsArea');
    const company = document.getElementById('editCompanyId');

    fetch(`/buildings/${id}/data`)
        .then(response => response.json())
        .then(data => {
            if (data) {
                address.value = data.address;
                floors.value = data.floors;
                apts.value = data.numberOfApartments;
                area.value = data.area;
                common.value = data.commonPartsArea || '';
                if (data.companyId) {
                    company.value = data.companyId;
                } else if (data.company && data.company.id) {
                    company.value = data.company.id;
                }

                form.action = `/buildings/edit/${id}`;
                modal.show();
            } else {
                alert('Failed to load building data.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('An error occurred while loading data.');
        });
};
