/**
 * CampusGrid — Timetable & Room Allocation Management Controller (Frontend)
 * Production UI logic for manual scheduling, conflict detection feedback,
 * persistent room allocation, and dynamic room availability lookup.
 */

let allTimetableEntries = [];
let allSubjects = [];
let allFaculty = [];
let allRooms = [];
let departmentsList = [];
let sectionsList = [];
let currentView = 'list'; // 'list' or 'group'

document.addEventListener('DOMContentLoaded', async () => {
    // 1. Verify User Session & Role
    const user = Auth.getUser();
    if (!user || user.role !== 'ADMIN') {
        window.location.href = '/login';
        return;
    }

    // Topbar User Info
    const topUserName = document.getElementById('topUserName');
    if (topUserName) topUserName.textContent = user.fullName || 'Administrator';
    const topAvatar = document.getElementById('topAvatar');
    if (topAvatar) topAvatar.textContent = (user.fullName || 'A').charAt(0).toUpperCase();

    // Topbar Date
    const dateEl = document.getElementById('currentDateStr');
    if (dateEl) {
        const now = new Date();
        dateEl.textContent = now.toLocaleDateString('en-GB', { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' });
    }

    // 2. Load Dropdowns & Metadata
    await loadInitialMetadata();

    // 3. Load Timetables & Stats
    await Promise.all([
        loadTimetables(),
        loadTimetableStats()
    ]);

    // 4. Attach Event Listeners
    setupFilterListeners();

    // 5. Check if direct navigation to subjects tab requested
    if (window.location.search.includes('tab=subjects') || window.location.hash === '#subjects') {
        switchTimetableView('subjects');
    }
});

async function loadInitialMetadata() {
    try {
        const [subjRes, facRes, roomsRes, deptRes, secRes] = await Promise.all([
            Auth.fetchWithAuth('/api/admin/subjects'),
            Auth.fetchWithAuth('/api/admin/users/faculty'),
            Auth.fetchWithAuth('/api/spaces/rooms'),
            Auth.fetchWithAuth('/api/admin/users/departments'),
            Auth.fetchWithAuth('/api/admin/users/sections')
        ]);

        const [subjJson, facJson, roomsJson, deptJson, secJson] = await Promise.all([
            subjRes.json(),
            facRes.json(),
            roomsRes.json(),
            deptRes.json(),
            secRes.json()
        ]);

        if (subjJson.status === 'SUCCESS' && Array.isArray(subjJson.data)) {
            allSubjects = subjJson.data;
            populateSubjectSelect(allSubjects);
        }

        if (facJson.status === 'SUCCESS' && Array.isArray(facJson.data)) {
            allFaculty = facJson.data;
            populateFacultySelect(allFaculty);
        }

        if (roomsJson.status === 'SUCCESS' && Array.isArray(roomsJson.data)) {
            allRooms = roomsJson.data;
            populateRoomSelects(allRooms);
        }

        if (deptJson.status === 'SUCCESS' && Array.isArray(deptJson.data)) {
            departmentsList = deptJson.data;
            populateDepartmentSelects(departmentsList);
        }

        if (secJson.status === 'SUCCESS' && Array.isArray(secJson.data)) {
            sectionsList = secJson.data;
            populateSectionSelects(sectionsList);
        }
    } catch (err) {
        console.error('Failed to load initial metadata:', err);
    }
}

function populateSubjectSelect(subjects) {
    const createSubj = document.getElementById('createSubject');
    if (!createSubj) return;
    createSubj.innerHTML = '<option value="">Select Subject...</option>' +
        subjects.map(s => `<option value="${s.id}">[${escapeHtml(s.subjectCode)}] ${escapeHtml(s.subjectName)} (${s.subjectType}, ${s.credits} cr)</option>`).join('');
}

function populateFacultySelect(facultyList) {
    const createFac = document.getElementById('createFaculty');
    if (!createFac) return;
    createFac.innerHTML = '<option value="">To Be Assigned</option>' +
        facultyList.map(f => `<option value="${f.id}">${escapeHtml(f.fullName)} (${escapeHtml(f.department || 'Faculty')})</option>`).join('');
}

function populateRoomSelects(rooms) {
    const filterRoom = document.getElementById('filterRoom');
    if (filterRoom) {
        filterRoom.innerHTML = '<option value="">All Rooms</option>' +
            rooms.map(r => `<option value="${r.id}">${escapeHtml(r.roomCode)} - ${escapeHtml(r.roomName)} (${r.roomType}, Cap: ${r.capacity})</option>`).join('');
    }

    const createRoom = document.getElementById('createRoom');
    if (createRoom) {
        createRoom.innerHTML = '<option value="">To Be Assigned (Allocate Later)</option>' +
            rooms.filter(r => r.active && r.status === 'AVAILABLE')
                 .map(r => `<option value="${r.id}">${escapeHtml(r.roomCode)} - ${escapeHtml(r.roomName)} [${r.roomType}, Cap: ${r.capacity}]</option>`).join('');
    }

    const allocRoom = document.getElementById('allocRoomSelect');
    if (allocRoom) {
        allocRoom.innerHTML = '<option value="">Select Room...</option>' +
            rooms.filter(r => r.active && r.status === 'AVAILABLE')
                 .map(r => `<option value="${r.id}">${escapeHtml(r.roomCode)} - ${escapeHtml(r.roomName)} [${r.roomType}, Cap: ${r.capacity}]</option>`).join('');
    }
}

function populateDepartmentSelects(depts) {
    const filterDept = document.getElementById('filterDepartment');
    if (filterDept) {
        filterDept.innerHTML = '<option value="">All Departments</option>' +
            depts.map(d => `<option value="${escapeHtml(d)}">${escapeHtml(d)}</option>`).join('');
    }

    const createDept = document.getElementById('createDepartment');
    if (createDept && depts.length > 0) {
        createDept.innerHTML = depts.map(d => `<option value="${escapeHtml(d)}">${escapeHtml(d)}</option>`).join('');
    }
}

function populateSectionSelects(sections) {
    const filterSec = document.getElementById('filterSection');
    if (filterSec) {
        filterSec.innerHTML = '<option value="">All Sections</option>' +
            sections.map(s => `<option value="${escapeHtml(s)}">${escapeHtml(s)}</option>`).join('');
    }

    const createSec = document.getElementById('createSection');
    if (createSec && sections.length > 0) {
        createSec.innerHTML = sections.map(s => `<option value="${escapeHtml(s)}">${escapeHtml(s)}</option>`).join('');
    }
}

// =========================================================================
// DATA FETCHING & RENDERING
// =========================================================================

async function loadTimetables() {
    try {
        const params = new URLSearchParams();
        const day = document.getElementById('filterDayOfWeek')?.value;
        const dept = document.getElementById('filterDepartment')?.value;
        const sec = document.getElementById('filterSection')?.value;
        const room = document.getElementById('filterRoom')?.value;
        const status = document.getElementById('filterStatus')?.value;
        const search = document.getElementById('timetableSearchInput')?.value;

        if (day) params.append('dayOfWeek', day);
        if (dept) params.append('department', dept);
        if (sec) params.append('sectionName', sec);
        if (room) params.append('roomId', room);
        if (status) params.append('status', status);
        if (search) params.append('search', search.trim());

        const res = await Auth.fetchWithAuth('/api/admin/timetables?' + params.toString());
        const json = await res.json();

        if (json.status === 'SUCCESS' && Array.isArray(json.data)) {
            allTimetableEntries = json.data;
            if (currentView === 'list') {
                renderTimetableTable(allTimetableEntries);
            } else {
                renderCohortGrid(allTimetableEntries);
            }
        }
    } catch (err) {
        console.error('Failed to load timetables:', err);
    }
}

async function loadTimetableStats() {
    try {
        const res = await Auth.fetchWithAuth('/api/admin/timetables/stats');
        const json = await res.json();
        if (json.status === 'SUCCESS' && json.data) {
            const s = json.data;
            document.getElementById('metricTotalScheduled').textContent = s.scheduledEntries;
            document.getElementById('metricTotalHours').textContent = s.totalHoursScheduled + ' hrs';
            document.getElementById('metricAllocatedRooms').textContent = s.allocatedRoomsCount;
            document.getElementById('metricPendingRooms').textContent = s.unallocatedEntriesCount;
        }
    } catch (err) {
        console.error('Failed to load timetable stats:', err);
    }
}

function renderTimetableTable(entries) {
    const tbody = document.getElementById('timetableTableBody');
    if (!tbody) return;

    if (entries.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="8" style="text-align: center; padding: 48px; color: var(--cg-text-muted);">
                    <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" style="margin-bottom:8px;">
                        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                        <line x1="16" y1="2" x2="16" y2="6"></line>
                        <line x1="8" y1="2" x2="8" y2="6"></line>
                        <line x1="3" y1="10" x2="21" y2="10"></line>
                    </svg>
                    <div style="font-weight:600; font-size:14px; color:var(--cg-text-primary);">No Timetable Entries Found</div>
                    <div style="font-size:12px; margin-top:4px;">No academic sessions match the current filter selection.</div>
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = entries.map(e => {
        const isCancelled = e.status === 'CANCELLED';
        const hasRoom = e.roomId != null;

        let roomPill = `<span style="display:inline-flex; align-items:center; gap:4px; padding: 3px 8px; border-radius: 4px; font-size:11.5px; font-weight:600; background: #FEF3C7; color: #D97706;">
            &bull; Unallocated
        </span>`;

        if (hasRoom) {
            roomPill = `<div>
                <span style="font-weight:700; color:var(--cg-text-primary);">${escapeHtml(e.roomCode)}</span>
                <span style="font-size:11px; color:var(--cg-text-muted);">(${escapeHtml(e.roomType)}, Cap: ${e.roomCapacity})</span>
                <div style="font-size:11px; color:var(--cg-text-secondary);">${escapeHtml(e.blockName || '')}</div>
            </div>`;
        }

        const facultyText = e.facultyName
            ? `<div><strong>${escapeHtml(e.facultyName)}</strong><div style="font-size:11px; color:var(--cg-text-muted);">${escapeHtml(e.facultyEmployeeId || '')}</div></div>`
            : `<span style="color:var(--cg-text-muted); font-style:italic;">To Be Assigned</span>`;

        const typeBadge = e.classType === 'LAB'
            ? `<span class="badge-cg badge-inactive" style="background:#E0E7FF; color:#3730A3;">LAB</span>`
            : `<span class="badge-cg badge-active" style="background:#F1F5F9; color:#334155;">${escapeHtml(e.classType)}</span>`;

        const statusBadge = isCancelled
            ? `<span class="badge-cg badge-inactive" style="background:#FEE2E2; color:#991B1B;">CANCELLED</span>`
            : `<span class="badge-cg badge-active">SCHEDULED</span>`;

        return `
            <tr style="${isCancelled ? 'opacity: 0.6; background: #FAFAFA;' : ''}">
                <td>
                    <div style="font-weight:700; color:var(--cg-brand-black);">${escapeHtml(e.dayOfWeek)}</div>
                    <div style="font-size:12px; color:var(--cg-text-secondary);">${escapeHtml(e.timeSlot)}</div>
                </td>
                <td>
                    <div style="font-weight:600; color:var(--cg-text-primary);">${escapeHtml(e.subjectName)}</div>
                    <div style="font-size:11px; color:var(--cg-text-muted);">${escapeHtml(e.subjectCode)} &bull; ${e.credits} credits</div>
                </td>
                <td>
                    <span style="font-weight:600; padding:2px 7px; background:#F1F5F9; border-radius:4px; font-size:11.5px;">${escapeHtml(e.sectionName)}</span>
                    <div style="font-size:11px; color:var(--cg-text-muted); margin-top:2px;">${escapeHtml(e.department)}</div>
                </td>
                <td>${roomPill}</td>
                <td>${facultyText}</td>
                <td>${typeBadge}</td>
                <td>${statusBadge}</td>
                <td style="text-align: right;">
                    <div style="display:inline-flex; gap:6px;">
                        ${!isCancelled ? `
                            <button class="btn btn-outline" style="padding: 4px 8px; font-size:11.5px;" onclick="openAllocateModalForEntry(${e.id})">
                                ${hasRoom ? 'Reassign' : 'Allocate Room'}
                            </button>
                            <button class="btn btn-outline" style="padding: 4px 8px; font-size:11.5px; color:#DC2626;" onclick="handleCancelTimetable(${e.id})">
                                Cancel
                            </button>
                        ` : `
                            <span style="font-size:11px; color:var(--cg-text-muted); font-style:italic;">Cancelled</span>
                        `}
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

function renderCohortGrid(entries) {
    const area = document.getElementById('timetableContentArea');
    if (!area) return;

    const days = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];
    const activeEntries = entries.filter(e => e.status === 'SCHEDULED');

    let gridHtml = `
        <div class="table-card" style="padding: 20px;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
                <div style="font-weight:700; font-size:15px; color:var(--cg-brand-black);">Cohort Weekly Schedule: Section CSE-A</div>
                <div style="font-size:12px; color:var(--cg-text-muted);">Showing all active scheduled lectures and labs</div>
            </div>
            <div style="display: grid; grid-template-columns: repeat(6, 1fr); gap: 12px;">
    `;

    days.forEach(day => {
        const dayEntries = activeEntries.filter(e => e.dayOfWeek === day);
        gridHtml += `
            <div style="background: #F8FAFC; border: 1px solid var(--cg-border); border-radius: var(--cg-radius-md); padding: 12px;">
                <div style="font-weight: 700; font-size: 13px; color: var(--cg-brand-black); padding-bottom: 8px; border-bottom: 2px solid var(--cg-border); margin-bottom: 10px;">
                    ${day.substring(0, 3)}
                </div>
                <div style="display: flex; flex-direction: column; gap: 8px;">
                    ${dayEntries.length === 0 ? `<div style="font-size:11.5px; color:var(--cg-text-muted); text-align:center; padding:12px 0;">No classes</div>` : ''}
                    ${dayEntries.map(e => `
                        <div style="padding: 8px; background: #FFFFFF; border: 1px solid var(--cg-border); border-left: 3px solid ${e.classType === 'LAB' ? '#4F46E5' : '#0F172A'}; border-radius: 4px; font-size: 11.5px;">
                            <div style="font-weight: 700; color: var(--cg-text-primary);">${escapeHtml(e.subjectCode)}</div>
                            <div style="font-size: 10.5px; color: var(--cg-text-secondary); margin: 2px 0;">${escapeHtml(e.timeSlot)}</div>
                            <div style="font-weight: 600; color: #059669;">${escapeHtml(e.roomCode || 'Unassigned')}</div>
                        </div>
                    `).join('')}
                </div>
            </div>
        `;
    });

    gridHtml += `</div></div>`;
    area.innerHTML = gridHtml;
}

function switchTimetableView(view) {
    currentView = view;
    document.getElementById('tabListView')?.classList.toggle('active', view === 'list');
    document.getElementById('tabGroupView')?.classList.toggle('active', view === 'group');
    document.getElementById('tabSubjectsView')?.classList.toggle('active', view === 'subjects');
    document.getElementById('sideLinkTimetable')?.classList.toggle('active', view !== 'subjects');
    document.getElementById('sideLinkSubjects')?.classList.toggle('active', view === 'subjects');

    if (view === 'subjects') {
        renderSubjectsView();
        return;
    }

    if (view === 'list') {
        const area = document.getElementById('timetableContentArea');
        area.innerHTML = `
            <div class="table-card">
                <div style="overflow-x: auto;">
                    <table class="table-cg">
                        <thead>
                            <tr>
                                <th>Day &amp; Time</th>
                                <th>Subject</th>
                                <th>Cohort</th>
                                <th>Assigned Room</th>
                                <th>Faculty Member</th>
                                <th>Type</th>
                                <th>Status</th>
                                <th style="text-align: right;">Actions</th>
                            </tr>
                        </thead>
                        <tbody id="timetableTableBody"></tbody>
                    </table>
                </div>
            </div>
        `;
        renderTimetableTable(allTimetableEntries);
    } else {
        renderCohortGrid(allTimetableEntries);
    }
}

// =========================================================================
// MODAL CONTROLS & SUBMISSION
// =========================================================================

function openCreateTimetableModal() {
    hideAlert('createAlert');
    document.getElementById('createTimetableForm').reset();
    document.getElementById('modalCreateTimetable').style.display = 'flex';
}

async function handleCreateTimetable(e) {
    e.preventDefault();
    hideAlert('createAlert');

    const btn = document.getElementById('btnSubmitCreate');
    btn.disabled = true;
    btn.textContent = 'Scheduling...';

    const payload = {
        subjectId: parseInt(document.getElementById('createSubject').value),
        department: document.getElementById('createDepartment').value,
        sectionName: document.getElementById('createSection').value,
        dayOfWeek: document.getElementById('createDayOfWeek').value,
        classType: document.getElementById('createClassType').value,
        startTime: document.getElementById('createStartTime').value,
        endTime: document.getElementById('createEndTime').value,
        expectedCapacity: parseInt(document.getElementById('createCapacity').value),
        facultyId: document.getElementById('createFaculty').value ? parseInt(document.getElementById('createFaculty').value) : null,
        roomId: document.getElementById('createRoom').value ? parseInt(document.getElementById('createRoom').value) : null,
        remarks: document.getElementById('createRemarks').value || null
    };

    try {
        const res = await Auth.fetchWithAuth('/api/admin/timetables', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const json = await res.json();
        if (!res.ok || json.status !== 'SUCCESS') {
            showAlert('createAlert', json.message || 'Scheduling failed.', 'error');
            btn.disabled = false;
            btn.textContent = 'Create & Schedule';
            return;
        }

        closeModal('modalCreateTimetable');
        await Promise.all([loadTimetables(), loadTimetableStats()]);
    } catch (err) {
        showAlert('createAlert', 'Error: ' + err.message, 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Create & Schedule';
    }
}

function openAllocateModalForEntry(entryId) {
    const entry = allTimetableEntries.find(e => e.id === entryId);
    if (!entry) return;

    hideAlert('allocAlert');
    document.getElementById('allocEntryId').value = entry.id;
    document.getElementById('allocSlotHeader').textContent = `${entry.subjectName} (${entry.subjectCode})`;
    document.getElementById('allocSlotTime').textContent = `${entry.dayOfWeek} • ${entry.timeSlot} • Section ${entry.sectionName}`;

    if (entry.roomId) {
        document.getElementById('allocRoomSelect').value = entry.roomId;
    } else {
        document.getElementById('allocRoomSelect').value = '';
    }

    document.getElementById('modalAllocateRoom').style.display = 'flex';
}

async function handleAllocateRoomSubmit(e) {
    e.preventDefault();
    hideAlert('allocAlert');

    const entryId = document.getElementById('allocEntryId').value;
    const roomId = document.getElementById('allocRoomSelect').value;

    if (!roomId) {
        showAlert('allocAlert', 'Please select a physical room to allocate.', 'error');
        return;
    }

    try {
        const res = await Auth.fetchWithAuth(`/api/admin/timetables/${entryId}/allocate-room`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ roomId: parseInt(roomId) })
        });

        const json = await res.json();
        if (!res.ok || json.status !== 'SUCCESS') {
            showAlert('allocAlert', json.message || 'Room allocation failed.', 'error');
            return;
        }

        closeModal('modalAllocateRoom');
        await Promise.all([loadTimetables(), loadTimetableStats()]);
    } catch (err) {
        showAlert('allocAlert', 'Error: ' + err.message, 'error');
    }
}

async function handleDeallocateCurrentRoom() {
    const entryId = document.getElementById('allocEntryId').value;
    if (!confirm('Deallocate room from this timetable entry? The class will remain scheduled without an assigned room.')) return;

    try {
        const res = await Auth.fetchWithAuth(`/api/admin/timetables/${entryId}/deallocate-room`, {
            method: 'PUT'
        });

        const json = await res.json();
        if (!res.ok || json.status !== 'SUCCESS') {
            showAlert('allocAlert', json.message || 'Deallocation failed.', 'error');
            return;
        }

        closeModal('modalAllocateRoom');
        await Promise.all([loadTimetables(), loadTimetableStats()]);
    } catch (err) {
        showAlert('allocAlert', 'Error: ' + err.message, 'error');
    }
}

async function handleCancelTimetable(id) {
    const reason = prompt('Please enter the reason for cancellation (optional):', 'Instructor unavailable');
    if (reason === null) return; // user cancelled prompt

    try {
        const res = await Auth.fetchWithAuth(`/api/admin/timetables/${id}?reason=` + encodeURIComponent(reason), {
            method: 'DELETE'
        });

        const json = await res.json();
        if (!res.ok || json.status !== 'SUCCESS') {
            alert('Cancellation failed: ' + (json.message || 'Server error'));
            return;
        }

        await Promise.all([loadTimetables(), loadTimetableStats()]);
    } catch (err) {
        alert('Error: ' + err.message);
    }
}

// =========================================================================
// ROOM AVAILABILITY SEARCH
// =========================================================================

function openAvailabilityModal() {
    document.getElementById('modalAvailability').style.display = 'flex';
}

async function searchAvailableRooms() {
    const container = document.getElementById('availResultsContainer');
    container.innerHTML = `<div style="text-align:center; padding:30px; color:var(--cg-text-muted);">Querying persisted unreserved rooms...</div>`;

    const day = document.getElementById('availDayOfWeek').value;
    const start = document.getElementById('availStartTime').value;
    const end = document.getElementById('availEndTime').value;
    const minCap = document.getElementById('availMinCapacity').value;

    const params = new URLSearchParams();
    params.append('dayOfWeek', day);
    params.append('startTime', start);
    params.append('endTime', end);
    if (minCap) params.append('minCapacity', minCap);

    try {
        const res = await Auth.fetchWithAuth('/api/admin/rooms/availability?' + params.toString());
        const json = await res.json();

        if (json.status !== 'SUCCESS' || !Array.isArray(json.data) || json.data.length === 0) {
            container.innerHTML = `
                <div style="text-align:center; padding:40px; color:var(--cg-text-muted);">
                    <div style="font-weight:600; color:var(--cg-text-primary); margin-bottom:4px;">No Available Rooms Found</div>
                    <div style="font-size:12px;">All suitable rooms are either booked during this interval or under maintenance.</div>
                </div>
            `;
            return;
        }

        container.innerHTML = `
            <table class="table-cg" style="font-size:12.5px;">
                <thead>
                    <tr>
                        <th>Room Code</th>
                        <th>Type</th>
                        <th>Capacity</th>
                        <th>Building Location</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>
                    ${json.data.map(r => `
                        <tr>
                            <td>
                                <strong>${escapeHtml(r.roomCode)}</strong>
                                <div style="font-size:11px; color:var(--cg-text-muted);">${escapeHtml(r.roomName)}</div>
                            </td>
                            <td><span class="badge-cg badge-active" style="background:#F1F5F9; color:#334155;">${escapeHtml(r.roomType)}</span></td>
                            <td><strong>${r.capacity}</strong> seats</td>
                            <td>${escapeHtml(r.blockName || '')} (Floor ${r.floorNumber != null ? r.floorNumber : 'Ground'})</td>
                            <td><span class="badge-cg badge-active">AVAILABLE</span></td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        `;
    } catch (err) {
        container.innerHTML = `<div style="text-align:center; padding:30px; color:#DC2626;">Error checking availability: ${escapeHtml(err.message)}</div>`;
    }
}

// =========================================================================
// UTILITIES & HELPERS
// =========================================================================

function setupFilterListeners() {
    ['filterDayOfWeek', 'filterDepartment', 'filterSection', 'filterRoom', 'filterStatus'].forEach(id => {
        document.getElementById(id)?.addEventListener('change', () => loadTimetables());
    });

    let searchTimeout;
    document.getElementById('timetableSearchInput')?.addEventListener('input', () => {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => loadTimetables(), 300);
    });
}

function resetFilters() {
    ['filterDayOfWeek', 'filterDepartment', 'filterSection', 'filterRoom', 'filterStatus', 'timetableSearchInput'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.value = '';
    });
    loadTimetables();
}

function closeModal(modalId) {
    const m = document.getElementById(modalId);
    if (m) m.style.display = 'none';
}

function showAlert(boxId, msg, type) {
    const b = document.getElementById(boxId);
    if (!b) return;
    b.textContent = msg;
    b.style.display = 'block';
    if (type === 'error') {
        b.style.background = '#FEE2E2';
        b.style.color = '#991B1B';
        b.style.border = '1px solid #FCA5A5';
    } else {
        b.style.background = '#ECFDF5';
        b.style.color = '#065F46';
        b.style.border = '1px solid #A7F3D0';
    }
}

function hideAlert(boxId) {
    const b = document.getElementById(boxId);
    if (b) b.style.display = 'none';
}

function escapeHtml(str) {
    if (str == null) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}


// =========================================================================
// MODULE 3: SUBJECTS & SECTIONS MANAGEMENT
// =========================================================================

function renderSubjectsView() {
    const area = document.getElementById('timetableContentArea');
    if (!area) return;

    area.innerHTML = `
        <div class="table-card" style="padding: 20px;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px;">
                <div>
                    <h3 style="font-size: 16px; font-weight: 700; color: var(--cg-brand-black); margin-bottom: 4px;">Academic Subjects &amp; Course Directory (Module 3)</h3>
                    <div style="font-size: 12.5px; color: var(--cg-text-secondary);">Active accredited courses, theory modules, and laboratory practicums</div>
                </div>
                <div>
                    <button class="btn btn-primary" onclick="openCreateSubjectModal()" style="display: inline-flex; align-items: center; gap: 6px;">
                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
                        <span>Add New Subject</span>
                    </button>
                </div>
            </div>

            <div style="overflow-x: auto;">
                <table class="table-cg">
                    <thead>
                        <tr>
                            <th>Subject Code</th>
                            <th>Course Title</th>
                            <th>Department</th>
                            <th>Type</th>
                            <th>Semester</th>
                            <th>Credits</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody id="subjectsTableBody">
                        ${allSubjects.map(s => `
                            <tr>
                                <td><span style="font-weight: 700; font-family: monospace; padding: 2px 8px; background: #F1F5F9; border-radius: 4px;">${escapeHtml(s.subjectCode)}</span></td>
                                <td><div style="font-weight: 600; color: var(--cg-text-primary);">${escapeHtml(s.subjectName)}</div></td>
                                <td>${escapeHtml(s.department)}</td>
                                <td><span class="badge-cg ${s.subjectType === 'PRACTICAL' ? 'badge-active' : 'badge-neutral'}" style="font-size: 11px;">${escapeHtml(s.subjectType)}</span></td>
                                <td>Semester ${s.semester}</td>
                                <td><strong>${s.credits}</strong> credits</td>
                                <td><span class="badge-cg badge-active">Active Accredited</span></td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        </div>
    `;
}

function openCreateSubjectModal() {
    const modal = document.getElementById('modalCreateSubject');
    if (modal) modal.classList.add('show');
}

async function submitNewSubject(event) {
    event.preventDefault();

    const payload = {
        subjectCode: document.getElementById('newSubjCode').value.trim().toUpperCase(),
        subjectName: document.getElementById('newSubjName').value.trim(),
        department: document.getElementById('newSubjDept').value.trim(),
        subjectType: document.getElementById('newSubjType').value,
        semester: parseInt(document.getElementById('newSubjSem').value),
        credits: parseInt(document.getElementById('newSubjCredits').value)
    };

    try {
        const res = await Auth.fetchWithAuth('/api/admin/subjects', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const json = await res.json();
        if (json.status === 'SUCCESS') {
            closeModal('modalCreateSubject');
            document.getElementById('createSubjectForm').reset();
            
            // Reload subjects
            const subjRes = await Auth.fetchWithAuth('/api/admin/subjects');
            const subjJson = await subjRes.json();
            if (subjJson.status === 'SUCCESS' && Array.isArray(subjJson.data)) {
                allSubjects = subjJson.data;
                populateSubjectSelect(allSubjects);
            }
            renderSubjectsView();
            alert('Subject added successfully!');
        } else {
            alert('Failed to add subject: ' + json.message);
        }
    } catch (err) {
        alert('Error adding subject: ' + err.message);
    }
}
