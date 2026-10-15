/**
 * CampusGrid — Module 2 Rooms & Facilities Management JS
 */

let globalRooms = [];

document.addEventListener('DOMContentLoaded', () => {
    checkAuthSession();
    updateDateDisplay();
    populateBlockFilters();
    populateFloorDropdown();
    loadRoomsTable();

    document.getElementById('roomTableSearch').addEventListener('input', debounce(() => {
        applyFilters();
    }, 300));
});

function checkAuthSession() {
    const token = localStorage.getItem('accessToken');
    const role = localStorage.getItem('userRole');
    if (!token || role !== 'ADMIN') {
        window.location.href = '/login';
    }
    const username = localStorage.getItem('username') || 'Administrator';
    const displayNameEl = document.getElementById('userDisplayName');
    const avatarEl = document.getElementById('userAvatar');
    if (displayNameEl) displayNameEl.textContent = username;
    if (avatarEl) avatarEl.textContent = username.charAt(0).toUpperCase();
}

function updateDateDisplay() {
    const dateEl = document.getElementById('currentDateStr');
    if (dateEl) {
        const options = { weekday: 'short', year: 'numeric', month: 'short', day: 'numeric' };
        dateEl.textContent = new Date().toLocaleDateString('en-US', options);
    }
}

function authHeaders() {
    return {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + localStorage.getItem('accessToken')
    };
}

async function populateBlockFilters() {
    const filterBlock = document.getElementById('filterBlock');
    try {
        const res = await fetch('/api/spaces/tree', { headers: authHeaders() });
        const json = await res.json();
        if (json.success && json.data) {
            filterBlock.innerHTML = '<option value="">All Blocks</option>';
            json.data.forEach(c => {
                (c.blocks || []).forEach(b => {
                    filterBlock.innerHTML += `<option value="${b.id}">${escapeHtml(b.name)} [${escapeHtml(b.code)}]</option>`;
                });
            });
        }
    } catch (e) {
        console.error(e);
    }
}

async function populateFloorDropdown() {
    const select = document.getElementById('roomFloorSelect');
    try {
        const res = await fetch('/api/spaces/tree', { headers: authHeaders() });
        const json = await res.json();
        if (json.success && json.data) {
            select.innerHTML = '<option value="">Select Floor...</option>';
            json.data.forEach(c => {
                (c.blocks || []).forEach(b => {
                    (b.floors || []).forEach(f => {
                        select.innerHTML += `<option value="${f.id}">${escapeHtml(c.name)} &gt; ${escapeHtml(b.name)} &gt; ${escapeHtml(f.name)}</option>`;
                    });
                });
            });
        }
    } catch (e) {
        console.error(e);
    }
}

async function loadRoomsTable(params = {}) {
    const tbody = document.getElementById('roomsTableBody');
    tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:32px; color:var(--cg-text-muted);">Loading rooms...</td></tr>';

    let url = '/api/spaces/rooms?';
    const query = new URLSearchParams(params).toString();
    url += query;

    try {
        const res = await fetch(url, { headers: authHeaders() });
        const json = await res.json();
        if (json.success && json.data) {
            globalRooms = json.data;
            renderRooms(globalRooms);
        } else {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:32px; color:var(--cg-danger);">Failed to load rooms.</td></tr>';
        }
    } catch (e) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:32px; color:var(--cg-danger);">Error connecting to server.</td></tr>';
    }
}

function renderRooms(rooms) {
    const tbody = document.getElementById('roomsTableBody');
    if (!rooms || rooms.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:32px; color:var(--cg-text-muted);">No matching rooms found.</td></tr>';
        return;
    }

    let html = '';
    rooms.forEach(r => {
        const statusBadge = getStatusBadge(r.status);
        const typeBadge = getTypeBadge(r.roomType);

        html += `
        <tr style="border-bottom: 1px solid var(--cg-border); transition: background-color 0.15s ease;">
            <td style="padding:12px 16px; font-weight:600; color:var(--cg-text-primary);">
                <a href="javascript:void(0)" onclick="viewRoomDetail(${r.id})" style="color:var(--cg-text-primary); text-decoration:none; font-weight:600;">
                    ${escapeHtml(r.roomCode)}
                </a>
            </td>
            <td style="padding:12px 16px;">
                <div style="font-weight:500;">${escapeHtml(r.roomName)}</div>
                <div style="font-size:12px; color:var(--cg-text-muted);">${escapeHtml(r.campusName)} &bull; ${escapeHtml(r.blockName)} &bull; ${escapeHtml(r.floorName)}</div>
            </td>
            <td style="padding:12px 16px;">${typeBadge}</td>
            <td style="padding:12px 16px; font-weight:600;">${r.capacity} seats</td>
            <td style="padding:12px 16px;">${statusBadge}</td>
            <td style="padding:12px 16px; font-size:12px; color:var(--cg-text-secondary); max-width:240px; text-overflow:ellipsis; overflow:hidden; white-space:nowrap;">
                ${escapeHtml(r.facilities || 'None listed')}
            </td>
            <td style="padding:12px 16px; text-align:right;">
                <div style="display:inline-flex; gap:6px;">
                    <a href="/admin/seating?roomId=${r.id}" class="btn btn-secondary btn-sm" style="text-decoration:none;" title="Open Seating Layout Designer">Seating Grid</a>
                    <button class="btn btn-subtle btn-sm" onclick="editRoom(${r.id})" title="Edit Details">Edit</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteRoom(${r.id})" title="Delete Room">Delete</button>
                </div>
            </td>
        </tr>
        `;
    });

    tbody.innerHTML = html;
}

function getStatusBadge(status) {
    if (status === 'AVAILABLE') {
        return '<span class="badge" style="background:#DCFCE7; color:#15803D; padding:2px 8px; border-radius:4px; font-size:11px; font-weight:600;">AVAILABLE</span>';
    } else if (status === 'OCCUPIED') {
        return '<span class="badge" style="background:#EFF6FF; color:#1D4ED8; padding:2px 8px; border-radius:4px; font-size:11px; font-weight:600;">OCCUPIED</span>';
    } else if (status === 'MAINTENANCE') {
        return '<span class="badge" style="background:#FEF3C7; color:#B45309; padding:2px 8px; border-radius:4px; font-size:11px; font-weight:600;">MAINTENANCE</span>';
    } else {
        return '<span class="badge" style="background:#F1F5F9; color:#64748B; padding:2px 8px; border-radius:4px; font-size:11px; font-weight:600;">INACTIVE</span>';
    }
}

function getTypeBadge(type) {
    if (type === 'AUDITORIUM') {
        return '<span class="badge" style="background:#F3E8FF; color:#7E22CE; padding:2px 8px; border-radius:4px; font-size:11px; font-weight:600;">AUDITORIUM</span>';
    } else if (type === 'LABORATORY') {
        return '<span class="badge" style="background:#E0E7FF; color:#4338CA; padding:2px 8px; border-radius:4px; font-size:11px; font-weight:600;">LAB</span>';
    } else {
        return '<span class="badge" style="background:#F1F5F9; color:#334155; padding:2px 8px; border-radius:4px; font-size:11px; font-weight:600;">CLASSROOM</span>';
    }
}

function applyFilters() {
    const params = {};
    const search = document.getElementById('roomTableSearch').value.trim();
    if (search) params.search = search;

    const roomType = document.getElementById('filterRoomType').value;
    if (roomType) params.roomType = roomType;

    const status = document.getElementById('filterStatus').value;
    if (status) params.status = status;

    const minCap = document.getElementById('filterMinCapacity').value;
    if (minCap) params.minCapacity = minCap;

    const blockId = document.getElementById('filterBlock').value;
    if (blockId) params.blockId = blockId;

    loadRoomsTable(params);
}

async function viewRoomDetail(roomId) {
    try {
        const res = await fetch(`/api/spaces/rooms/${roomId}`, { headers: authHeaders() });
        const json = await res.json();
        if (json.success && json.data) {
            const d = json.data;
            const r = d.room;
            const content = document.getElementById('roomDetailContent');

            let facilitiesPills = (d.facilitiesList && d.facilitiesList.length > 0)
                ? d.facilitiesList.map(f => `<span style="background:#F1F5F9; padding:3px 10px; border-radius:12px; font-size:12px; font-weight:500;">${escapeHtml(f)}</span>`).join(' ')
                : '<span style="color:var(--cg-text-muted);">None recorded</span>';

            content.innerHTML = `
                <div style="margin-bottom:16px;">
                    <div style="font-size:12px; color:var(--cg-text-muted);">${escapeHtml(d.hierarchyPath)}</div>
                    <h2 style="font-size:20px; font-weight:700; margin-top:4px;">${escapeHtml(r.roomName)} (${escapeHtml(r.roomCode)})</h2>
                </div>

                <div style="display:grid; grid-template-columns: repeat(3, 1fr); gap:12px; margin-bottom:20px;">
                    <div class="card" style="padding:12px; text-align:center;">
                        <div style="font-size:11px; color:var(--cg-text-muted); text-transform:uppercase;">Capacity</div>
                        <div style="font-size:18px; font-weight:700; margin-top:2px;">${r.capacity}</div>
                    </div>
                    <div class="card" style="padding:12px; text-align:center;">
                        <div style="font-size:11px; color:var(--cg-text-muted); text-transform:uppercase;">Configured Seats</div>
                        <div style="font-size:18px; font-weight:700; margin-top:2px;">${d.configuredSeats}</div>
                    </div>
                    <div class="card" style="padding:12px; text-align:center;">
                        <div style="font-size:11px; color:var(--cg-text-muted); text-transform:uppercase;">Available Seats</div>
                        <div style="font-size:18px; font-weight:700; margin-top:2px; color:var(--cg-success);">${d.availableSeats}</div>
                    </div>
                </div>

                <div style="margin-bottom:16px;">
                    <div style="font-size:12px; font-weight:600; margin-bottom:8px;">Facilities &amp; Amenities:</div>
                    <div style="display:flex; flex-wrap:wrap; gap:6px;">${facilitiesPills}</div>
                </div>

                <div style="background:#F8FAFC; border:1px solid var(--cg-border); border-radius:6px; padding:14px; margin-top:16px;">
                    <div style="font-size:12px; font-weight:700; color:var(--cg-brand-black); margin-bottom: 8px;">Cross-Module Operations &amp; Schedules:</div>
                    <div style="display: flex; gap: 8px; flex-wrap: wrap;">
                        <a href="/admin/timetable?search=${encodeURIComponent(r.roomCode)}" class="btn btn-outline" style="font-size:11.5px; padding:4px 10px; text-decoration:none;">
                            📅 View Room Timetable (Mod 3)
                        </a>
                        <a href="/admin/examinations" class="btn btn-outline" style="font-size:11.5px; padding:4px 10px; text-decoration:none;">
                            📝 View Exam Allocations (Mod 4)
                        </a>
                        <a href="/admin/seating" class="btn btn-outline" style="font-size:11.5px; padding:4px 10px; text-decoration:none;">
                            🪑 Configure Seating Layout (Mod 2)
                        </a>
                    </div>
                </div>
            `;
            openModal('roomDetailModal');
        }
    } catch (e) {
        alert('Failed to load room details: ' + e.message);
    }
}

function openCreateRoomModal() {
    document.getElementById('roomForm').reset();
    document.getElementById('editRoomIdHidden').value = '';
    document.getElementById('roomModalTitle').textContent = 'Add Academic Room';
    document.getElementById('autoLayoutSection').style.display = 'block';
    openModal('roomModal');
}

function editRoom(id) {
    const r = globalRooms.find(x => x.id === id);
    if (!r) return;

    document.getElementById('roomForm').reset();
    document.getElementById('editRoomIdHidden').value = r.id;
    document.getElementById('roomFloorSelect').value = r.floorId;
    document.getElementById('roomCodeInput').value = r.roomCode;
    document.getElementById('roomNameInput').value = r.roomName;
    document.getElementById('roomTypeSelect').value = r.roomType;
    document.getElementById('roomCapacityInput').value = r.capacity;
    document.getElementById('roomStatusSelect').value = r.status;
    document.getElementById('roomFacilitiesInput').value = r.facilities || '';
    document.getElementById('roomModalTitle').textContent = 'Edit Room ' + r.roomCode;
    document.getElementById('autoLayoutSection').style.display = 'none'; // Don't wipe seating layout on simple edit

    openModal('roomModal');
}

async function handleSaveRoom(e) {
    e.preventDefault();
    const editId = document.getElementById('editRoomIdHidden').value;
    const isEdit = !!editId;

    const payload = {
        floorId: parseInt(document.getElementById('roomFloorSelect').value),
        roomCode: document.getElementById('roomCodeInput').value.trim().toUpperCase(),
        roomName: document.getElementById('roomNameInput').value.trim(),
        roomType: document.getElementById('roomTypeSelect').value,
        capacity: parseInt(document.getElementById('roomCapacityInput').value),
        status: document.getElementById('roomStatusSelect').value,
        facilities: document.getElementById('roomFacilitiesInput').value.trim()
    };

    if (!isEdit) {
        const rows = document.getElementById('roomRowsInput').value;
        const cols = document.getElementById('roomColsInput').value;
        if (rows && cols) {
            payload.rowsCount = parseInt(rows);
            payload.columnsCount = parseInt(cols);
            payload.layoutTemplate = document.getElementById('roomLayoutTemplateSelect').value;
        }
    }

    const url = isEdit ? `/api/admin/spaces/rooms/${editId}` : '/api/admin/spaces/rooms';
    const method = isEdit ? 'PUT' : 'POST';

    try {
        const res = await fetch(url, {
            method: method,
            headers: authHeaders(),
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            closeModal('roomModal');
            loadRoomsTable();
        } else {
            alert('Failed: ' + data.message);
        }
    } catch (err) {
        alert('Error: ' + err.message);
    }
}

async function deleteRoom(id) {
    if (!confirm('Are you sure you want to delete this room and all associated seating records?')) return;
    try {
        const res = await fetch(`/api/admin/spaces/rooms/${id}`, {
            method: 'DELETE',
            headers: authHeaders()
        });
        const data = await res.json();
        if (data.success) {
            loadRoomsTable();
        } else {
            alert(data.message);
        }
    } catch (e) {
        alert('Error: ' + e.message);
    }
}

function openModal(id) {
    const modal = document.getElementById(id);
    if (modal) modal.classList.add('show');
}

function closeModal(id) {
    const modal = document.getElementById(id);
    if (modal) modal.classList.remove('show');
}

function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
}
