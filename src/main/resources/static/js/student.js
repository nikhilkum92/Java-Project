/**
 * CampusGrid — Student Portal Controller (Frontend)
 * Module 5: Student Portal + UI Integration
 */

// Safe HTML-escaping utility handling &, <, >, ", and '
function escapeHtml(str) {
    if (str === null || str === undefined) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}
window.escapeHtml = escapeHtml;

const Student = {
    currentStudent: null,
    dashboardData: null,
    allRooms: [],
    campusTree: [],

    // 1. Initial Access & Session Guard
    verifySession() {
        const user = Auth.getUser();
        if (!user) {
            window.location.href = '/login';
            return null;
        }
        if (user.role !== 'STUDENT' && user.role !== 'ADMIN') {
            Auth.redirectByRole(user.role);
            return null;
        }
        this.currentStudent = user;
        return user;
    },

    // 2. Common Topbar & Navigation Initializer
    initCommon() {
        const user = this.verifySession();
        if (!user) return;

        // Set Topbar user name & avatar
        const topUserName = document.getElementById('topUserName');
        if (topUserName) {
            topUserName.textContent = user.fullName || user.username || 'Student';
        }
        const topAvatar = document.getElementById('topAvatar');
        if (topAvatar) {
            const initial = (user.fullName ? user.fullName[0] : (user.username ? user.username[0] : 'S')).toUpperCase();
            topAvatar.textContent = initial;
        }

        // Set Current Date in Topbar
        const dateEl = document.getElementById('currentDateStr');
        if (dateEl) {
            const now = new Date();
            dateEl.textContent = now.toLocaleDateString('en-GB', {
                weekday: 'short',
                day: 'numeric',
                month: 'short',
                year: 'numeric'
            });
        }
    },

    // 3. Load Live Dashboard Data
    async loadDashboard() {
        this.initCommon();
        try {
            const res = await Auth.fetchWithAuth('/api/student/dashboard');
            const json = await res.json();
            if (json.status !== 'SUCCESS' || !json.data) {
                this.showError('Unable to retrieve dashboard information.');
                return;
            }

            const data = json.data;
            this.dashboardData = data;
            const p = data.profile || {};

            // Header Greeting & Subtitle
            const greetingEl = document.getElementById('studentGreeting');
            if (greetingEl) {
                greetingEl.textContent = `Welcome back, ${p.fullName || 'Student'}!`;
            }
            const subTitleEl = document.getElementById('studentSubTitle');
            if (subTitleEl) {
                const sec = data.academicGroup || p.section || 'N/A';
                const roll = data.rollNumber || p.specificId || 'N/A';
                const dept = data.department || p.department || 'Academic Department';
                subTitleEl.textContent = `Section ${sec} • Roll Number: ${roll} • ${dept}`;
            }

            // Stat Cards
            const groupEl = document.getElementById('statAcademicGroup');
            if (groupEl) groupEl.textContent = data.academicGroup || 'Unassigned';

            const deptEl = document.getElementById('statDepartment');
            if (deptEl) deptEl.textContent = data.department || 'General';

            const termEl = document.getElementById('statAcademicTerm');
            if (termEl) termEl.textContent = `Year ${data.academicYear || 1} • Sem ${data.currentSemester || 1}`;

            const statusEl = document.getElementById('statAccountStatus');
            if (statusEl) {
                const isActive = data.activeStatus;
                statusEl.innerHTML = `<span class="badge-cg ${isActive ? 'badge-active' : 'badge-inactive'}">${isActive ? 'Active Enrolled' : 'Inactive'}</span>`;
            }

            // Schedule Section
            this.renderDashboardSchedule(data);

            // Examinations Section
            this.renderDashboardExams(data);

        } catch (err) {
            console.error('Failed to load student dashboard:', err);
            this.showError('Could not load dashboard data: ' + err.message);
        }
    },

    renderDashboardSchedule(data) {
        const container = document.getElementById('dashboardScheduleContainer');
        if (!container) return;

        if (data.todaySchedule && data.todaySchedule.length > 0) {
            container.innerHTML = `
                <div style="overflow-x: auto;">
                    <table class="table-cg">
                        <thead>
                            <tr>
                                <th>Time</th>
                                <th>Subject</th>
                                <th>Room</th>
                                <th>Block</th>
                                <th>Faculty</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${data.todaySchedule.map(s => `
                                <tr>
                                    <td><strong style="color: var(--cg-brand-black);">${escapeHtml(s.timeSlot || (s.startTime + ' - ' + s.endTime))}</strong></td>
                                    <td>
                                        <div style="font-weight: 600;">${escapeHtml(s.subjectName)}</div>
                                        <div style="font-size: 11px; color: var(--cg-text-muted);">${escapeHtml(s.subjectCode || '')}</div>
                                    </td>
                                    <td><span style="font-weight: 600; padding: 2px 7px; background: #F1F5F9; border-radius: 4px;">${escapeHtml(s.roomCode)}</span></td>
                                    <td>${escapeHtml(s.blockName || '')}</td>
                                    <td>${escapeHtml(s.facultyName || 'TBA')}</td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            `;
        } else {
            container.innerHTML = `
                <div style="text-align: center; padding: 36px 20px; background: #F8FAFC; border: 1px dashed var(--cg-border); border-radius: var(--cg-radius-md);">
                    <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" style="color: var(--cg-text-muted); margin-bottom: 8px;">
                        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                        <line x1="16" y1="2" x2="16" y2="6"></line>
                        <line x1="8" y1="2" x2="8" y2="6"></line>
                        <line x1="3" y1="10" x2="21" y2="10"></line>
                    </svg>
                    <div style="font-weight: 600; font-size: 14px; color: var(--cg-text-primary); margin-bottom: 4px;">No Classes Scheduled for Today</div>
                    <div style="font-size: 12.5px; color: var(--cg-text-muted); max-width: 420px; margin: 0 auto;">
                        There are no lectures or lab sessions scheduled for your cohort (${escapeHtml(data.academicGroup || 'Section')}) today. Check the weekly timetable for upcoming schedules.
                    </div>
                </div>
            `;
        }
    },

    renderDashboardExams(data) {
        const container = document.getElementById('dashboardExamsContainer');
        if (!container) return;

        if (data.upcomingExams && data.upcomingExams.length > 0) {
            container.innerHTML = `
                <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 14px;">
                    ${data.upcomingExams.map(ex => `
                        <div style="border: 1px solid var(--cg-border); border-radius: var(--cg-radius-md); padding: 16px; background: #FFFFFF;">
                            <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px;">
                                <div>
                                    <div style="font-weight: 700; font-size: 14px; color: var(--cg-brand-black);">${escapeHtml(ex.courseName)}</div>
                                    <div style="font-size: 11.5px; color: var(--cg-text-muted);">${escapeHtml(ex.examName)} (${escapeHtml(ex.courseCode)})</div>
                                </div>
                                <span class="badge-cg badge-active">${escapeHtml(ex.status || 'SCHEDULED')}</span>
                            </div>
                            <div style="font-size: 12px; color: var(--cg-text-secondary); margin-bottom: 12px; line-height: 1.5;">
                                📅 <strong>${escapeHtml(ex.examDate)}</strong> &bull; ${escapeHtml(ex.startTime)} - ${escapeHtml(ex.endTime)}
                            </div>
                            <div style="padding: 10px; background: var(--cg-primary-blue-soft); border: 1px solid var(--cg-primary-blue-border); border-radius: var(--cg-radius-sm); font-size: 12px; display: flex; justify-content: space-between; align-items: center;">
                                <div>
                                    <div style="font-size: 10.5px; color: var(--cg-primary-blue); font-weight: 700;">ASSIGNED SEAT</div>
                                    <div style="font-size: 15px; font-weight: 700; color: var(--cg-primary-blue);">${escapeHtml(ex.seatNumber || 'Unassigned')}</div>
                                </div>
                                <div style="text-align: right;">
                                    <div style="font-size: 10.5px; color: var(--cg-text-muted);">ROOM LOCATION</div>
                                    <div style="font-weight: 600; color: var(--cg-text-primary);">${escapeHtml(ex.roomCode)} (${escapeHtml(ex.blockName || '')})</div>
                                </div>
                            </div>
                        </div>
                    `).join('')}
                </div>
            `;
        } else {
            container.innerHTML = `
                <div style="text-align: center; padding: 36px 20px; background: #F8FAFC; border: 1px dashed var(--cg-border); border-radius: var(--cg-radius-md);">
                    <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" style="color: var(--cg-text-muted); margin-bottom: 8px;">
                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                        <polyline points="14 2 14 8 20 8"></polyline>
                        <line x1="16" y1="13" x2="8" y2="13"></line>
                        <line x1="16" y1="17" x2="8" y2="17"></line>
                        <polyline points="10 9 9 9 8 9"></polyline>
                    </svg>
                    <div style="font-weight: 600; font-size: 14px; color: var(--cg-text-primary); margin-bottom: 4px;">No Examinations Scheduled</div>
                    <div style="font-size: 12.5px; color: var(--cg-text-muted); max-width: 420px; margin: 0 auto;">
                        No upcoming semester examinations or seating allocations are currently scheduled for your roll number (${escapeHtml(data.rollNumber || '')}).
                    </div>
                </div>
            `;
        }
    },

    // 4. Load Detailed Student Profile
    async loadProfile() {
        this.initCommon();
        try {
            const res = await Auth.fetchWithAuth('/api/student/profile');
            const json = await res.json();
            if (json.status !== 'SUCCESS' || !json.data) {
                this.showError('Could not load student profile.');
                return;
            }

            const p = json.data;
            document.getElementById('profileFullName').textContent = p.fullName || 'Student';
            document.getElementById('profileRollNumber').textContent = p.specificId || 'N/A';
            document.getElementById('profileUid').textContent = p.uid || 'N/A';
            document.getElementById('profileEmail').textContent = p.email || 'N/A';
            document.getElementById('profileUsername').textContent = p.username || 'N/A';
            document.getElementById('profileDepartment').textContent = p.department || 'Computer Science';
            document.getElementById('profileSection').textContent = p.section || 'N/A';
            document.getElementById('profileYear').textContent = `Year ${p.year || 1}`;
            document.getElementById('profileSemester').textContent = `Semester ${p.semester || 1}`;

            const badgeEl = document.getElementById('profileStatusBadge');
            if (badgeEl) {
                badgeEl.className = `badge-cg ${p.active ? 'badge-active' : 'badge-inactive'}`;
                badgeEl.textContent = p.active ? 'Active Enrolled' : 'Deactivated';
            }

        } catch (err) {
            console.error('Failed to load profile:', err);
            this.showError('Profile load failed: ' + err.message);
        }
    },

    // 5. Load Student Timetable View
    async loadTimetable() {
        this.initCommon();
        try {
            const [profileRes, ttRes] = await Promise.all([
                Auth.fetchWithAuth('/api/student/profile'),
                Auth.fetchWithAuth('/api/student/timetable')
            ]);
            const profileJson = await profileRes.json();
            const ttJson = await ttRes.json();

            if (profileJson.status === 'SUCCESS' && profileJson.data) {
                const p = profileJson.data;
                const headerInfo = document.getElementById('timetableHeaderInfo');
                if (headerInfo) {
                    headerInfo.textContent = `Cohort: Section ${p.section || 'CSE-A'} • Department of ${p.department || 'Computer Science'} • Year ${p.year || 1}, Semester ${p.semester || 1}`;
                }
            }

            const entries = (ttJson.status === 'SUCCESS' && Array.isArray(ttJson.data)) ? ttJson.data : [];
            this.renderTimetableEntries(entries);

        } catch (err) {
            console.error('Failed to load student timetable:', err);
            this.showError('Could not load timetable: ' + err.message);
        }
    },

    renderTimetableEntries(entries) {
        const container = document.getElementById('timetableEntriesContainer');
        if (!container) return;

        if (entries.length === 0) {
            container.innerHTML = `
                <div style="text-align: center; padding: 60px 24px; background: #FFFFFF; border: 1px solid var(--cg-border); border-radius: var(--cg-radius-md);">
                    <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" style="color: var(--cg-text-muted); margin-bottom: 12px;">
                        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                        <line x1="16" y1="2" x2="16" y2="6"></line>
                        <line x1="8" y1="2" x2="8" y2="6"></line>
                        <line x1="3" y1="10" x2="21" y2="10"></line>
                    </svg>
                    <div style="font-weight: 700; font-size: 16px; color: var(--cg-brand-black); margin-bottom: 6px;">No Timetable Published for Current Session</div>
                    <div style="font-size: 13px; color: var(--cg-text-muted); max-width: 480px; margin: 0 auto 16px;">
                        The academic timetable for your section is currently being prepared by the administration. Once published, your daily lecture schedules, laboratory slots, and room locations will automatically appear here.
                    </div>
                    <a href="/student/campus" class="btn-cg-outline" style="display: inline-flex; align-items: center; gap: 6px; text-decoration: none;">
                        <span>Explore Campus Rooms</span> &rarr;
                    </a>
                </div>
            `;
            return;
        }

        // Render populated timetable
        container.innerHTML = `
            <div style="overflow-x: auto;">
                <table class="table-cg">
                    <thead>
                        <tr>
                            <th>Day</th>
                            <th>Time Slot</th>
                            <th>Subject</th>
                            <th>Room Location</th>
                            <th>Faculty</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${entries.map(e => `
                            <tr>
                                <td><strong>${escapeHtml(e.dayOfWeek)}</strong></td>
                                <td>${escapeHtml(e.timeSlot || (e.startTime + ' - ' + e.endTime))}</td>
                                <td>
                                    <div style="font-weight: 600;">${escapeHtml(e.subjectName)}</div>
                                    <div style="font-size: 11px; color: var(--cg-text-muted);">${escapeHtml(e.subjectCode || '')}</div>
                                </td>
                                <td>
                                    <span style="font-weight: 600; padding: 2px 7px; background: #F1F5F9; border-radius: 4px;">${escapeHtml(e.roomCode)}</span>
                                    <span style="font-size: 11px; color: var(--cg-text-muted); margin-left: 4px;">(${escapeHtml(e.blockName || '')})</span>
                                </td>
                                <td>${escapeHtml(e.facultyName || 'TBA')}</td>
                                <td><span class="badge-cg badge-active">${escapeHtml(e.status || 'ACTIVE')}</span></td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        `;
    },

    // 6. Load Student Examinations & Seating View
    async loadExaminations() {
        this.initCommon();
        try {
            const [profileRes, examRes] = await Promise.all([
                Auth.fetchWithAuth('/api/student/profile'),
                Auth.fetchWithAuth('/api/student/examinations')
            ]);
            const profileJson = await profileRes.json();
            const examJson = await examRes.json();

            if (profileJson.status === 'SUCCESS' && profileJson.data) {
                const p = profileJson.data;
                const headerInfo = document.getElementById('examHeaderInfo');
                if (headerInfo) {
                    headerInfo.textContent = `Candidate: ${p.fullName} • Roll Number: ${p.specificId} • ${p.department || 'Computer Science'}`;
                }
            }

            const exams = (examJson.status === 'SUCCESS' && Array.isArray(examJson.data)) ? examJson.data : [];
            this.renderExaminations(exams);

        } catch (err) {
            console.error('Failed to load examinations:', err);
            this.showError('Could not load examination records: ' + err.message);
        }
    },

    renderExaminations(exams) {
        const container = document.getElementById('examinationsContainer');
        if (!container) return;

        if (exams.length === 0) {
            container.innerHTML = `
                <div style="text-align: center; padding: 60px 24px; background: #FFFFFF; border: 1px solid var(--cg-border); border-radius: var(--cg-radius-md);">
                    <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" style="color: var(--cg-text-muted); margin-bottom: 12px;">
                        <circle cx="12" cy="12" r="10"></circle>
                        <circle cx="12" cy="12" r="6"></circle>
                        <circle cx="12" cy="12" r="2"></circle>
                    </svg>
                    <div style="font-weight: 700; font-size: 16px; color: var(--cg-brand-black); margin-bottom: 6px;">No Active Examination Seating Allocations</div>
                    <div style="font-size: 13px; color: var(--cg-text-muted); max-width: 480px; margin: 0 auto 16px;">
                        You currently have no examination schedules or assigned examination hall seats for this semester. When the examination cell publishes seat allotments, your physical seat number, examination block, and room details will be visible here.
                    </div>
                    <div style="padding: 12px 16px; background: #F8FAFC; border: 1px solid var(--cg-border); border-radius: var(--cg-radius-sm); display: inline-block; font-size: 12px; color: var(--cg-text-secondary); text-align: left;">
                        🔒 <strong>Institutional Seating Policy:</strong> Examination seat allocations are strictly individual and tamper-proof. Each student must occupy only their officially designated desk.
                    </div>
                </div>
            `;
            return;
        }

        // Render examinations list
        container.innerHTML = `
            <div style="display: grid; gap: 16px;">
                ${exams.map(e => `
                    <div class="table-card" style="padding: 20px;">
                        <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px;">
                            <div>
                                <h3 style="font-size: 16px; font-weight: 700; color: var(--cg-brand-black); margin-bottom: 2px;">${escapeHtml(e.courseName)}</h3>
                                <div style="font-size: 12px; color: var(--cg-text-muted);">${escapeHtml(e.examName)} &bull; Course Code: <strong>${escapeHtml(e.courseCode)}</strong></div>
                            </div>
                            <span class="badge-cg badge-active">${escapeHtml(e.status || 'CONFIRMED')}</span>
                        </div>
                        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 12px; padding: 14px; background: #F8FAFC; border-radius: var(--cg-radius-sm); border: 1px solid var(--cg-border); margin-bottom: 12px;">
                            <div>
                                <div style="font-size: 11px; color: var(--cg-text-muted); font-weight: 600;">DATE & TIME</div>
                                <div style="font-weight: 600; font-size: 13px;">${escapeHtml(e.examDate)}</div>
                                <div style="font-size: 11.5px; color: var(--cg-text-muted);">${escapeHtml(e.startTime)} - ${escapeHtml(e.endTime)}</div>
                            </div>
                            <div>
                                <div style="font-size: 11px; color: var(--cg-text-muted); font-weight: 600;">EXAM HALL</div>
                                <div style="font-weight: 700; font-size: 14px; color: var(--cg-brand-black);">${escapeHtml(e.roomCode)}</div>
                                <div style="font-size: 11.5px; color: var(--cg-text-muted);">${escapeHtml(e.blockName || '')}</div>
                            </div>
                            <div>
                                <div style="font-size: 11px; color: var(--cg-primary-blue); font-weight: 700;">ASSIGNED SEAT NUMBER</div>
                                <div style="font-weight: 800; font-size: 20px; color: var(--cg-primary-blue);">${escapeHtml(e.seatNumber || 'Unassigned')}</div>
                            </div>
                        </div>
                        ${e.instructions ? `<div style="font-size: 11.5px; color: var(--cg-text-muted); line-height: 1.45;">📌 Instructions: ${escapeHtml(e.instructions)}</div>` : ''}
                    </div>
                `).join('')}
            </div>
        `;
    },

    // 7. Load Student Campus & Room Directory (Module 2 Integration)
    async loadCampusExplorer() {
        this.initCommon();
        try {
            const [treeRes, roomsRes] = await Promise.all([
                Auth.fetchWithAuth('/api/spaces/tree'),
                Auth.fetchWithAuth('/api/spaces/rooms')
            ]);
            const treeJson = await treeRes.json();
            const roomsJson = await roomsRes.json();

            if (treeJson.status === 'SUCCESS' && Array.isArray(treeJson.data)) {
                this.campusTree = treeJson.data;
                this.populateFilterDropdowns(this.campusTree);
            }

            if (roomsJson.status === 'SUCCESS' && Array.isArray(roomsJson.data)) {
                this.allRooms = roomsJson.data;
                this.renderRoomsDirectory(this.allRooms);
            }

            // Setup search and filter listeners
            const searchInput = document.getElementById('roomSearchInput');
            if (searchInput) {
                searchInput.addEventListener('input', () => this.filterRoomsDirectory());
            }
            const typeFilter = document.getElementById('roomTypeFilter');
            if (typeFilter) {
                typeFilter.addEventListener('change', () => this.filterRoomsDirectory());
            }
            const campusFilter = document.getElementById('campusFilter');
            if (campusFilter) {
                campusFilter.addEventListener('change', () => this.filterRoomsDirectory());
            }

        } catch (err) {
            console.error('Failed to load campus directory:', err);
            this.showError('Could not load campus directory: ' + err.message);
        }
    },

    populateFilterDropdowns(tree) {
        const campusFilter = document.getElementById('campusFilter');
        if (campusFilter) {
            campusFilter.innerHTML = '<option value="">All Campuses & Blocks</option>' +
                tree.map(c => `
                    <optgroup label="${escapeHtml(c.name)}">
                        ${c.blocks.map(b => `<option value="${b.id}">${escapeHtml(b.name)}</option>`).join('')}
                    </optgroup>
                `).join('');
        }
    },

    filterRoomsDirectory() {
        const query = (document.getElementById('roomSearchInput')?.value || '').toLowerCase().trim();
        const type = document.getElementById('roomTypeFilter')?.value || '';
        const blockId = document.getElementById('campusFilter')?.value || '';

        const filtered = this.allRooms.filter(r => {
            const matchQuery = !query ||
                (r.roomNumber && r.roomNumber.toLowerCase().includes(query)) ||
                (r.displayName && r.displayName.toLowerCase().includes(query)) ||
                (r.blockName && r.blockName.toLowerCase().includes(query));
            const matchType = !type || r.roomType === type;
            const matchBlock = !blockId || (r.blockId && String(r.blockId) === String(blockId));
            return matchQuery && matchType && matchBlock;
        });

        this.renderRoomsDirectory(filtered);
    },

    renderRoomsDirectory(rooms) {
        const container = document.getElementById('roomsDirectoryContainer');
        const countBadge = document.getElementById('roomsCountBadge');
        if (countBadge) countBadge.textContent = rooms.length;

        if (!container) return;

        if (rooms.length === 0) {
            container.innerHTML = `
                <div style="text-align: center; padding: 48px 20px; background: #FFFFFF; border: 1px solid var(--cg-border); border-radius: var(--cg-radius-md);">
                    <div style="font-weight: 600; font-size: 14px; color: var(--cg-brand-black); margin-bottom: 4px;">No Rooms Found</div>
                    <div style="font-size: 12.5px; color: var(--cg-text-muted);">Try adjusting your search criteria or clearing filters.</div>
                </div>
            `;
            return;
        }

        container.innerHTML = `
            <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(290px, 1fr)); gap: 16px;">
                ${rooms.map(r => `
                    <div class="table-card" style="padding: 18px; display: flex; flex-direction: column; justify-content: space-between;">
                        <div>
                            <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px;">
                                <div>
                                    <div style="font-weight: 700; font-size: 16px; color: var(--cg-brand-black);">${escapeHtml(r.roomNumber)}</div>
                                    <div style="font-size: 12px; color: var(--cg-text-muted);">${escapeHtml(r.displayName || r.roomNumber)}</div>
                                </div>
                                <span class="badge-cg badge-active">${escapeHtml(r.roomType || 'CLASSROOM')}</span>
                            </div>
                            <div style="font-size: 12px; color: var(--cg-text-secondary); margin-bottom: 12px; line-height: 1.5;">
                                📍 <strong>${escapeHtml(r.blockName || 'Main Block')}</strong> &bull; ${escapeHtml(r.floorName || 'Floor 1')}<br>
                                👥 Capacity: <strong>${r.capacity} seats</strong>
                            </div>
                        </div>
                        <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--cg-border); padding-top: 12px; margin-top: 6px;">
                            <span style="font-size: 11px; color: var(--cg-success); font-weight: 600;">${escapeHtml(r.status || 'AVAILABLE')}</span>
                            <button class="btn-action-cg" onclick="Student.viewRoomSeatingGrid(${r.id}, '${escapeHtml(r.roomNumber)}')">
                                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><line x1="3" y1="9" x2="21" y2="9"></line><line x1="9" y1="21" x2="9" y2="9"></line></svg>
                                <span>View Seating Layout</span>
                            </button>
                        </div>
                    </div>
                `).join('')}
            </div>
        `;
    },

    async viewRoomSeatingGrid(roomId, roomNumber) {
        const modal = document.getElementById('modalRoomSeating');
        const modalTitle = document.getElementById('modalRoomSeatingTitle');
        const gridContainer = document.getElementById('modalRoomSeatingGrid');
        if (!modal || !gridContainer) return;

        if (modalTitle) modalTitle.textContent = `Room ${roomNumber} — Physical Seating Layout`;
        gridContainer.innerHTML = '<div style="padding: 24px; text-align: center; color: var(--cg-text-muted);">Loading seating layout grid...</div>';
        modal.classList.add('show');

        try {
            const res = await Auth.fetchWithAuth(`/api/spaces/rooms/${roomId}/grid`);
            const json = await res.json();
            if (json.status !== 'SUCCESS' || !json.data) {
                gridContainer.innerHTML = '<div style="padding: 24px; text-align: center; color: var(--cg-text-muted);">No configured seating layout exists for this room.</div>';
                return;
            }

            const grid = json.data;
            if (!grid.seats || grid.seats.length === 0) {
                gridContainer.innerHTML = '<div style="padding: 24px; text-align: center; color: var(--cg-text-muted);">No individual seats mapped for this room yet.</div>';
                return;
            }

            // Render interactive physical seat layout grid
            let rowsMap = {};
            grid.seats.forEach(s => {
                const r = s.rowNumber || 1;
                if (!rowsMap[r]) rowsMap[r] = [];
                rowsMap[r].push(s);
            });

            let gridHtml = `
                <div style="background: #0F172A; color: #FFFFFF; text-align: center; padding: 6px; border-radius: 4px; font-size: 11px; font-weight: 700; margin-bottom: 16px; letter-spacing: 0.05em;">
                    TEACHING PODIUM / SCREEN FRONT
                </div>
                <div style="display: flex; flex-direction: column; gap: 8px; align-items: center; max-height: 420px; overflow-y: auto; padding: 8px;">
            `;

            Object.keys(rowsMap).sort((a,b) => a - b).forEach(rNum => {
                const rowSeats = rowsMap[rNum].sort((a,b) => a.columnNumber - b.columnNumber);
                gridHtml += `<div style="display: flex; gap: 8px; align-items: center;">`;
                gridHtml += `<span style="font-size: 11px; font-weight: 700; color: var(--cg-text-muted); width: 24px; text-align: right;">R${rNum}</span>`;
                rowSeats.forEach(st => {
                    gridHtml += `
                        <div style="width: 38px; height: 38px; border-radius: 4px; border: 1px solid var(--cg-border); display: flex; align-items: center; justify-content: center; font-size: 10px; font-weight: 700; background: ${st.status === 'AVAILABLE' ? '#DCFCE7' : '#F1F5F9'}; color: ${st.status === 'AVAILABLE' ? '#16803D' : '#64748B'};" title="Seat ${escapeHtml(st.seatNumber)} (${st.seatType})">
                            ${escapeHtml(st.seatNumber)}
                        </div>
                    `;
                });
                gridHtml += `</div>`;
            });

            gridHtml += `
                </div>
                <div style="display: flex; gap: 14px; justify-content: center; margin-top: 14px; font-size: 11px; color: var(--cg-text-muted);">
                    <div style="display: flex; align-items: center; gap: 6px;"><span style="width: 12px; height: 12px; background: #DCFCE7; border: 1px solid #BBF7D0; border-radius: 2px;"></span> Available Seat</div>
                    <div style="display: flex; align-items: center; gap: 6px;"><span style="width: 12px; height: 12px; background: #F1F5F9; border: 1px solid #E2E8F0; border-radius: 2px;"></span> Standard Desk</div>
                </div>
            `;

            gridContainer.innerHTML = gridHtml;

        } catch (e) {
            gridContainer.innerHTML = `<div style="padding: 24px; text-align: center; color: var(--cg-danger);">Failed to load seating grid: ${e.message}</div>`;
        }
    },

    closeRoomSeatingModal() {
        const modal = document.getElementById('modalRoomSeating');
        if (modal) modal.classList.remove('show');
    },

    showError(msg) {
        alert(msg);
    }
};

window.Student = Student;