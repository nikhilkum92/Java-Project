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

function getStoredToken() {
    return (typeof Auth !== 'undefined' && Auth.getToken && Auth.getToken()) ||
        localStorage.getItem('campusgrid_access_token') ||
        localStorage.getItem('accessToken');
}

function getStoredUser() {
    if (typeof Auth !== 'undefined' && Auth.getUser) {
        const u = Auth.getUser();
        if (u) return u;
    }
    const raw = localStorage.getItem('campusgrid_user');
    if (raw) {
        try { return JSON.parse(raw); } catch (e) {}
    }
    const role = localStorage.getItem('userRole');
    const username = localStorage.getItem('username');
    if (role) return { role: role, username: username || '', fullName: username || '' };
    return null;
}

function checkAuthSession() {
    const token = getStoredToken();
    const user = getStoredUser();
    const role = user ? user.role : localStorage.getItem('userRole');
    if (!token || role !== 'ADMIN') {
        window.location.href = '/login';
        return;
    }
    const username = (user && (user.fullName || user.username)) || localStorage.getItem('username') || 'Administrator';
    const displayNameEl = document.getElementById('userDisplayName') || document.getElementById('topUserName');
    const avatarEl = document.getElementById('userAvatar') || document.getElementById('topAvatar');
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

function isSuccess(res) {
    return res && (res.success === true || res.status === 'SUCCESS');
}

function authHeaders() {
    return {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + getStoredToken()
    };
}

async function populateRoomsDropdown() {
    const select = document.getElementById('designerRoomSelect');
    select.innerHTML = '<option value="">Select Room...</option>';

    try {
        const res = await fetch('/api/spaces/rooms', { headers: authHeaders() });
        const json = await res.json();
        if (isSuccess(json) && json.data) {
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
        if (isSuccess(json) && json.data) {
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
        if (isSuccess(json)) {
            // Reload grid
            loadRoomGrid(currentRoomId);
        } else {
            alert('Failed to update seat: ' + json.message);
        }
    } catch (e) {
        alert('Error: ' + e.message);
    }
}

function determineAisleCols(template, cols) {
    const aisles = [];
    if (template === 'CENTRAL_AISLE' || template === 'H_PATTERN') {
        if (cols >= 3) aisles.push(Math.floor(cols / 2) + 1);
    } else if (template === 'LABORATORY') {
        for (let c = 3; c <= cols; c += 3) aisles.push(c);
    }
    return aisles;
}

function determineAisleRows(template, rows) {
    const aisles = [];
    if (template === 'H_PATTERN') {
        if (rows >= 4) aisles.push(Math.floor(rows / 2) + 1);
    }
    return aisles;
}

function getRowLetter(rowNumber) {
    let sb = '';
    let r = rowNumber;
    while (r > 0) {
        r--;
        sb = String.fromCharCode(65 + (r % 26)) + sb;
        r = Math.floor(r / 26);
    }
    return sb;
}

function onTemplateOrDimensionsChanged() {
    if (!currentRoomId || !currentGridData) return;

    const template = document.getElementById('designerTemplateSelect').value;
    const rows = parseInt(document.getElementById('designerRowsInput').value);
    const cols = parseInt(document.getElementById('designerColsInput').value);

    if (isNaN(rows) || isNaN(cols) || rows < 1 || cols < 1) return;

    const aisleCols = determineAisleCols(template, cols);
    const aisleRows = determineAisleRows(template, rows);

    const validRows = Math.max(0, rows - aisleRows.length);
    const validCols = Math.max(0, cols - aisleCols.length);
    const estSeats = validRows * validCols;
    const capacity = currentGridData.capacity || 0;

    const feedbackEl = document.getElementById('layoutFeedbackMsg');
    const applyBtn = document.getElementById('btnApplyLayout');

    if (feedbackEl) {
        feedbackEl.style.display = 'flex';
        if (estSeats > capacity) {
            feedbackEl.innerHTML = `
                <span style="color:var(--cg-danger); font-weight:600;">⚠️ ${estSeats} physical seats exceeds room capacity of ${capacity}.</span>
                <span style="color:var(--cg-text-muted);">Please decrease rows or columns to fit within capacity.</span>
            `;
            if (applyBtn) applyBtn.disabled = true;
        } else {
            feedbackEl.innerHTML = `
                <span style="color:var(--cg-primary-blue); font-weight:600;">⚡ Live Preview:</span>
                <span style="color:var(--cg-text-secondary);">${estSeats} seats calculated (${capacity} capacity). Click <strong>Apply &amp; Save Layout</strong> to commit.</span>
            `;
            if (applyBtn) applyBtn.disabled = false;
        }
    }

    // Render visual live preview
    renderPreviewGrid(template, rows, cols, aisleCols, aisleRows);
}

function renderPreviewGrid(template, rows, cols, aisleCols, aisleRows) {
    const wrapper = document.getElementById('seatingMatrixWrapper');
    if (!wrapper) return;

    const aisleColSet = new Set(aisleCols);
    const aisleRowSet = new Set(aisleRows);

    const matrix = document.createElement('div');
    matrix.className = 'seating-matrix';
    matrix.style.gridTemplateColumns = `repeat(${cols}, 38px)`;

    let seatCounter = 1;
    for (let r = 1; r <= rows; r++) {
        const isAisleRow = aisleRowSet.has(r);
        const rowLabel = isAisleRow ? '' : getRowLetter(r);

        for (let c = 1; c <= cols; c++) {
            const cell = document.createElement('div');
            const isAisleCol = aisleColSet.has(c);

            if (isAisleRow || isAisleCol) {
                cell.className = 'seat-cell aisle-gap';
                cell.title = 'Aisle Walkway';
            } else {
                const seatNum = `${rowLabel}-${String(c).padStart(2, '0')}`;
                const seatType = (template === 'LABORATORY') ? 'workstation' : '';
                cell.className = `seat-cell available ${seatType}`;
                cell.textContent = seatNum;
                cell.title = `Preview Seat ${seatNum}`;
                seatCounter++;
            }
            matrix.appendChild(cell);
        }
    }

    wrapper.innerHTML = '';
    wrapper.appendChild(matrix);
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

    const payload = {
        layoutTemplate: template,
        rowsCount: rows,
        columnsCount: cols
    };

    const feedbackEl = document.getElementById('layoutFeedbackMsg');
    if (feedbackEl) {
        feedbackEl.style.display = 'flex';
        feedbackEl.innerHTML = `<span style="color:var(--cg-primary-blue);">Saving and generating ${template} layout...</span>`;
    }

    try {
        const res = await fetch(`/api/admin/spaces/rooms/${currentRoomId}/layout`, {
            method: 'POST',
            headers: authHeaders(),
            body: JSON.stringify(payload)
        });
        const json = await res.json();
        if (isSuccess(json) && json.data) {
            currentGridData = json.data;
            renderSeatingGrid(currentGridData);

            if (feedbackEl) {
                feedbackEl.innerHTML = `
                    <span style="color:var(--cg-success); font-weight:600;">✓ Layout saved successfully!</span>
                    <span style="color:var(--cg-text-secondary);">${json.data.configuredSeatsCount} physical seats active in ${rows}x${cols} ${template} grid.</span>
                `;
                setTimeout(() => {
                    if (feedbackEl) feedbackEl.style.display = 'none';
                }, 5000);
            }
        } else {
            alert('Layout Generation Rejected: ' + (json.message || 'Unknown error'));
            if (feedbackEl) feedbackEl.style.display = 'none';
        }
    } catch (e) {
        alert('Error: ' + e.message);
        if (feedbackEl) feedbackEl.style.display = 'none';
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
}
