/**
 * EVENTLOOP – Web Application Client Logic
 * Interacts with EventLoop Embedded REST API
 */

// Global State
let currentUser = {
    userId: 1,
    username: 'admin',
    fullName: 'Prof. Rajesh Sharma',
    role: 'ADMIN'
};

let allResources = [];
let allEvents = [];
let allReservations = [];

// ==================== INITIALIZATION ====================
document.addEventListener('DOMContentLoaded', () => {
    updateUserBadge();
    loadDashboard();
    
    // Auto-generate sample IDs for modals
    initModalDefaults();
});

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
}

// ==================== USER & AUTH ====================
function updateUserBadge() {
    const nameEl = document.getElementById('userName');
    const roleEl = document.getElementById('userRole');
    if (nameEl) nameEl.textContent = currentUser.fullName;
    if (roleEl) {
        roleEl.textContent = currentUser.role;
        roleEl.className = 'role-pill ' + 
            (currentUser.role === 'ADMIN' ? 'role-admin' : 
            (currentUser.role === 'ORGANIZER' ? 'role-organizer' : 'role-cultural'));
    }
}

function openLoginModal() {
    openModal('modalLogin');
}

function quickLogin(username, password) {
    document.getElementById('loginUsername').value = username;
    document.getElementById('loginPassword').value = password;
    submitLogin();
}

async function submitLogin() {
    const u = document.getElementById('loginUsername').value.trim();
    const p = document.getElementById('loginPassword').value.trim();
    
    try {
        const res = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: u, password: p })
        });
        const data = await res.json();
        
        if (data.success && data.user) {
            currentUser = data.user;
            updateUserBadge();
            closeModal('modalLogin');
            showToast(`Welcome, ${currentUser.fullName} (${currentUser.role})!`, 'success');
            loadDashboard();
        } else {
            showToast(data.message || 'Login failed', 'danger');
        }
    } catch (err) {
        showToast('Login server error: ' + err.message, 'danger');
    }
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
