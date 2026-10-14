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
    selectedBlockId: null,
    activeTimetableSection: null,
    activeExamSection: null,

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
                <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(290px, 1fr)); gap: 14px;">
                    ${data.upcomingExams.map(ex => `
                        <div style="border: 1px solid var(--cg-border); border-radius: var(--cg-radius-md); padding: 16px; background: #FFFFFF; display: flex; flex-direction: column; justify-content: space-between;">
                            <div>
                                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px;">
                                    <div>
                                        <div style="font-weight: 700; font-size: 14px; color: var(--cg-brand-black);">${escapeHtml(ex.courseName)}</div>
                                        <div style="font-size: 11.5px; color: var(--cg-text-muted);">${escapeHtml(ex.examName)} (${escapeHtml(ex.courseCode)})</div>
                                    </div>
                                    <span class="badge-cg badge-active">${escapeHtml(ex.status || 'SCHEDULED')}</span>
                                </div>
                                <div style="font-size: 12px; color: var(--cg-text-secondary); margin-bottom: 12px; line-height: 1.5;">
                                    📅 <strong>${escapeHtml(ex.examDate)}</strong> &bull; ${escapeHtml(ex.startTime)} - ${escapeHtml(ex.endTime)}
                                    ${ex.sectionName ? `<span style="margin-left: 6px; padding: 2px 7px; background: #E0E7FF; color: #3730A3; border-radius: 4px; font-weight: 600; font-size: 11px;">Section ${escapeHtml(ex.sectionName)}</span>` : ''}
                                </div>
                                <div style="padding: 10px; background: var(--cg-primary-blue-soft); border: 1px solid var(--cg-primary-blue-border); border-radius: var(--cg-radius-sm); font-size: 12px; display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
                                    <div>
                                        <div style="font-size: 10.5px; color: var(--cg-primary-blue); font-weight: 700;">ASSIGNED SEAT</div>
                                        <div style="font-size: 15px; font-weight: 700; color: var(--cg-primary-blue);">${escapeHtml(ex.seatNumber || 'Unassigned')}</div>
                                    </div>
                                    <div style="text-align: right;">
                                        <div style="font-size: 10.5px; color: var(--cg-text-muted);">EXAM VENUE</div>
                                        <div style="font-weight: 600; color: var(--cg-text-primary);">${escapeHtml(ex.roomCode)} (${escapeHtml(ex.blockName || '')})</div>
                                    </div>
                                </div>
                            </div>
                            <div style="border-top: 1px solid var(--cg-border); padding-top: 10px; margin-top: 6px; display: flex; justify-content: space-between; align-items: center;">
                                <span style="font-size: 11px; color: var(--cg-text-muted);">Venue: <strong>${escapeHtml(ex.blockName || 'Main Campus')}</strong></span>
                                <a href="/student/campus?block=${encodeURIComponent(ex.blockName || '')}" class="btn-action-cg" style="text-decoration: none; font-size: 11.5px; padding: 4px 10px; display: inline-flex; align-items: center; gap: 4px;">
                                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="1 6 1 22 8 18 16 22 23 18 23 2 16 6 8 2 1 6"></polygon><line x1="8" y1="2" x2="8" y2="18"></line><line x1="16" y1="6" x2="16" y2="22"></line></svg>
                                    <span>View Block</span> &rarr;
                                </a>
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
    async loadTimetable(requestedSection) {
        this.initCommon();
        try {
            // Determine active section from parameter or URL query string
            const urlParams = new URLSearchParams(window.location.search);
            let section = requestedSection || urlParams.get('section');

            // 1. Fetch available sections list and profile in parallel
            const [profileRes, sectionsRes] = await Promise.all([
                Auth.fetchWithAuth('/api/student/profile'),
                Auth.fetchWithAuth('/api/student/timetable/sections')
            ]);
            const profileJson = await profileRes.json();
            const sectionsJson = await sectionsRes.json();

            const profile = (profileJson.status === 'SUCCESS' && profileJson.data) ? profileJson.data : {};
            let availableSections = (sectionsJson.status === 'SUCCESS' && Array.isArray(sectionsJson.data))
                ? sectionsJson.data
                : ['CSE-A', 'CSE-B', 'AI-DS-A', 'AI-DS-B'];

            if (!section) {
                section = profile.section || (availableSections.length > 0 ? availableSections[0] : 'CSE-A');
            }
            this.activeTimetableSection = section;

            // Populate Section Dropdown
            const secSelector = document.getElementById('sectionSelector');
            if (secSelector) {
                secSelector.innerHTML = availableSections.map(s => `
                    <option value="${escapeHtml(s)}" ${s === section ? 'selected' : ''}>${escapeHtml(s)}</option>
                `).join('');
            }

            // Update Header Info
            const headerInfo = document.getElementById('timetableHeaderInfo');
            if (headerInfo) {
                headerInfo.textContent = `Cohort: Section ${section} • Department of ${profile.department || 'Academic Department'} • Year ${profile.year || 1}, Semester ${profile.semester || 1}`;
            }

            // 2. Fetch Section Timetable
            const ttRes = await Auth.fetchWithAuth(`/api/student/timetable?section=${encodeURIComponent(section)}`);
            const ttJson = await ttRes.json();
            const entries = (ttJson.status === 'SUCCESS' && Array.isArray(ttJson.data)) ? ttJson.data : [];
            this.renderTimetableEntries(entries, section);

        } catch (err) {
            console.error('Failed to load student timetable:', err);
            this.showError('Could not load timetable: ' + err.message);
        }
    },

    changeTimetableSection(section) {
        if (!section) return;
        const url = new URL(window.location);
        url.searchParams.set('section', section);
        window.history.replaceState(null, '', url);
        this.loadTimetable(section);
    },

    renderTimetableEntries(entries, sectionName) {
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
                    <div style="font-weight: 700; font-size: 16px; color: var(--cg-brand-black); margin-bottom: 6px;">No Timetable Published for Section ${escapeHtml(sectionName || '')}</div>
                    <div style="font-size: 13px; color: var(--cg-text-muted); max-width: 480px; margin: 0 auto 16px;">
                        The academic timetable for section <strong>${escapeHtml(sectionName || '')}</strong> is currently being prepared by the administration. Select another section from the dropdown above or check back later.
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
                            <th>Section</th>
                            <th>Room Location</th>
                            <th>Campus Block</th>
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
                                    <span style="font-weight: 600; padding: 2px 7px; background: #E0E7FF; color: #3730A3; border-radius: 4px; font-size: 11.5px;">${escapeHtml(e.sectionName || sectionName || 'CSE-A')}</span>
                                </td>
                                <td>
                                    <span style="font-weight: 600; padding: 2px 7px; background: #F1F5F9; border-radius: 4px;">${escapeHtml(e.roomCode)}</span>
                                </td>
                                <td>
                                    <a href="/student/campus?block=${encodeURIComponent(e.blockName || '')}" style="color: var(--cg-primary-blue); text-decoration: none; font-weight: 500;" title="View Block in Campus Explorer">
                                        ${escapeHtml(e.blockName || '')} &rarr;
                                    </a>
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
    async loadExaminations(requestedSection) {
        this.initCommon();
        try {
            // Determine active section from parameter or URL query string
            const urlParams = new URLSearchParams(window.location.search);
            let section = requestedSection || urlParams.get('section');

            // 1. Fetch available sections & profile in parallel
            const [profileRes, sectionsRes] = await Promise.all([
                Auth.fetchWithAuth('/api/student/profile'),
                Auth.fetchWithAuth('/api/student/examinations/sections')
            ]);
            const profileJson = await profileRes.json();
            const sectionsJson = await sectionsRes.json();

            const profile = (profileJson.status === 'SUCCESS' && profileJson.data) ? profileJson.data : {};
            let availableSections = (sectionsJson.status === 'SUCCESS' && Array.isArray(sectionsJson.data))
                ? sectionsJson.data
                : ['CSE-A', 'CSE-B', 'AI-DS-A', 'AI-DS-B'];

            if (!section) {
                section = profile.section || (availableSections.length > 0 ? availableSections[0] : 'CSE-A');
            }
            this.activeExamSection = section;

            // Populate Section Dropdown
            const secSelector = document.getElementById('examSectionSelector');
            if (secSelector) {
                secSelector.innerHTML = availableSections.map(s => `
                    <option value="${escapeHtml(s)}" ${s === section ? 'selected' : ''}>${escapeHtml(s)}</option>
                `).join('');
            }

            // Update Header Info
            const headerInfo = document.getElementById('examHeaderInfo');
            if (headerInfo) {
                headerInfo.textContent = `Candidate: ${profile.fullName || 'Student'} • Section: ${section} • Roll Number: ${profile.specificId || 'N/A'} • ${profile.department || 'Academic Department'}`;
            }

            // 2. Fetch Section Examinations
            const examRes = await Auth.fetchWithAuth(`/api/student/examinations?section=${encodeURIComponent(section)}`);
            const examJson = await examRes.json();
            const exams = (examJson.status === 'SUCCESS' && Array.isArray(examJson.data)) ? examJson.data : [];
            this.renderExaminations(exams, section);

        } catch (err) {
            console.error('Failed to load examinations:', err);
            this.showError('Could not load examination records: ' + err.message);
        }
    },

    changeExamSection(section) {
        if (!section) return;
        const url = new URL(window.location);
        url.searchParams.set('section', section);
        window.history.replaceState(null, '', url);
        this.loadExaminations(section);
    },

    renderExaminations(exams, sectionName) {
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
                    <div style="font-weight: 700; font-size: 16px; color: var(--cg-brand-black); margin-bottom: 6px;">No Examinations Scheduled for Section ${escapeHtml(sectionName || '')}</div>
                    <div style="font-size: 13px; color: var(--cg-text-muted); max-width: 480px; margin: 0 auto 16px;">
                        There are no upcoming examinations or assigned hall seatings recorded for section <strong>${escapeHtml(sectionName || '')}</strong>. Please select another section from the dropdown above.
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
                        <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px; flex-wrap: wrap; gap: 8px;">
                            <div>
                                <div style="display: flex; align-items: center; gap: 8px;">
                                    <h3 style="font-size: 16px; font-weight: 700; color: var(--cg-brand-black); margin-bottom: 0;">${escapeHtml(e.courseName)}</h3>
                                    <span style="padding: 2px 8px; background: #E0E7FF; color: #3730A3; border-radius: 4px; font-size: 11.5px; font-weight: 600;">Section ${escapeHtml(e.sectionName || sectionName || '')}</span>
                                </div>
                                <div style="font-size: 12px; color: var(--cg-text-muted); margin-top: 4px;">${escapeHtml(e.examName)} &bull; Course Code: <strong>${escapeHtml(e.courseCode)}</strong></div>
                            </div>
                            <span class="badge-cg badge-active">${escapeHtml(e.status || 'CONFIRMED')}</span>
                        </div>
                        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 12px; padding: 14px; background: #F8FAFC; border-radius: var(--cg-radius-sm); border: 1px solid var(--cg-border); margin-bottom: 12px;">
                            <div>
                                <div style="font-size: 11px; color: var(--cg-text-muted); font-weight: 600;">EXAM DATE &amp; TIME</div>
                                <div style="font-weight: 600; font-size: 13px;">📅 ${escapeHtml(e.examDate)}</div>
                                <div style="font-size: 11.5px; color: var(--cg-text-muted);">${escapeHtml(e.startTime)} - ${escapeHtml(e.endTime)}</div>
                            </div>
                            <div>
                                <div style="font-size: 11px; color: var(--cg-text-muted); font-weight: 600;">EXAMINATION VENUE</div>
                                <div style="font-weight: 700; font-size: 14px; color: var(--cg-brand-black);">${escapeHtml(e.roomCode)}</div>
                                <div style="font-size: 11.5px; color: var(--cg-text-muted);">${escapeHtml(e.blockName || '')}</div>
                            </div>
                            <div>
                                <div style="font-size: 11px; color: var(--cg-primary-blue); font-weight: 700;">ASSIGNED SEAT NUMBER</div>
                                <div style="font-weight: 800; font-size: 20px; color: var(--cg-primary-blue);">${escapeHtml(e.seatNumber || 'Unassigned')}</div>
                            </div>
                        </div>
                        <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 8px;">
                            <div style="font-size: 11.5px; color: var(--cg-text-muted); line-height: 1.45;">
                                ${e.instructions ? `📌 <strong>Instructions:</strong> ${escapeHtml(e.instructions)}` : '📌 Report 15 minutes before exam commencement with institutional ID.'}
                            </div>
                            <a href="/student/campus?block=${encodeURIComponent(e.blockName || '')}" class="btn-action-cg" style="text-decoration: none; font-size: 12px; padding: 6px 12px; display: inline-flex; align-items: center; gap: 5px; white-space: nowrap;">
                                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="1 6 1 22 8 18 16 22 23 18 23 2 16 6 8 2 1 6"></polygon><line x1="8" y1="2" x2="8" y2="18"></line><line x1="16" y1="6" x2="16" y2="22"></line></svg>
                                <span>View Block</span> &rarr;
                            </a>
                        </div>
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
                this.renderCampusBlocks(this.campusTree);
            }

            if (roomsJson.status === 'SUCCESS' && Array.isArray(roomsJson.data)) {
                this.allRooms = roomsJson.data;
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
                campusFilter.addEventListener('change', (e) => {
                    const blockId = e.target.value;
                    if (blockId) {
                        this.selectBlock(blockId, false);
                    } else {
                        this.clearBlockFilter();
                    }
                });
            }

            // Check URL parameters for direct deep linking (e.g. ?block=Block+A or ?blockId=1)
            const urlParams = new URLSearchParams(window.location.search);
            const blockParam = urlParams.get('block');
            const blockIdParam = urlParams.get('blockId');

            let matchedBlockId = null;
            if (blockIdParam) {
                matchedBlockId = blockIdParam;
            } else if (blockParam && this.campusTree) {
                const normParam = blockParam.trim().toLowerCase();
                for (const campus of this.campusTree) {
                    for (const b of (campus.blocks || [])) {
                        if (b.name.toLowerCase() === normParam ||
                            (b.code && b.code.toLowerCase() === normParam) ||
                            b.name.toLowerCase().includes(normParam) ||
                            normParam.includes(b.name.toLowerCase())) {
                            matchedBlockId = b.id;
                            break;
                        }
                    }
                    if (matchedBlockId) break;
                }
            }

            if (matchedBlockId) {
                this.selectBlock(matchedBlockId, false);
            } else {
                this.renderRoomsDirectory(this.allRooms);
            }

        } catch (err) {
            console.error('Failed to load campus directory:', err);
            this.showError('Could not load campus directory: ' + err.message);
        }
    },

    populateFilterDropdowns(tree) {
        const campusFilter = document.getElementById('campusFilter');
        if (campusFilter) {
            campusFilter.innerHTML = '<option value="">All Campuses &amp; Blocks</option>' +
                tree.map(c => `
                    <optgroup label="${escapeHtml(c.name)}">
                        ${(c.blocks || []).map(b => `<option value="${b.id}">${escapeHtml(b.name)} (${escapeHtml(b.code || '')})</option>`).join('')}
                    </optgroup>
                `).join('');
        }
    },

    renderCampusBlocks(tree) {
        const container = document.getElementById('campusBlocksContainer');
        if (!container) return;

        if (!tree || tree.length === 0) {
            container.innerHTML = '<div style="padding: 20px; color: var(--cg-text-muted);">No campus blocks available.</div>';
            return;
        }

        const blocks = [];
        tree.forEach(campus => {
            (campus.blocks || []).forEach(b => {
                let totalRooms = 0;
                let roomTypesSet = new Set();
                let floorsCount = (b.floors || []).length;
                let totalCapacity = 0;

                (b.floors || []).forEach(f => {
                    const rList = f.rooms || [];
                    totalRooms += (rList.length > 0 ? rList.length : (f.roomCount || 0));
                    rList.forEach(r => {
                        if (r.roomType) roomTypesSet.add(r.roomType);
                        if (r.capacity) totalCapacity += r.capacity;
                    });
                });

                blocks.push({
                    id: b.id,
                    name: b.name,
                    code: b.code,
                    description: b.description || 'Academic Department & Laboratories',
                    campusName: campus.name,
                    totalRooms,
                    floorsCount,
                    totalCapacity,
                    roomTypes: Array.from(roomTypesSet)
                });
            });
        });

        if (blocks.length === 0) {
            container.innerHTML = '<div style="padding: 20px; color: var(--cg-text-muted);">No campus blocks found.</div>';
            return;
        }

        container.innerHTML = blocks.map(b => {
            const isSelected = this.selectedBlockId && String(this.selectedBlockId) === String(b.id);
            const activeClass = isSelected ? 'block-card-cg active' : 'block-card-cg';

            return `
                <div class="${activeClass}" data-block-id="${b.id}" data-block-name="${escapeHtml(b.name)}" onclick="Student.selectBlock(${b.id}, true)"
                    style="display: flex; flex-direction: column; justify-content: space-between;">
                    <div>
                        <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 6px;">
                            <div style="font-weight: 700; font-size: 15px; color: var(--cg-brand-black);">${escapeHtml(b.name)}</div>
                            <span class="badge-cg" style="font-size: 10.5px; background: #F1F5F9; color: var(--cg-text-secondary); font-weight: 600;">${escapeHtml(b.code || '')}</span>
                        </div>
                        <div style="font-size: 12px; color: var(--cg-primary-blue); font-weight: 600; margin-bottom: 8px;">
                            ${escapeHtml(b.description)}
                        </div>
                        <div style="font-size: 11.5px; color: var(--cg-text-secondary); line-height: 1.5; margin-bottom: 10px;">
                            🏛️ ${escapeHtml(b.campusName)}<br>
                            🏢 ${b.floorsCount} ${b.floorsCount === 1 ? 'Floor' : 'Floors'} &bull; 🚪 ${b.totalRooms} Rooms
                        </div>
                    </div>
                    <div>
                        <div style="display: flex; flex-wrap: wrap; gap: 4px; margin-bottom: 10px;">
                            ${b.roomTypes.length > 0
                                ? b.roomTypes.map(rt => `<span style="font-size: 10px; padding: 2px 6px; background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 3px; color: var(--cg-text-muted);">${escapeHtml(rt)}</span>`).join('')
                                : '<span style="font-size: 10px; color: var(--cg-text-muted);">Classrooms &amp; Labs</span>'
                            }
                        </div>
                        <div style="border-top: 1px solid ${isSelected ? '#BFDBFE' : 'var(--cg-border)'}; padding-top: 8px; display: flex; justify-content: space-between; align-items: center; font-size: 11.5px;">
                            <span style="font-weight: 600; color: ${isSelected ? 'var(--cg-primary-blue)' : 'var(--cg-text-secondary)'};">
                                ${isSelected ? '✓ Selected Block' : 'Click to View Rooms'}
                            </span>
                            <span style="color: ${isSelected ? 'var(--cg-primary-blue)' : 'var(--cg-text-muted)'};">&rarr;</span>
                        </div>
                    </div>
                </div>
            `;
        }).join('');
    },

    selectBlock(blockId, shouldScroll) {
        this.selectedBlockId = blockId;

        // Sync dropdown
        const campusFilter = document.getElementById('campusFilter');
        if (campusFilter) {
            campusFilter.value = blockId ? String(blockId) : '';
        }

        // Show reset button
        const resetBtn = document.getElementById('btnResetBlockFilter');
        if (resetBtn) {
            resetBtn.style.display = blockId ? 'inline-flex' : 'none';
        }

        // Re-render blocks cards to reflect active selection
        if (this.campusTree && this.campusTree.length > 0) {
            this.renderCampusBlocks(this.campusTree);
        }

        // Filter rooms list
        this.filterRoomsDirectory();

        // Optional smooth scroll to rooms list
        if (shouldScroll) {
            const tableCard = document.getElementById('roomSearchInput');
            if (tableCard) {
                tableCard.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
            }
        }
    },

    clearBlockFilter() {
        this.selectedBlockId = null;

        const campusFilter = document.getElementById('campusFilter');
        if (campusFilter) {
            campusFilter.value = '';
        }

        const resetBtn = document.getElementById('btnResetBlockFilter');
        if (resetBtn) {
            resetBtn.style.display = 'none';
        }

        const searchInput = document.getElementById('roomSearchInput');
        if (searchInput) {
            searchInput.value = '';
        }

        const typeFilter = document.getElementById('roomTypeFilter');
        if (typeFilter) {
            typeFilter.value = '';
        }

        // Clear URL search params
        if (window.history.replaceState) {
            const url = new URL(window.location);
            url.searchParams.delete('block');
            url.searchParams.delete('blockId');
            window.history.replaceState(null, '', url);
        }

        if (this.campusTree && this.campusTree.length > 0) {
            this.renderCampusBlocks(this.campusTree);
        }

        this.filterRoomsDirectory();
    },

    filterRoomsDirectory() {
        const query = (document.getElementById('roomSearchInput')?.value || '').toLowerCase().trim();
        const type = document.getElementById('roomTypeFilter')?.value || '';
        const dropdownBlockId = document.getElementById('campusFilter')?.value || '';
        const activeBlockId = this.selectedBlockId || dropdownBlockId;

        const filtered = this.allRooms.filter(r => {
            const matchQuery = !query ||
                (r.roomNumber && r.roomNumber.toLowerCase().includes(query)) ||
                (r.roomCode && r.roomCode.toLowerCase().includes(query)) ||
                (r.displayName && r.displayName.toLowerCase().includes(query)) ||
                (r.roomName && r.roomName.toLowerCase().includes(query)) ||
                (r.blockName && r.blockName.toLowerCase().includes(query));
            const matchType = !type || r.roomType === type;
            const matchBlock = !activeBlockId || (r.blockId && String(r.blockId) === String(activeBlockId));
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
                ${rooms.map(r => {
                    const code = r.roomCode || r.roomNumber || 'Room';
                    const name = r.roomName || r.displayName || code;
                    return `
                        <div class="table-card" style="padding: 18px; display: flex; flex-direction: column; justify-content: space-between;">
                            <div>
                                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px;">
                                    <div>
                                        <div style="font-weight: 700; font-size: 16px; color: var(--cg-brand-black);">${escapeHtml(code)}</div>
                                        <div style="font-size: 12px; color: var(--cg-text-muted);">${escapeHtml(name)}</div>
                                    </div>
                                    <span class="badge-cg badge-active">${escapeHtml(r.roomType || 'CLASSROOM')}</span>
                                </div>
                                <div style="font-size: 12px; color: var(--cg-text-secondary); margin-bottom: 12px; line-height: 1.5;">
                                    📍 <strong>${escapeHtml(r.blockName || 'Academic Block')}</strong> &bull; ${escapeHtml(r.floorName || 'Floor 1')}<br>
                                    👥 Capacity: <strong>${r.capacity} seats</strong>
                                </div>
                            </div>
                            <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--cg-border); padding-top: 12px; margin-top: 6px;">
                                <span style="font-size: 11px; color: var(--cg-success); font-weight: 600;">${escapeHtml(r.status || 'AVAILABLE')}</span>
                                <button class="btn-action-cg" onclick="Student.viewRoomSeatingGrid(${r.id}, '${escapeHtml(code)}')">
                                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><line x1="3" y1="9" x2="21" y2="9"></line><line x1="9" y1="21" x2="9" y2="9"></line></svg>
                                    <span>View Seating Layout</span>
                                </button>
                            </div>
                        </div>
                    `;
                }).join('')}
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
