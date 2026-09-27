/**
 * EVENTLOOP – Web Application Client Logic
 * Interacts with EventLoop Embedded REST API
 */

// Global State
// Global State
let currentUser = null;

let allResources = [];
let allEvents = [];
let allReservations = [];

// ==================== INITIALIZATION ====================
document.addEventListener('DOMContentLoaded', () => {
    initAuthSession();
    updateUserBadge();
    loadDashboard();
    
    // Auto-generate sample IDs for modals
    initModalDefaults();
});

function initAuthSession() {
    const saved = localStorage.getItem('eventloop_user') || sessionStorage.getItem('eventloop_user');
    if (saved) {
        try {
            currentUser = JSON.parse(saved);
        } catch (e) {
            currentUser = null;
        }
    } else {
        // Default seed admin session for immediate usage
        currentUser = {
            userId: 1,
            username: 'admin',
            fullName: 'Prof. Rajesh Sharma',
            role: 'ADMIN'
        };
        localStorage.setItem('eventloop_user', JSON.stringify(currentUser));
    }
}

function initModalDefaults() {
    const today = new Date().toISOString().split('T')[0];
    const dateInput = document.getElementById('evtDate');
    if (dateInput) dateInput.value = today;
    
    const evtId = document.getElementById('evtId');
    if (evtId) evtId.value = 'EVT-2026-0' + Math.floor(100 + Math.random() * 900);
    
    const resId = document.getElementById('resId');
    if (resId) resId.value = 'EL-GEN-0' + Math.floor(100 + Math.random() * 900);
}

// ==================== TAB SWITCHING ====================
function switchTab(tabName) {
    document.querySelectorAll('.tab-pane').forEach(el => el.classList.remove('active'));
    document.querySelectorAll('.nav-tab').forEach(el => el.classList.remove('active'));

    const targetTab = document.getElementById('tab-' + tabName);
    if (targetTab) {
        targetTab.classList.add('active');
    }

    const clickedBtn = Array.from(document.querySelectorAll('.nav-tab')).find(btn => 
        btn.getAttribute('onclick') && btn.getAttribute('onclick').includes(tabName)
    );
    if (clickedBtn) clickedBtn.classList.add('active');

    // Trigger tab-specific refresh
    if (tabName === 'dashboard') loadDashboard();
    else if (tabName === 'catalog') loadCatalog();
    else if (tabName === 'verification') loadVerificationQueue();
    else if (tabName === 'events') loadEvents();
    else if (tabName === 'matching') populateMatchingDropdowns();
    else if (tabName === 'reservations') loadReservations();
    else if (tabName === 'reports') loadReport();
    else if (tabName === 'optimizer') { initOptimizerEvents(); runBudgetOptimizer(); }
    else if (tabName === 'enterprise') { runEnterpriseQuotation(); }
}

// ==================== USER & PRODUCTION AUTH ====================
function updateUserBadge() {
    const badge = document.getElementById('userProfileBadge');
    const btnLogout = document.getElementById('btnLogout');
    const btnLoginPrompt = document.getElementById('btnLoginPrompt');
    const nameEl = document.getElementById('userName');
    const roleEl = document.getElementById('userRole');

    if (currentUser && currentUser.username) {
        if (badge) badge.style.display = 'flex';
        if (btnLogout) btnLogout.style.display = 'inline-flex';
        if (btnLoginPrompt) btnLoginPrompt.style.display = 'none';

        if (nameEl) nameEl.textContent = currentUser.fullName || currentUser.username;
        if (roleEl) {
            roleEl.textContent = currentUser.role || 'USER';
            roleEl.className = 'role-pill ' + 
                (currentUser.role === 'ADMIN' ? 'role-admin' : 
                (currentUser.role === 'ORGANIZER' ? 'role-organizer' : 'role-cultural'));
        }
    } else {
        if (badge) badge.style.display = 'none';
        if (btnLogout) btnLogout.style.display = 'none';
        if (btnLoginPrompt) btnLoginPrompt.style.display = 'inline-flex';
    }
}

function openLoginModal() {
    switchAuthTab('login');
    openModal('modalLogin');
    setTimeout(() => {
        const input = document.getElementById('loginUsername');
        if (input) input.focus();
    }, 150);
}

function togglePasswordVisibility(inputId, iconId) {
    const input = document.getElementById(inputId);
    const icon = document.getElementById(iconId);
    if (!input) return;

    if (input.type === 'password') {
        input.type = 'text';
        if (icon) icon.textContent = '🙈';
    } else {
        input.type = 'password';
        if (icon) icon.textContent = '👁️';
    }
}

function switchAuthTab(tab) {
    const loginForm = document.getElementById('authLoginForm');
    const regForm = document.getElementById('authRegisterForm');
    const tabLogin = document.getElementById('tabBtnLogin');
    const tabReg = document.getElementById('tabBtnRegister');
    const title = document.getElementById('authModalTitle');

    if (tab === 'register') {
        if (loginForm) loginForm.style.display = 'none';
        if (regForm) regForm.style.display = 'block';
        if (tabLogin) tabLogin.classList.remove('active');
        if (tabReg) tabReg.classList.add('active');
        if (title) title.textContent = '📝 Create EventLoop Account';
    } else {
        if (loginForm) loginForm.style.display = 'block';
        if (regForm) regForm.style.display = 'none';
        if (tabLogin) tabLogin.classList.add('active');
        if (tabReg) tabReg.classList.remove('active');
        if (title) title.textContent = '🔐 EventLoop Account Access';
    }
}

async function submitLogin() {
    const u = document.getElementById('loginUsername').value.trim();
    const p = document.getElementById('loginPassword').value;
    const btn = document.getElementById('btnSubmitLogin');

    if (!u || !p) {
        showToast('Please enter both username/email and password', 'warning');
        return;
    }

    if (btn) {
        btn.disabled = true;
        btn.textContent = 'Signing in...';
    }

    try {
        const res = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: u, password: p })
        });
        const data = await res.json();

        if (res.ok && (data.userId || data.username)) {
            currentUser = data;
            const remember = document.getElementById('chkRememberMe')?.checked;
            if (remember) {
                localStorage.setItem('eventloop_user', JSON.stringify(currentUser));
            } else {
                sessionStorage.setItem('eventloop_user', JSON.stringify(currentUser));
            }
            updateUserBadge();
            closeModal('modalLogin');
            showToast(`Welcome back, ${currentUser.fullName || currentUser.username}!`, 'success');
            loadDashboard();
        } else {
            showToast(data.error || data.message || 'Invalid credentials. Please try again.', 'danger');
        }
    } catch (err) {
        showToast('Login error: ' + err.message, 'danger');
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.textContent = 'Sign In';
        }
    }
}

async function submitRegister() {
    const fullName = document.getElementById('regFullName')?.value.trim();
    const email = document.getElementById('regEmail')?.value.trim();
    const username = document.getElementById('regUsername')?.value.trim();
    const role = document.getElementById('regRole')?.value || 'ORGANIZER';
    const dept = document.getElementById('regDept')?.value.trim() || 'Student Activities';
    const password = document.getElementById('regPassword')?.value;
    const btn = document.getElementById('btnSubmitRegister');

    if (!fullName || !email || !username || !password) {
        showToast('Please fill in all required registration fields', 'warning');
        return;
    }

    if (btn) {
        btn.disabled = true;
        btn.textContent = 'Creating account...';
    }

    try {
        const res = await fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                fullName: fullName,
                email: email,
                username: username,
                role: role,
                department: dept,
                password: password
            })
        });
        const data = await res.json();

        if (res.ok && (data.success || data.message)) {
            showToast('Account registered successfully! Please sign in.', 'success');
            switchAuthTab('login');
            const loginUserField = document.getElementById('loginUsername');
            const loginPassField = document.getElementById('loginPassword');
            if (loginUserField) loginUserField.value = username;
            if (loginPassField) {
                loginPassField.value = '';
                loginPassField.focus();
            }
        } else {
            showToast(data.error || data.message || 'Registration failed', 'danger');
        }
    } catch (err) {
        showToast('Registration error: ' + err.message, 'danger');
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.textContent = 'Create Account';
        }
    }
}

function logout() {
    localStorage.removeItem('eventloop_user');
    sessionStorage.removeItem('eventloop_user');
    currentUser = null;
    updateUserBadge();
    showToast('You have been logged out safely.', 'info');
    openLoginModal();
}


// ==================== 1. DASHBOARD ====================
async function loadDashboard() {
    try {
        const res = await fetch('/api/dashboard');
        const data = await res.json();
        
        if (!data) return;

        // KPI Counts
        setVal('heroAvoidedValue', formatCurrency(data.purchaseAvoidedTotal || 0));
        setVal('kpiTotal', data.totalResources || 0);
        setVal('kpiAvailable', data.availableCount || 0);
        setVal('kpiInUse', data.inUseCount || 0);
        setVal('kpiUnderRepair', data.underRepairCount || 0);
        setVal('kpiVerificationDue', data.verificationDueCount || 0);
        setVal('kpiActiveEvents', data.activeEventsCount || 0);
        setVal('kpiPendingReturns', data.pendingReturnsCount || 0);
        setVal('kpiOverdue', data.overdueCount || 0);
        setVal('kpiPurchaseAvoided', formatCurrency(data.purchaseAvoidedTotal || 0));
        setVal('kpiReusedCount', data.reusedCount || 0);
        setVal('kpiAssetVal', formatCurrency(data.totalAssetValue || 0));
        setVal('kpiRepairNeeds', formatCurrency(data.estimatedRepairNeeds || 0));

        // Health Progress Bar
        const tot = Math.max(1, (data.availableCount || 0) + (data.inUseCount || 0) + (data.underRepairCount || 0) + (data.verificationDueCount || 0));
        const pAvail = ((data.availableCount || 0) / tot) * 100;
        const pInUse = ((data.inUseCount || 0) / tot) * 100;
        const pRepair = ((data.underRepairCount || 0) / tot) * 100;
        const pVerif = ((data.verificationDueCount || 0) / tot) * 100;

        document.getElementById('barAvailable').style.width = pAvail + '%';
        document.getElementById('barInUse').style.width = pInUse + '%';
        document.getElementById('barRepair').style.width = pRepair + '%';
        document.getElementById('barVerif').style.width = pVerif + '%';

        setVal('legAvail', `Available: ${data.availableCount || 0} (${pAvail.toFixed(0)}%)`);
        setVal('legInUse', `In Use: ${data.inUseCount || 0} (${pInUse.toFixed(0)}%)`);
        setVal('legRepair', `Under Repair: ${data.underRepairCount || 0} (${pRepair.toFixed(0)}%)`);
        setVal('legVerif', `Verification Due: ${data.verificationDueCount || 0} (${pVerif.toFixed(0)}%)`);

        // Dashboard Upcoming Events
        const tbodyEvt = document.getElementById('tblDashboardEvents');
        if (data.upcomingEvents && data.upcomingEvents.length > 0) {
            tbodyEvt.innerHTML = data.upcomingEvents.map(e => `
                <tr>
                    <td><b>${e.eventId}</b></td>
                    <td>${e.eventName}</td>
                    <td>${e.eventDate}</td>
                    <td><span class="badge ${getStatusBadge(e.status)}">${e.status}</span></td>
                    <td class="text-success font-bold">${formatCurrency(e.avoidedCost || 0)}</td>
                </tr>
            `).join('');
        } else {
            tbodyEvt.innerHTML = '<tr><td colspan="5" class="text-center text-muted">No upcoming events scheduled</td></tr>';
        }

        // Live History Movement Log
        const tbodyHist = document.getElementById('tblDashboardHistory');
        if (data.recentHistory && data.recentHistory.length > 0) {
            tbodyHist.innerHTML = data.recentHistory.map(h => `
                <tr>
                    <td class="text-muted small">${h.timestamp || 'Just now'}</td>
                    <td><b>${h.resourceId || '-'}</b></td>
                    <td><span class="badge badge-info">${h.action || 'UPDATE'}</span></td>
                    <td>${h.performedBy || 'System'}</td>
                    <td class="small">${h.details || ''}</td>
                </tr>
            `).join('');
        } else {
            tbodyHist.innerHTML = '<tr><td colspan="5" class="text-center text-muted">No recent movements recorded</td></tr>';
        }

    } catch (err) {
        console.error('Failed to load dashboard:', err);
    }
}

// ==================== 2. RESOURCE CATALOGUE ====================
async function loadCatalog() {
    try {
        const res = await fetch('/api/resources');
        allResources = await res.json();
        
        const q = (document.getElementById('catSearch').value || '').toLowerCase();
        const cat = document.getElementById('catCategory').value;
        const stat = document.getElementById('catStatus').value;
        const verif = document.getElementById('catVerification').value;

        const filtered = allResources.filter(r => {
            const matchQ = !q || (r.name && r.name.toLowerCase().includes(q)) || (r.resourceId && r.resourceId.toLowerCase().includes(q)) || (r.specs && r.specs.toLowerCase().includes(q));
            const matchCat = cat === 'All Categories' || r.category === cat;
            const matchStat = stat === 'All Statuses' || r.status === stat;
            const matchVer = verif === 'All' || r.verificationStatus === verif;
            return matchQ && matchCat && matchStat && matchVer;
        });

        const tbody = document.getElementById('tblCatalog');
        if (filtered.length > 0) {
            tbody.innerHTML = filtered.map(r => `
                <tr>
                    <td><b>${r.resourceId}</b></td>
                    <td><b>${r.name}</b><br><span class="small text-muted">${r.specs || ''}</span></td>
                    <td>${r.category}</td>
                    <td><b>${r.availableQuantity}</b> / ${r.totalQuantity}</td>
                    <td>${renderStars(r.conditionRating)}</td>
                    <td><span class="badge ${getStatusBadge(r.status)}">${r.status}</span></td>
                    <td><span class="badge ${getVerifBadge(r.verificationStatus)}">${r.verificationStatus}</span></td>
                    <td class="small">${r.storageLocation || '-'}</td>
                    <td>${formatCurrency(r.purchaseCost)}</td>
                    <td>
                        <button class="btn btn-outline btn-sm" onclick="reserveQuick('${r.resourceId}')">Reserve</button>
                    </td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = '<tr><td colspan="10" class="text-center text-muted">No campus resources found matching criteria.</td></tr>';
        }
    } catch (err) {
        console.error('Failed to load catalog:', err);
    }
}

function openAddResourceModal() {
    initModalDefaults();
    openModal('modalAddResource');
}

async function submitAddResource() {
    const payload = {
        resourceId: document.getElementById('resId').value.trim(),
        name: document.getElementById('resName').value.trim(),
        category: document.getElementById('resCategory').value,
        totalQuantity: parseInt(document.getElementById('resQty').value) || 1,
        availableQuantity: parseInt(document.getElementById('resQty').value) || 1,
        conditionRating: parseInt(document.getElementById('resCond').value) || 5,
        purchaseCost: parseFloat(document.getElementById('resCost').value) || 0,
        storageLocation: document.getElementById('resLoc').value.trim(),
        specs: document.getElementById('resSpecs').value.trim(),
        status: 'AVAILABLE',
        verificationStatus: 'VERIFIED'
    };

    if (!payload.resourceId || !payload.name) {
        showToast('Resource ID and Name are required', 'warning');
        return;
    }

    try {
        const res = await fetch('/api/resources', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            closeModal('modalAddResource');
            showToast(`Resource ${payload.name} registered successfully!`, 'success');
            loadCatalog();
            loadDashboard();
        } else {
            showToast(data.message || 'Failed to add resource', 'danger');
        }
    } catch (err) {
        showToast('Add resource error: ' + err.message, 'danger');
    }
}

// ==================== 3. VERIFICATION QUEUE ====================
async function loadVerificationQueue() {
    try {
        const res = await fetch('/api/resources');
        const resources = await res.json();
        
        const unverified = resources.filter(r => r.verificationStatus !== 'VERIFIED' || r.status === 'UNDER_REPAIR');
        const tbody = document.getElementById('tblVerificationQueue');
        
        if (unverified.length > 0) {
            tbody.innerHTML = unverified.map(r => `
                <tr>
                    <td><b>${r.resourceId}</b></td>
                    <td>${r.name}</td>
                    <td>${r.category}</td>
                    <td><span class="badge ${getStatusBadge(r.status)}">${r.status}</span></td>
                    <td><span class="badge ${getVerifBadge(r.verificationStatus)}">${r.verificationStatus}</span></td>
                    <td>${r.lastAuditDate || 'Never'}</td>
                    <td class="text-danger font-bold">${r.nextAuditDueDate || 'EXPIRED'}</td>
                    <td>${r.storageLocation || '-'}</td>
                    <td>
                        <button class="btn btn-success btn-sm" onclick="certifyResource('${r.resourceId}')">🛡️ Certify & Verify</button>
                    </td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = '<tr><td colspan="9" class="text-center text-success font-bold">✨ All campus resources are currently verified and certified!</td></tr>';
        }
    } catch (err) {
        console.error('Failed to load verification queue:', err);
    }
}

async function certifyResource(resourceId) {
    if (currentUser.role !== 'ADMIN') {
        showToast('Permission Denied: Only Store Manager (Admin) can certify resources', 'danger');
        return;
    }
    
    try {
        const res = await fetch('/api/resources/verify', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ resourceId: resourceId, auditor: currentUser.fullName })
        });
        const data = await res.json();
        if (data.success) {
            showToast(`Resource ${resourceId} successfully certified as VERIFIED!`, 'success');
            loadVerificationQueue();
            loadDashboard();
        } else {
            showToast(data.message || 'Verification update failed', 'danger');
        }
    } catch (err) {
        showToast('Error certifying: ' + err.message, 'danger');
    }
}

// ==================== 4. EVENT LIFECYCLE ====================
async function loadEvents() {
    try {
        const res = await fetch('/api/events');
        allEvents = await res.json();
        
        const tbody = document.getElementById('tblEvents');
        if (allEvents.length > 0) {
            tbody.innerHTML = allEvents.map(e => `
                <tr>
                    <td><b>${e.eventId}</b></td>
                    <td><b>${e.eventName}</b></td>
                    <td>${e.organizerName}</td>
                    <td>${e.department}</td>
                    <td><span class="badge badge-purple">${e.eventType}</span></td>
                    <td>${e.eventDate} <br><span class="small text-muted">${e.startTime} - ${e.endTime}</span></td>
                    <td>${e.venue}</td>
                    <td><span class="badge ${getStatusBadge(e.status)}">${e.status}</span></td>
                    <td class="text-success font-bold">${formatCurrency(e.avoidedCost || 0)}</td>
                    <td>
                        ${e.status !== 'COMPLETED' && e.status !== 'CANCELLED' ? 
                            `<button class="btn btn-danger btn-sm" onclick="openCloseEventModal('${e.eventId}', '${e.eventName}')">🔒 Close Guard</button>` :
                            `<span class="text-muted small">Closed</span>`}
                    </td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = '<tr><td colspan="10" class="text-center text-muted">No events found.</td></tr>';
        }
    } catch (err) {
        console.error('Failed to load events:', err);
    }
}

function openCreateEventModal() {
    initModalDefaults();
    openModal('modalCreateEvent');
}

async function submitCreateEvent() {
    const payload = {
        eventId: document.getElementById('evtId').value.trim(),
        eventName: document.getElementById('evtName').value.trim(),
        organizerName: document.getElementById('evtOrg').value.trim(),
        department: document.getElementById('evtDept').value.trim(),
        eventType: document.getElementById('evtType').value,
        eventDate: document.getElementById('evtDate').value,
        startTime: document.getElementById('evtStart').value.trim(),
        endTime: document.getElementById('evtEnd').value.trim(),
        venue: document.getElementById('evtLoc').value.trim()
    };

    if (!payload.eventId || !payload.eventName) {
        showToast('Event ID and Event Name are required', 'warning');
        return;
    }

    try {
        const res = await fetch('/api/events', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            closeModal('modalCreateEvent');
            showToast(`Event "${payload.eventName}" created successfully!`, 'success');
            loadEvents();
            loadDashboard();
        } else {
            showToast(data.message || 'Failed to create event', 'danger');
        }
    } catch (err) {
        showToast('Create event error: ' + err.message, 'danger');
    }
}

function openCloseEventModal(eventId, eventName) {
    document.getElementById('closeEvtId').value = eventId;
    document.getElementById('closeEvtMessage').textContent = 
        `Reconciling event: [${eventId}] ${eventName}. Checking for unreturned assets, damages, or active checkouts...`;
    openModal('modalCloseEvent');
}

async function executeEventClose() {
    const eventId = document.getElementById('closeEvtId').value;
    try {
        const res = await fetch('/api/events/close', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ eventId: eventId })
        });
        const data = await res.json();
        
        if (data.success) {
            closeModal('modalCloseEvent');
            showToast(`Event [${eventId}] safely audited and closed!`, 'success');
            loadEvents();
            loadDashboard();
        } else {
            // Strict closure guard blocked it
            showToast(`Closure Blocked: ${data.message}`, 'danger');
        }
    } catch (err) {
        showToast('Closure error: ' + err.message, 'danger');
    }
}

// ==================== 5. SMART MATCHING & DECISIONS ====================
async function populateMatchingDropdowns() {
    try {
        const res = await fetch('/api/events');
        allEvents = await res.json();
        
        const selEvt = document.getElementById('matchEventSelect');
        selEvt.innerHTML = allEvents.map(e => `<option value="${e.eventId}">${e.eventId} - ${e.eventName} (${e.eventDate})</option>`).join('');
        
        onMatchingEventChanged();
    } catch (err) {
        console.error('Failed to populate matching events:', err);
    }
}

async function onMatchingEventChanged() {
    const eventId = document.getElementById('matchEventSelect').value;
    if (!eventId) return;

    try {
        const res = await fetch(`/api/events/requirements?eventId=${eventId}`);
        const reqs = await res.json();
        
        const selReq = document.getElementById('matchReqSelect');
        if (reqs && reqs.length > 0) {
            selReq.innerHTML = reqs.map(r => `<option value="${r.requirementId}">${r.itemName} (Qty: ${r.quantityRequired}, Cat: ${r.category})</option>`).join('');
            runSmartMatching();
        } else {
            selReq.innerHTML = '<option value="">No requirements defined for event</option>';
            document.getElementById('tblMatches').innerHTML = '<tr><td colspan="10" class="text-center text-muted">No requirements found. Click "+ Add Requirement" to create one.</td></tr>';
            document.getElementById('matchingSummaryTag').textContent = 'No requirements listed for this event.';
        }
    } catch (err) {
        console.error('Failed to load requirements:', err);
    }
}

async function runSmartMatching() {
    const eventId = document.getElementById('matchEventSelect').value;
    const reqId = document.getElementById('matchReqSelect').value;
    if (!eventId || !reqId) return;

    try {
        const res = await fetch(`/api/matching?eventId=${eventId}&reqId=${reqId}`);
        const data = await res.json();
        
        const tbody = document.getElementById('tblMatches');
        const summary = document.getElementById('matchingSummaryTag');
        
        if (data && data.matches && data.matches.length > 0) {
            summary.innerHTML = `✅ Found <b>${data.matches.length}</b> campus resource candidates evaluated across 10 compatibility gates.`;
            tbody.innerHTML = data.matches.map(m => `
                <tr>
                    <td><span class="badge ${getMatchBadge(m.matchType)}">${m.matchType}</span></td>
                    <td><b>${m.matchScore}%</b></td>
                    <td><b>${m.resourceId}</b></td>
                    <td>${m.resourceName}</td>
                    <td>${m.availableQuantity}</td>
                    <td>${renderStars(m.conditionRating)}</td>
                    <td><span class="badge ${getStatusBadge(m.status)}">${m.status}</span></td>
                    <td><span class="badge ${getVerifBadge(m.verificationStatus)}">${m.verificationStatus}</span></td>
                    <td class="small">${m.explanation || ''}</td>
                    <td>
                        <button class="btn btn-primary btn-sm" onclick="bookMatch('${eventId}', '${m.resourceId}', 1)">Book Now</button>
                    </td>
                </tr>
            `).join('');
        } else {
            summary.innerHTML = '⚠️ No matching campus resources found in inventory for this requirement.';
            tbody.innerHTML = '<tr><td colspan="10" class="text-center text-warning font-bold">No inventory matches found. Open Pre-Purchase Decision Matrix to evaluate Renting vs Purchasing.</td></tr>';
        }
    } catch (err) {
        console.error('Failed to run smart matching:', err);
    }
}

async function openPrePurchaseDecisionModal() {
    const reqSel = document.getElementById('matchReqSelect');
    const selectedText = reqSel.options[reqSel.selectedIndex]?.text || 'Projector';
    
    try {
        const res = await fetch(`/api/decision?itemName=${encodeURIComponent(selectedText)}`);
        const data = await res.json();
        
        if (data) {
            setVal('decisionAvoidedVal', formatCurrency(data.purchaseAvoided || 35000));
            setVal('decisionBadge', data.recommendedOption || 'DIRECT_REUSE');
            setVal('decisionSavings', 'Estimated Savings: ' + formatCurrency(data.estimatedSavings || 35000));
            setVal('decisionReasonText', data.recommendationReason || 'Optimal campus inventory reuse preserves 100% of purchase budget.');

            const tbody = document.getElementById('tblDecisionMatrix');
            if (data.options) {
                tbody.innerHTML = data.options.map(opt => `
                    <tr class="${opt.option === data.recommendedOption ? 'highlight-row' : ''}">
                        <td><b>${opt.option}</b></td>
                        <td class="font-bold">${formatCurrency(opt.cost)}</td>
                        <td>${opt.inventoryImpact}</td>
                        <td>${opt.viabilityNotes}</td>
                    </tr>
                `).join('');
            }
            openModal('modalDecision');
        }
    } catch (err) {
        showToast('Error fetching decision matrix: ' + err.message, 'danger');
    }
}

async function bookMatch(eventId, resourceId, qty) {
    try {
        const res = await fetch('/api/reservations', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                eventId: eventId,
                resourceId: resourceId,
                quantity: qty,
                reservedBy: currentUser.fullName
            })
        });
        const data = await res.json();
        if (data.success) {
            showToast(`Resource ${resourceId} reserved successfully!`, 'success');
            switchTab('reservations');
            loadReservations();
            loadDashboard();
        } else {
            showToast(data.message || 'Reservation blocked', 'danger');
        }
    } catch (err) {
        showToast('Reservation error: ' + err.message, 'danger');
    }
}

// ==================== 6. RESERVATIONS, CHECKOUT & RETURNS ====================
async function loadReservations() {
    try {
        const res = await fetch('/api/reservations');
        allReservations = await res.json();
        
        const filter = document.getElementById('resStatusFilter').value;
        const filtered = filter === 'All' ? allReservations : allReservations.filter(r => r.status === filter);
        
        const tbody = document.getElementById('tblReservations');
        if (filtered.length > 0) {
            tbody.innerHTML = filtered.map(r => `
                <tr>
                    <td><b>${r.reservationId}</b></td>
                    <td><b>${r.eventId}</b></td>
                    <td>${r.resourceId}</td>
                    <td>${r.resourceName || '-'}</td>
                    <td><b>${r.quantity}</b></td>
                    <td class="small">${r.startTime}</td>
                    <td class="small">${r.endTime}</td>
                    <td class="small font-bold text-danger">${r.returnDeadline || '-'}</td>
                    <td><span class="badge ${getStatusBadge(r.status)}">${r.status}</span></td>
                    <td>
                        ${r.status === 'APPROVED' || r.status === 'REQUESTED' ? 
                            `<button class="btn btn-primary btn-sm" onclick="openCheckoutModal('${r.reservationId}')">📤 Checkout</button>` : ''}
                        ${r.status === 'ACTIVE' || r.status === 'OVERDUE' ? 
                            `<button class="btn btn-success btn-sm" onclick="openReturnModal('${r.reservationId}')">📥 Return</button>` : ''}
                        ${r.status === 'COMPLETED' ? '<span class="text-muted small">Returned</span>' : ''}
                    </td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = '<tr><td colspan="10" class="text-center text-muted">No reservations found for current filter.</td></tr>';
        }
    } catch (err) {
        console.error('Failed to load reservations:', err);
    }
}

function openCheckoutModal(resId) {
    document.getElementById('chkResId').value = resId;
    openModal('modalCheckout');
}

async function submitCheckout() {
    const resId = document.getElementById('chkResId').value;
    const cond = parseInt(document.getElementById('chkCond').value) || 5;
    const safety = document.getElementById('chkSafety').checked;
    const notes = document.getElementById('chkNotes').value.trim();

    try {
        const res = await fetch('/api/reservations/checkout', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                reservationId: resId,
                inspector: currentUser.fullName,
                conditionRating: cond,
                safetyCheck: safety,
                notes: notes
            })
        });
        const data = await res.json();
        if (data.success) {
            closeModal('modalCheckout');
            showToast(`Pre-use inspection passed! Resource dispatched.`, 'success');
            loadReservations();
            loadDashboard();
        } else {
            showToast(data.message || 'Checkout failed', 'danger');
        }
    } catch (err) {
        showToast('Checkout error: ' + err.message, 'danger');
    }
}

function openReturnModal(resId) {
    document.getElementById('retResId').value = resId;
    openModal('modalReturn');
}

async function submitReturn() {
    const resId = document.getElementById('retResId').value;
    const qty = parseInt(document.getElementById('retQty').value) || 1;
    const cond = parseInt(document.getElementById('retCond').value) || 4;
    const outcome = document.getElementById('retOutcome').value;
    const notes = document.getElementById('retNotes').value.trim();

    try {
        const res = await fetch('/api/reservations/return', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                reservationId: resId,
                inspector: currentUser.fullName,
                returnedQuantity: qty,
                conditionRating: cond,
                outcomeStatus: outcome,
                notes: notes
            })
        });
        const data = await res.json();
        if (data.success) {
            closeModal('modalReturn');
            showToast(`Post-use return recorded! Inventory updated to ${outcome}.`, 'success');
            loadReservations();
            loadDashboard();
        } else {
            showToast(data.message || 'Return failed', 'danger');
        }
    } catch (err) {
        showToast('Return error: ' + err.message, 'danger');
    }
}

// ==================== 7. REPORTS & EXPORT ====================
let currentReportData = null;

async function loadReport() {
    const typeIdx = document.getElementById('reportSelect').value;
    try {
        const res = await fetch(`/api/reports?type=${typeIdx}`);
        const data = await res.json();
        currentReportData = data;
        
        const thead = document.getElementById('tblReportHead');
        const tbody = document.getElementById('tblReportBody');

        if (data && data.headers && data.rows) {
            thead.innerHTML = `<tr>${data.headers.map(h => `<th>${h}</th>`).join('')}</tr>`;
            if (data.rows.length > 0) {
                tbody.innerHTML = data.rows.map(row => `
                    <tr>${row.map(cell => `<td>${cell}</td>`).join('')}</tr>
                `).join('');
            } else {
                tbody.innerHTML = `<tr><td colspan="${data.headers.length}" class="text-center text-muted">No records found for this report.</td></tr>`;
            }
        }
    } catch (err) {
        console.error('Failed to load report:', err);
    }
}

function downloadReportCSV() {
    if (!currentReportData || !currentReportData.rows || currentReportData.rows.length === 0) {
        showToast('No report data available to export', 'warning');
        return;
    }

    const headers = currentReportData.headers.join(',');
    const rows = currentReportData.rows.map(r => r.map(c => `"${String(c).replace(/"/g, '""')}"`).join(','));
    const csvContent = "data:text/csv;charset=utf-8," + [headers, ...rows].join('\n');
    
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", `EventLoop_Report_${Date.now()}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    showToast('Report CSV successfully downloaded!', 'success');
}

// ==================== 8. OOP & POLYMORPHISM LAB ====================
async function runOopDemo() {
    const choice = document.getElementById('oopChoice').value;
    const consoleEl = document.getElementById('oopConsole');
    consoleEl.textContent = 'Executing Dynamic Method Dispatch on JVM...';

    try {
        const res = await fetch(`/api/oop-demo?choice=${choice}`);
        const data = await res.text();
        consoleEl.textContent = data;
    } catch (err) {
        consoleEl.textContent = 'JVM Execution Error: ' + err.message;
    }
}

// ==================== MODAL HELPERS ====================
function openModal(id) {
    const m = document.getElementById(id);
    if (m) m.classList.add('active');
}

function closeModal(id) {
    const m = document.getElementById(id);
    if (m) m.classList.remove('active');
}

// Close modal on background click
window.onclick = function(event) {
    if (event.target.classList.contains('modal-overlay')) {
        event.target.classList.remove('active');
    }
};

// ==================== UTILITIES ====================
function setVal(id, val) {
    const el = document.getElementById(id);
    if (el) el.textContent = val;
}

function formatCurrency(val) {
    const num = parseFloat(val) || 0;
    return '₹' + num.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function renderStars(rating) {
    const r = Math.min(5, Math.max(1, parseInt(rating) || 5));
    return '⭐'.repeat(r) + ` (${r}/5)`;
}

function getStatusBadge(status) {
    switch (status) {
        case 'AVAILABLE': case 'COMPLETED': return 'badge-success';
        case 'RESERVED': case 'ACTIVE': return 'badge-info';
        case 'UNDER_REPAIR': case 'MISSING': case 'CANCELLED': return 'badge-danger';
        case 'OVERDUE': case 'REPURPOSE_REQUIRED': return 'badge-warning';
        default: return 'badge-gray';
    }
}

function getVerifBadge(verif) {
    switch (verif) {
        case 'VERIFIED': return 'badge-success';
        case 'EXPIRED': case 'BLOCKED': return 'badge-danger';
        case 'VERIFICATION_REQUIRED': return 'badge-warning';
        default: return 'badge-gray';
    }
}

function getMatchBadge(matchType) {
    switch (matchType) {
        case 'EXACT': return 'badge-success';
        case 'FUNCTIONAL': return 'badge-info';
        case 'REPAIRABLE': return 'badge-warning';
        case 'BORROWABLE': return 'badge-purple';
        case 'REPURPOSE': return 'badge-warning';
        default: return 'badge-gray';
    }
}

function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `<span>${type === 'success' ? '✅' : type === 'danger' ? '❌' : 'ℹ️'}</span> <span>${message}</span>`;

    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// ==================== 9. AI BUDGET OPTIMIZER & SCENARIOS ====================
let currentScenarioPlans = [];

async function initOptimizerEvents() {
    try {
        const res = await fetch('/api/events');
        const events = await res.json();
        const sel = document.getElementById('optEventSelect');
        if (sel && events) {
            sel.innerHTML = '<option value="">General Pre-Purchase Optimization</option>' +
                events.map(e => `<option value="${e.eventId}">${e.eventId} - ${e.eventName} (${e.eventDate})</option>`).join('');
        }
    } catch (e) {
        console.error('Failed to populate optimizer events:', e);
    }
}

async function runBudgetOptimizer() {
    const budgetVal = parseFloat(document.getElementById('optBudget')?.value) || 50000;
    const itemsText = document.getElementById('optItemsText')?.value.trim() || '20 chairs, 3 projectors, 10 cables, 5 banners, 200 badges';
    const btn = document.getElementById('btnRunOptimizer');
    const container = document.getElementById('optimizerCardsContainer');
    const tbody = document.getElementById('tblOptimizerBody');

    if (btn) {
        btn.disabled = true;
        btn.textContent = '⚡ Computing Multi-Scenario Plans...';
    }

    try {
        const res = await fetch('/api/optimizer/generate-plans', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ budget: String(budgetVal), itemsText: itemsText })
        });
        const plans = await res.json();
        currentScenarioPlans = plans;

        if (plans && plans.length > 0) {
            // Render 3 Scenario Comparison Cards
            container.innerHTML = plans.map(p => {
                const planClass = p.planId === 'PLAN_A' ? 'scenario-plan-a' : (p.planId === 'PLAN_B' ? 'scenario-plan-b' : 'scenario-plan-c');
                const barClass = p.planId === 'PLAN_A' ? 'bar-plan-a' : (p.planId === 'PLAN_B' ? 'bar-plan-b' : 'bar-plan-c');
                const badgeClass = p.planId === 'PLAN_C' ? 'badge-success' : (p.planId === 'PLAN_B' ? 'badge-info' : 'badge-danger');
                
                return `
                    <div class="scenario-card ${planClass}">
                        <div class="scenario-header">
                            <span class="scenario-title">${p.planName}</span>
                            <span class="badge ${badgeClass}">${p.recommendationBadge}</span>
                        </div>
                        <div class="scenario-cost-row">
                            <span class="scenario-cost ${p.planId === 'PLAN_C' ? 'text-success' : ''}">${formatCurrency(p.totalCost)}</span>
                            <span class="scenario-saved text-success">Saved: ${formatCurrency(p.amountSaved)}</span>
                        </div>
                        <div class="scenario-progress">
                            <div class="scenario-progress-bar ${barClass}" style="width: ${Math.min(100, p.budgetUtilizationPct)}%;"></div>
                        </div>
                        <div class="scenario-metrics">
                            <span class="scenario-metric-item">⏱️ Lead Time: <b>${p.estimatedLeadTimeDays}d</b></span>
                            <span class="scenario-metric-item">🎯 Feasibility: <b>${p.feasibilityScore}/100</b></span>
                            <span class="scenario-metric-item">🔁 Reused: <b>${p.itemsReused}</b></span>
                            ${p.itemsBorrowed > 0 ? `<span class="scenario-metric-item">🤝 Borrowed: <b>${p.itemsBorrowed}</b></span>` : ''}
                            ${p.itemsRepaired > 0 ? `<span class="scenario-metric-item">🛠️ Repaired: <b>${p.itemsRepaired}</b></span>` : ''}
                            ${p.itemsRented > 0 ? `<span class="scenario-metric-item">🚚 Rented: <b>${p.itemsRented}</b></span>` : ''}
                            <span class="scenario-metric-item">🛒 Bought: <b>${p.itemsPurchased}</b></span>
                        </div>
                        <p class="small text-muted mt-1 mb-2">${p.rationale}</p>
                        <button class="btn ${p.planId === 'PLAN_C' ? 'btn-success' : 'btn-outline'} btn-sm btn-block" onclick="applyScenarioPlan('${p.planId}')">
                            ${p.planId === 'PLAN_C' ? '⭐ Select Plan C (Recommended)' : 'Select ' + p.planName.split('—')[0].trim()}
                        </button>
                    </div>
                `;
            }).join('');

            // Render Itemized Comparison Table (Rows: Item names, Cols: Plan A vs Plan B vs Plan C)
            const planA = plans.find(p => p.planId === 'PLAN_A') || plans[0];
            const planB = plans.find(p => p.planId === 'PLAN_B') || plans[1];
            const planC = plans.find(p => p.planId === 'PLAN_C') || plans[2];

            if (planA && planA.itemBreakdown) {
                tbody.innerHTML = planA.itemBreakdown.map((itemA, idx) => {
                    const itemB = planB?.itemBreakdown?.[idx] || {};
                    const itemC = planC?.itemBreakdown?.[idx] || {};

                    return `
                        <tr>
                            <td><b>${itemA.itemName}</b></td>
                            <td><b>${itemA.quantity}</b></td>
                            <td class="text-danger font-bold">${formatCurrency(itemA.totalCost)}<br><span class="small text-muted">${itemA.sourcingMethod}</span></td>
                            <td class="text-blue font-bold">${formatCurrency(itemB.totalCost || 0)}<br><span class="small text-muted">${itemB.sourcingMethod || '-'}</span></td>
                            <td class="text-success font-bold">${formatCurrency(itemC.totalCost || 0)}<br><span class="small text-muted">${itemC.sourcingMethod || '-'}</span></td>
                            <td class="small">
                                <b>Plan C Allocation:</b> ${itemC.inventorySource || '-'}<br>
                                <span class="text-muted">${itemC.notes || ''}</span>
                            </td>
                        </tr>
                    `;
                }).join('');
            }
        }
    } catch (err) {
        showToast('Optimizer error: ' + err.message, 'danger');
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.textContent = '⚡ Generate Optimized Scenarios';
        }
    }
}

function applyScenarioPlan(planId) {
    const plan = currentScenarioPlans.find(p => p.planId === planId);
    if (!plan) return;

    const eventId = document.getElementById('optEventSelect')?.value || 'EVT-2026-101';
    showToast(`✅ ${plan.planName} applied to ${eventId}! ₹${plan.amountSaved.toLocaleString('en-IN')} budget preserved.`, 'success');
}

function exportOptimizerCSV() {
    if (!currentScenarioPlans || currentScenarioPlans.length === 0) {
        showToast('Run the optimizer first to generate data', 'warning');
        return;
    }

    const rows = [
        ["Scenario Plan", "Strategy Tag", "Total Cost (INR)", "Budget Saved (INR)", "Lead Time (Days)", "Feasibility Score", "Items Reused", "Items Borrowed", "Items Bought"],
        ...currentScenarioPlans.map(p => [
            `"${p.planName}"`, `"${p.strategyTag}"`, p.totalCost, p.amountSaved, p.estimatedLeadTimeDays, p.feasibilityScore, p.itemsReused, p.itemsBorrowed, p.itemsPurchased
        ])
    ];

    const csvContent = "data:text/csv;charset=utf-8," + rows.map(e => e.join(",")).join("\n");
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", `EventLoop_Budget_Scenarios_${Date.now()}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    showToast('Budget Scenarios CSV exported!', 'success');
}

// ==================== 10. FLOATING AI COPILOT CHATBOT ====================
function toggleAiChat() {
    const win = document.getElementById('aiChatWindow');
    if (!win) return;
    win.classList.toggle('active');
    if (win.classList.contains('active')) {
        setTimeout(() => {
            const inp = document.getElementById('aiChatInput');
            if (inp) inp.focus();
        }, 200);
    }
}

function sendAiQuickPrompt(promptText) {
    const inp = document.getElementById('aiChatInput');
    if (inp) inp.value = promptText;
    sendAiMessage();
}

async function sendAiMessage() {
    const input = document.getElementById('aiChatInput');
    const feed = document.getElementById('aiMessageFeed');
    const sendBtn = document.getElementById('aiSendBtn');
    const text = input ? input.value.trim() : '';

    if (!text) return;

    // Append user message
    const userBubble = document.createElement('div');
    userBubble.className = 'ai-message user-message';
    userBubble.textContent = text;
    feed.appendChild(userBubble);
    input.value = '';
    feed.scrollTop = feed.scrollHeight;

    if (sendBtn) sendBtn.disabled = true;

    // Append typing indicator
    const typingBubble = document.createElement('div');
    typingBubble.className = 'ai-message bot-message';
    typingBubble.innerHTML = '<i>⚡ Analyzing campus constraints & budget models...</i>';
    feed.appendChild(typingBubble);
    feed.scrollTop = feed.scrollHeight;

    try {
        const res = await fetch('/api/ai/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ message: text })
        });
        const data = await res.json();

        typingBubble.remove();

        const botBubble = document.createElement('div');
        botBubble.className = 'ai-message bot-message';

        // Format markdown boldly
        let replyHtml = (data.reply || 'Analysis complete')
            .replace(/\n\n/g, '<br><br>')
            .replace(/\n/g, '<br>')
            .replace(/\*\*(.*?)\*\*/g, '<b>$1</b>')
            .replace(/\*(.*?)\*/g, '<i>$1</i>');

        botBubble.innerHTML = `<div class="message-content">${replyHtml}</div>`;
        feed.appendChild(botBubble);

        // Update suggestion chips if provided
        if (data.suggestionChips && data.suggestionChips.length > 0) {
            const chipsContainer = document.getElementById('aiSuggestionChips');
            if (chipsContainer) {
                chipsContainer.innerHTML = data.suggestionChips.map(c => 
                    `<button class="chip-btn" onclick="sendAiQuickPrompt('${c.replace(/'/g, "\\'")}')">${c}</button>`
                ).join('');
            }
        }

        // If scenarios returned, optionally auto-switch to optimizer tab
        if (data.scenarioPlans && data.scenarioPlans.length > 0) {
            currentScenarioPlans = data.scenarioPlans;
            // update optimizer tab data in the background
            runBudgetOptimizer();
        }

    } catch (err) {
        typingBubble.innerHTML = '⚠️ AI Copilot connection error: ' + err.message;
    } finally {
        if (sendBtn) sendBtn.disabled = false;
        feed.scrollTop = feed.scrollHeight;
    }
}

// ==================== 11. PRIVATE EVENT PLANNER ENTERPRISE STUDIO ====================
let currentEnterprisePackages = [];
let currentSecurityDeposit = 30000.0;

async function runEnterpriseQuotation() {
    const clientName = document.getElementById('entClientName')?.value.trim() || 'Luxury Wedding';
    const eventType = document.getElementById('entEventType')?.value || 'Luxury Wedding & Reception';
    const guests = parseInt(document.getElementById('entGuests')?.value) || 450;
    const budget = parseFloat(document.getElementById('entBudget')?.value) || 300000;
    const btn = document.getElementById('btnRunEnterprise');
    const container = document.getElementById('enterpriseCardsContainer');
    const tbody = document.getElementById('tblEnterpriseBody');

    if (btn) {
        btn.disabled = true;
        btn.textContent = '⚡ Computing Commercial Proposals...';
    }

    try {
        const res = await fetch('/api/enterprise/quotation', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ eventType: eventType, budget: String(budget), guests: String(guests) })
        });
        const packages = await res.json();
        currentEnterprisePackages = packages;

        if (packages && packages.length > 0) {
            currentSecurityDeposit = packages[0].securityDepositRequired || 30000;
            const depositEl = document.getElementById('entDepositHeld');
            if (depositEl) depositEl.textContent = formatCurrency(currentSecurityDeposit);

            // Render Commercial Package Cards
            container.innerHTML = packages.map(pkg => {
                const isGold = pkg.packageTier === 'GOLD_BALANCED';
                const isPlat = pkg.packageTier === 'PLATINUM_VIP';
                const borderClass = isGold ? 'border-primary' : (isPlat ? 'border-purple' : 'border-success');

                return `
                    <div class="scenario-card" style="border-left: 5px solid ${isGold ? '#2563EB' : (isPlat ? '#8B5CF6' : '#10B981')};">
                        <div class="scenario-header">
                            <span class="scenario-title">${pkg.packageName}</span>
                            <span class="badge ${isGold ? 'badge-info' : (isPlat ? 'badge-purple' : 'badge-success')}">
                                ${isGold ? '⭐ BEST SELLER (HIGH MARGIN)' : (isPlat ? 'VIP LUXURY' : 'MAX PROFIT (100% IN-HOUSE)')}
                            </span>
                        </div>
                        <div class="scenario-cost-row">
                            <div>
                                <span class="small text-muted">Client Quote Price:</span><br>
                                <span class="scenario-cost font-bold">${formatCurrency(pkg.clientQuotePrice)}</span>
                            </div>
                            <div class="text-right">
                                <span class="small text-muted">Net Company Profit:</span><br>
                                <span class="scenario-cost text-success font-bold">${formatCurrency(pkg.grossProfit)}</span>
                                <span class="badge badge-success small">(${pkg.profitMarginPct.toFixed(1)}% Margin)</span>
                            </div>
                        </div>
                        <div class="scenario-metrics">
                            <span class="scenario-metric-item">🔒 Deposit: <b>${formatCurrency(pkg.securityDepositRequired)}</b></span>
                            <span class="scenario-metric-item">👷 Crew: <b>${pkg.crewTechniciansNeeded} Techs</b></span>
                            <span class="scenario-metric-item">⏱️ Setup: <b>${pkg.setupDurationHours} Hours</b></span>
                            <span class="scenario-metric-item">🏢 In-House: <b>${pkg.inHouseItemsUsed} units</b></span>
                            ${pkg.vendorCrossHiredItems > 0 ? `<span class="scenario-metric-item">🚚 Cross-Hire: <b>${pkg.vendorCrossHiredItems} units</b></span>` : '<span class="scenario-metric-item text-success">✨ 0% Cross-Hire Bleed</span>'}
                        </div>
                        <p class="small text-muted mt-1 mb-2">${pkg.clientTargetSuitability}</p>
                        <button class="btn ${isGold ? 'btn-primary' : 'btn-outline'} btn-sm btn-block" onclick="selectEnterprisePackage('${pkg.packageTier}')">
                            Select ${pkg.packageName.split('(')[0].trim()} Proposal
                        </button>
                    </div>
                `;
            }).join('');

            // Render Itemized Line-Items of the Gold Package
            const goldPkg = packages.find(p => p.packageTier === 'GOLD_BALANCED') || packages[0];
            if (goldPkg && goldPkg.itemizedList) {
                tbody.innerHTML = goldPkg.itemizedList.map(item => `
                    <tr>
                        <td><span class="badge badge-purple">${item.category}</span></td>
                        <td><b>${item.itemName}</b></td>
                        <td>${item.quantity}</td>
                        <td>
                            ${item.sourcingType === 'VENDOR_CROSS_HIRE' ? 
                                `<span class="badge badge-warning">🚚 Cross-Hire: ${item.partnerVendor}</span>` :
                                `<span class="badge badge-success">🏢 In-House: ${item.partnerVendor}</span>`}
                        </td>
                        <td class="text-danger font-bold">${formatCurrency(item.internalCost)}</td>
                        <td class="text-blue font-bold">${formatCurrency(item.clientBillingRate)}</td>
                        <td class="text-success font-bold">${formatCurrency(item.clientBillingRate - item.internalCost)}</td>
                    </tr>
                `).join('');
            }
        }
    } catch (e) {
        showToast('Enterprise quote error: ' + e.message, 'danger');
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.textContent = '⚡ Generate Commercial Packages & Profit Margins';
        }
    }
}

function selectEnterprisePackage(tier) {
    const pkg = currentEnterprisePackages.find(p => p.packageTier === tier);
    if (!pkg) return;
    showToast(`✅ ${pkg.packageName} locked! Profit of ${formatCurrency(pkg.grossProfit)} estimated.`, 'success');
}

function simulateDamagePenalty() {
    const penalty = 6500.0; // e.g. cracked lighting lens
    currentSecurityDeposit = Math.max(0, currentSecurityDeposit - penalty);
    const depositEl = document.getElementById('entDepositHeld');
    if (depositEl) depositEl.textContent = formatCurrency(currentSecurityDeposit);

    const logEl = document.getElementById('entDepositLog');
    if (logEl) {
        logEl.className = 'alert-box warning-alert';
        logEl.innerHTML = `
            <div class="alert-text">
                ⚠️ <b>Client Damage Logged:</b> 1x Stage Par Can Lens cracked during load-out. Penalty of <b>${formatCurrency(penalty)}</b> deducted from security deposit. Net refundable balance: <b>${formatCurrency(currentSecurityDeposit)}</b>.
            </div>
        `;
    }
    showToast(`Logged ₹6,500 damage penalty. Updated deposit: ${formatCurrency(currentSecurityDeposit)}`, 'warning');
}

function releaseSecurityDeposit() {
    const depositEl = document.getElementById('entDepositHeld');
    if (depositEl) depositEl.textContent = '₹0.00 (Refunded)';

    const logEl = document.getElementById('entDepositLog');
    if (logEl) {
        logEl.className = 'alert-box';
        logEl.style.backgroundColor = '#ECFDF5';
        logEl.style.border = '1px solid #A7F3D0';
        logEl.style.color = '#065F46';
        logEl.innerHTML = `
            <div class="alert-text">
                ✅ <b>Clean Gatepass Signed:</b> All gear verified and accounted for. Refund Voucher of <b>${formatCurrency(currentSecurityDeposit)}</b> generated for client.
            </div>
        `;
    }
    showToast('Clean Return Gatepass issued! Security deposit released.', 'success');
}

function exportEnterpriseQuoteCSV() {
    if (!currentEnterprisePackages || currentEnterprisePackages.length === 0) {
        showToast('Generate a proposal first', 'warning');
        return;
    }

    const gold = currentEnterprisePackages.find(p => p.packageTier === 'GOLD_BALANCED') || currentEnterprisePackages[0];
    const rows = [
        ["Category", "Gear & Services", "Quantity", "Sourcing Type", "Vendor/Warehouse", "Internal Direct Cost (INR)", "Client Billable Rate (INR)", "Net Gross Profit (INR)"],
        ...gold.itemizedList.map(i => [
            `"${i.category}"`, `"${i.itemName}"`, i.quantity, `"${i.sourcingType}"`, `"${i.partnerVendor}"`, i.internalCost, i.clientBillingRate, (i.clientBillingRate - i.internalCost)
        ])
    ];

    const csvContent = "data:text/csv;charset=utf-8," + rows.map(e => e.join(",")).join("\n");
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", `EventLoop_Enterprise_Quotation_${Date.now()}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    showToast('Client Quotation CSV exported!', 'success');
}


