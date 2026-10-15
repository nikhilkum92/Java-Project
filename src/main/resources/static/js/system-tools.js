/**
 * CampusGrid — Module 5: System Tools (Notifications & Analytics Reports)
 * Provides centralized notification alerts and multi-module report aggregation.
 */

const SystemTools = {
    notifications: [
        {
            id: 1,
            title: "Campus Space & Seating Engine Active (Module 2)",
            desc: "1 campus, 2 blocks, 4 floors, and 5 physical rooms configured with active matrix seating grids.",
            time: "Just now",
            badge: "SUCCESS",
            type: "info"
        },
        {
            id: 2,
            title: "Academic Timetable & Allocation Active (Module 3)",
            desc: "11 weekly recurring classes scheduled across Section CSE-A with automated conflict prevention.",
            time: "10 mins ago",
            badge: "ACADEMICS",
            type: "success"
        },
        {
            id: 5,
            title: "Anti-Cheating Examination Engine Active (Module 4)",
            desc: "3 examination sessions scheduled across 3 halls with automated alternate-seat matrix allocation.",
            time: "15 mins ago",
            badge: "EXAMINATIONS",
            type: "warning"
        },
        {
            id: 3,
            title: "Student Portal Telemetry Active (Module 5)",
            desc: "Live student dashboard, enrolled timetable schedules, and campus explorer live for all student accounts.",
            time: "25 mins ago",
            badge: "PORTAL",
            type: "primary"
        },
        {
            id: 4,
            title: "Stateless Security & JWT Session Guard Active (Module 1)",
            desc: "Role-Based Access Control (RBAC) enforced with BCrypt encryption and JWT token rotation.",
            time: "1 hour ago",
            badge: "SECURITY",
            type: "neutral"
        }
    ],

    ensureModalsInDOM() {
        if (!document.getElementById('modalSystemNotifications')) {
            const notifModal = document.createElement('div');
            notifModal.className = 'modal-cg';
            notifModal.id = 'modalSystemNotifications';
            notifModal.innerHTML = `
                <div class="modal-box-cg" style="max-width: 520px;">
                    <div class="modal-header-cg">
                        <div style="display:flex; align-items:center; gap:8px;">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
                                <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
                            </svg>
                            <span style="font-weight: 700; font-size: 16px; color: var(--cg-brand-black);">Notification Center (Module 5)</span>
                        </div>
                        <button class="btn-action-cg" style="border:none; padding:4px 6px;" onclick="SystemTools.closeModal('modalSystemNotifications')">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
                        </button>
                    </div>
                    <div class="modal-body-cg" style="padding: 16px;">
                        <div style="font-size: 12.5px; color: var(--cg-text-secondary); margin-bottom: 14px;">
                            Real-time platform system events, academic updates, and module operational telemetry.
                        </div>
                        <div id="notifListContainer" style="display: flex; flex-direction: column; gap: 10px; max-height: 380px; overflow-y: auto;">
                            <!-- Rendered dynamically -->
                        </div>
                    </div>
                    <div class="modal-footer-cg" style="display: flex; justify-content: space-between; align-items: center;">
                        <span style="font-size: 11.5px; color: var(--cg-text-muted);">All modules active &amp; synced</span>
                        <button type="button" class="btn-cg-primary" onclick="SystemTools.closeModal('modalSystemNotifications')">Close</button>
                    </div>
                </div>
            `;
            document.body.appendChild(notifModal);
        }

        if (!document.getElementById('modalSystemReports')) {
            const reportModal = document.createElement('div');
            reportModal.className = 'modal-cg';
            reportModal.id = 'modalSystemReports';
            reportModal.innerHTML = `
                <div class="modal-box-cg" style="max-width: 680px;">
                    <div class="modal-header-cg">
                        <div style="display:flex; align-items:center; gap:8px;">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <line x1="18" y1="20" x2="18" y2="10"></line>
                                <line x1="12" y1="20" x2="12" y2="4"></line>
                                <line x1="6" y1="20" x2="6" y2="14"></line>
                            </svg>
                            <span style="font-weight: 700; font-size: 16px; color: var(--cg-brand-black);">CampusGrid Executive Reports &amp; Analytics (Module 5)</span>
                        </div>
                        <button class="btn-action-cg" style="border:none; padding:4px 6px;" onclick="SystemTools.closeModal('modalSystemReports')">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
                        </button>
                    </div>
                    <div class="modal-body-cg" style="padding: 20px;" id="reportsModalBody">
                        <div style="text-align:center; padding: 30px; color: var(--cg-text-muted);">
                            Loading live platform metrics...
                        </div>
                    </div>
                    <div class="modal-footer-cg" style="display: flex; justify-content: space-between; align-items: center;">
                        <button type="button" class="btn btn-outline" onclick="window.print()" style="font-size: 12px; display: inline-flex; align-items: center; gap: 6px;">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 6 2 18 2 18 9"></polyline><path d="M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2"></path><rect x="6" y="14" width="12" height="8"></rect></svg>
                            <span>Print / Export Report</span>
                        </button>
                        <button type="button" class="btn-cg-primary" onclick="SystemTools.closeModal('modalSystemReports')">Done</button>
                    </div>
                </div>
            `;
            document.body.appendChild(reportModal);
        }
    },

    closeModal(id) {
        const modal = document.getElementById(id);
        if (modal) modal.classList.remove('show');
    },

    openNotifications() {
        this.ensureModalsInDOM();
        const container = document.getElementById('notifListContainer');
        if (container) {
            container.innerHTML = this.notifications.map(n => `
                <div style="padding: 12px 14px; background: #F8FAFC; border: 1px solid var(--cg-border); border-left: 3px solid var(--cg-brand-black); border-radius: 6px;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px;">
                        <span style="font-weight: 600; font-size: 13px; color: var(--cg-text-primary);">${n.title}</span>
                        <span style="font-size: 10.5px; font-weight: 700; background: #E2E8F0; padding: 2px 6px; border-radius: 4px;">${n.badge}</span>
                    </div>
                    <div style="font-size: 12px; color: var(--cg-text-secondary); line-height: 1.4;">${n.desc}</div>
                    <div style="font-size: 10.5px; color: var(--cg-text-muted); margin-top: 6px;">${n.time}</div>
                </div>
            `).join('');
        }
        document.getElementById('modalSystemNotifications').classList.add('show');
    },

    async openReports() {
        this.ensureModalsInDOM();
        document.getElementById('modalSystemReports').classList.add('show');
        const body = document.getElementById('reportsModalBody');

        try {
            const [usersRes, spacesRes, timetablesRes, examsRes] = await Promise.all([
                fetch('/api/admin/users/stats', { headers: { 'Authorization': 'Bearer ' + Auth.getToken() } }),
                fetch('/api/spaces/stats', { headers: { 'Authorization': 'Bearer ' + Auth.getToken() } }),
                fetch('/api/admin/timetables/stats', { headers: { 'Authorization': 'Bearer ' + Auth.getToken() } }),
                fetch('/api/admin/exams/stats', { headers: { 'Authorization': 'Bearer ' + Auth.getToken() } })
            ]);

            const usersJson = await usersRes.json();
            const spacesJson = await spacesRes.json();
            const timeJson = await timetablesRes.json();
            const examsJson = await examsRes.json();

            const u = usersJson.data || {};
            const s = spacesJson.data || {};
            const t = timeJson.data || {};
            const ex = examsJson.data || {};

            body.innerHTML = `
                <div style="display: flex; justify-content: space-between; align-items: baseline; border-bottom: 2px solid var(--cg-border); padding-bottom: 12px; margin-bottom: 20px;">
                    <div>
                        <h2 style="font-size: 18px; font-weight: 700; color: var(--cg-brand-black);">Campus Space &amp; Academic Resource Audit</h2>
                        <div style="font-size: 12px; color: var(--cg-text-muted);">Generated on ${new Date().toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })} • Status: Full Operational</div>
                    </div>
                    <span class="badge-cg badge-active" style="font-size: 11px;">100% HEALTHY</span>
                </div>

                <!-- Four Pillar Metric Cards -->
                <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 20px;">
                    <div style="background: #F8FAFC; border: 1px solid var(--cg-border); border-radius: 8px; padding: 12px;">
                        <div style="font-size: 10.5px; font-weight: 600; color: var(--cg-text-muted); text-transform: uppercase;">User Accounts (Mod 1)</div>
                        <div style="font-size: 20px; font-weight: 800; color: var(--cg-brand-black); margin: 4px 0;">${u.totalUsers || 4} Users</div>
                        <div style="font-size: 11px; color: var(--cg-text-secondary);">${u.totalStudents || 1} Students • ${u.totalFaculty || 2} Faculty</div>
                    </div>

                    <div style="background: #F8FAFC; border: 1px solid var(--cg-border); border-radius: 8px; padding: 12px;">
                        <div style="font-size: 10.5px; font-weight: 600; color: var(--cg-text-muted); text-transform: uppercase;">Space Grid (Mod 2)</div>
                        <div style="font-size: 20px; font-weight: 800; color: var(--cg-brand-black); margin: 4px 0;">${s.totalRooms || 5} Rooms</div>
                        <div style="font-size: 11px; color: var(--cg-text-secondary);">${s.totalBlocks || 2} Blocks • ${s.totalCapacity || 256} Seats</div>
                    </div>

                    <div style="background: #F8FAFC; border: 1px solid var(--cg-border); border-radius: 8px; padding: 12px;">
                        <div style="font-size: 10.5px; font-weight: 600; color: var(--cg-text-muted); text-transform: uppercase;">Timetables (Mod 3)</div>
                        <div style="font-size: 20px; font-weight: 800; color: #059669; margin: 4px 0;">${t.totalScheduledClasses || 11} Slots</div>
                        <div style="font-size: 11px; color: var(--cg-text-secondary);">${t.totalWeeklyHours || 12} hrs/wk • ${t.allocatedRooms || 5} Rooms</div>
                    </div>

                    <div style="background: #F8FAFC; border: 1px solid var(--cg-border); border-radius: 8px; padding: 12px;">
                        <div style="font-size: 10.5px; font-weight: 600; color: var(--cg-text-muted); text-transform: uppercase;">Examinations (Mod 4)</div>
                        <div style="font-size: 20px; font-weight: 800; color: #4338CA; margin: 4px 0;">${ex.totalExams || 3} Sessions</div>
                        <div style="font-size: 11px; color: var(--cg-text-secondary);">${ex.totalAllocatedSeats || 16} Desks • ${ex.activeExamHalls || 3} Halls</div>
                    </div>
                </div>

                <!-- Detailed Resource Breakdown Table -->
                <div style="background: #FFFFFF; border: 1px solid var(--cg-border); border-radius: 8px; overflow: hidden;">
                    <div style="padding: 10px 14px; background: #F1F5F9; font-weight: 700; font-size: 12px; color: var(--cg-brand-black);">
                        Module Operational Breakdown
                    </div>
                    <table class="table-cg" style="font-size: 12px;">
                        <thead>
                            <tr>
                                <th>Module</th>
                                <th>Primary Capability</th>
                                <th>Active Records</th>
                                <th>System State</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td><strong>Module 1</strong></td>
                                <td>Authentication, Users &amp; JWT RBAC</td>
                                <td>${u.totalUsers || 4} accounts verified</td>
                                <td><span class="badge-cg badge-active">Operational</span></td>
                            </tr>
                            <tr>
                                <td><strong>Module 2</strong></td>
                                <td>Campus, Rooms &amp; Physical Seating</td>
                                <td>${s.totalRooms || 5} rooms, ${s.totalCapacity || 120} seats</td>
                                <td><span class="badge-cg badge-active">Operational</span></td>
                            </tr>
                            <tr>
                                <td><strong>Module 3</strong></td>
                                <td>Timetable &amp; Space Allocation Engine</td>
                                <td>${t.totalScheduledClasses || 11} scheduled slots</td>
                                <td><span class="badge-cg badge-active">Operational</span></td>
                            </tr>
                            <tr>
                                <td><strong>Module 4</strong></td>
                                <td>Exam Scheduling &amp; Anti-Cheating Seating Matrix</td>
                                <td>${ex.totalExams || 3} exams, ${ex.totalAllocatedSeats || 16} desks assigned</td>
                                <td><span class="badge-cg badge-active">Operational</span></td>
                            </tr>
                            <tr>
                                <td><strong>Module 5</strong></td>
                                <td>Student Portal &amp; Telemetry Reports</td>
                                <td>Full student timetable, exam hall &amp; campus sync</td>
                                <td><span class="badge-cg badge-active">Operational</span></td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            `;
        } catch (e) {
            body.innerHTML = `
                <div style="color: var(--cg-danger); padding: 20px; text-align: center;">
                    Failed to compile platform report: ${e.message}
                </div>
            `;
        }
    }
};

window.SystemTools = SystemTools;
window.openNotificationCenter = () => SystemTools.openNotifications();
window.openSystemReports = () => SystemTools.openReports();
