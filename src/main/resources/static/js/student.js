/**
 * CampusGrid — Student Portal Controller (Frontend)
 * Module 5: Student Portal + UI Integration
 */

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

        const topUserName = document.getElementById('topUserName');
        if (topUserName) {
            topUserName.textContent = user.fullName || user.username || 'Student';
        }
        const topAvatar = document.getElementById('topAvatar');
        if (topAvatar) {
            const initial = (user.fullName ? user.fullName[0] : (user.username ? user.username[0] : 'S')).toUpperCase();
            topAvatar.textContent = initial;
        }

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

    showError(msg) {
        alert(msg);
    }
};

window.Student = Student;
