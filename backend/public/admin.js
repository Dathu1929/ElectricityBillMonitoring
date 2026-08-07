const API_BASE = window.location.origin + window.location.pathname.replace('admin.html', '').replace(/\/$/, '');

document.addEventListener('DOMContentLoaded', () => {
    loadUsers();
    loadBoards();
    loadBills();
    loadPayments();
});

// Switch Tab
function switchAdminTab(tabName, btnEl) {
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    if (btnEl) btnEl.classList.add('active');
    
    document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));
    const targetTab = document.getElementById('tab' + tabName.charAt(0).toUpperCase() + tabName.slice(1));
    if (targetTab) {
        targetTab.classList.add('active');
    }
}

// Load Users List
async function loadUsers() {
    try {
        const res = await fetch(`${API_BASE}/users`);
        const data = await res.json();
        const users = data.users || [];
        
        document.getElementById('usersCount').textContent = `Total: ${users.length}`;
        const tbody = document.getElementById('usersTableBody');
        
        tbody.innerHTML = users.map(u => `
            <tr>
                <td>${u.id}</td>
                <td><strong>${u.full_name}</strong></td>
                <td>${u.email}</td>
                <td><span class="status-badge ${u.role === 'admin' ? 'status-pending' : 'status-paid'}">${u.role.toUpperCase()}</span></td>
                <td>${u.created_at}</td>
            </tr>
        `).join('');
    } catch (e) {
        console.error('Error loading users:', e);
    }
}

// Load Boards
async function loadBoards() {
    try {
        const res = await fetch(`${API_BASE}/boards`);
        const data = await res.json();
        const boards = data.boards || [];
        
        document.getElementById('boardsCount').textContent = `Total: ${boards.length}`;
        const tbody = document.getElementById('boardsTableBody');
        
        tbody.innerHTML = boards.map(b => `
            <tr>
                <td>${b.id}</td>
                <td><strong>${b.name}</strong></td>
                <td>${b.code}</td>
                <td>${b.country}</td>
                <td>${b.connector_class}</td>
            </tr>
        `).join('');
    } catch (e) {
        console.error('Error loading boards:', e);
    }
}

// Load Bills
async function loadBills() {
    try {
        const res = await fetch(`${API_BASE}/bills`);
        const data = await res.json();
        const bills = data.bills || [];
        
        const tbody = document.getElementById('billsTableBody');
        tbody.innerHTML = bills.map(b => `
            <tr>
                <td>${b.id}</td>
                <td>${b.consumer_number}</td>
                <td>${b.bill_month}</td>
                <td>${b.units_used}</td>
                <td style="color: var(--accent); font-weight:700;">₹${b.amount_due}</td>
                <td style="color: var(--success);">₹${b.amount_paid}</td>
                <td>${b.due_date}</td>
                <td><span class="status-badge ${b.status === 'paid' ? 'status-paid' : 'status-unpaid'}">${b.status.toUpperCase()}</span></td>
                <td>
                    <i class="fas fa-check-circle action-icon edit" title="Mark as Paid" onclick="markBillPaid(${b.id}, ${b.amount_due})"></i>
                    <i class="fas fa-trash-alt action-icon delete" title="Delete Bill" onclick="deleteBill(${b.id})"></i>
                </td>
            </tr>
        `).join('');
    } catch (e) {
        console.error('Error loading bills:', e);
    }
}

// Create Bill
async function createBill() {
    const consumer = document.getElementById('billConsumer').value.trim();
    const month = document.getElementById('billMonth').value.trim();
    const units = parseFloat(document.getElementById('billUnits').value);
    const rate = parseFloat(document.getElementById('billRate').value) || 7.5;
    const dueDate = document.getElementById('billDueDate').value;
    
    if (!consumer || !month || isNaN(units) || !dueDate) {
        alert('Please fill out all required fields.');
        return;
    }
    
    const amount = units * rate;
    
    try {
        const res = await fetch(`${API_BASE}/bills/create`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                consumer_number: consumer,
                bill_month: month,
                units_used: units,
                rate_per_unit: rate,
                amount_due: amount,
                due_date: dueDate
            })
        });
        
        if (res.ok) {
            alert('Bill generated successfully!');
            // Reset input values
            document.getElementById('billConsumer').value = '';
            document.getElementById('billMonth').value = '';
            document.getElementById('billUnits').value = '';
            document.getElementById('billRate').value = '';
            document.getElementById('billDueDate').value = '';
            
            // Reload
            loadBills();
        } else {
            const data = await res.json();
            alert(data.error || 'Failed to generate bill');
        }
    } catch (e) {
        console.error(e);
    }
}

// Mark Bill as Paid
async function markBillPaid(id, amount) {
    if (!confirm('Are you sure you want to mark this bill as PAID?')) return;
    
    try {
        const res = await fetch(`${API_BASE}/bills/update`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                id: id,
                status: 'paid',
                amount_paid: amount
            })
        });
        
        if (res.ok) {
            alert('Bill marked as paid.');
            loadBills();
            loadPayments();
        } else {
            alert('Failed to update bill.');
        }
    } catch (e) {
        console.error(e);
    }
}

// Delete Bill
async function deleteBill(id) {
    if (!confirm('Are you sure you want to delete this bill? This action cannot be undone.')) return;
    
    try {
        const res = await fetch(`${API_BASE}/bills/delete`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id: id })
        });
        
        if (res.ok) {
            alert('Bill deleted.');
            loadBills();
        } else {
            alert('Failed to delete bill.');
        }
    } catch (e) {
        console.error(e);
    }
}

// Load Payments List
async function loadPayments() {
    try {
        const res = await fetch(`${API_BASE}/payments`);
        const data = await res.json();
        const payments = data.payments || [];
        
        const tbody = document.getElementById('paymentsTableBody');
        tbody.innerHTML = payments.map(p => `
            <tr>
                <td>${p.id}</td>
                <td>${p.bill_id}</td>
                <td><strong>₹${p.amount}</strong></td>
                <td>${p.method}</td>
                <td><span class="status-badge status-paid">${p.status.toUpperCase()}</span></td>
                <td>${p.created_at}</td>
            </tr>
        `).join('');
    } catch (e) {
        console.error('Error loading payments:', e);
    }
}
