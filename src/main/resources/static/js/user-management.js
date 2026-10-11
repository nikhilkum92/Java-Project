/**
 * CampusGrid — Dynamic User Management Controller (Frontend)
 * Fully Dynamic: Real DB Dropdowns, Dynamic Metrics, Edit Modals,
 * Live CSV Export & Batch Import with Preview & Validation,
 * Bulk Actions, Row Selection, Instant Password Reset & Status Toggles.
 */

let allStudents = [];
let allFaculty = [];
let departmentsList = [];
let sectionsList = [];
let currentTab = 'students';
let selectedUserIds = new Set();

document.addEventListener('DOMContentLoaded', async () => {
    // 1. Verify User Session & Role
    const user = Auth.getUser();
    if (!user || user.role !== 'ADMIN') {
        window.location.href = '/login';
        return;
    }

    // Set Topbar user name
    const topUserName = document.getElementById('topUserName');
    if (topUserName) topUserName.textContent = user.fullName || 'Admin';

    // Set date string
    const dateEl = document.getElementById('currentDateStr');
    if (dateEl) {
        const now = new Date();
        dateEl.textContent = now.toLocaleDateString('en-GB', { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' });
    }

    // 2. Load Dynamic Dropdowns (Departments & Sections)
    await loadDropdownOptions();

    // 3. Load Dynamic Stats & User Cohorts
    await loadStats();
    await loadStudents();
    await loadFaculty();

    // 4. Attach Event Listeners
    setupEventListeners();
});

async function loadDropdownOptions() {
    try {
        const [deptRes, secRes] = await Promise.all([
            Auth.fetchWithAuth('/api/admin/users/departments'),
            Auth.fetchWithAuth('/api/admin/users/sections')
        ]);
        const deptJson = await deptRes.json();
        const secJson = await secRes.json();

        if (deptJson.status === 'SUCCESS' && Array.isArray(deptJson.data)) {
            departmentsList = deptJson.data;
            populateDepartmentSelects(departmentsList);
        }
        if (secJson.status === 'SUCCESS' && Array.isArray(secJson.data)) {
            sectionsList = secJson.data;
            populateSectionSelects(sectionsList);
        }
    } catch (e) {
        console.error('Failed to load dynamic options:', e);
    }
}

function populateDepartmentSelects(depts) {
    const filterSelect = document.getElementById('deptFilterSelect');
    if (filterSelect) {
        const current = filterSelect.value;
        filterSelect.innerHTML = '<option value="">All Departments</option>' +
            depts.map(d => `<option value="${escapeHtml(d)}">${escapeHtml(d)}</option>`).join('');
        filterSelect.value = current;
    }

    const stuDept = document.getElementById('stuDepartment');
    if (stuDept) {
        if (depts.length === 0) {
            stuDept.innerHTML = '<option value="">No departments available. Create a department first.</option>';
        } else {
            stuDept.innerHTML = depts.map(d => `<option value="${escapeHtml(d)}">${escapeHtml(d)}</option>`).join('');
        }
        stuDept.onchange = () => updateSectionsForDepartment('stuDepartment', 'stuSection');
    }

    const facDept = document.getElementById('facDepartment');
    if (facDept) {
        if (depts.length === 0) {
            facDept.innerHTML = '<option value="">No departments available. Create a department first.</option>';
        } else {
            facDept.innerHTML = depts.map(d => `<option value="${escapeHtml(d)}">${escapeHtml(d)}</option>`).join('');
        }
    }

    const editDept = document.getElementById('editDepartment');
    if (editDept) {
        editDept.innerHTML = depts.map(d => `<option value="${escapeHtml(d)}">${escapeHtml(d)}</option>`).join('');
        editDept.onchange = () => updateSectionsForDepartment('editDepartment', 'editSection');
    }
}

function updateSectionsForDepartment(deptSelectId, sectionSelectId) {
    const deptElem = document.getElementById(deptSelectId);
    const secElem = document.getElementById(sectionSelectId);
    if (!deptElem || !secElem) return;

    const selectedDept = deptElem.value;
    let filteredSections = sectionsList;
    if (selectedDept.toLowerCase().includes('computer')) {
        filteredSections = sectionsList.filter(s => s.toUpperCase().startsWith('CSE') || s.toUpperCase().startsWith('CS'));
    } else if (selectedDept.toLowerCase().includes('electron')) {
        filteredSections = sectionsList.filter(s => s.toUpperCase().startsWith('ECE') || s.toUpperCase().startsWith('EC'));
    } else if (selectedDept.toLowerCase().includes('mech')) {
        filteredSections = sectionsList.filter(s => s.toUpperCase().startsWith('MECH') || s.toUpperCase().startsWith('ME'));
    }

    if (filteredSections.length === 0) {
        filteredSections = sectionsList; // fallback to configured sections
    }

    secElem.innerHTML = filteredSections.map(s => `<option value="${escapeHtml(s)}">${escapeHtml(s)}</option>`).join('');
}

function populateSectionSelects(sections) {
    const secSelect = document.getElementById('sectionFilterSelect');
    if (secSelect) {
        const current = secSelect.value;
        secSelect.innerHTML = '<option value="">All Sections</option>' +
            sections.map(s => `<option value="${escapeHtml(s)}">${escapeHtml(s)}</option>`).join('');
        secSelect.value = current;
    }

    const stuSec = document.getElementById('stuSection');
    if (stuSec) {
        updateSectionsForDepartment('stuDepartment', 'stuSection');
    }

    const editSec = document.getElementById('editSection');
    if (editSec) {
        updateSectionsForDepartment('editDepartment', 'editSection');
    }
}

async function loadStats() {
    try {
        const res = await Auth.fetchWithAuth('/api/admin/users/stats');
        const json = await res.json();
        if (json.status === 'SUCCESS' && json.data) {
            const d = json.data;
            const inactiveStudents = Math.max(0, d.totalStudents - d.activeStudents);
            const inactiveFaculty = Math.max(0, d.totalFaculty - d.activeFaculty);

            document.getElementById('statTotalStudents').textContent = d.totalStudents;
            document.getElementById('statActiveStudents').textContent = `${d.activeStudents} active · ${inactiveStudents} inactive`;
            document.getElementById('statTotalFaculty').textContent = d.totalFaculty;
            document.getElementById('statActiveFaculty').textContent = `${d.activeFaculty} active · ${inactiveFaculty} inactive`;
            document.getElementById('statTotalDepts').textContent = d.totalDepartments;
            document.getElementById('statTotalSections').textContent = d.totalSections;
        }
    } catch (e) {
        console.warn('Stats load error:', e);
    }
}

async function loadStudents() {
    try {
        const res = await Auth.fetchWithAuth('/api/admin/users/students');
        const json = await res.json();
        if (json.status === 'SUCCESS') {
            allStudents = json.data;
            if (currentTab === 'students') renderStudentsTable();
            updateCounts();
        }
    } catch (e) {
        showToast('Failed to load students: ' + e.message, 'danger');
    }
}

async function loadFaculty() {
    try {
        const res = await Auth.fetchWithAuth('/api/admin/users/faculty');
        const json = await res.json();
        if (json.status === 'SUCCESS') {
            allFaculty = json.data;
            if (currentTab === 'faculty') renderFacultyTable();
            updateCounts();
        }
    } catch (e) {
        showToast('Failed to load faculty: ' + e.message, 'danger');
    }
}

function updateCounts() {
    const sCount = document.getElementById('studentCountBadge');
    const fCount = document.getElementById('facultyCountBadge');
    if (sCount) sCount.textContent = allStudents.length;
    if (fCount) fCount.textContent = allFaculty.length;
}

function switchTab(tab) {
    currentTab = tab;
    clearBulkSelection();
    document.querySelectorAll('.tab-btn-cg').forEach(btn => btn.classList.remove('active'));

    const searchInput = document.getElementById('userSearchInput');
    if (searchInput) searchInput.value = '';

    if (tab === 'students') {
        document.getElementById('tabBtnStudents').classList.add('active');
        const secFilter = document.getElementById('sectionFilterGroup');
        if (secFilter) secFilter.style.display = 'block';
        const btnStu = document.getElementById('btnAddStudent');
        const btnFac = document.getElementById('btnAddFaculty');
        if (btnStu) btnStu.style.display = 'inline-flex';
        if (btnFac) btnFac.style.display = 'none';
        renderStudentsTable();
    } else {
        document.getElementById('tabBtnFaculty').classList.add('active');
        const secFilter = document.getElementById('sectionFilterGroup');
        if (secFilter) secFilter.style.display = 'none';
        const btnStu = document.getElementById('btnAddStudent');
        const btnFac = document.getElementById('btnAddFaculty');
        if (btnStu) btnStu.style.display = 'none';
        if (btnFac) btnFac.style.display = 'inline-flex';
        renderFacultyTable();
    }
}

function renderStudentsTable() {
    const tbody = document.getElementById('usersTableBody');
    const search = document.getElementById('userSearchInput').value.toLowerCase().trim();
    const dept = document.getElementById('deptFilterSelect').value;
    const sec = document.getElementById('sectionFilterSelect').value;
    const stat = document.getElementById('statusFilterSelect').value;

    let filtered = allStudents.filter(s => {
        const matchSearch = !search ||
            (s.fullName && s.fullName.toLowerCase().includes(search)) ||
            (s.rollNumber && s.rollNumber.toLowerCase().includes(search)) ||
            (s.email && s.email.toLowerCase().includes(search));
        const matchDept = !dept || s.department === dept;
        const matchSec = !sec || s.sectionName === sec;
        const matchStat = !stat || (stat === 'active' ? s.active : !s.active);
        return matchSearch && matchDept && matchSec && matchStat;
    });

    const thead = document.getElementById('usersTableHead');
    thead.innerHTML = `
        <tr>
            <th style="width: 38px;"><input type="checkbox" id="selectAllCheckbox" onchange="toggleSelectAll(this)"></th>
            <th style="width: 44px;">#</th>
            <th>Roll Number</th>
            <th>Name &amp; Email</th>
            <th>Department</th>
            <th>Section</th>
            <th>Year &amp; Term</th>
            <th>Status</th>
            <th style="text-align: right; width: 220px;">Actions</th>
        </tr>
    `;

    if (filtered.length === 0) {
        if (allStudents.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="9" style="text-align: center; padding: 48px 24px;">
                        <div style="font-weight: 700; font-size: 15px; color: var(--cg-brand-black); margin-bottom: 6px;">No student records yet</div>
                        <div style="font-size: 13px; color: var(--cg-text-muted); margin-bottom: 16px;">Add student members individually or batch onboard them using an actual CSV file.</div>
                        <div style="display: flex; gap: 8px; justify-content: center;">
                            <button class="btn-cg-outline" onclick="openModal('modalImportUsers')">Import CSV</button>
                            <button class="btn-cg-primary" onclick="openModal('modalAddStudent')">+ Add Student</button>
                        </div>
                    </td>
                </tr>`;
        } else {
            tbody.innerHTML = `
                <tr>
                    <td colspan="9" style="text-align: center; padding: 48px 24px;">
                        <div style="font-weight: 700; font-size: 14px; color: var(--cg-brand-black); margin-bottom: 6px;">No student records match your filters</div>
                        <div style="font-size: 13px; color: var(--cg-text-muted); margin-bottom: 14px;">Try changing your search term or clearing active filters.</div>
                        <button class="btn-cg-outline" onclick="clearFilters()">Clear Filters</button>
                    </td>
                </tr>`;
        }
        return;
    }

    tbody.innerHTML = filtered.map((s, idx) => `
        <tr>
            <td><input type="checkbox" class="row-checkbox" value="${s.id}" ${selectedUserIds.has(s.id) ? 'checked' : ''} onchange="toggleRowSelection(${s.id}, this)"></td>
            <td><span style="color: var(--cg-text-muted); font-size: 12px;">${idx + 1}</span></td>
            <td><strong>${escapeHtml(s.rollNumber)}</strong></td>
            <td>
                <div style="font-weight: 600; color: var(--cg-brand-black);">${escapeHtml(s.fullName)}</div>
                <div style="font-size: 11.5px; color: var(--cg-text-muted);">${escapeHtml(s.email)}</div>
            </td>
            <td>${escapeHtml(s.department)}</td>
            <td><span style="font-weight: 600; padding: 2px 7px; background: #F1F5F9; border-radius: 4px; font-size: 11.5px;">${escapeHtml(s.sectionName)}</span></td>
            <td>Year ${s.year} &bull; Sem ${s.semester}</td>
            <td>
                <span class="badge-cg ${s.active ? 'badge-active' : 'badge-inactive'}">
                    ${s.active ? 'Active' : 'Inactive'}
                </span>
            </td>
            <td style="text-align: right; white-space: nowrap;">
                <button class="btn-action-cg" onclick="openEditModal(${s.id}, 'STUDENT')" title="Edit Student Record">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path></svg>
                    <span>Edit</span>
                </button>
                <button class="btn-action-cg" onclick="promptResetPassword(${s.id}, '${escapeHtml(s.fullName)}')" title="Reset Password">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
                    <span>Reset</span>
                </button>
                <button class="btn-action-cg" onclick="toggleUser(${s.id})" title="Toggle Active Status">
                    <span>${s.active ? 'Deactivate' : 'Activate'}</span>
                </button>
                <button class="btn-action-cg danger" onclick="deleteUser(${s.id}, '${escapeHtml(s.fullName)}')" title="Delete Account">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg>
                    <span>Delete</span>
                </button>
            </td>
        </tr>
    `).join('');
}

function renderFacultyTable() {
    const tbody = document.getElementById('usersTableBody');
    const search = document.getElementById('userSearchInput').value.toLowerCase().trim();
    const dept = document.getElementById('deptFilterSelect').value;
    const stat = document.getElementById('statusFilterSelect').value;

    let filtered = allFaculty.filter(f => {
        const matchSearch = !search ||
            (f.fullName && f.fullName.toLowerCase().includes(search)) ||
            (f.employeeId && f.employeeId.toLowerCase().includes(search)) ||
            (f.email && f.email.toLowerCase().includes(search));
        const matchDept = !dept || f.department === dept;
        const matchStat = !stat || (stat === 'active' ? f.active : !f.active);
        return matchSearch && matchDept && matchStat;
    });

    const thead = document.getElementById('usersTableHead');
    thead.innerHTML = `
        <tr>
            <th style="width: 38px;"><input type="checkbox" id="selectAllCheckbox" onchange="toggleSelectAll(this)"></th>
            <th style="width: 44px;">#</th>
            <th>Employee ID</th>
            <th>Name &amp; Email</th>
            <th>Department</th>
            <th>Designation</th>
            <th>Status</th>
            <th style="text-align: right; width: 220px;">Actions</th>
        </tr>
    `;

    if (filtered.length === 0) {
        if (allFaculty.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="8" style="text-align: center; padding: 48px 24px;">
                        <div style="font-weight: 700; font-size: 15px; color: var(--cg-brand-black); margin-bottom: 6px;">No faculty members yet</div>
                        <div style="font-size: 13px; color: var(--cg-text-muted); margin-bottom: 16px;">Add faculty members individually or import them using a CSV file.</div>
                        <div style="display: flex; gap: 8px; justify-content: center;">
                            <button class="btn-cg-outline" onclick="openModal('modalImportUsers')">Import CSV</button>
                            <button class="btn-cg-primary" onclick="openModal('modalAddFaculty')">+ Add Faculty</button>
                        </div>
                    </td>
                </tr>`;
        } else {
            tbody.innerHTML = `
                <tr>
                    <td colspan="8" style="text-align: center; padding: 48px 24px;">
                        <div style="font-weight: 700; font-size: 14px; color: var(--cg-brand-black); margin-bottom: 6px;">No faculty records match your filters</div>
                        <div style="font-size: 13px; color: var(--cg-text-muted); margin-bottom: 14px;">Try changing your search term or clearing active filters.</div>
                        <button class="btn-cg-outline" onclick="clearFilters()">Clear Filters</button>
                    </td>
                </tr>`;
        }
        return;
    }

    tbody.innerHTML = filtered.map((f, idx) => `
        <tr>
            <td><input type="checkbox" class="row-checkbox" value="${f.id}" ${selectedUserIds.has(f.id) ? 'checked' : ''} onchange="toggleRowSelection(${f.id}, this)"></td>
            <td><span style="color: var(--cg-text-muted); font-size: 12px;">${idx + 1}</span></td>
            <td><strong>${escapeHtml(f.employeeId)}</strong></td>
            <td>
                <div style="font-weight: 600; color: var(--cg-brand-black);">${escapeHtml(f.fullName)}</div>
                <div style="font-size: 11.5px; color: var(--cg-text-muted);">${escapeHtml(f.email)}</div>
            </td>
            <td>${escapeHtml(f.department)}</td>
            <td>${escapeHtml(f.designation)}</td>
            <td>
                <span class="badge-cg ${f.active ? 'badge-active' : 'badge-inactive'}">
                    ${f.active ? 'Active' : 'Inactive'}
                </span>
            </td>
            <td style="text-align: right; white-space: nowrap;">
                <button class="btn-action-cg" onclick="openEditModal(${f.id}, 'FACULTY')" title="Edit Faculty Record">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path></svg>
                    <span>Edit</span>
                </button>
                <button class="btn-action-cg" onclick="promptResetPassword(${f.id}, '${escapeHtml(f.fullName)}')" title="Reset Password">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
                    <span>Reset</span>
                </button>
                <button class="btn-action-cg" onclick="toggleUser(${f.id})" title="Toggle Active Status">
                    <span>${f.active ? 'Deactivate' : 'Activate'}</span>
                </button>
                <button class="btn-action-cg danger" onclick="deleteUser(${f.id}, '${escapeHtml(f.fullName)}')" title="Delete Account">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg>
                    <span>Delete</span>
                </button>
            </td>
        </tr>
    `).join('');
}

// Bulk Actions Logic
function toggleRowSelection(id, checkbox) {
    if (checkbox.checked) {
        selectedUserIds.add(id);
    } else {
        selectedUserIds.delete(id);
    }
    updateBulkToolbar();
}

function toggleSelectAll(masterCheckbox) {
    const list = currentTab === 'students' ? allStudents : allFaculty;
    if (masterCheckbox.checked) {
        list.forEach(item => selectedUserIds.add(item.id));
    } else {
        selectedUserIds.clear();
    }
    document.querySelectorAll('.row-checkbox').forEach(cb => {
        cb.checked = masterCheckbox.checked;
    });
    updateBulkToolbar();
}

function updateBulkToolbar() {
    const bar = document.getElementById('bulkActionsToolbar');
    const countEl = document.getElementById('bulkSelectedCount');
    if (selectedUserIds.size > 0) {
        bar.style.display = 'flex';
        countEl.textContent = selectedUserIds.size;
    } else {
        bar.style.display = 'none';
    }
}

function clearBulkSelection() {
    selectedUserIds.clear();
    const master = document.getElementById('selectAllCheckbox');
    if (master) master.checked = false;
    document.querySelectorAll('.row-checkbox').forEach(cb => cb.checked = false);
    updateBulkToolbar();
}

async function bulkToggleStatus() {
    if (selectedUserIds.size === 0) return;
    for (let id of selectedUserIds) {
        try {
            await Auth.fetchWithAuth(`/api/admin/users/${id}/toggle-status`, { method: 'PUT' });
        } catch (e) {}
    }
    showToast(`Updated status for ${selectedUserIds.size} user(s).`, 'success');
    clearBulkSelection();
    await loadStudents();
    await loadFaculty();
    await loadStats();
}

async function bulkDeleteSelected() {
    if (selectedUserIds.size === 0) return;
    if (!confirm(`Are you sure you want to permanently delete ${selectedUserIds.size} selected user(s)?`)) return;

    for (let id of selectedUserIds) {
        try {
            await Auth.fetchWithAuth(`/api/admin/users/${id}`, { method: 'DELETE' });
        } catch (e) {}
    }
    showToast(`Deleted ${selectedUserIds.size} user account(s).`, 'success');
    clearBulkSelection();
    await loadStudents();
    await loadFaculty();
    await loadStats();
}

function bulkExportSelected() {
    const list = (currentTab === 'students' ? allStudents : allFaculty).filter(u => selectedUserIds.has(u.id));
    if (list.length === 0) return;

    let csvContent = "";
    if (currentTab === 'students') {
        csvContent = "ID,Roll Number,Full Name,Email,Department,Section,Year,Semester,Phone,Status\n" +
            list.map(s => `"${s.uid}","${s.rollNumber || ''}","${s.fullName}","${s.email}","${s.department || ''}","${s.sectionName || ''}",${s.year},${s.semester},"${s.phoneNumber || ''}","${s.active ? 'ACTIVE' : 'INACTIVE'}"`).join('\n');
    } else {
        csvContent = "ID,Employee ID,Full Name,Email,Department,Designation,Phone,Status\n" +
            list.map(f => `"${f.uid}","${f.employeeId || ''}","${f.fullName}","${f.email}","${f.department || ''}","${f.designation || ''}","${f.phoneNumber || ''}","${f.active ? 'ACTIVE' : 'INACTIVE'}"`).join('\n');
    }

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `campusgrid-${currentTab}-selected-${new Date().toISOString().slice(0,10)}.csv`;
    a.click();
    URL.revokeObjectURL(url);
    showToast(`Exported ${list.length} selected record(s).`, 'success');
}

function openEditModal(userId, role) {
    document.getElementById('editUserId').value = userId;
    document.getElementById('editUserRole').value = role;

    if (role === 'STUDENT') {
        const s = allStudents.find(x => x.id === userId);
        if (!s) return;
        document.getElementById('modalEditTitle').textContent = `Edit Student: ${s.fullName}`;
        document.getElementById('editFullName').value = s.fullName || '';
        document.getElementById('editEmail').value = s.email || '';
        document.getElementById('editIdLabel').textContent = 'Roll Number *';
        document.getElementById('editSpecificId').value = s.rollNumber || '';
        document.getElementById('editDepartment').value = s.department || '';
        document.getElementById('editSection').value = s.sectionName || '';
        document.getElementById('editYear').value = s.year || 2;
        document.getElementById('editSemester').value = s.semester || 3;
        document.getElementById('editPhone').value = s.phoneNumber || '';

        document.getElementById('editStudentFields').style.display = 'grid';
        document.getElementById('editFacultyFields').style.display = 'none';
    } else {
        const f = allFaculty.find(x => x.id === userId);
        if (!f) return;
        document.getElementById('modalEditTitle').textContent = `Edit Faculty: ${f.fullName}`;
        document.getElementById('editFullName').value = f.fullName || '';
        document.getElementById('editEmail').value = f.email || '';
        document.getElementById('editIdLabel').textContent = 'Employee ID *';
        document.getElementById('editSpecificId').value = f.employeeId || '';
        document.getElementById('editDepartment').value = f.department || '';
        document.getElementById('editDesignation').value = f.designation || 'Assistant Professor';
        document.getElementById('editOffice').value = f.officeRoom || '';
        document.getElementById('editPhone').value = f.phoneNumber || '';

        document.getElementById('editStudentFields').style.display = 'none';
        document.getElementById('editFacultyFields').style.display = 'grid';
    }

    openModal('modalEditUser');
}

async function promptResetPassword(userId, name) {
    const newPwd = prompt(`Enter new password for "${name}" (minimum 6 characters):`);
    if (!newPwd) return;

    if (newPwd.length < 6) {
        alert('Password must be at least 6 characters long.');
        return;
    }

    try {
        const res = await Auth.fetchWithAuth(`/api/admin/users/${userId}/reset-password`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ newPassword: newPwd })
        });
        const json = await res.json();
        if (json.status === 'SUCCESS') {
            showToast(`Password updated successfully for ${name}.`, 'success');
        } else {
            showToast(json.message, 'danger');
        }
    } catch (err) {
        showToast('Password reset failed: ' + err.message, 'danger');
    }
}

async function toggleUser(userId) {
    try {
        const res = await Auth.fetchWithAuth(`/api/admin/users/${userId}/toggle-status`, { method: 'PUT' });
        const json = await res.json();
        if (json.status === 'SUCCESS') {
            showToast(json.message, 'success');
            await loadStudents();
            await loadFaculty();
            await loadStats();
        } else {
            showToast(json.message, 'danger');
        }
    } catch (e) {
        showToast('Status update failed: ' + e.message, 'danger');
    }
}

async function deleteUser(userId, name) {
    if (!confirm(`Are you sure you want to permanently delete user "${name}"?`)) return;

    try {
        const res = await Auth.fetchWithAuth(`/api/admin/users/${userId}`, { method: 'DELETE' });
        const json = await res.json();
        if (json.status === 'SUCCESS') {
            showToast('User deleted successfully.', 'success');
            await loadStudents();
            await loadFaculty();
            await loadStats();
        } else {
            showToast(json.message, 'danger');
        }
    } catch (e) {
        showToast('Delete operation failed: ' + e.message, 'danger');
    }
}

function clearFilters() {
    const searchInput = document.getElementById('userSearchInput');
    const deptSelect = document.getElementById('deptFilterSelect');
    const secSelect = document.getElementById('sectionFilterSelect');
    const statusSelect = document.getElementById('statusFilterSelect');
    if (searchInput) searchInput.value = '';
    if (deptSelect) deptSelect.value = '';
    if (secSelect) secSelect.value = '';
    if (statusSelect) statusSelect.value = '';
    if (currentTab === 'students') renderStudentsTable();
    else renderFacultyTable();
}

function exportData() {
    const type = currentTab === 'faculty' ? 'faculty' : 'students';
    const dateStr = new Date().toISOString().slice(0, 10);
    const filename = `campusgrid-${type}-${dateStr}.csv`;
    const url = `/api/admin/users/export?type=${type}`;
    
    showToast(`Preparing ${type} CSV export...`, 'info');

    Auth.fetchWithAuth(url)
        .then(async res => {
            if (!res.ok) {
                if (res.status === 401 || res.status === 403) {
                    throw new Error('Your session has expired or administrator privileges are required. Please log in again.');
                }
                const errorText = await res.text().catch(() => '');
                throw new Error(errorText || `We could not generate the ${type} CSV. Please try again.`);
            }
            return res.blob();
        })
        .then(blob => {
            const downloadUrl = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.style.display = 'none';
            a.href = downloadUrl;
            a.download = filename;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(downloadUrl);
            a.remove();
            showToast(`Export completed: Saved as ${filename}.`, 'success');
        })
        .catch(err => {
            showToast(`Export failed: ${err.message}`, 'danger');
        });
}

// CSV Template Downloader
function downloadCsvTemplate() {
    const role = document.getElementById('importRoleSelect').value;
    let template = "";
    if (role === 'STUDENT') {
        template = "Full Name,Email,Username,Roll Number,Department,Section,Phone\n" +
                   "Aditya Rao,aditya.rao@college.ac.in,aditya.rao,23CSE015,Computer Science,CSE-A,+91 9876543201\n" +
                   "Kavita Reddy,kavita.reddy@college.ac.in,kavita.reddy,23CSE016,Computer Science,CSE-B,+91 9876543202\n";
    } else {
        template = "Full Name,Email,Username,Employee ID,Department,Designation,Phone\n" +
                   "Dr. Suresh Menon,suresh.menon@college.ac.in,suresh.menon,FAC008,Computer Science,Professor,+91 9876543215\n";
    }

    const blob = new Blob([template], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `campusgrid-${role.toLowerCase()}-import-template.csv`;
    a.click();
    URL.revokeObjectURL(url);
    showToast(`Downloaded CSV onboarding template for ${role}.`, 'success');
}

let selectedCsvFile = null;

function handleCsvFileSelected(input) {
    if (input.files && input.files[0]) {
        processSelectedCsvFile(input.files[0]);
    }
}

function processSelectedCsvFile(file) {
    if (!file.name.toLowerCase().endsWith('.csv') && file.type !== 'text/csv' && file.type !== 'application/vnd.ms-excel') {
        showToast('Please select a valid .csv file.', 'danger');
        return;
    }
    selectedCsvFile = file;
    document.getElementById('csvFileDropLabel').innerHTML = `✓ <strong>${escapeHtml(file.name)}</strong> (${(file.size / 1024).toFixed(1)} KB)`;
    document.getElementById('csvFileDropMeta').innerText = 'Ready for validation and batch upload. Click to change file.';
    document.getElementById('csvDropZone').style.borderColor = 'var(--cg-success)';
    document.getElementById('csvDropZone').style.background = '#F0FDF4';

    // Also read and populate textarea preview so user can inspect or edit
    const reader = new FileReader();
    reader.onload = function(e) {
        document.getElementById('importCsvData').value = e.target.result;
    };
    reader.readAsText(file);
}

function clearCsvInput() {
    selectedCsvFile = null;
    document.getElementById('importCsvFileInput').value = '';
    document.getElementById('importCsvData').value = '';
    document.getElementById('csvFileDropLabel').innerText = 'Click to upload CSV file or drag and drop';
    document.getElementById('csvFileDropMeta').innerText = 'Supports standard .csv file format (UTF-8, comma-separated)';
    document.getElementById('csvDropZone').style.borderColor = 'var(--cg-border)';
    document.getElementById('csvDropZone').style.background = '#FAFAFA';
}

function resetImportModal() {
    document.getElementById('importStage1').style.display = 'block';
    document.getElementById('importStage2').style.display = 'none';
}

function updateCsvTemplate() {
    const role = document.getElementById('importRoleSelect').value;
    const placeholder = role === 'STUDENT'
        ? "Full Name,Email,Username,Roll Number,Department,Section,Phone\nManish Roy,manish.roy@college.ac.in,manish.roy,23CSE010,Computer Science,CSE-A,+91 9876543220"
        : "Full Name,Email,Username,Employee ID,Department,Designation,Phone\nDr. Anand Verma,anand.verma@college.ac.in,anand.verma,FAC005,Computer Science,Professor,+91 9876543212";
    document.getElementById('importCsvData').placeholder = placeholder;
}

function setupEventListeners() {
    // CSV Drag and drop events
    const dropZone = document.getElementById('csvDropZone');
    if (dropZone) {
        ['dragenter', 'dragover'].forEach(eventName => {
            dropZone.addEventListener(eventName, (e) => {
                e.preventDefault();
                e.stopPropagation();
                dropZone.style.borderColor = 'var(--cg-brand-black)';
                dropZone.style.background = '#F1F5F9';
            }, false);
        });

        ['dragleave', 'drop'].forEach(eventName => {
            dropZone.addEventListener(eventName, (e) => {
                e.preventDefault();
                e.stopPropagation();
                if (eventName === 'dragleave') {
                    dropZone.style.borderColor = selectedCsvFile ? 'var(--cg-success)' : 'var(--cg-border)';
                    dropZone.style.background = selectedCsvFile ? '#F0FDF4' : '#FAFAFA';
                }
            }, false);
        });

        dropZone.addEventListener('drop', (e) => {
            const dt = e.dataTransfer;
            const files = dt.files;
            if (files && files.length > 0) {
                processSelectedCsvFile(files[0]);
            }
        }, false);
    }

    // Search & Filter listeners
    document.getElementById('userSearchInput').addEventListener('input', () => {
        if (currentTab === 'students') renderStudentsTable();
        else renderFacultyTable();
    });

    document.getElementById('deptFilterSelect').addEventListener('change', () => {
        if (currentTab === 'students') renderStudentsTable();
        else renderFacultyTable();
    });

    document.getElementById('sectionFilterSelect').addEventListener('change', renderStudentsTable);
    document.getElementById('statusFilterSelect').addEventListener('change', () => {
        if (currentTab === 'students') renderStudentsTable();
        else renderFacultyTable();
    });

    // Student Form Submit
    document.getElementById('formAddStudent').addEventListener('submit', async (e) => {
        e.preventDefault();
        const payload = {
            role: 'STUDENT',
            fullName: document.getElementById('stuFullName').value,
            username: document.getElementById('stuUsername').value,
            email: document.getElementById('stuEmail').value,
            password: document.getElementById('stuPassword').value,
            rollNumber: document.getElementById('stuRollNumber').value,
            department: document.getElementById('stuDepartment').value,
            sectionName: document.getElementById('stuSection').value,
            year: parseInt(document.getElementById('stuYear').value, 10),
            semester: parseInt(document.getElementById('stuSemester').value, 10),
            phoneNumber: document.getElementById('stuPhone').value
        };

        try {
            const res = await Auth.fetchWithAuth('/api/admin/users/create', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            const json = await res.json();
            if (json.status === 'SUCCESS') {
                showToast('Student registered successfully!', 'success');
                closeModal('modalAddStudent');
                document.getElementById('formAddStudent').reset();
                await loadStudents();
                await loadStats();
                await loadDropdownOptions();
            } else {
                showToast(json.message, 'danger');
            }
        } catch (err) {
            showToast('Error creating student: ' + err.message, 'danger');
        }
    });

    // Faculty Form Submit
    document.getElementById('formAddFaculty').addEventListener('submit', async (e) => {
        e.preventDefault();
        const payload = {
            role: 'FACULTY',
            fullName: document.getElementById('facFullName').value,
            username: document.getElementById('facUsername').value,
            email: document.getElementById('facEmail').value,
            password: document.getElementById('facPassword').value,
            employeeId: document.getElementById('facEmployeeId').value,
            department: document.getElementById('facDepartment').value,
            designation: document.getElementById('facDesignation').value,
            officeRoom: document.getElementById('facOffice').value,
            phoneNumber: document.getElementById('facPhone').value
        };

        try {
            const res = await Auth.fetchWithAuth('/api/admin/users/create', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            const json = await res.json();
            if (json.status === 'SUCCESS') {
                showToast('Faculty registered successfully!', 'success');
                closeModal('modalAddFaculty');
                document.getElementById('formAddFaculty').reset();
                await loadFaculty();
                await loadStats();
                await loadDropdownOptions();
            } else {
                showToast(json.message, 'danger');
            }
        } catch (err) {
            showToast('Error creating faculty: ' + err.message, 'danger');
        }
    });

    // Edit Form Submit
    document.getElementById('formEditUser').addEventListener('submit', async (e) => {
        e.preventDefault();
        const userId = document.getElementById('editUserId').value;
        const role = document.getElementById('editUserRole').value;

        const payload = {
            role: role,
            fullName: document.getElementById('editFullName').value,
            email: document.getElementById('editEmail').value,
            department: document.getElementById('editDepartment').value,
            phoneNumber: document.getElementById('editPhone').value
        };

        if (role === 'STUDENT') {
            payload.rollNumber = document.getElementById('editSpecificId').value;
            payload.sectionName = document.getElementById('editSection').value;
            payload.year = parseInt(document.getElementById('editYear').value, 10);
            payload.semester = parseInt(document.getElementById('editSemester').value, 10);
        } else {
            payload.employeeId = document.getElementById('editSpecificId').value;
            payload.designation = document.getElementById('editDesignation').value;
            payload.officeRoom = document.getElementById('editOffice').value;
        }

        try {
            const res = await Auth.fetchWithAuth(`/api/admin/users/${userId}/update`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            const json = await res.json();
            if (json.status === 'SUCCESS') {
                showToast('User record updated successfully!', 'success');
                closeModal('modalEditUser');
                if (role === 'STUDENT') await loadStudents();
                else await loadFaculty();
                await loadStats();
            } else {
                showToast(json.message, 'danger');
            }
        } catch (err) {
            showToast('Failed to update user: ' + err.message, 'danger');
        }
    });

    // Import Form Submit (3-Stage Validation and Preview)
    document.getElementById('formImportUsers').addEventListener('submit', async (e) => {
        e.preventDefault();
        const role = document.getElementById('importRoleSelect').value;
        const csvContent = document.getElementById('importCsvData').value.trim();

        if (!selectedCsvFile && !csvContent) {
            alert('Please select a CSV file or paste CSV data.');
            return;
        }

        try {
            let res;
            if (selectedCsvFile) {
                const formData = new FormData();
                formData.append('file', selectedCsvFile);
                res = await Auth.fetchWithAuth(`/api/admin/users/import?role=${role}`, {
                    method: 'POST',
                    body: formData
                });
            } else {
                res = await Auth.fetchWithAuth(`/api/admin/users/import?role=${role}`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'text/plain' },
                    body: csvContent
                });
            }
            const json = await res.json();
            if (json.status === 'SUCCESS' && json.data) {
                const r = json.data;
                document.getElementById('importStage1').style.display = 'none';
                document.getElementById('importStage2').style.display = 'block';

                const summaryHtml = `
                    <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; margin-bottom: 12px;">
                        <div style="background: #DCFCE7; padding: 10px; border-radius: 6px; text-align: center;">
                            <div style="font-size: 11px; color: #16803D; font-weight: 700;">IMPORTED</div>
                            <div style="font-size: 20px; font-weight: 700; color: #16803D;">${r.successfullyImported}</div>
                        </div>
                        <div style="background: #FEF3C7; padding: 10px; border-radius: 6px; text-align: center;">
                            <div style="font-size: 11px; color: #B45309; font-weight: 700;">DUPLICATES</div>
                            <div style="font-size: 20px; font-weight: 700; color: #B45309;">${r.duplicatesCount}</div>
                        </div>
                        <div style="background: #FEE2E2; padding: 10px; border-radius: 6px; text-align: center;">
                            <div style="font-size: 11px; color: #DC2626; font-weight: 700;">ERRORS</div>
                            <div style="font-size: 20px; font-weight: 700; color: #DC2626;">${r.errorsCount}</div>
                        </div>
                    </div>
                `;
                document.getElementById('importSummaryReport').innerHTML = summaryHtml;

                let issuesHtml = '';
                if (r.issues && r.issues.length > 0) {
                    issuesHtml = '<div style="padding: 8px; font-weight: 600; background: #F8FAFC; border-bottom: 1px solid var(--cg-border);">Reported Validation Issues</div>' +
                        r.issues.map(iss => `
                            <div style="padding: 8px 12px; border-bottom: 1px solid var(--cg-border); display: flex; justify-content: space-between; align-items: center;">
                                <div><strong>${escapeHtml(iss.identifier)}</strong> (Row ${iss.rowNumber}): ${escapeHtml(iss.reason)}</div>
                                <span class="badge-cg ${iss.status === 'DUPLICATE' ? 'badge-inactive' : 'badge-inactive'}" style="${iss.status === 'ERROR' ? 'background:#FEE2E2; color:#DC2626;' : ''}">${iss.status}</span>
                            </div>
                        `).join('');
                } else {
                    issuesHtml = '<div style="padding: 16px; text-align: center; color: var(--cg-success); font-weight: 600;">✓ All rows passed validation with zero errors!</div>';
                }
                document.getElementById('importIssuesList').innerHTML = issuesHtml;

                await loadStudents();
                await loadFaculty();
                await loadStats();
                await loadDropdownOptions();
            } else {
                showToast(json.message, 'danger');
            }
        } catch (err) {
            showToast('Batch import failed: ' + err.message, 'danger');
        }
    });
}

function openModal(id) {
    document.getElementById(id).classList.add('show');
}

function closeModal(id) {
    document.getElementById(id).classList.remove('show');
    if (id === 'modalImportUsers') {
        resetImportModal();
        clearCsvInput();
    }
}

function showToast(msg, type = 'info') {
    const toast = document.createElement('div');
    toast.style.position = 'fixed';
    toast.style.bottom = '24px';
    toast.style.right = '24px';
    toast.style.padding = '12px 18px';
    toast.style.borderRadius = 'var(--cg-radius-md)';
    toast.style.background = type === 'danger' ? '#DC2626' : (type === 'success' ? '#15803D' : '#0F172A');
    toast.style.color = '#FFFFFF';
    toast.style.fontWeight = '500';
    toast.style.fontSize = '13px';
    toast.style.boxShadow = 'var(--cg-shadow-lg)';
    toast.style.zIndex = '9999';
    toast.style.display = 'flex';
    toast.style.alignItems = 'center';
    toast.style.gap = '8px';
    toast.textContent = msg;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3800);
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function toggleModalPassword(inputId, btn) {
    const input = document.getElementById(inputId);
    if (!input) return;
    if (input.type === 'password') {
        input.type = 'text';
        btn.innerHTML = `<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>`;
    } else {
        input.type = 'password';
        btn.innerHTML = `<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>`;
    }
}

window.openEditModal = openEditModal;
window.promptResetPassword = promptResetPassword;
window.toggleUser = toggleUser;
window.deleteUser = deleteUser;
window.exportData = exportData;
window.clearFilters = clearFilters;
window.switchTab = switchTab;
window.openModal = openModal;
window.closeModal = closeModal;
window.toggleRowSelection = toggleRowSelection;
window.toggleSelectAll = toggleSelectAll;
window.clearBulkSelection = clearBulkSelection;
window.bulkToggleStatus = bulkToggleStatus;
window.bulkDeleteSelected = bulkDeleteSelected;
window.bulkExportSelected = bulkExportSelected;
window.downloadCsvTemplate = downloadCsvTemplate;
window.resetImportModal = resetImportModal;
window.handleCsvFileSelected = handleCsvFileSelected;
window.clearCsvInput = clearCsvInput;
window.updateCsvTemplate = updateCsvTemplate;
window.toggleModalPassword = toggleModalPassword;
