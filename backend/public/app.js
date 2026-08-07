// API Base Configuration
const API_BASE = window.location.origin + window.location.pathname.replace('index.html', '').replace(/\/$/, '');

let currentUser = null;
let currentToken = null;
let allBoards = [];
let activeTab = 'customer';
let searchedConnection = null;
let searchedBills = [];
let trendChart = null;

document.addEventListener('DOMContentLoaded', () => {
    loadSession();
    fetchBoards();
});

// Session Management
function loadSession() {
    const userStr = localStorage.getItem('user');
    const tokenStr = localStorage.getItem('token');
    if (userStr && tokenStr) {
        currentUser = JSON.parse(userStr);
        currentToken = tokenStr;
        showApp();
    } else {
        showAuth();
    }
}

function showAuth() {
    document.getElementById('authContainer').classList.remove('hidden');
    document.getElementById('mainApp').classList.add('hidden');
}

function showApp() {
    document.getElementById('authContainer').classList.add('hidden');
    document.getElementById('mainApp').classList.remove('hidden');
    document.getElementById('userNameDisplay').textContent = currentUser.full_name;
    document.getElementById('userAvatar').textContent = currentUser.full_name.charAt(0).toUpperCase();
    
    // Initial fetch for the dashboard and components
    refreshDashboard();
    fetchNotifications();
    loadAnalytics();
}

function logout() {
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    currentUser = null;
    currentToken = null;
    showAuth();
}

// Authentication Forms Toggle
function showRegister() {
    document.getElementById('loginForm').classList.add('hidden');
    document.getElementById('registerForm').classList.remove('hidden');
}

function showLogin() {
    document.getElementById('registerForm').classList.add('hidden');
    document.getElementById('loginForm').classList.remove('hidden');
}

// Fetch Boards
async function fetchBoards() {
    try {
        const response = await fetch(`${API_BASE}/boards`);
        const data = await response.json();
        allBoards = data.boards || [];
        
        const regBoardSelect = document.getElementById('regBoard');
        const searchBoardSelect = document.getElementById('searchBoard');
        
        // Clear options except first
        regBoardSelect.innerHTML = '<option value="" disabled selected style="background: #0a0e1a; color: rgba(255,255,255,0.5);">⚡ Select Electricity Board</option>';
        searchBoardSelect.innerHTML = '<option value="" disabled selected style="background: #0a0e1a; color: rgba(255,255,255,0.5);">⚡ Select Electricity Board</option>';
        
        allBoards.forEach(board => {
            const opt1 = document.createElement('option');
            opt1.value = board.id;
            opt1.textContent = `${board.name} (${board.code})`;
            opt1.style.background = '#0a0e1a';
            regBoardSelect.appendChild(opt1);
            
            const opt2 = document.createElement('option');
            opt2.value = board.id;
            opt2.textContent = `${board.name} (${board.code})`;
            opt2.style.background = '#0a0e1a';
            searchBoardSelect.appendChild(opt2);
        });
    } catch (e) {
        console.error('Error fetching boards:', e);
    }
}

// Register
async function handleRegister() {
    const fullName = document.getElementById('regName').value.trim();
    const email = document.getElementById('regEmail').value.trim();
    const password = document.getElementById('regPassword').value.trim();
    const boardId = document.getElementById('regBoard').value;
    const meterNum = document.getElementById('regMeter').value.trim();
    
    if (!fullName || !email || !password) {
        alert('Please fill out Name, Email, and Password.');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ full_name: fullName, email: email, password: password })
        });
        
        const data = await response.json();
        if (response.ok) {
            alert('Registration successful! Please login.');
            
            // If user filled connection details, let's auto login and link the connection
            showLogin();
        } else {
            alert(data.error || 'Registration failed');
        }
    } catch (e) {
        console.error(e);
        alert('Server connection error during registration.');
    }
}

// Login
async function handleLogin() {
    const email = document.getElementById('loginEmail').value.trim();
    const password = document.getElementById('loginPassword').value.trim();
    
    if (!email || !password) {
        alert('Please fill in both Email and Password.');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: email, password: password })
        });
        
        const data = await response.json();
        if (response.ok) {
            currentUser = data.user;
            currentToken = data.token;
            localStorage.setItem('user', JSON.stringify(currentUser));
            localStorage.setItem('token', currentToken);
            showApp();
        } else {
            alert(data.error || 'Invalid credentials');
        }
    } catch (e) {
        console.error(e);
        alert('Server connection error during login.');
    }
}

// Tabs switching
function switchTab(tabName, btnEl) {
    activeTab = tabName;
    
    // Update active tab button style
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    if (btnEl) btnEl.classList.add('active');
    
    // Update tab visibility
    document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));
    
    const targetTab = document.getElementById('tab' + tabName.charAt(0).toUpperCase() + tabName.slice(1));
    if (targetTab) {
        targetTab.classList.add('active');
    }
}

// Dashboard statistics
async function refreshDashboard() {
    try {
        // Fetch dashboard stats from backend
        const dashRes = await fetch(`${API_BASE}/dashboard`);
        const dashData = await dashRes.json();
        
        // Fetch bills & connections to make calculated live stats
        const billsRes = await fetch(`${API_BASE}/bills`);
        const billsData = await billsRes.json();
        const bills = billsData.bills || [];
        
        const connRes = await fetch(`${API_BASE}/connections`);
        const connData = await connRes.json();
        const connections = connData.connections || [];
        
        let liveUsage = 0;
        let liveRevenue = 0;
        let pendingCount = 0;
        
        bills.forEach(bill => {
            liveUsage += parseFloat(bill.units_used || 0);
            liveRevenue += parseFloat(bill.amount_paid || 0);
            if (bill.status === 'pending' || bill.status === 'overdue') {
                pendingCount++;
            }
        });
        
        document.getElementById('totalCustomers').textContent = connections.length || dashData.dashboard?.total_bills || 0;
        document.getElementById('totalUsage').textContent = liveUsage.toLocaleString();
        document.getElementById('totalRevenue').textContent = '₹' + liveRevenue.toLocaleString();
        document.getElementById('pendingBills').textContent = pendingCount;
        
        const badge = document.getElementById('pendingBadge');
        if (pendingCount > 0) {
            badge.style.display = 'inline-block';
            badge.textContent = `${pendingCount} Pending`;
            badge.className = "status-badge status-unpaid";
        } else {
            badge.textContent = 'Clear';
            badge.className = "status-badge status-paid";
        }
    } catch (e) {
        console.error('Error updating dashboard stats:', e);
    }
}

// Search Customer
async function searchCustomer() {
    const boardId = document.getElementById('searchBoard').value;
    const consumerNum = document.getElementById('meterSearch').value.trim();
    
    if (!consumerNum) {
        alert('Please enter a consumer number.');
        return;
    }
    
    try {
        // Fetch all connections and search locally
        const connRes = await fetch(`${API_BASE}/connections`);
        const connData = await connRes.json();
        const connections = connData.connections || [];
        
        searchedConnection = connections.find(c => c.consumer_number.toLowerCase() === consumerNum.toLowerCase());
        
        if (!searchedConnection) {
            // Let's create a temporary connection for demo purposes if not found in seeded db
            alert('Consumer connection not found in local DB. Creating connection mock.');
            searchedConnection = {
                id: 99,
                service_number: 'SN-' + consumerNum,
                consumer_number: consumerNum,
                address: 'Main St, Digital Town',
                meter_type: 'Smart Meter'
            };
        }
        
        // Fetch bills for this consumer number
        const billsRes = await fetch(`${API_BASE}/bills/by_consumer?consumer_number=${consumerNum}`);
        const billsData = await billsRes.json();
        searchedBills = billsData.bills || [];
        
        // Populate Customer Tab details
        document.getElementById('detailService').textContent = searchedConnection.service_number;
        document.getElementById('detailMeter').textContent = searchedConnection.consumer_number;
        document.getElementById('detailAddress').textContent = searchedConnection.address;
        document.getElementById('detailMeterStatus').textContent = searchedConnection.meter_type;
        
        const unpaidBill = searchedBills.find(b => b.status === 'pending' || b.status === 'overdue');
        if (unpaidBill) {
            document.getElementById('detailBillingStatus').textContent = '⚠️ Bill Pending (₹' + unpaidBill.amount_due + ')';
            document.getElementById('detailBillingStatus').style.color = '#fbbf24';
            
            // Set payment tab details
            document.getElementById('payableAmount').textContent = '₹' + unpaidBill.amount_due;
            document.getElementById('dueDate').textContent = unpaidBill.due_date;
            document.getElementById('paymentStatusBadge').className = 'status-badge status-pending';
            document.getElementById('paymentStatusBadge').textContent = 'Pending';
        } else {
            document.getElementById('detailBillingStatus').textContent = '✅ Settled / Paid';
            document.getElementById('detailBillingStatus').style.color = '#10b981';
            
            document.getElementById('payableAmount').textContent = '₹0.00';
            document.getElementById('dueDate').textContent = 'N/A';
            document.getElementById('paymentStatusBadge').className = 'status-badge status-paid';
            document.getElementById('paymentStatusBadge').textContent = 'No Dues';
        }
        
        // Populate recent bills section
        const billsHtml = searchedBills.length > 0 ? searchedBills.map(b => `
            <div style="background: rgba(255,255,255,0.03); border: 1px solid var(--card-border); border-radius: 12px; padding: 15px; margin-bottom: 10px; display: flex; justify-content: space-between; align-items: center;">
                <div>
                    <div style="font-weight:700;">${b.bill_month}</div>
                    <div style="font-size:12px; color:var(--text-muted);">${b.units_used} Units</div>
                </div>
                <div style="text-align: right;">
                    <div style="font-weight:700; color:var(--accent);">₹${b.amount_due}</div>
                    <span class="status-badge ${b.status === 'paid' ? 'status-paid' : 'status-unpaid'}">${b.status.toUpperCase()}</span>
                </div>
            </div>
        `).join('') : '<div class="empty-state"><i class="fas fa-receipt"></i><p>No bills found for this customer</p></div>';
        
        document.getElementById('recentBills').innerHTML = billsHtml;
        
        document.getElementById('customerDetails').style.display = 'block';
        document.getElementById('noCustomer').style.display = 'none';
        
    } catch (e) {
        console.error(e);
        alert('Error performing live search.');
    }
}

// Payment method selection
let selectedMethod = 'UPI';
function selectPayment(element, method) {
    document.querySelectorAll('.payment-option').forEach(opt => opt.classList.remove('selected'));
    element.classList.add('selected');
    selectedMethod = method;
}

// Process Bill Payment
async function processPayment() {
    if (!searchedConnection || searchedBills.length === 0) {
        alert('Please search a consumer connection first.');
        return;
    }
    
    const unpaidBill = searchedBills.find(b => b.status === 'pending' || b.status === 'overdue');
    if (!unpaidBill) {
        alert('No pending dues found for this consumer connection.');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE}/payments/create`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                bill_id: unpaidBill.id,
                amount: unpaidBill.amount_due,
                method: selectedMethod
            })
        });
        
        if (response.ok) {
            const data = await response.json();
            document.getElementById('transactionId').textContent = 'TXN-' + Date.now().toString().slice(-6);
            document.getElementById('paymentDetails').style.display = 'none';
            document.getElementById('paymentConfirmation').style.display = 'block';
            
            // Refresh stats and search list after successful payment
            refreshDashboard();
            searchCustomer();
        } else {
            alert('Failed to execute payment.');
        }
    } catch (e) {
        console.error(e);
        alert('Server network error during payment.');
    }
}

function resetPayment() {
    document.getElementById('paymentDetails').style.display = 'block';
    document.getElementById('paymentConfirmation').style.display = 'none';
    switchTab('customer', document.querySelectorAll('.tab-btn')[0]);
}

// Notifications
async function fetchNotifications() {
    try {
        const response = await fetch(`${API_BASE}/notifications`);
        const data = await response.json();
        const notifications = data.notifications || [];
        
        document.getElementById('notificationCount').textContent = notifications.length;
        
        const listDiv = document.getElementById('notificationList');
        if (notifications.length > 0) {
            listDiv.innerHTML = notifications.map(n => `
                <div class="notification-item ${n.is_read == 0 ? 'unread' : ''}" onclick="markAsRead(${n.id}, this)">
                    <i class="fas fa-bell"></i>
                    <div class="content">
                        <h4>${n.title}</h4>
                        <p>${n.message}</p>
                    </div>
                </div>
            `).join('');
        } else {
            listDiv.innerHTML = '<div class="empty-state"><i class="fas fa-bell-slash"></i><p>No notifications</p></div>';
        }
    } catch (e) {
        console.error(e);
    }
}

async function markAsRead(id, element) {
    try {
        const response = await fetch(`${API_BASE}/notifications/read`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id: id })
        });
        if (response.ok) {
            element.classList.remove('unread');
            fetchNotifications();
        }
    } catch (e) {
        console.error(e);
    }
}

// Send Manual Reminder
async function sendReminder() {
    if (!searchedConnection) {
        alert('Search for a consumer connection first.');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE}/notifications/create`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                user_id: currentUser ? currentUser.id : 1,
                title: '⚡ Bill Due Notification Reminder',
                message: `Hi, the bill for consumer connection ${searchedConnection.consumer_number} is pending. Please pay immediately to avoid late fees.`
            })
        });
        
        if (response.ok) {
            alert('Payment reminder notification sent successfully!');
            fetchNotifications();
        } else {
            alert('Failed to send reminder.');
        }
    } catch (e) {
        console.error(e);
    }
}

// Analytics and Charts
async function loadAnalytics() {
    try {
        const response = await fetch(`${API_BASE}/bills`);
        const data = await response.json();
        const bills = data.bills || [];
        
        // Prepare data
        const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul'];
        const values = [120, 180, 230, 150, 310, 280, 320]; // Default mock trends
        
        if (bills.length > 0) {
            // Group and map if we have real bills data
            bills.slice(0, 7).reverse().forEach((b, idx) => {
                months[idx] = b.bill_month;
                values[idx] = b.units_used || 0;
            });
        }
        
        // Draw Chart.js
        const ctx = document.getElementById('trendChart').getContext('2d');
        
        if (trendChart) {
            trendChart.destroy();
        }
        
        trendChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: months,
                datasets: [{
                    label: 'Units Used',
                    data: values,
                    borderColor: '#4facfe',
                    backgroundColor: 'rgba(79, 172, 254, 0.1)',
                    borderWidth: 2,
                    tension: 0.4,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    x: { grid: { color: 'rgba(255,255,255,0.05)' }, ticks: { color: '#9ca3af' } },
                    y: { grid: { color: 'rgba(255,255,255,0.05)' }, ticks: { color: '#9ca3af' } }
                },
                plugins: {
                    legend: { labels: { color: 'white' } }
                }
            }
        });
        
        // Calculate collection efficiency
        const paidCount = bills.filter(b => b.status === 'paid').length;
        const totalCount = bills.length;
        const efficiency = totalCount > 0 ? Math.round((paidCount / totalCount) * 100) : 100;
        
        document.getElementById('collectionEfficiency').textContent = efficiency + '%';
        document.getElementById('efficiencyBar').style.width = efficiency + '%';
        
    } catch (e) {
        console.error('Error drawing chart:', e);
    }
}

function viewUsageHistory() {
    switchTab('analytics', document.querySelectorAll('.tab-btn')[3]);
}

function viewSavingTips() {
    alert("⚡ Quick Electricity Saving Tips:\n1. Switch to LED lightbulbs to save up to 80% on lighting energy.\n2. Clean/replace air conditioning filters regularly.\n3. Unplug chargers and appliances when not in use to eliminate phantom load.\n4. Utilize natural light during the day.");
}
