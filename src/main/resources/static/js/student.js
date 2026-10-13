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

    showError(msg) {
        alert(msg);
    }
};

window.Student = Student;
