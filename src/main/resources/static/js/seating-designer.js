/**
 * CampusGrid — Module 2 Seating Designer & Grid Matrix JS
 */

let currentRoomId = null;
let currentGridData = null;

document.addEventListener('DOMContentLoaded', () => {
    checkAuthSession();
    updateDateDisplay();
    populateRoomsDropdown();
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

async function populateRoomsDropdown() {
    const select = document.getElementById('designerRoomSelect');
    select.innerHTML = '<option value="">Select Room...</option>';

    try {
        const res = await fetch('/api/spaces/rooms', { headers: authHeaders() });
        const json = await res.json();
        if (json.success && json.data) {
            json.data.forEach(r => {
                select.innerHTML += `<option value="${r.id}">${escapeHtml(r.roomCode)} &mdash; ${escapeHtml(r.roomName)} (${r.capacity} seats)</option>`;
            });

            // Check if roomId query param is present
            const params = new URLSearchParams(window.location.search);
            const queryRoomId = params.get('roomId');
            if (queryRoomId) {
                select.value = queryRoomId;
                onRoomSelected();
            }
        }
    } catch (e) {
        console.error(e);
    }
}

async function onRoomSelected() {
    const select = document.getElementById('designerRoomSelect');
    const roomId = select.value;
    if (!roomId) {
        currentRoomId = null;
        document.getElementById('gridRoomHeader').textContent = 'Select a room to load layout';
        document.getElementById('gridRoomMeta').textContent = '--';
        document.getElementById('seatingMatrixWrapper').innerHTML = '<div style="color:var(--cg-text-muted); font-size:13px;">Please select an academic room above.</div>';
        return;
    }

    currentRoomId = parseInt(roomId);
    loadRoomGrid(currentRoomId);
}

async function loadRoomGrid(roomId) {
    const wrapper = document.getElementById('seatingMatrixWrapper');
    wrapper.innerHTML = '<div style="color:var(--cg-text-muted); font-size:13px;">Loading seating grid...</div>';

    try {
        const res = await fetch(`/api/spaces/rooms/${roomId}/grid`, { headers: authHeaders() });
        const json = await res.json();
        if (json.success && json.data) {
            currentGridData = json.data;
            renderSeatingGrid(currentGridData);
        } else {
            wrapper.innerHTML = '<div style="color:var(--cg-danger); font-size:13px;">Failed to load seating grid.</div>';
        }
    } catch (e) {
        wrapper.innerHTML = '<div style="color:var(--cg-danger); font-size:13px;">Error connecting to server.</div>';
    }
}

function renderSeatingGrid(data) {
    // Update header & metrics
    const totalCount = data.configuredSeatsCount !== undefined ? data.configuredSeatsCount : (data.seats ? data.seats.length : 0);
    const availCount = data.availableSeatsCount !== undefined ? data.availableSeatsCount : 0;
    const blockedCount = data.blockedSeatsCount !== undefined ? data.blockedSeatsCount : 0;

    document.getElementById('gridRoomHeader').textContent = `${data.roomCode} — ${data.roomName}`;
    document.getElementById('gridRoomMeta').textContent = `Type: ${data.roomType} | Declared Capacity: ${data.capacity} | Grid Dimensions: ${data.rowsCount} x ${data.columnsCount}`;
    document.getElementById('metricActiveSeats').textContent = `${availCount} / ${totalCount}`;
    document.getElementById('metricBlockedSeats').textContent = blockedCount;

    // Populate inputs in controls bar
    document.getElementById('designerTemplateSelect').value = data.layoutTemplate || 'STANDARD_GRID';
    document.getElementById('designerRowsInput').value = data.rowsCount;
    document.getElementById('designerColsInput').value = data.columnsCount;

    const wrapper = document.getElementById('seatingMatrixWrapper');

    if (!data.seats || data.seats.length === 0) {
        wrapper.innerHTML = `
            <div style="text-align:center; padding:30px;">
                <p style="color:var(--cg-text-muted); font-size:13px; margin-bottom:12px;">No seating layout currently configured for this room.</p>
                <button class="btn btn-secondary btn-sm" onclick="applyLayoutConfiguration()">Generate Initial Grid</button>
            </div>
        `;
        return;
    }

    // Build lookup map for seats by "r-c"
    const seatMap = new Map();
    data.seats.forEach(s => {
        seatMap.set(`${s.rowNumber}-${s.columnNumber}`, s);
    });

    const aisleCols = new Set(data.aisleColumns || []);
    const aisleRows = new Set(data.aisleRows || []);

    const matrix = document.createElement('div');
    matrix.className = 'seating-matrix';
    matrix.style.gridTemplateColumns = `repeat(${data.columnsCount}, 38px)`;

    for (let r = 1; r <= data.rowsCount; r++) {
        for (let c = 1; c <= data.columnsCount; c++) {
            const cell = document.createElement('div');

            if (aisleRows.has(r) || aisleCols.has(c)) {
                // Walkway / Aisle Gap
                cell.className = 'seat-cell aisle-gap';
                cell.title = 'Aisle Walkway';
            } else {
                const seat = seatMap.get(`${r}-${c}`);
                if (seat) {
                    const statusClass = seat.status.toLowerCase();
                    const workstationClass = seat.seatType === 'WORKSTATION' ? 'workstation' : '';
                    cell.className = `seat-cell ${statusClass} ${workstationClass}`;
                    cell.textContent = seat.seatNumber;
                    cell.title = `Seat ${seat.seatNumber} (${seat.status}) - Click to toggle status`;
                    cell.onclick = () => toggleSeatStatus(seat.id, seat.status);
                } else {
                    cell.className = 'seat-cell aisle-gap';
                }
            }

            matrix.appendChild(cell);
        }
    }

    wrapper.innerHTML = '';
    wrapper.appendChild(matrix);
}

async function toggleSeatStatus(seatId, currentStatus) {
    if (!currentRoomId) return;

    // Cycle status: AVAILABLE -> BLOCKED -> MAINTENANCE -> AVAILABLE
    let newStatus = 'BLOCKED';
    if (currentStatus === 'AVAILABLE') {
        newStatus = 'BLOCKED';
    } else if (currentStatus === 'BLOCKED') {
        newStatus = 'MAINTENANCE';
    } else {
        newStatus = 'AVAILABLE';
    }

    try {
        const res = await fetch(`/api/admin/spaces/rooms/${currentRoomId}/seats/${seatId}/status?status=${newStatus}`, {
            method: 'PUT',
            headers: authHeaders()
        });
        const json = await res.json();
        if (json.success) {
            // Reload grid
            loadRoomGrid(currentRoomId);
        } else {
            alert('Failed to update seat: ' + json.message);
        }
    } catch (e) {
        alert('Error: ' + e.message);
    }
}

async function applyLayoutConfiguration() {
    if (!currentRoomId) {
        alert('Please select a room first.');
        return;
    }

    const template = document.getElementById('designerTemplateSelect').value;
    const rows = parseInt(document.getElementById('designerRowsInput').value);
    const cols = parseInt(document.getElementById('designerColsInput').value);

    if (isNaN(rows) || isNaN(cols) || rows < 1 || cols < 1) {
        alert('Please enter valid positive dimensions for rows and columns.');
        return;
    }

    if (!confirm(`Warning: Regenerating the grid will replace existing physical seat numbers and custom statuses with a newly configured ${rows}x${cols} ${template} layout. Continue?`)) {
        return;
    }

    const payload = {
        layoutTemplate: template,
        rowsCount: rows,
        columnsCount: cols
    };

    try {
        const res = await fetch(`/api/admin/spaces/rooms/${currentRoomId}/layout`, {
            method: 'POST',
            headers: authHeaders(),
            body: JSON.stringify(payload)
        });
        const json = await res.json();
        if (json.success && json.data) {
            currentGridData = json.data;
            renderSeatingGrid(currentGridData);
        } else {
            alert('Layout Generation Rejected: ' + json.message);
        }
    } catch (e) {
        alert('Error: ' + e.message);
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
}
