const AUTH_TOKEN_KEY = "spendwise_token";
const categories = [
    "FOOD", "TRANSPORT", "HOUSING", "UTILITIES", "HEALTHCARE",
    "ENTERTAINMENT", "EDUCATION", "SHOPPING", "TRAVEL", "OTHER"
];

const state = {
    token: localStorage.getItem(AUTH_TOKEN_KEY) || "",
    expenses: [],
    editingId: null
};

const authScreen = document.getElementById("auth-screen");
const dashboardScreen = document.getElementById("dashboard-screen");
const authMessage = document.getElementById("auth-message");
const dashboardMessage = document.getElementById("dashboard-message");

const loginForm = document.getElementById("login-form");
const registerForm = document.getElementById("register-form");
const loginTab = document.getElementById("login-tab");
const registerTab = document.getElementById("register-tab");
const promoRegisterButton = document.getElementById("promo-register-button");
const filterStatus = document.getElementById("filter-status");
const categoryInsights = document.getElementById("category-insights");
const dashboardGreeting = document.getElementById("dashboard-greeting");
const registerStrengthBar = document.getElementById("register-strength-bar");
const registerStrengthText = document.getElementById("register-strength-text");
const registerPasswordInput = document.getElementById("register-password");

const expenseForm = document.getElementById("expense-form");
const expenseFormTitle = document.getElementById("expense-form-title");
const expenseSubmit = document.getElementById("expense-submit");
const cancelEditButton = document.getElementById("cancel-edit");
const logoutButton = document.getElementById("logout-button");
const expensesTableBody = document.getElementById("expenses-table-body");
const filterCategory = document.getElementById("filter-category");
const filterNote = document.getElementById("filter-note");
const sortBy = document.getElementById("sort-by");
const resetFilters = document.getElementById("reset-filters");

function setMessage(element, text, isError = false) {
    element.textContent = text || "";
    element.classList.toggle("error", isError);
}

function clearMessages() {
    setMessage(authMessage, "");
    setMessage(dashboardMessage, "");
}

function getErrorMessage(payload, fallback) {
    if (!payload) return fallback;
    if (Array.isArray(payload.errors) && payload.errors.length > 0) {
        return payload.errors.join(", ");
    }
    if (typeof payload.message === "string") {
        return payload.message;
    }
    return fallback;
}

async function apiRequest(path, options = {}, allowUnauthorized = false) {
    const headers = { "Content-Type": "application/json", ...(options.headers || {}) };
    if (state.token) {
        headers.Authorization = `Bearer ${state.token}`;
    }

    const response = await fetch(path, { ...options, headers });
    if (response.status === 401 && !allowUnauthorized) {
        logout();
        throw new Error("Your session expired. Please login again.");
    }
    if (response.status === 204) {
        return null;
    }

    const payload = await response.json().catch(() => null);
    if (!response.ok) {
        throw new Error(getErrorMessage(payload, "Request failed."));
    }
    return payload;
}

function fillCategoryOptions() {
    const expenseCategory = document.getElementById("expense-category");
    expenseCategory.innerHTML = categories
        .map((c) => `<option value="${c}">${c}</option>`)
        .join("");

    filterCategory.innerHTML = [
        `<option value="">All categories</option>`,
        ...categories.map((c) => `<option value="${c}">${c}</option>`)
    ].join("");
}

function formatAmount(amount) {
    return `Rs ${Number(amount).toFixed(2)}`;
}

function formatCategory(category) {
    return category.charAt(0) + category.slice(1).toLowerCase();
}

function setGreeting() {
    const hour = new Date().getHours();
    if (hour < 12) {
        dashboardGreeting.textContent = "Good morning";
        return;
    }
    if (hour < 18) {
        dashboardGreeting.textContent = "Good afternoon";
        return;
    }
    dashboardGreeting.textContent = "Good evening";
}

function setCategoryInsights(items) {
    if (items.length === 0) {
        categoryInsights.innerHTML = `<li><span>No category data yet.</span><span>Rs 0.00</span></li>`;
        return;
    }

    const totals = items.reduce((acc, item) => {
        acc[item.category] = (acc[item.category] || 0) + Number(item.amount);
        return acc;
    }, {});

    const insightRows = Object.entries(totals)
        .sort((a, b) => b[1] - a[1])
        .slice(0, 4);

    categoryInsights.innerHTML = insightRows.map(([category, total]) => `
        <li>
            <span>${formatCategory(category)}</span>
            <span>${formatAmount(total)}</span>
        </li>
    `).join("");
}

function updateSummary(items) {
    const totalAmount = items.reduce((sum, item) => sum + Number(item.amount), 0);
    const maxAmount = items.length > 0 ? Math.max(...items.map((item) => Number(item.amount))) : 0;
    const avgAmount = items.length > 0 ? totalAmount / items.length : 0;

    document.getElementById("total-expenses").textContent = String(items.length);
    document.getElementById("total-amount").textContent = formatAmount(totalAmount);
    document.getElementById("max-expense").textContent = formatAmount(maxAmount);
    document.getElementById("avg-expense").textContent = formatAmount(avgAmount);
    setCategoryInsights(items);
}

function applyClientFilters(expenses) {
    const noteTerm = filterNote.value.trim().toLowerCase();
    const sortValue = sortBy.value;
    const filtered = expenses.filter((expense) => {
        if (!noteTerm) return true;
        return (expense.note || "").toLowerCase().includes(noteTerm);
    });

    filtered.sort((a, b) => {
        if (sortValue === "amount-asc") return a.amount - b.amount;
        if (sortValue === "amount-desc") return b.amount - a.amount;
        if (sortValue === "date-asc") return a.date.localeCompare(b.date);
        return b.date.localeCompare(a.date);
    });

    return filtered;
}

function renderExpenses() {
    const items = applyClientFilters(state.expenses);
    updateSummary(items);
    filterStatus.textContent = `Showing ${items.length} expense(s) from ${state.expenses.length} fetched record(s).`;

    if (items.length === 0) {
        expensesTableBody.innerHTML = `<tr><td colspan="5">No expenses found for this view.</td></tr>`;
        return;
    }

    expensesTableBody.innerHTML = items.map((expense) => `
        <tr>
            <td>${formatAmount(expense.amount)}</td>
            <td><span class="chip">${formatCategory(expense.category)}</span></td>
            <td>${expense.date}</td>
            <td>${expense.note || "-"}</td>
            <td>
                <button type="button" class="btn btn-tertiary action-btn" data-action="edit" data-id="${expense.id}">Edit</button>
                <button type="button" class="btn danger action-btn" data-action="delete" data-id="${expense.id}">Delete</button>
            </td>
        </tr>
    `).join("");
}

async function fetchExpenses() {
    const category = filterCategory.value;
    const path = category ? `/expenses?category=${encodeURIComponent(category)}` : "/expenses";
    state.expenses = await apiRequest(path);
    renderExpenses();
}

function setEditMode(expense) {
    state.editingId = expense.id;
    expenseFormTitle.textContent = "Update Expense";
    expenseSubmit.textContent = "Update Expense";
    cancelEditButton.classList.remove("hidden");
    document.getElementById("expense-amount").value = expense.amount;
    document.getElementById("expense-category").value = expense.category;
    document.getElementById("expense-date").value = expense.date;
    document.getElementById("expense-note").value = expense.note || "";
}

function resetExpenseForm() {
    state.editingId = null;
    expenseFormTitle.textContent = "Add Expense";
    expenseSubmit.textContent = "Add Expense";
    cancelEditButton.classList.add("hidden");
    expenseForm.reset();
    document.getElementById("expense-date").value = new Date().toISOString().slice(0, 10);
}

function showDashboard() {
    authScreen.classList.add("hidden");
    dashboardScreen.classList.remove("hidden");
}

function showAuth() {
    dashboardScreen.classList.add("hidden");
    authScreen.classList.remove("hidden");
}

function saveToken(token) {
    state.token = token;
    localStorage.setItem(AUTH_TOKEN_KEY, token);
}

function logout() {
    state.token = "";
    localStorage.removeItem(AUTH_TOKEN_KEY);
    showAuth();
    resetExpenseForm();
    setMessage(dashboardMessage, "");
}

function switchAuthTab(tab) {
    const loginActive = tab === "login";
    loginTab.classList.toggle("active", loginActive);
    registerTab.classList.toggle("active", !loginActive);
    loginForm.classList.toggle("hidden", !loginActive);
    registerForm.classList.toggle("hidden", loginActive);
    setMessage(authMessage, "");
}

function setupPasswordToggles() {
    document.querySelectorAll("[data-toggle-password]").forEach((button) => {
        button.addEventListener("click", () => {
            const input = document.getElementById(button.dataset.togglePassword);
            if (!input) return;
            const isHidden = input.type === "password";
            input.type = isHidden ? "text" : "password";
            button.textContent = isHidden ? "Hide" : "Show";
        });
    });
}

function calculatePasswordStrength(value) {
    let score = 0;
    if (value.length >= 8) score += 1;
    if (/[A-Z]/.test(value) && /[a-z]/.test(value)) score += 1;
    if (/\d/.test(value)) score += 1;
    if (/[^A-Za-z0-9]/.test(value)) score += 1;
    return score;
}

function updatePasswordStrength() {
    const strength = calculatePasswordStrength(registerPasswordInput.value);
    const percentages = ["0%", "25%", "50%", "75%", "100%"];
    const labels = ["Very weak", "Weak", "Fair", "Strong", "Very strong"];
    const colors = ["#ef4444", "#f97316", "#f59e0b", "#22c55e", "#16a34a"];

    registerStrengthBar.style.width = percentages[strength];
    registerStrengthBar.style.backgroundColor = colors[strength];
    registerStrengthText.textContent = `Password strength: ${labels[strength]}.`;
}

function animatePromoCounters() {
    const counters = [
        { id: "promo-count-1", target: 5000, suffix: "+" },
        { id: "promo-count-2", target: categories.length, suffix: "" },
        { id: "promo-count-3", target: 8, suffix: "+" }
    ];

    counters.forEach(({ id, target, suffix }) => {
        const element = document.getElementById(id);
        if (!element) return;
        let value = 0;
        const step = Math.max(1, Math.ceil(target / 45));
        const interval = setInterval(() => {
            value = Math.min(target, value + step);
            element.textContent = `${value}${suffix}`;
            if (value >= target) {
                clearInterval(interval);
            }
        }, 20);
    });
}

loginTab.addEventListener("click", () => switchAuthTab("login"));
registerTab.addEventListener("click", () => switchAuthTab("register"));
promoRegisterButton.addEventListener("click", () => switchAuthTab("register"));

loginForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    clearMessages();
    const payload = {
        email: document.getElementById("login-email").value.trim(),
        password: document.getElementById("login-password").value
    };
    try {
        const data = await apiRequest("/auth/login", {
            method: "POST",
            body: JSON.stringify(payload)
        }, true);
        saveToken(data.token);
        showDashboard();
        await fetchExpenses();
        setMessage(dashboardMessage, "Welcome back!");
        loginForm.reset();
    } catch (error) {
        setMessage(authMessage, error.message, true);
    }
});

registerForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    clearMessages();
    const payload = {
        username: document.getElementById("register-username").value.trim(),
        email: document.getElementById("register-email").value.trim(),
        password: document.getElementById("register-password").value
    };
    try {
        await apiRequest("/auth/register", {
            method: "POST",
            body: JSON.stringify(payload)
        }, true);
        setMessage(authMessage, "Account created successfully. Please login.");
        switchAuthTab("login");
        registerForm.reset();
    } catch (error) {
        setMessage(authMessage, error.message, true);
    }
});

expenseForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    clearMessages();
    const payload = {
        amount: Number(document.getElementById("expense-amount").value),
        category: document.getElementById("expense-category").value,
        date: document.getElementById("expense-date").value,
        note: document.getElementById("expense-note").value.trim()
    };
    const editMode = state.editingId !== null;
    const path = editMode ? `/expenses/${state.editingId}` : "/expenses";
    const method = editMode ? "PUT" : "POST";
    try {
        await apiRequest(path, { method, body: JSON.stringify(payload) });
        resetExpenseForm();
        await fetchExpenses();
        setMessage(dashboardMessage, editMode ? "Expense updated." : "Expense added.");
    } catch (error) {
        setMessage(dashboardMessage, error.message, true);
    }
});

cancelEditButton.addEventListener("click", () => {
    resetExpenseForm();
    setMessage(dashboardMessage, "Edit cancelled.");
});

logoutButton.addEventListener("click", () => {
    logout();
    setMessage(authMessage, "You have been logged out.");
});

resetFilters.addEventListener("click", async () => {
    filterCategory.value = "";
    filterNote.value = "";
    sortBy.value = "date-desc";
    try {
        await fetchExpenses();
    } catch (error) {
        setMessage(dashboardMessage, error.message, true);
    }
});

filterCategory.addEventListener("change", async () => {
    try {
        await fetchExpenses();
    } catch (error) {
        setMessage(dashboardMessage, error.message, true);
    }
});

filterNote.addEventListener("input", renderExpenses);
sortBy.addEventListener("change", renderExpenses);
registerPasswordInput.addEventListener("input", updatePasswordStrength);

expensesTableBody.addEventListener("click", async (event) => {
    const target = event.target;
    if (!(target instanceof HTMLButtonElement)) return;

    const id = Number(target.dataset.id);
    const action = target.dataset.action;
    const expense = state.expenses.find((item) => item.id === id);
    if (!expense) return;

    if (action === "edit") {
        setEditMode(expense);
        setMessage(dashboardMessage, "Editing selected expense.");
    }

    if (action === "delete") {
        const confirmDelete = window.confirm("Delete this expense permanently?");
        if (!confirmDelete) return;
        try {
            await apiRequest(`/expenses/${id}`, { method: "DELETE" });
            await fetchExpenses();
            if (state.editingId === id) {
                resetExpenseForm();
            }
            setMessage(dashboardMessage, "Expense deleted.");
        } catch (error) {
            setMessage(dashboardMessage, error.message, true);
        }
    }
});

fillCategoryOptions();
resetExpenseForm();
switchAuthTab("login");
setGreeting();
setupPasswordToggles();
updatePasswordStrength();
animatePromoCounters();

if (state.token) {
    showDashboard();
    fetchExpenses().catch((error) => {
        setMessage(dashboardMessage, error.message, true);
    });
} else {
    showAuth();
}
