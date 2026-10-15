/**
 * CampusGrid — Examination & Seat Allocation Management Client (Module 4)
 */
const ExamMgmt = {
    exams: [],
    rooms: [],
    facultyList: [],
    subjects: [],
    selectedExamId: null,
    selectedRoomId: null,

    async init() {
        if (typeof Auth !== 'undefined' && !Auth.isAuthenticated()) {
            window.location.href = '/login';
            return;
        }

        const dateEl = document.getElementById('currentDateStr');
        if (dateEl) {
            const now = new Date();
            dateEl.textContent = now.toLocaleDateString('en-US', { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' });
        }

        if (typeof Auth !== 'undefined') {
            const user = Auth.getUser();
            if (user) {
                const nameEl = document.getElementById('userDisplayName');
                const avEl = document.getElementById('userAvatar');
                if (nameEl) nameEl.textContent = user.fullName || user.username || 'Administrator';
                if (avEl) avEl.textContent = (user.fullName || user.username || 'A').charAt(0).toUpperCase();
            }
        }

        // Global search input
        const searchInput = document.getElementById('globalExamSearch');
        if (searchInput) {
            let debounceTimer;
            searchInput.addEventListener('input', (e) => {
                clearTimeout(debounceTimer);
                debounceTimer = setTimeout(() => {
                    this.loadExams(e.target.value.trim());
                }, 300);
            });
        }

        // Parallel load metadata
        await Promise.all([
            this.loadStats(),
            this.loadExams(),
            this.loadReferenceData()
        ]);
    },

    // =========================================================================
    // METADATA & STATS
    // =========================================================================

    async loadStats() {
        try {
            const res = await Auth.fetchWithAuth('/api/admin/exams/stats');
            const json = await res.json();
            if (json.status === 'SUCCESS' && json.data) {
                const s = json.data;
                const setTxt = (id, val) => { const el = document.getElementById(id); if (el) el.textContent = val; };
                setTxt('statTotalExams', s.totalExams || 0);
                setTxt('statAllocatedSeats', s.totalAllocatedSeats || 0);
                setTxt('statActiveHalls', s.activeExamHalls || 0);
                setTxt('statScheduledExams', (s.scheduledExams || 0) + (s.seatingAllocatedExams || 0));
            }
        } catch (e) {
            console.error('Failed to load exam statistics:', e);
        }
    },

    async loadReferenceData() {
        try {
            const [roomsRes, facultyRes, subjectsRes] = await Promise.all([
                Auth.fetchWithAuth('/api/spaces/rooms'),
                Auth.fetchWithAuth('/api/users/faculty'),
                Auth.fetchWithAuth('/api/timetables/subjects')
            ]);

            const roomsJson = await roomsRes.json();
            if (roomsJson.status === 'SUCCESS' && Array.isArray(roomsJson.data)) {
                this.rooms = roomsJson.data;
            }

            const facultyJson = await facultyRes.json();
            if (facultyJson.status === 'SUCCESS' && Array.isArray(facultyJson.data)) {
                this.facultyList = facultyJson.data;
            }

            const subJson = await subjectsRes.json();
            if (subJson.status === 'SUCCESS' && Array.isArray(subJson.data)) {
                this.subjects = subJson.data;
            }

            this.populateDropdowns();
        } catch (e) {
            console.warn('Reference data load notice:', e);
        }
    },

    populateDropdowns() {
        // Populate Rooms in Allocator
        const roomSelect = document.getElementById('allocatorRoomSelect');
        if (roomSelect) {
            roomSelect.innerHTML = '<option value="">-- Choose Hall / Room --</option>' +
                this.rooms.map(r => `<option value="${r.id}">${escapeHtml(r.roomCode)} — ${escapeHtml(r.roomName)} (Cap: ${r.capacity})</option>`).join('');
        }

        // Populate Faculty in Allocator
        const facSelect = document.getElementById('allocatorFacultySelect');
        if (facSelect) {
            facSelect.innerHTML = '<option value="">-- Choose Invigilator (Optional) --</option>' +
                this.facultyList.map(f => `<option value="${f.id}">${escapeHtml(f.fullName)} (${escapeHtml(f.rollNumber || f.specificId || f.department || 'Faculty')})</option>`).join('');
        }

        // Populate Subjects in Schedule Modal
        const subSelect = document.getElementById('schedSubjectSelect');
        if (subSelect) {
            subSelect.innerHTML = '<option value="">-- Select Existing Subject (Optional) --</option>' +
                this.subjects.map(s => `<option value="${s.id}" data-code="${escapeHtml(s.subjectCode)}" data-name="${escapeHtml(s.subjectName)}" data-dept="${escapeHtml(s.department)}" data-sem="${s.semester}">[${escapeHtml(s.subjectCode)}] ${escapeHtml(s.subjectName)}</option>`).join('');
        }
    },

    // =========================================================================
    // TAB MANAGEMENT
    // =========================================================================

    switchTab(tabKey) {
        const tabs = ['schedules', 'allocator', 'matrix', 'roster'];
        tabs.forEach(t => {
            const tabBtn = document.getElementById(`tabLink${t.charAt(0).toUpperCase() + t.slice(1)}`);
            const tabContent = document.getElementById(`tabContent${t.charAt(0).toUpperCase() + t.slice(1)}`);
            if (tabBtn) tabBtn.classList.remove('active');
            if (tabContent) tabContent.style.display = 'none';
        });

        const activeBtn = document.getElementById(`tabLink${tabKey.charAt(0).toUpperCase() + tabKey.slice(1)}`);
        const activeContent = document.getElementById(`tabContent${tabKey.charAt(0).toUpperCase() + tabKey.slice(1)}`);
        if (activeBtn) activeBtn.classList.add('active');
        if (activeContent) activeContent.style.display = 'block';

        if (tabKey === 'allocator') {
            this.syncExamSelectDropdown('allocatorExamSelect');
            this.onExamSelectedForAllocation();
        } else if (tabKey === 'matrix') {
            this.syncExamSelectDropdown('matrixExamSelect');
            this.onMatrixExamChanged();
        } else if (tabKey === 'roster') {
            this.syncExamSelectDropdown('rosterExamSelect');
            this.onRosterExamChanged();
        }
    },

    syncExamSelectDropdown(selectId) {
        const select = document.getElementById(selectId);
        if (!select) return;

        const currentVal = select.value || (this.selectedExamId ? String(this.selectedExamId) : '');
        select.innerHTML = '<option value="">-- Choose Examination Session --</option>' +
            this.exams.map(e => `<option value="${e.id}">${escapeHtml(e.examCode)}: ${escapeHtml(e.title)} (${escapeHtml(e.examDate)})</option>`).join('');

        if (currentVal && Array.from(select.options).some(o => o.value === currentVal)) {
            select.value = currentVal;
        } else if (this.exams.length > 0) {
            select.value = String(this.exams[0].id);
        }
    },

    // =========================================================================
    // TAB 1: EXAM SCHEDULES
    // =========================================================================

    async loadExams(searchQuery = '') {
        const dept = document.getElementById('filterDept') ? document.getElementById('filterDept').value : '';
        const sem = document.getElementById('filterSemester') ? document.getElementById('filterSemester').value : '';
        const status = document.getElementById('filterStatus') ? document.getElementById('filterStatus').value : '';

        let url = '/api/admin/exams?';
        const params = [];
        if (dept) params.push(`department=${encodeURIComponent(dept)}`);
        if (sem) params.push(`semester=${encodeURIComponent(sem)}`);
        if (status) params.push(`status=${encodeURIComponent(status)}`);
        if (searchQuery) params.push(`search=${encodeURIComponent(searchQuery)}`);
        url += params.join('&');

        const tbody = document.getElementById('examsTableBody');
        if (tbody) tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; padding:30px; color:var(--cg-text-muted);">Loading exam records...</td></tr>';

        try {
            const res = await Auth.fetchWithAuth(url);
            const json = await res.json();
            if (json.status === 'SUCCESS' && Array.isArray(json.data)) {
                this.exams = json.data;
                this.renderExams(this.exams);
            } else {
                if (tbody) tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; padding:30px; color:var(--cg-danger);">${escapeHtml(json.message || 'Failed to load exams')}</td></tr>`;
            }
        } catch (e) {
            if (tbody) tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; padding:30px; color:var(--cg-danger);">Error: ${escapeHtml(e.message)}</td></tr>`;
        }
    },

    renderExams(exams) {
        const tbody = document.getElementById('examsTableBody');
        if (!tbody) return;

        if (exams.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="8" style="text-align: center; padding: 48px 20px;">
                        <div style="font-weight: 600; font-size: 14px; color: var(--cg-brand-black); margin-bottom: 4px;">No Examination Sessions Found</div>
                        <div style="font-size: 12.5px; color: var(--cg-text-muted); margin-bottom: 16px;">Create a new examination schedule or adjust filters.</div>
                        <button class="btn btn-primary" onclick="ExamMgmt.openScheduleModal()">Schedule First Exam</button>
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = exams.map(e => {
            const statusClass = e.status === 'SEATING_ALLOCATED' ? 'badge-active' :
                               (e.status === 'COMPLETED' ? 'badge-primary' : 'badge-inactive');

            const hallsSummary = (e.rooms && e.rooms.length > 0)
                ? e.rooms.map(r => `<span style="display:inline-block; background:#F1F5F9; border-radius:4px; padding:2px 6px; font-size:11px; margin:2px;">${escapeHtml(r.roomCode)}</span>`).join(' ')
                : '<span style="color:var(--cg-text-muted); font-size:12px;">No halls assigned</span>';

            return `
                <tr>
                    <td>
                        <span style="font-weight: 700; color: var(--cg-brand-black); font-size: 13px;">${escapeHtml(e.examCode)}</span>
                    </td>
                    <td>
                        <div style="font-weight: 600; color: var(--cg-brand-black);">${escapeHtml(e.title)}</div>
                        <div style="font-size: 11.5px; color: var(--cg-text-muted);">${escapeHtml(e.courseCode || '')} &bull; ${escapeHtml(e.courseName || '')}</div>
                    </td>
                    <td>
                        <div style="font-size: 12px; font-weight: 600;">${escapeHtml(e.department)}</div>
                        <div style="font-size: 11px; color: var(--cg-text-muted);">Semester ${e.semester} (${escapeHtml(e.targetSection || 'ALL')})</div>
                    </td>
                    <td>
                        <div style="font-weight: 600; font-size: 12.5px;">📅 ${escapeHtml(e.examDate)}</div>
                        <div style="font-size: 11.5px; color: var(--cg-text-muted);">🕒 ${escapeHtml(e.startTime)} - ${escapeHtml(e.endTime)} (${e.durationMinutes}m)</div>
                    </td>
                    <td>${hallsSummary}</td>
                    <td>
                        <div style="display: flex; align-items: center; gap: 6px;">
                            <span style="font-weight: 700; font-size: 14px; color: ${e.allocatedStudentsCount > 0 ? '#10B981' : 'var(--cg-text-muted)'};">
                                ${e.allocatedStudentsCount}
                            </span>
                            <span style="font-size: 11px; color: var(--cg-text-muted);">candidates</span>
                        </div>
                    </td>
                    <td>
                        <span class="badge-cg ${statusClass}">${escapeHtml(e.status)}</span>
                    </td>
                    <td style="text-align: right;">
                        <div style="display: flex; gap: 6px; justify-content: flex-end;">
                            <button class="btn-action-cg" title="Manage Halls &amp; Seating Engine" onclick="ExamMgmt.jumpToAllocator(${e.id})">
                                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"></circle><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path></svg>
                                <span>Seat</span>
                            </button>
                            <button class="btn-action-cg" title="View 2D Seating Matrix" onclick="ExamMgmt.jumpToMatrix(${e.id})">
                                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><line x1="3" y1="9" x2="21" y2="9"></line><line x1="9" y1="21" x2="9" y2="9"></line></svg>
                                <span>Matrix</span>
                            </button>
                            <button class="btn-action-cg" style="color:var(--cg-danger);" title="Delete Exam" onclick="ExamMgmt.deleteExam(${e.id}, '${escapeHtml(e.title)}')">
                                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');
    },

    resetFilters() {
        if (document.getElementById('filterDept')) document.getElementById('filterDept').value = '';
        if (document.getElementById('filterSemester')) document.getElementById('filterSemester').value = '';
        if (document.getElementById('filterStatus')) document.getElementById('filterStatus').value = '';
        this.loadExams();
    },

    openScheduleModal() {
        const modal = document.getElementById('modalScheduleExam');
        if (modal) {
            document.getElementById('formScheduleExam').reset();
            const dateInput = document.getElementById('schedDate');
            if (dateInput) {
                const nextWeek = new Date();
                nextWeek.setDate(nextWeek.getDate() + 7);
                dateInput.value = nextWeek.toISOString().split('T')[0];
            }
            modal.classList.add('show');
        }
    },

    closeScheduleModal() {
        const modal = document.getElementById('modalScheduleExam');
        if (modal) modal.classList.remove('show');
    },

    onSubjectAutoFill() {
        const subSelect = document.getElementById('schedSubjectSelect');
        const selected = subSelect.options[subSelect.selectedIndex];
        if (!selected || !selected.value) return;

        const code = selected.getAttribute('data-code');
        const name = selected.getAttribute('data-name');
        const dept = selected.getAttribute('data-dept');
        const sem = selected.getAttribute('data-sem');

        if (code && document.getElementById('schedCourseCode')) document.getElementById('schedCourseCode').value = code;
        if (name && document.getElementById('schedCourseName')) document.getElementById('schedCourseName').value = name;
        if (dept && document.getElementById('schedDept')) document.getElementById('schedDept').value = dept;
        if (sem && document.getElementById('schedSemester')) document.getElementById('schedSemester').value = sem;
        if (name && document.getElementById('schedTitle')) {
            document.getElementById('schedTitle').value = `Mid-Term Examination: ${name}`;
        }
    },

    async submitScheduleExam(e) {
        e.preventDefault();
        const payload = {
            title: document.getElementById('schedTitle').value.trim(),
            examCode: document.getElementById('schedExamCode').value.trim() || null,
            subjectId: document.getElementById('schedSubjectSelect').value ? parseInt(document.getElementById('schedSubjectSelect').value) : null,
            courseCode: document.getElementById('schedCourseCode').value.trim() || null,
            courseName: document.getElementById('schedCourseName').value.trim() || null,
            department: document.getElementById('schedDept').value,
            semester: parseInt(document.getElementById('schedSemester').value),
            examDate: document.getElementById('schedDate').value,
            startTime: document.getElementById('schedStartTime').value,
            endTime: document.getElementById('schedEndTime').value,
            instructions: document.getElementById('schedInstructions').value.trim() || null
        };

        try {
            const res = await Auth.fetchWithAuth('/api/admin/exams', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            const json = await res.json();
            if (json.status === 'SUCCESS') {
                this.closeScheduleModal();
                await this.loadExams();
                await this.loadStats();
                alert('Examination session scheduled successfully!');
            } else {
                alert('Could not schedule exam: ' + json.message);
            }
        } catch (err) {
            alert('Request error: ' + err.message);
        }
    },

    async deleteExam(id, title) {
        if (!confirm(`Are you sure you want to delete examination "${title}" and all associated hall seating records?`)) {
            return;
        }

        try {
            const res = await Auth.fetchWithAuth(`/api/admin/exams/${id}`, { method: 'DELETE' });
            const json = await res.json();
            if (json.status === 'SUCCESS') {
                await this.loadExams();
                await this.loadStats();
            } else {
                alert('Could not delete exam: ' + json.message);
            }
        } catch (err) {
            alert('Request error: ' + err.message);
        }
    },

    // =========================================================================
    // TAB 2: HALL & SEATING ENGINE
    // =========================================================================

    jumpToAllocator(examId) {
        this.selectedExamId = examId;
        this.switchTab('allocator');
    },

    jumpToMatrix(examId) {
        this.selectedExamId = examId;
        this.switchTab('matrix');
    },

    onExamSelectedForAllocation() {
        const select = document.getElementById('allocatorExamSelect');
        const examId = select ? select.value : null;
        if (!examId) {
            document.getElementById('assignedHallsList').innerHTML = '<div style="font-size: 12px; color: var(--cg-text-muted);">Please select an exam to view assigned halls.</div>';
            return;
        }

        this.selectedExamId = parseInt(examId);
        const exam = this.exams.find(e => e.id === this.selectedExamId);
        const container = document.getElementById('assignedHallsList');

        if (!exam || !exam.rooms || exam.rooms.length === 0) {
            container.innerHTML = '<div style="font-size: 12.5px; color: var(--cg-text-muted); padding: 8px 0;">No examination halls assigned yet. Add one above.</div>';
            return;
        }

        container.innerHTML = exam.rooms.map(r => `
            <div style="background: #FFFFFF; border: 1px solid var(--cg-border); border-radius: 6px; padding: 10px 14px; display: flex; justify-content: space-between; align-items: center;">
                <div>
                    <div style="font-weight: 700; font-size: 13.5px; color: var(--cg-brand-black);">Hall: ${escapeHtml(r.roomCode)} (${escapeHtml(r.roomName)})</div>
                    <div style="font-size: 11.5px; color: var(--cg-text-muted);">
                        📍 ${escapeHtml(r.blockName || '')} &bull; Capacity: <strong>${r.roomTotalCapacity}</strong> seats &bull; Invigilator: <strong>${escapeHtml(r.invigilatorName || 'Unassigned')}</strong>
                    </div>
                </div>
                <button class="btn-action-cg" style="color:var(--cg-danger);" title="Unassign Hall" onclick="ExamMgmt.unassignHall(${exam.id}, ${r.roomId})">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
                    <span>Remove</span>
                </button>
            </div>
        `).join('');
    },

    async assignHallToExam() {
        const examId = document.getElementById('allocatorExamSelect').value;
        const roomId = document.getElementById('allocatorRoomSelect').value;
        const facId = document.getElementById('allocatorFacultySelect').value;
        const notes = document.getElementById('allocatorRoomNotes').value.trim();

        if (!examId) {
            alert('Please select a target examination session first.');
            return;
        }
        if (!roomId) {
            alert('Please select an examination hall / room.');
            return;
        }

        const payload = {
            roomId: parseInt(roomId),
            invigilatorId: facId ? parseInt(facId) : null,
            allocatedCapacity: 0,
            notes: notes || null
        };

        try {
            const res = await Auth.fetchWithAuth(`/api/admin/exams/${examId}/rooms`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            const json = await res.json();
            if (json.status === 'SUCCESS') {
                await this.loadExams();
                await this.loadStats();
                this.onExamSelectedForAllocation();
                document.getElementById('allocatorRoomNotes').value = '';
                alert('Examination hall assigned successfully!');
            } else {
                alert('Could not assign hall: ' + json.message);
            }
        } catch (err) {
            alert('Conflict or network error: ' + err.message);
        }
    },

    async unassignHall(examId, roomId) {
        if (!confirm('Unassign this hall from the examination? Any existing seat placements in this hall will be removed.')) {
            return;
        }

        try {
            const res = await Auth.fetchWithAuth(`/api/admin/exams/${examId}/rooms/${roomId}`, { method: 'DELETE' });
            const json = await res.json();
            if (json.status === 'SUCCESS') {
                await this.loadExams();
                await this.loadStats();
                this.onExamSelectedForAllocation();
            } else {
                alert('Could not remove hall: ' + json.message);
            }
        } catch (err) {
            alert('Request error: ' + err.message);
        }
    },

    async runAutomatedSeatingEngine() {
        const examId = document.getElementById('allocatorExamSelect').value;
        const strategy = document.getElementById('allocatorStrategySelect').value;
        const spacingGap = parseInt(document.getElementById('allocatorSpacingGap').value);

        if (!examId) {
            alert('Please select a target examination session first.');
            return;
        }

        const payload = {
            strategy: strategy,
            spacingGap: spacingGap
        };

        try {
            const res = await Auth.fetchWithAuth(`/api/admin/exams/${examId}/generate-seating`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            const json = await res.json();
            if (json.status === 'SUCCESS') {
                await this.loadExams();
                await this.loadStats();
                this.onExamSelectedForAllocation();
                alert(`Automated Seating Generated Successfully!\nTotal candidates seated: ${json.data.allocatedStudentsCount}`);
                // Jump to visual matrix
                this.jumpToMatrix(parseInt(examId));
            } else {
                alert('Seating engine notice: ' + json.message);
            }
        } catch (err) {
            alert('Request error: ' + err.message);
        }
    },

    async clearSeatingAllocation() {
        const examId = document.getElementById('allocatorExamSelect').value;
        if (!examId) {
            alert('Please select an examination first.');
            return;
        }
        if (!confirm('Clear all seated candidate desk allocations for this exam session?')) {
            return;
        }

        try {
            const res = await Auth.fetchWithAuth(`/api/admin/exams/${examId}/seating`, { method: 'DELETE' });
            const json = await res.json();
            if (json.status === 'SUCCESS') {
                await this.loadExams();
                await this.loadStats();
                this.onExamSelectedForAllocation();
                alert('Seating arrangement cleared.');
            } else {
                alert('Could not clear seating: ' + json.message);
            }
        } catch (err) {
            alert('Request error: ' + err.message);
        }
    },

    // =========================================================================
    // TAB 3: VISUAL SEATING MATRIX
    // =========================================================================

    onMatrixExamChanged() {
        const examSelect = document.getElementById('matrixExamSelect');
        const roomSelect = document.getElementById('matrixRoomSelect');
        if (!examSelect || !roomSelect) return;

        const examId = parseInt(examSelect.value);
        const exam = this.exams.find(e => e.id === examId);

        if (!exam || !exam.rooms || exam.rooms.length === 0) {
            roomSelect.innerHTML = '<option value="">-- No Halls Assigned to this Exam --</option>';
            document.getElementById('visualMatrixContainer').innerHTML = '<div style="padding:40px; color:var(--cg-text-muted);">Please assign an examination hall in Step 2 first.</div>';
            return;
        }

        roomSelect.innerHTML = exam.rooms.map(r => `<option value="${r.roomId}">${escapeHtml(r.roomCode)} (${escapeHtml(r.roomName)})</option>`).join('');
        this.loadVisualSeatingMatrix();
    },

    async loadVisualSeatingMatrix() {
        const examId = document.getElementById('matrixExamSelect').value;
        const roomId = document.getElementById('matrixRoomSelect').value;
        const container = document.getElementById('visualMatrixContainer');

        if (!examId || !roomId) {
            if (container) container.innerHTML = '<div style="padding:40px; color:var(--cg-text-muted);">Select exam and hall to load seating layout.</div>';
            return;
        }

        container.innerHTML = '<div style="padding:40px; color:var(--cg-text-muted);">Rendering physical seating layout grid...</div>';

        try {
            const res = await Auth.fetchWithAuth(`/api/admin/exams/${examId}/rooms/${roomId}/seating-grid`);
            const json = await res.json();
            if (json.status === 'SUCCESS' && json.data) {
                this.renderVisualMatrix(json.data);
            } else {
                container.innerHTML = `<div style="padding:40px; color:var(--cg-danger);">${escapeHtml(json.message || 'Could not load seating grid')}</div>`;
            }
        } catch (err) {
            container.innerHTML = `<div style="padding:40px; color:var(--cg-danger);">Error: ${escapeHtml(err.message)}</div>`;
        }
    },

    renderVisualMatrix(grid) {
        const titleEl = document.getElementById('matrixHallTitle');
        const metaEl = document.getElementById('matrixHallMeta');
        const countEl = document.getElementById('matrixCountSeated');
        const container = document.getElementById('visualMatrixContainer');

        if (titleEl) titleEl.textContent = `Hall ${grid.roomCode} (${grid.roomName}) — ${grid.examTitle}`;
        if (metaEl) metaEl.textContent = `📅 ${grid.examDate} • 🕒 ${grid.timeSlot} • 📍 ${grid.blockName} • Invigilator: ${grid.invigilatorName}`;
        if (countEl) countEl.textContent = `${grid.allocatedSeats} / ${grid.totalSeats} Desks Occupied`;

        if (!grid.seats || grid.seats.length === 0) {
            container.innerHTML = '<div style="padding:40px; color:var(--cg-text-muted);">No physical seats mapped for this room. Please configure layout in Space Management.</div>';
            return;
        }

        // Group by row
        const rowsMap = {};
        grid.seats.forEach(s => {
            const r = s.rowNumber || 1;
            if (!rowsMap[r]) rowsMap[r] = [];
            rowsMap[r].push(s);
        });

        let html = '<div style="display: flex; flex-direction: column; gap: 12px; align-items: center; width: 100%;">';

        Object.keys(rowsMap).sort((a,b) => a - b).forEach(rNum => {
            const rowSeats = rowsMap[rNum].sort((a,b) => a.columnNumber - b.columnNumber);
            html += `<div style="display: flex; gap: 10px; align-items: center;">`;
            html += `<span style="font-size: 11px; font-weight: 700; color: var(--cg-text-muted); width: 26px; text-align: right;">R${rNum}</span>`;

            rowSeats.forEach(cell => {
                if (cell.walkway) {
                    html += `
                        <div class="seat-desk-box walkway" title="Aisle / Walkway Gap">
                            <span style="font-size:9px; color:var(--cg-text-muted); text-align:center; margin:auto;">AISLE</span>
                        </div>
                    `;
                } else if (cell.allocated) {
                    const isAbsent = cell.attendanceStatus === 'ABSENT';
                    const boxClass = isAbsent ? 'seat-desk-box absent' : 'seat-desk-box allocated';
                    const statusTag = isAbsent ? '<span style="color:#DC2626; font-weight:700;">ABSENT</span>' :
                                      (cell.attendanceStatus === 'PRESENT' ? '<span style="color:#16A34A; font-weight:700;">PRESENT</span>' : '<span style="color:#0284C7; font-weight:700;">SEATED</span>');

                    html += `
                        <div class="${boxClass}" onclick="ExamMgmt.openAttendanceModal(${cell.allocationId}, '${escapeHtml(cell.studentRollNumber)}', '${escapeHtml(cell.studentName)}', '${escapeHtml(cell.seatNumber)}', '${cell.attendanceStatus}')" title="Click to mark attendance for ${escapeHtml(cell.studentName)}">
                            <div style="display:flex; justify-content:space-between; align-items:center;">
                                <span style="font-weight:700; color:var(--cg-brand-black);">${escapeHtml(cell.seatNumber)}</span>
                                <span style="font-size:9.5px;">${statusTag}</span>
                            </div>
                            <div style="font-weight:700; font-size:10.5px; color:var(--cg-brand-black); overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">
                                ${escapeHtml(cell.studentRollNumber || 'No Roll')}
                            </div>
                            <div style="font-size:9.5px; color:var(--cg-text-muted); overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">
                                ${escapeHtml(cell.studentName || 'Student')} (${escapeHtml(cell.studentSection || '')})
                            </div>
                        </div>
                    `;
                } else {
                    html += `
                        <div class="seat-desk-box" title="Desk ${escapeHtml(cell.seatNumber)} (Unoccupied)">
                            <div style="font-weight:700; color:var(--cg-text-muted);">${escapeHtml(cell.seatNumber)}</div>
                            <div style="font-size:9.5px; color:var(--cg-text-muted); margin:auto;">Empty</div>
                        </div>
                    `;
                }
            });

            html += `</div>`;
        });

        html += '</div>';
        container.innerHTML = html;
    },

    openAttendanceModal(allocId, roll, name, seatNumber, currentStatus) {
        document.getElementById('attAllocId').value = allocId;
        document.getElementById('attCandidateRoll').textContent = roll;
        document.getElementById('attCandidateName').textContent = name;
        document.getElementById('attSeatNumber').textContent = seatNumber;
        document.getElementById('attStatusSelect').value = currentStatus || 'PRESENT';
        document.getElementById('attRemarks').value = '';
        document.getElementById('modalAttendance').classList.add('show');
    },

    closeAttendanceModal() {
        const modal = document.getElementById('modalAttendance');
        if (modal) modal.classList.remove('show');
    },

    async submitAttendance() {
        const allocId = document.getElementById('attAllocId').value;
        const status = document.getElementById('attStatusSelect').value;
        const remarks = document.getElementById('attRemarks').value.trim();

        try {
            const res = await Auth.fetchWithAuth(`/api/admin/exams/attendance/${allocId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ attendanceStatus: status, remarks: remarks || null })
            });
            const json = await res.json();
            if (json.status === 'SUCCESS') {
                this.closeAttendanceModal();
                this.loadVisualSeatingMatrix();
            } else {
                alert('Could not update attendance: ' + json.message);
            }
        } catch (err) {
            alert('Request error: ' + err.message);
        }
    },

    // =========================================================================
    // TAB 4: PRINTABLE DOOR NOTICE & ROSTER
    // =========================================================================

    openRosterPrintModal() {
        this.switchTab('roster');
    },

    onRosterExamChanged() {
        const examSelect = document.getElementById('rosterExamSelect');
        const roomSelect = document.getElementById('rosterRoomSelect');
        if (!examSelect || !roomSelect) return;

        const examId = parseInt(examSelect.value);
        const exam = this.exams.find(e => e.id === examId);

        if (!exam || !exam.rooms || exam.rooms.length === 0) {
            roomSelect.innerHTML = '<option value="">-- No Halls Assigned --</option>';
            return;
        }

        roomSelect.innerHTML = exam.rooms.map(r => `<option value="${r.roomId}">${escapeHtml(r.roomCode)} (${escapeHtml(r.roomName)})</option>`).join('');
        this.loadRoster();
    },

    async loadRoster() {
        const examId = document.getElementById('rosterExamSelect').value;
        const roomId = document.getElementById('rosterRoomSelect').value;
        const tbody = document.getElementById('rosterTableBody');

        if (!examId || !roomId) {
            if (tbody) tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:20px; color:var(--cg-text-muted);">Please select exam and hall.</td></tr>';
            return;
        }

        const exam = this.exams.find(e => e.id === parseInt(examId));
        const roomAlloc = exam ? (exam.rooms || []).find(r => r.roomId === parseInt(roomId)) : null;

        // Set document headers
        document.getElementById('docExamTitle').textContent = exam ? exam.title : '--';
        document.getElementById('docCourseCode').textContent = exam ? `${exam.courseCode || ''} - ${exam.courseName || ''}` : '--';
        document.getElementById('docDateTime').textContent = exam ? `${exam.examDate} (${exam.startTime} - ${exam.endTime})` : '--';
        document.getElementById('docHall').textContent = roomAlloc ? `${roomAlloc.roomCode} - ${roomAlloc.roomName} (${roomAlloc.blockName || 'Main Block'})` : '--';
        document.getElementById('docInvigilator').textContent = roomAlloc ? roomAlloc.invigilatorName : 'Faculty TBA';
        document.getElementById('docSignInvigilator').textContent = roomAlloc ? roomAlloc.invigilatorName : 'Faculty TBA';
        document.getElementById('docDateVerified').textContent = new Date().toLocaleDateString('en-US');

        try {
            const res = await Auth.fetchWithAuth(`/api/admin/exams/${examId}/rooms/${roomId}/roster`);
            const json = await res.json();
            if (json.status === 'SUCCESS' && Array.isArray(json.data)) {
                this.renderRoster(json.data);
            } else {
                tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding:20px; color:var(--cg-danger);">${escapeHtml(json.message || 'Failed to load roster')}</td></tr>`;
            }
        } catch (err) {
            tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding:20px; color:var(--cg-danger);">Error: ${escapeHtml(err.message)}</td></tr>`;
        }
    },

    renderRoster(students) {
        const tbody = document.getElementById('rosterTableBody');
        document.getElementById('docCandidateCount').textContent = students.length;

        if (students.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:24px; color:var(--cg-text-muted);">No candidates allocated to this hall yet. Run the seating engine in Step 2.</td></tr>';
            return;
        }

        tbody.innerHTML = students.map(s => `
            <tr>
                <td style="font-weight:700; color:var(--cg-brand-black);">${escapeHtml(s.seatNumber)}</td>
                <td style="font-weight:600; font-family:monospace;">${escapeHtml(s.studentRollNumber)}</td>
                <td style="font-weight:600;">${escapeHtml(s.studentName)}</td>
                <td>${escapeHtml(s.studentDepartment)}</td>
                <td>${escapeHtml(s.studentSection)}</td>
                <td>
                    <span style="font-size:11px; font-weight:700; color:${s.attendanceStatus === 'ABSENT' ? '#DC2626' : (s.attendanceStatus === 'PRESENT' ? '#16A34A' : '#475569')};">
                        ${escapeHtml(s.attendanceStatus)}
                    </span>
                </td>
                <td style="border-bottom:1px solid #CBD5E1;"></td>
            </tr>
        `).join('');
    }
};

window.ExamMgmt = ExamMgmt;
