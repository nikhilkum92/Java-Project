/**
 * CampusGrid — Module 2 Campus Hierarchy Management JS
 */

let globalCampuses = [];
let globalTree = [];

document.addEventListener('DOMContentLoaded', () => {
    checkAuthSession();
    updateDateDisplay();
    loadSpaceStats();
    loadCampusTree();
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

async function loadSpaceStats() {
    try {
        const res = await fetch('/api/spaces/stats', { headers: authHeaders() });
        const data = await res.json();
        if (data.success && data.data) {
            document.getElementById('statCampuses').textContent = data.data.totalCampuses;
            document.getElementById('statBlocks').textContent = data.data.totalBlocks;
            document.getElementById('statFloors').textContent = data.data.totalFloors;
            document.getElementById('statRooms').textContent = data.data.totalRooms;
        }
    } catch (err) {
        console.error('Failed to load space stats:', err);
    }
}

async function loadCampusTree() {
    const treeContainer = document.getElementById('treeContainer');
    treeContainer.innerHTML = '<div style="text-align:center; padding: 20px; color: var(--cg-text-muted);">Loading hierarchy...</div>';

    try {
        const res = await fetch('/api/spaces/tree', { headers: authHeaders() });
        const json = await res.json();
        if (json.success && json.data) {
            globalTree = json.data;
            renderTree(globalTree);
        } else {
            treeContainer.innerHTML = '<div style="color:var(--cg-danger); padding:10px;">Failed to load hierarchy.</div>';
        }
    } catch (err) {
        treeContainer.innerHTML = '<div style="color:var(--cg-danger); padding:10px;">Error connecting to server.</div>';
    }
}

function renderTree(campuses) {
    const treeContainer = document.getElementById('treeContainer');
    if (!campuses || campuses.length === 0) {
        treeContainer.innerHTML = '<div style="text-align:center; padding: 20px; color: var(--cg-text-muted);">No campuses found. Click "+ Add Campus" to create one.</div>';
        return;
    }

    let html = '';
    campuses.forEach(c => {
        html += `
        <div class="tree-node">
            <div class="tree-header" onclick="selectCampusNode(${c.id})">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polygon points="1 6 1 22 8 18 16 22 23 18 23 2 16 6 8 2 1 6"></polygon>
                </svg>
                <span style="font-weight:600;">${escapeHtml(c.name)} (${escapeHtml(c.code)})</span>
            </div>
            <div class="tree-children">
        `;

        if (c.blocks && c.blocks.length > 0) {
            c.blocks.forEach(b => {
                html += `
                <div class="tree-node">
                    <div class="tree-header" onclick="selectBlockNode(${c.id}, ${b.id})">
                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                        </svg>
                        <span>${escapeHtml(b.name)} [${escapeHtml(b.code)}]</span>
                    </div>
                    <div class="tree-children">
                `;

                if (b.floors && b.floors.length > 0) {
                    b.floors.forEach(f => {
                        html += `
                        <div class="tree-node">
                            <div class="tree-header" onclick="selectFloorNode(${c.id}, ${b.id}, ${f.id})">
                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <line x1="18" y1="20" x2="18" y2="10"></line>
                                    <line x1="12" y1="20" x2="12" y2="4"></line>
                                    <line x1="6" y1="20" x2="6" y2="14"></line>
                                </svg>
                                <span>${escapeHtml(f.name)} (${f.rooms ? f.rooms.length : 0} rooms)</span>
                            </div>
                        </div>
                        `;
                    });
                } else {
                    html += `<div style="font-size:12px; color:var(--cg-text-muted); padding:4px 8px;">No floors configured.</div>`;
                }

                html += `</div></div>`;
            });
        } else {
            html += `<div style="font-size:12px; color:var(--cg-text-muted); padding:4px 8px;">No blocks added.</div>`;
        }

        html += `</div></div>`;
    });

    treeContainer.innerHTML = html;
}

function selectCampusNode(campusId) {
    const campus = globalTree.find(c => c.id === campusId);
    if (!campus) return;

    const pane = document.getElementById('nodeDetailsPane');
    pane.innerHTML = `
        <div style="display:flex; justify-content:space-between; align-items:flex-start;">
            <div>
                <span class="badge" style="background:#EFF6FF; color:#1D4ED8; font-weight:600; padding:2px 8px; border-radius:4px; font-size:11px;">CAMPUS</span>
                <h2 style="font-size: 20px; font-weight: 700; margin-top: 6px;">${escapeHtml(campus.name)}</h2>
                <div style="font-size: 13px; color: var(--cg-text-secondary); margin-top: 4px;">Code: <strong>${escapeHtml(campus.code)}</strong> | Address: ${escapeHtml(campus.address || 'Not specified')}</div>
            </div>
            <div style="display:flex; gap:8px;">
                <button class="btn btn-secondary btn-sm" onclick="openAddBlockForCampus(${campus.id})">+ Add Block</button>
                <button class="btn btn-danger btn-sm" onclick="deleteCampus(${campus.id})">Delete</button>
            </div>
        </div>

        <div style="margin-top: 24px;">
            <h3 style="font-size: 14px; font-weight: 600; margin-bottom: 12px;">Blocks in this Campus (${campus.blocks ? campus.blocks.length : 0})</h3>
            <div style="display:grid; grid-template-columns: repeat(2, 1fr); gap: 12px;">
                ${(campus.blocks || []).map(b => `
                    <div class="card" style="padding: 14px; border: 1px solid var(--cg-border); cursor:pointer;" onclick="selectBlockNode(${campus.id}, ${b.id})">
                        <div style="font-weight: 600; font-size: 14px;">${escapeHtml(b.name)}</div>
                        <div style="font-size: 12px; color: var(--cg-text-muted); margin-top: 2px;">Code: ${escapeHtml(b.code)} &bull; ${b.floors ? b.floors.length : 0} Floors</div>
                    </div>
                `).join('')}
            </div>
        </div>
    `;
}

function selectBlockNode(campusId, blockId) {
    const campus = globalTree.find(c => c.id === campusId);
    if (!campus) return;
    const block = (campus.blocks || []).find(b => b.id === blockId);
    if (!block) return;

    const pane = document.getElementById('nodeDetailsPane');
    pane.innerHTML = `
        <div style="display:flex; justify-content:space-between; align-items:flex-start;">
            <div>
                <span class="badge" style="background:#F1F5F9; color:#0F172A; font-weight:600; padding:2px 8px; border-radius:4px; font-size:11px;">BLOCK</span>
                <h2 style="font-size: 20px; font-weight: 700; margin-top: 6px;">${escapeHtml(block.name)}</h2>
                <div style="font-size: 13px; color: var(--cg-text-secondary); margin-top: 4px;">Campus: <strong>${escapeHtml(campus.name)}</strong> | Code: <strong>${escapeHtml(block.code)}</strong></div>
            </div>
            <div style="display:flex; gap:8px;">
                <button class="btn btn-secondary btn-sm" onclick="openAddFloorModal(${block.id})">+ Add Floor</button>
                <button class="btn btn-danger btn-sm" onclick="deleteBlock(${block.id})">Delete Block</button>
            </div>
        </div>

        <div style="margin-top: 24px;">
            <h3 style="font-size: 14px; font-weight: 600; margin-bottom: 12px;">Floors &amp; Rooms in ${escapeHtml(block.name)}</h3>
            <div style="display:flex; flex-direction:column; gap: 12px;">
                ${(block.floors || []).map(f => `
                    <div class="card" style="padding: 16px; border: 1px solid var(--cg-border);">
                        <div style="display:flex; justify-content:space-between; align-items:center;">
                            <div>
                                <span style="font-weight: 600; font-size: 14px;">${escapeHtml(f.name)} (Level ${f.floorNumber})</span>
                                <span style="font-size: 12px; color: var(--cg-text-muted); margin-left: 8px;">${f.rooms ? f.rooms.length : 0} Rooms</span>
                            </div>
                            <div style="display:flex; gap:6px;">
                                <a href="/admin/rooms?blockId=${block.id}&floorId=${f.id}" class="btn btn-subtle btn-sm" style="text-decoration:none;">View Rooms</a>
                                <button class="btn btn-danger btn-sm" onclick="deleteFloor(${f.id})">Delete</button>
                            </div>
                        </div>
                    </div>
                `).join('')}
            </div>
        </div>
    `;
}

function selectFloorNode(campusId, blockId, floorId) {
    selectBlockNode(campusId, blockId);
}

// Modal Handlers
function openModal(id) {
    const modal = document.getElementById(id);
    if (modal) modal.classList.add('show');
}

function closeModal(id) {
    const modal = document.getElementById(id);
    if (modal) modal.classList.remove('show');
}

function openCreateCampusModal() {
    document.getElementById('campusForm').reset();
    openModal('campusModal');
}

async function handleSaveCampus(e) {
    e.preventDefault();
    const payload = {
        name: document.getElementById('campusNameInput').value.trim(),
        code: document.getElementById('campusCodeInput').value.trim().toUpperCase(),
        address: document.getElementById('campusAddressInput').value.trim()
    };

    try {
        const res = await fetch('/api/admin/spaces/campuses', {
            method: 'POST',
            headers: authHeaders(),
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            closeModal('campusModal');
            loadCampusTree();
            loadSpaceStats();
        } else {
            alert('Failed: ' + data.message);
        }
    } catch (err) {
        alert('Error saving campus: ' + err.message);
    }
}

async function openCreateBlockModal() {
    // Populate campus dropdown
    const select = document.getElementById('blockCampusSelect');
    select.innerHTML = '<option value="">Select Campus...</option>';
    try {
        const res = await fetch('/api/admin/spaces/campuses', { headers: authHeaders() });
        const json = await res.json();
        if (json.success && json.data) {
            json.data.forEach(c => {
                select.innerHTML += `<option value="${c.id}">${escapeHtml(c.name)} (${escapeHtml(c.code)})</option>`;
            });
        }
    } catch (e) {
        console.error(e);
    }
    document.getElementById('blockForm').reset();
    openModal('blockModal');
}

function openAddBlockForCampus(campusId) {
    openCreateBlockModal().then(() => {
        const select = document.getElementById('blockCampusSelect');
        if (select) select.value = campusId;
    });
}

async function handleSaveBlock(e) {
    e.preventDefault();
    const payload = {
        campusId: parseInt(document.getElementById('blockCampusSelect').value),
        name: document.getElementById('blockNameInput').value.trim(),
        code: document.getElementById('blockCodeInput').value.trim().toUpperCase()
    };

    try {
        const res = await fetch('/api/admin/spaces/blocks', {
            method: 'POST',
            headers: authHeaders(),
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            closeModal('blockModal');
            loadCampusTree();
            loadSpaceStats();
        } else {
            alert('Failed: ' + data.message);
        }
    } catch (err) {
        alert('Error saving block: ' + err.message);
    }
}

function openAddFloorModal(blockId) {
    document.getElementById('floorForm').reset();
    document.getElementById('floorBlockIdHidden').value = blockId;
    openModal('floorModal');
}

async function handleSaveFloor(e) {
    e.preventDefault();
    const payload = {
        blockId: parseInt(document.getElementById('floorBlockIdHidden').value),
        floorNumber: parseInt(document.getElementById('floorNumberInput').value),
        name: document.getElementById('floorNameInput').value.trim()
    };

    try {
        const res = await fetch('/api/admin/spaces/floors', {
            method: 'POST',
            headers: authHeaders(),
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            closeModal('floorModal');
            loadCampusTree();
            loadSpaceStats();
        } else {
            alert('Failed: ' + data.message);
        }
    } catch (err) {
        alert('Error saving floor: ' + err.message);
    }
}

async function deleteCampus(id) {
    if (!confirm('Are you sure you want to delete this campus?')) return;
    try {
        const res = await fetch(`/api/admin/spaces/campuses/${id}`, {
            method: 'DELETE',
            headers: authHeaders()
        });
        const data = await res.json();
        if (data.success) {
            loadCampusTree();
            loadSpaceStats();
            document.getElementById('nodeDetailsPane').innerHTML = '<div style="padding:40px; text-align:center; color:var(--cg-text-muted);">Campus deleted.</div>';
        } else {
            alert(data.message);
        }
    } catch (err) {
        alert('Error: ' + err.message);
    }
}

async function deleteBlock(id) {
    if (!confirm('Are you sure you want to delete this block?')) return;
    try {
        const res = await fetch(`/api/admin/spaces/blocks/${id}`, {
            method: 'DELETE',
            headers: authHeaders()
        });
        const data = await res.json();
        if (data.success) {
            loadCampusTree();
            loadSpaceStats();
            document.getElementById('nodeDetailsPane').innerHTML = '<div style="padding:40px; text-align:center; color:var(--cg-text-muted);">Block deleted.</div>';
        } else {
            alert(data.message);
        }
    } catch (err) {
        alert('Error: ' + err.message);
    }
}

async function deleteFloor(id) {
    if (!confirm('Are you sure you want to delete this floor?')) return;
    try {
        const res = await fetch(`/api/admin/spaces/floors/${id}`, {
            method: 'DELETE',
            headers: authHeaders()
        });
        const data = await res.json();
        if (data.success) {
            loadCampusTree();
            loadSpaceStats();
            document.getElementById('nodeDetailsPane').innerHTML = '<div style="padding:40px; text-align:center; color:var(--cg-text-muted);">Floor deleted.</div>';
        } else {
            alert(data.message);
        }
    } catch (err) {
        alert('Error: ' + err.message);
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
}
