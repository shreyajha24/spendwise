const AUTH_TOKEN_KEY = "spendwise_token";
const USER_NAME_KEY = "spendwise_user_name";
const BUDGET_KEY = "spendwise_budget";
const CATEGORY_BUDGETS_KEY = "spendwise_category_budgets";
const FAVORITES_KEY = "spendwise_favorite_templates";
const DEMO_MODE_KEY = "spendwise_demo_mode";
const DEMO_EXPENSES_KEY = "spendwise_demo_expenses";
const API_BASE_OVERRIDE = (window.SPENDWISE_API_BASE || "").trim();
const API_BASE_URL = API_BASE_OVERRIDE
    ? API_BASE_OVERRIDE.replace(/\/+$/, "")
    : (window.location.port && window.location.port !== "8080" ? "http://localhost:8080" : "");

const CATEGORY_MAP = {
    FOOD: { emoji: "🍕", label: "Food", color: "#ef4444" },
    TRANSPORT: { emoji: "🚗", label: "Transport", color: "#f59e0b" },
    SHOPPING: { emoji: "🛍️", label: "Shopping", color: "#ec4899" },
    BILLS: { emoji: "📄", label: "Bills", color: "#3b82f6" },
    HOUSING: { emoji: "🏠", label: "Housing", color: "#8b5cf6" },
    HEALTH: { emoji: "💊", label: "Health", color: "#10b981" },
    ENTERTAINMENT: { emoji: "🎮", label: "Fun", color: "#06b6d4" },
    OTHER: { emoji: "📦", label: "Other", color: "#64748b" }
};

const INITIAL_DEMO_EXPENSES = [
    { id: 101, amount: 450, category: "FOOD", date: new Date().toISOString().slice(0, 10), note: "Dinner with friends" },
    { id: 102, amount: 1200, category: "SHOPPING", date: new Date().toISOString().slice(0, 10), note: "New sneakers" },
    { id: 103, amount: 2500, category: "BILLS", date: new Date(Date.now() - 86400000).toISOString().slice(0, 10), note: "Electricity Bill" },
    { id: 104, amount: 350, category: "TRANSPORT", date: new Date(Date.now() - 172800000).toISOString().slice(0, 10), note: "Uber ride" },
    { id: 105, amount: 1800, category: "HEALTH", date: new Date(Date.now() - 259200000).toISOString().slice(0, 10), note: "Pharmacy supplies" }
];

const QUICK_TEMPLATE_DEFINITIONS = [
    { templateKey: "bus", label: "Bus", category: "TRANSPORT", amount: 60, defaultNote: "Bus fare" },
    { templateKey: "coffee", label: "Coffee", category: "FOOD", amount: 150, defaultNote: "Coffee" },
    { templateKey: "lunch", label: "Lunch", category: "FOOD", amount: 250, defaultNote: "Lunch" },
    { templateKey: "breakfast", label: "Breakfast", category: "FOOD", amount: 180, defaultNote: "Breakfast" },
    { templateKey: "snack", label: "Snack", category: "FOOD", amount: 120, defaultNote: "Snack" }
];

const state = {
    token: localStorage.getItem(AUTH_TOKEN_KEY) || "",
    username: localStorage.getItem(USER_NAME_KEY) || "Alex",
    isDemo: localStorage.getItem(DEMO_MODE_KEY) === "true",
    budget: Number(localStorage.getItem(BUDGET_KEY)) || 30000,
    selectedCategory: "FOOD",
    expenses: [],
    editingId: null,
    pagination: {
        page: 0,
        size: 10,
        totalElements: 0,
        totalPages: 0,
        first: true,
        last: true
    },
    quickTemplates: [],
    noteDebounceTimer: null,
    overviewItems: [],
    categoryBudgets: JSON.parse(localStorage.getItem(CATEGORY_BUDGETS_KEY) || "{}"),
    favorites: JSON.parse(localStorage.getItem(FAVORITES_KEY) || "[]"),
    favoritesOnly: false,
    lastAddedExpense: null,
    undoTimer: null
};

// DOM Elements
const authScreen = document.getElementById("auth-screen");
const dashboardScreen = document.getElementById("dashboard-screen");
const authMessage = document.getElementById("auth-message");
const dashboardMessage = document.getElementById("dashboard-message");

const loginForm = document.getElementById("login-form");
const registerForm = document.getElementById("register-form");
const loginTab = document.getElementById("login-tab");
const registerTab = document.getElementById("register-tab");
const promoRegisterButton = document.getElementById("promo-register-button");
const demoModeButton = document.getElementById("demo-mode-button");

const userGreetingName = document.getElementById("user-greeting-name");
const dashboardGreeting = document.getElementById("dashboard-greeting");
const userAvatar = document.getElementById("user-avatar");
const headerBudgetText = document.getElementById("header-budget-text");
const logoutButton = document.getElementById("logout-button");

const registerStrengthBar = document.getElementById("register-strength-bar");
const registerStrengthText = document.getElementById("register-strength-text");
const registerPasswordInput = document.getElementById("register-password");

const expenseForm = document.getElementById("expense-form");
const expenseFormTitle = document.getElementById("expense-form-title");
const expenseSubmit = document.getElementById("expense-submit");
const cancelEditButton = document.getElementById("cancel-edit");
const categoryGrid = document.getElementById("category-grid");
const expenseCategoryInput = document.getElementById("expense-category");
const templateChipList = document.getElementById("template-chip-list");
const templateStatus = document.getElementById("template-status");

const totalAmountEl = document.getElementById("total-amount");
const totalExpensesEl = document.getElementById("total-expenses");
const maxExpenseEl = document.getElementById("max-expense");
const avgExpenseEl = document.getElementById("avg-expense");
const categoryInsightsEl = document.getElementById("category-insights");
const smartInsightsEl = document.getElementById("smart-insights");

const filterCategory = document.getElementById("filter-category");
const filterNote = document.getElementById("filter-note");
const sortBy = document.getElementById("sort-by");
const resetFilters = document.getElementById("reset-filters");
const expensesTableBody = document.getElementById("expenses-table-body");
const filterStatus = document.getElementById("filter-status");

const prevPageButton = document.getElementById("prev-page");
const nextPageButton = document.getElementById("next-page");
const pageIndicator = document.getElementById("page-indicator");
const pageSize = document.getElementById("page-size");

const budgetModal = document.getElementById("budget-modal");
const budgetEditTrigger = document.getElementById("budget-edit-trigger");
const budgetInputVal = document.getElementById("budget-input-val");
const saveBudgetBtn = document.getElementById("save-budget-btn");
const closeBudgetBtn = document.getElementById("close-budget-btn");
const exportCsvBtn = document.getElementById("export-csv-button");
const exportPdfBtn = document.getElementById("export-pdf-button");
const trendChartEl = document.getElementById("spending-trend-chart");
const monthlyComparisonEl = document.getElementById("monthly-comparison");
const categoryBudgetsEl = document.getElementById("category-budgets");
const saveCategoryBudgetsBtn = document.getElementById("save-category-budgets");
const noteSuggestionsEl = document.getElementById("note-suggestions");
const entrySuggestionsEl = document.getElementById("smart-entry-suggestions");
const emptyStateEl = document.getElementById("empty-state");
const emptyAddButton = document.getElementById("empty-add-button");
const showFavoritesButton = document.getElementById("show-favorites-button");

function setMessage(element, text, isError = false) {
    if (!element) return;
    element.textContent = text || "";
    element.classList.toggle("error", isError);
}

function clearMessages() {
    setMessage(authMessage, "");
    setMessage(dashboardMessage, "");
}

function formatINR(amount) {
    return new Intl.NumberFormat("en-IN", {
        style: "currency",
        currency: "INR",
        maximumFractionDigits: 2
    }).format(amount || 0);
}

function setGreeting() {
    const hour = new Date().getHours();
    let text = "Good day";
    if (hour < 12) text = "Good morning";
    else if (hour < 18) text = "Good afternoon";
    else text = "Good evening";

    if (dashboardGreeting) dashboardGreeting.textContent = text;
    if (userGreetingName) userGreetingName.textContent = `Welcome, ${state.username}`;
    if (userAvatar) userAvatar.textContent = (state.username[0] || "S").toUpperCase();
}

function fillCategoryFilterOptions() {
    if (!filterCategory) return;
    filterCategory.innerHTML = [
        `<option value="">All Categories</option>`,
        ...Object.keys(CATEGORY_MAP).map(key => {
            const cat = CATEGORY_MAP[key];
            return `<option value="${key}">${cat.emoji} ${cat.label}</option>`;
        })
    ].join("");
}

function formatCategoryLabel(catKey) {
    const mapVal = CATEGORY_MAP[catKey];
    if (mapVal?.label) return mapVal.label;
    return String(catKey || "Other")
        .toLowerCase()
        .replace(/_/g, " ")
        .replace(/\b\w/g, (ch) => ch.toUpperCase());
}

function getQuickTemplatesPayload() {
    return QUICK_TEMPLATE_DEFINITIONS.map((template) => ({ ...template }));
}

function buildWeeklyInsightsFromItems(items) {
    const now = new Date();
    const day = now.getDay();
    const mondayOffset = day === 0 ? -6 : 1 - day;
    const weekStartDate = new Date(now);
    weekStartDate.setDate(now.getDate() + mondayOffset);
    weekStartDate.setHours(0, 0, 0, 0);
    const weekEndDate = new Date(weekStartDate);
    weekEndDate.setDate(weekStartDate.getDate() + 6);

    const startIso = weekStartDate.toISOString().slice(0, 10);
    const endIso = weekEndDate.toISOString().slice(0, 10);

    const weeklyItems = items.filter((item) => item.date >= startIso && item.date <= endIso);
    const totalSpent = weeklyItems.reduce((sum, item) => sum + Number(item.amount || 0), 0);
    const totalTransactions = weeklyItems.length;
    const averageExpense = totalTransactions === 0 ? 0 : totalSpent / totalTransactions;

    const totalsByCategory = weeklyItems.reduce((acc, item) => {
        const key = item.category || "OTHER";
        acc[key] = (acc[key] || 0) + Number(item.amount || 0);
        return acc;
    }, {});

    const categoryBreakdown = Object.entries(totalsByCategory)
        .map(([category, amount]) => ({
            category,
            amount,
            percentage: totalSpent === 0 ? 0 : Math.round((amount * 10000) / totalSpent) / 100
        }))
        .sort((a, b) => b.amount - a.amount);

    const top = categoryBreakdown[0];
    const summary = top
        ? `You spent ${formatINR(top.amount)} on ${formatCategoryLabel(top.category)} this week (${top.percentage}% of weekly spending).`
        : "No expenses logged this week yet. Use Quick Templates to add daily expenses in one tap.";

    return {
        weekStartDate: startIso,
        weekEndDate: endIso,
        totalSpent,
        totalTransactions,
        averageExpense,
        topCategory: top ? top.category : null,
        topCategorySpent: top ? top.amount : 0,
        categoryBreakdown,
        summary
    };
}

function renderQuickTemplates(templates) {
    if (!templateChipList) return;
    if (!Array.isArray(templates) || templates.length === 0) {
        templateChipList.innerHTML = `<p class="muted">No templates available.</p>`;
        return;
    }

    templateChipList.innerHTML = templates.map((template) => `
        <button type="button" class="template-chip" data-template-key="${template.templateKey}">
            ${template.label} · ${formatINR(template.amount)}
        </button>
    `).join("");
}

function renderQuickTemplates(templates) {
    if (!templateChipList) return;
    if (!Array.isArray(templates) || templates.length === 0) {
        templateChipList.innerHTML = `<p class="muted">No templates available.</p>`;
        return;
    }
    const visible = state.favoritesOnly ? templates.filter(t => state.favorites.includes(t.templateKey)) : templates;
    templateChipList.innerHTML = visible.map((template) => `
        <button type="button" class="template-chip" data-template-key="${template.templateKey}">${template.label} · ${formatINR(template.amount)}</button>
        <button type="button" class="favorite-template ${state.favorites.includes(template.templateKey) ? "is-favorite" : ""}" data-favorite-key="${template.templateKey}" aria-label="Favorite ${template.label}">★</button>
    `).join("");
    if (!visible.length) templateChipList.innerHTML = `<p class="muted">No favorite templates yet. Tap a star to save one.</p>`;
    if (showFavoritesButton) showFavoritesButton.textContent = state.favoritesOnly ? "Show all" : "Favorites only";
}

function renderSmartInsights(payload) {
    if (!smartInsightsEl) return;
    const breakdown = Array.isArray(payload?.categoryBreakdown) ? payload.categoryBreakdown.slice(0, 3) : [];
    const summary = payload?.summary || "No insights available yet.";
    const topCategoryLabel = payload?.topCategory ? formatCategoryLabel(payload.topCategory) : "-";

    smartInsightsEl.innerHTML = `
        <p class="insight-summary">${summary}</p>
        <div class="smart-insight-metrics">
            <span class="metric-pill"><strong>${formatINR(payload?.totalSpent || 0)}</strong><em>Total this week</em></span>
            <span class="metric-pill"><strong>${payload?.totalTransactions || 0}</strong><em>Transactions</em></span>
            <span class="metric-pill"><strong>${formatINR(payload?.averageExpense || 0)}</strong><em>Avg expense</em></span>
            <span class="metric-pill"><strong>${topCategoryLabel}</strong><em>Top category</em></span>
        </div>
        <div class="smart-insight-top">
            ${breakdown.map((item) => `
                <div class="smart-row">
                    <span>${formatCategoryLabel(item.category)}</span>
                    <span>${formatINR(item.amount)} (${item.percentage}%)</span>
                </div>
            `).join("")}
        </div>
    `;
}

async function fetchQuickTemplates() {
    try {
        const templates = state.isDemo ? getQuickTemplatesPayload() : await apiRequest("/expenses/templates");
        state.quickTemplates = Array.isArray(templates) ? templates : [];
        renderQuickTemplates(state.quickTemplates);
        if (templateStatus) templateStatus.textContent = "Tap any template to add an expense instantly.";
    } catch (err) {
        if (templateStatus) templateStatus.textContent = "Unable to load templates.";
        setMessage(dashboardMessage, err.message, true);
    }
}

async function applyQuickTemplate(templateKey) {
    if (!templateKey) return;
    try {
        const created = await apiRequest(`/expenses/templates/${encodeURIComponent(templateKey)}`, { method: "POST" });
        await fetchExpenses(0);
        showUndo(created, "Template expense added.");
    } catch (err) {
        setMessage(dashboardMessage, err.message, true);
    }
}

async function fetchWeeklyInsights() {
    try {
        const payload = state.isDemo
            ? buildWeeklyInsightsFromItems(getDemoExpenses())
            : await apiRequest("/expenses/insights/weekly");
        renderSmartInsights(payload);
    } catch (err) {
        if (smartInsightsEl) {
            smartInsightsEl.innerHTML = `<p class="muted">Could not load smart insights right now.</p>`;
        }
        setMessage(dashboardMessage, err.message, true);
    }
}

// Local Storage Helper for Demo Mode
function getDemoExpenses() {
    const stored = localStorage.getItem(DEMO_EXPENSES_KEY);
    if (!stored) {
        localStorage.setItem(DEMO_EXPENSES_KEY, JSON.stringify(INITIAL_DEMO_EXPENSES));
        return [...INITIAL_DEMO_EXPENSES];
    }
    try {
        return JSON.parse(stored);
    } catch {
        return [...INITIAL_DEMO_EXPENSES];
    }
}

function saveDemoExpenses(items) {
    localStorage.setItem(DEMO_EXPENSES_KEY, JSON.stringify(items));
}

// API Call Wrapper with local fallback
async function apiRequest(path, options = {}, allowUnauthorized = false) {
    if (state.isDemo) {
        return handleDemoRequest(path, options);
    }

    const isAbsoluteUrl = /^https?:\/\//i.test(path);
    const resolvedPath = isAbsoluteUrl ? path : (path.startsWith("/") ? path : `/${path}`);
    const requestUrl = isAbsoluteUrl
        ? path
        : (API_BASE_URL ? `${API_BASE_URL}${resolvedPath}` : resolvedPath);

    const headers = { "Content-Type": "application/json", ...(options.headers || {}) };
    if (state.token) {
        headers.Authorization = `Bearer ${state.token}`;
    }

    try {
        const response = await fetch(requestUrl, { ...options, headers });
        if (response.status === 401 && !allowUnauthorized) {
            logout();
            throw new Error("Session expired. Please log in again.");
        }
        if (response.status === 204) return null;

        const payload = await response.json().catch(() => null);
        if (!response.ok) {
            throw new Error(payload?.message || payload?.errors?.[0] || "Request failed.");
        }
        return payload;
    } catch (err) {
        // Fall back to demo mode gracefully if server is unreachable
        if (err.message.includes("Failed to fetch") || err.message.includes("NetworkError")) {
            console.warn("API server unreachable. Switching to local interactive demo mode.");
            state.isDemo = true;
            localStorage.setItem(DEMO_MODE_KEY, "true");
            return handleDemoRequest(path, options);
        }
        throw err;
    }
}

function handleDemoRequest(path, options) {
    const method = options.method || "GET";
    let items = getDemoExpenses();

    if (path === "/expenses/templates" && method === "GET") {
        return Promise.resolve(getQuickTemplatesPayload());
    }

    if (path.startsWith("/expenses/templates/") && method === "POST") {
        const templateKey = decodeURIComponent(path.split("/")[3] || "").toLowerCase();
        const template = QUICK_TEMPLATE_DEFINITIONS.find((item) => item.templateKey === templateKey);
        if (!template) {
            return Promise.reject(new Error("Invalid template key."));
        }
        const newItem = {
            id: Date.now(),
            amount: template.amount,
            category: template.category,
            date: new Date().toISOString().slice(0, 10),
            note: template.defaultNote
        };
        items.unshift(newItem);
        saveDemoExpenses(items);
        return Promise.resolve(newItem);
    }

    if (path === "/expenses/insights/weekly" && method === "GET") {
        return Promise.resolve(buildWeeklyInsightsFromItems(items));
    }

    if (path.startsWith("/expenses")) {
        if (method === "GET") {
            const urlObj = new URL("http://dummy" + path);
            const category = urlObj.searchParams.get("category");
            const note = urlObj.searchParams.get("note");
            const sortByVal = urlObj.searchParams.get("sortBy") || "date";
            const dir = urlObj.searchParams.get("direction") || "desc";
            const page = Number(urlObj.searchParams.get("page") || 0);
            const size = Number(urlObj.searchParams.get("size") || 10);

            let filtered = [...items];
            if (category) filtered = filtered.filter(i => i.category === category);
            if (note) filtered = filtered.filter(i => i.note.toLowerCase().includes(note.toLowerCase()));

            filtered.sort((a, b) => {
                let valA = a[sortByVal];
                let valB = b[sortByVal];
                if (sortByVal === "amount") {
                    valA = Number(valA);
                    valB = Number(valB);
                }
                if (dir === "desc") return valA < valB ? 1 : -1;
                return valA > valB ? 1 : -1;
            });

            const totalElements = filtered.length;
            const totalPages = Math.ceil(totalElements / size) || 1;
            const start = page * size;
            const content = filtered.slice(start, start + size);

            return Promise.resolve({
                content,
                page,
                size,
                totalElements,
                totalPages,
                first: page === 0,
                last: page >= totalPages - 1
            });
        }

        if (method === "POST") {
            const body = JSON.parse(options.body);
            const newItem = { id: Date.now(), ...body };
            items.unshift(newItem);
            saveDemoExpenses(items);
            return Promise.resolve(newItem);
        }

        if (method === "PUT") {
            const id = Number(path.split("/")[2]);
            const body = JSON.parse(options.body);
            items = items.map(i => i.id === id ? { ...i, ...body } : i);
            saveDemoExpenses(items);
            return Promise.resolve({ id, ...body });
        }

        if (method === "DELETE") {
            const id = Number(path.split("/")[2]);
            items = items.filter(i => i.id !== id);
            saveDemoExpenses(items);
            return Promise.resolve(null);
        }
    }

    if (path === "/auth/login") {
        return Promise.resolve({ token: "demo-jwt-token-xyz", username: "Demo User" });
    }

    if (path === "/auth/register") {
        return Promise.resolve({ message: "Registered successfully" });
    }

    return Promise.resolve({});
}

// Budget Gauge Ring & Insights Update
function updateBudgetRingAndOverview(allItems) {
    const totalSpent = allItems.reduce((sum, i) => sum + Number(i.amount), 0);
    const budget = state.budget;
    const remaining = Math.max(0, budget - totalSpent);
    const pct = Math.min(100, Math.round((totalSpent / budget) * 100));

    // Update Header Text & Modal Input
    if (headerBudgetText) headerBudgetText.textContent = formatINR(budget);
    if (budgetInputVal) budgetInputVal.value = budget;

    // SVG Ring Progress
    const ring = document.getElementById("budget-ring");
    const ringPercent = document.getElementById("ring-percent");
    const ringSpent = document.getElementById("ring-spent-amount");
    const ringRemaining = document.getElementById("ring-remaining-amount");
    const badge = document.getElementById("budget-status-badge");

    const totalCircumference = 364.42; // 2 * PI * 58
    const strokeOffset = totalCircumference * (1 - pct / 100);

    if (ring) {
        ring.style.strokeDashoffset = strokeOffset;
        if (pct > 85) {
            ring.style.stroke = "#ef4444";
        } else if (pct > 60) {
            ring.style.stroke = "#f59e0b";
        } else {
            ring.style.stroke = "#6366f1";
        }
    }

    if (ringPercent) ringPercent.textContent = `${pct}%`;
    if (ringSpent) ringSpent.textContent = formatINR(totalSpent);
    if (ringRemaining) ringRemaining.textContent = formatINR(remaining);

    if (badge) {
        if (pct >= 100) {
            badge.textContent = "Over Budget!";
            badge.className = "badge-status danger";
        } else if (pct >= 80) {
            badge.textContent = "Near Limit";
            badge.className = "badge-status warning";
        } else {
            badge.textContent = "On Track";
            badge.className = "badge-status";
        }
    }

    // KPI Cards
    const maxVal = allItems.length > 0 ? Math.max(...allItems.map(i => Number(i.amount))) : 0;
    const avgVal = allItems.length > 0 ? totalSpent / allItems.length : 0;

    if (totalAmountEl) totalAmountEl.textContent = formatINR(totalSpent);
    if (totalExpensesEl) totalExpensesEl.textContent = String(allItems.length);
    if (maxExpenseEl) maxExpenseEl.textContent = formatINR(maxVal);
    if (avgExpenseEl) avgExpenseEl.textContent = formatINR(avgVal);

    // Category Breakdown Progress Bars
    renderCategoryBreakdown(allItems, totalSpent);
}

function renderCategoryBreakdown(items, totalSpent) {
    if (!categoryInsightsEl) return;

    if (items.length === 0) {
        categoryInsightsEl.innerHTML = `<p class="muted">No spending recorded yet. Use Quick Add above!</p>`;
        return;
    }

    const catTotals = items.reduce((acc, item) => {
        acc[item.category] = (acc[item.category] || 0) + Number(item.amount);
        return acc;
    }, {});

    const sortedEntries = Object.entries(catTotals).sort((a, b) => b[1] - a[1]);

    categoryInsightsEl.innerHTML = sortedEntries.map(([key, catTotal]) => {
        const cat = CATEGORY_MAP[key] || { emoji: "📦", label: key, color: "#6366f1" };
        const sharePct = totalSpent > 0 ? Math.round((catTotal / totalSpent) * 100) : 0;

        return `
            <div class="insight-row">
                <div class="insight-meta">
                    <span>${cat.emoji} ${cat.label}</span>
                    <span>${formatINR(catTotal)} (${sharePct}%)</span>
                </div>
                <div class="insight-bar-track">
                    <div class="insight-bar-fill" style="width: ${sharePct}%; background: ${cat.color}"></div>
                </div>
            </div>
        `;
    }).join("");
}

function monthKey(date) { return String(date || "").slice(0, 7); }

function renderTrendAndComparison(items) {
    if (!trendChartEl || !monthlyComparisonEl) return;
    const now = new Date();
    const keys = Array.from({ length: 6 }, (_, index) => {
        const d = new Date(now.getFullYear(), now.getMonth() - (5 - index), 1);
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}`;
    });
    const totals = Object.fromEntries(keys.map(key => [key, 0]));
    items.forEach(item => { const key = monthKey(item.date); if (key in totals) totals[key] += Number(item.amount || 0); });
    const max = Math.max(...Object.values(totals), 1);
    trendChartEl.innerHTML = keys.map(key => {
        const date = new Date(`${key}-01T00:00:00`);
        const amount = totals[key];
        return `<div class="trend-bar-wrap" title="${date.toLocaleString("en-IN", { month: "long", year: "numeric" })}: ${formatINR(amount)}"><span class="trend-value">${amount ? formatINR(amount) : ""}</span><div class="trend-bar" style="height:${Math.max(5, Math.round(amount / max * 100))}%"></div><span>${date.toLocaleString("en-IN", { month: "short" })}</span></div>`;
    }).join("");
    const current = totals[keys[5]], previous = totals[keys[4]];
    if (!previous) monthlyComparisonEl.textContent = current ? "No spend recorded last month" : "Add an expense to start your trend";
    else {
        const change = Math.round(((current - previous) / previous) * 100);
        monthlyComparisonEl.textContent = `${change >= 0 ? "↑" : "↓"}${Math.abs(change)}% vs last month`;
        monthlyComparisonEl.className = `comparison-text ${change > 0 ? "up" : "down"}`;
    }
}

function renderCategoryBudgets(items) {
    if (!categoryBudgetsEl) return;
    const currentKey = monthKey(new Date().toISOString());
    const totals = items.filter(item => monthKey(item.date) === currentKey).reduce((acc, item) => {
        acc[item.category] = (acc[item.category] || 0) + Number(item.amount || 0); return acc;
    }, {});
    categoryBudgetsEl.innerHTML = Object.entries(CATEGORY_MAP).map(([key, cat]) => {
        const limit = Number(state.categoryBudgets[key] || 0), spent = totals[key] || 0;
        const pct = limit ? Math.min(100, Math.round(spent / limit * 100)) : 0;
        return `<div class="category-budget-row"><span>${cat.emoji} ${cat.label}</span><div class="category-budget-control"><input type="number" min="0" step="100" data-category-budget="${key}" value="${limit || ""}" placeholder="No limit" aria-label="${cat.label} budget"><small>${formatINR(spent)} spent${limit ? ` · ${pct}%` : ""}</small></div></div>`;
    }).join("");
}

function renderEntrySuggestions(items) {
    if (!noteSuggestionsEl || !entrySuggestionsEl) return;
    const uniqueNotes = [...new Set(items.map(i => (i.note || "").trim()).filter(Boolean))].slice(0, 8);
    noteSuggestionsEl.innerHTML = uniqueNotes.map(note => `<option value="${note.replace(/"/g, "&quot;")}"></option>`).join("");
    const category = state.selectedCategory;
    const recent = items.filter(i => i.category === category && i.note).slice(0, 3);
    entrySuggestionsEl.innerHTML = recent.length ? `Suggestions: ${recent.map(i => `<button type="button" data-note-suggestion="${(i.note || "").replace(/"/g, "&quot;")}">${i.note}</button>`).join("")}` : "";
}

function showUndo(expense, message) {
    if (!expense?.id) { setMessage(dashboardMessage, message); return; }
    state.lastAddedExpense = expense;
    clearTimeout(state.undoTimer);
    dashboardMessage.innerHTML = `${message} <button type="button" class="undo-button" id="undo-add-button">Undo</button>`;
    state.undoTimer = setTimeout(() => setMessage(dashboardMessage, ""), 10000);
    document.getElementById("undo-add-button")?.addEventListener("click", async () => {
        try { await apiRequest(`/expenses/${expense.id}`, { method: "DELETE" }); await fetchExpenses(0); setMessage(dashboardMessage, "Expense removed."); }
        catch (err) { setMessage(dashboardMessage, err.message, true); }
    }, { once: true });
}

// Transactions Table Rendering
function renderExpensesTable() {
    const items = state.expenses;
    const { page, totalPages, totalElements } = state.pagination;
    const currentPage = totalPages === 0 ? 0 : page + 1;

    if (filterStatus) {
        filterStatus.textContent = `Showing ${items.length} of ${totalElements} records (Page ${currentPage} of ${totalPages}).`;
    }

    if (pageIndicator) pageIndicator.textContent = `Page ${currentPage} of ${totalPages}`;
    if (prevPageButton) prevPageButton.disabled = state.pagination.first;
    if (nextPageButton) nextPageButton.disabled = state.pagination.last;
    if (pageSize) pageSize.value = String(state.pagination.size);

    if (items.length === 0) {
        expensesTableBody.innerHTML = `<tr><td colspan="5" style="text-align: center; padding: 2rem; color: #94a3b8;">No matching expenses found.</td></tr>`;
        return;
    }

    expensesTableBody.innerHTML = items.map(expense => {
        const cat = CATEGORY_MAP[expense.category] || { emoji: "📦", label: expense.category };
        return `
            <tr>
                <td>
                    <span class="chip-cat">
                        <span>${cat.emoji}</span>
                        <span>${cat.label}</span>
                    </span>
                </td>
                <td class="amount-text">${formatINR(expense.amount)}</td>
                <td>${expense.date}</td>
                <td>${expense.note || "-"}</td>
                <td>
                    <div class="action-btn-group">
                        <button type="button" class="btn-icon" data-action="edit" data-id="${expense.id}" title="Edit Expense" aria-label="Edit expense">
                            <i data-lucide="edit-3" style="width:16px;height:16px;"></i>
                            <span class="action-text">Edit</span>
                        </button>
                        <button type="button" class="btn-icon danger" data-action="delete" data-id="${expense.id}" title="Delete Expense" aria-label="Delete expense">
                            <i data-lucide="trash-2" style="width:16px;height:16px;"></i>
                            <span class="action-text">Delete</span>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join("");

    if (window.lucide) window.lucide.createIcons();
}

async function fetchExpenses(page = state.pagination.page) {
    const requestedPage = Math.max(0, Number(page) || 0);
    const params = new URLSearchParams();
    const category = filterCategory ? filterCategory.value : "";
    const note = filterNote ? filterNote.value.trim() : "";
    const [sortField, sortDirection] = (sortBy ? sortBy.value : "date-desc").split("-");

    if (category) params.set("category", category);
    if (note) params.set("note", note);
    params.set("sortBy", sortField || "date");
    params.set("direction", sortDirection || "desc");
    params.set("page", String(requestedPage));
    params.set("size", String(state.pagination.size));

    const payload = await apiRequest(`/expenses?${params.toString()}`);
    state.expenses = payload.content || [];
    state.pagination = {
        page: payload.page,
        size: payload.size,
        totalElements: payload.totalElements,
        totalPages: payload.totalPages,
        first: payload.first,
        last: payload.last
    };

    if (requestedPage > 0 && payload.totalElements > 0 && requestedPage >= payload.totalPages) {
        return fetchExpenses(Math.max(0, payload.totalPages - 1));
    }

    renderExpensesTable();

    const overviewItems = await fetchAllExpensesForOverview();
    state.overviewItems = overviewItems;
    updateBudgetRingAndOverview(overviewItems);
    renderTrendAndComparison(overviewItems);
    renderCategoryBudgets(overviewItems);
    renderEntrySuggestions(overviewItems);
    if (emptyStateEl) emptyStateEl.classList.toggle("hidden", overviewItems.length !== 0);
    await fetchWeeklyInsights();
}

async function fetchAllExpensesForOverview() {
    const maxAllowedPageSize = 100;
    let page = 0;
    let totalPages = 1;
    const allItems = [];

    while (page < totalPages) {
        const params = new URLSearchParams({
            page: String(page),
            size: String(maxAllowedPageSize),
            sortBy: "date",
            direction: "desc"
        });

        const payload = await apiRequest(`/expenses?${params.toString()}`);
        allItems.push(...(payload?.content || []));
        totalPages = Math.max(1, Number(payload?.totalPages) || 1);
        page += 1;
    }

    return allItems;
}

// Form & Quick Add Logic
function selectCategory(catKey) {
    state.selectedCategory = catKey;
    if (expenseCategoryInput) expenseCategoryInput.value = catKey;

    document.querySelectorAll(".category-btn").forEach(btn => {
        const isMatch = btn.dataset.cat === catKey;
        btn.classList.toggle("active", isMatch);
    });
    renderEntrySuggestions(state.overviewItems);
}

function suggestCategoryFromNote(note) {
    const value = String(note || "").toLowerCase();
    const rules = {
        FOOD: ["food", "lunch", "dinner", "coffee", "swiggy", "zomato", "restaurant", "grocery"],
        TRANSPORT: ["uber", "ola", "metro", "bus", "petrol", "fuel", "cab"],
        BILLS: ["electricity", "internet", "recharge", "bill", "wifi"],
        SHOPPING: ["amazon", "clothes", "shoes", "shopping"],
        HEALTH: ["doctor", "pharmacy", "medicine", "gym"]
    };
    const match = Object.entries(rules).find(([, words]) => words.some(word => value.includes(word)));
    if (match) selectCategory(match[0]);
}

function setupQuickAddEvents() {
    if (categoryGrid) {
        categoryGrid.addEventListener("click", (e) => {
            const btn = e.target.closest(".category-btn");
            if (btn && btn.dataset.cat) {
                selectCategory(btn.dataset.cat);
            }
        });
    }

    document.querySelectorAll(".amount-chip").forEach(chip => {
        chip.addEventListener("click", () => {
            const addVal = Number(chip.dataset.amount || 0);
            const input = document.getElementById("expense-amount");
            if (input) {
                const current = Number(input.value || 0);
                input.value = current + addVal;
            }
        });
    });
}

function setupTemplateEvents() {
    if (!templateChipList) return;
    templateChipList.addEventListener("click", (e) => {
        const favorite = e.target.closest("[data-favorite-key]");
        if (favorite) {
            const key = favorite.dataset.favoriteKey;
            state.favorites = state.favorites.includes(key) ? state.favorites.filter(k => k !== key) : [...state.favorites, key];
            localStorage.setItem(FAVORITES_KEY, JSON.stringify(state.favorites));
            renderQuickTemplates(state.quickTemplates);
            return;
        }
        const btn = e.target.closest("[data-template-key]");
        if (!btn) return;
        applyQuickTemplate(btn.dataset.templateKey);
    });
}

function setEditMode(expense) {
    state.editingId = expense.id;
    if (expenseFormTitle) expenseFormTitle.textContent = "✏️ Edit Expense";
    if (expenseSubmit) expenseSubmit.innerHTML = `<i data-lucide="check"></i> Update Expense`;
    if (cancelEditButton) cancelEditButton.classList.remove("hidden");

    selectCategory(expense.category);
    document.getElementById("expense-amount").value = expense.amount;
    document.getElementById("expense-date").value = expense.date;
    document.getElementById("expense-note").value = expense.note || "";
    if (window.lucide) window.lucide.createIcons();
}

function resetExpenseForm() {
    state.editingId = null;
    if (expenseFormTitle) expenseFormTitle.textContent = "⚡ Quick Add Expense";
    if (expenseSubmit) expenseSubmit.innerHTML = `<i data-lucide="plus-circle"></i> Add Expense`;
    if (cancelEditButton) cancelEditButton.classList.add("hidden");

    if (expenseForm) expenseForm.reset();
    selectCategory("FOOD");

    const dateInput = document.getElementById("expense-date");
    if (dateInput) dateInput.value = new Date().toISOString().slice(0, 10);
    if (window.lucide) window.lucide.createIcons();
}

function showDashboard() {
    if (authScreen) authScreen.classList.add("hidden");
    if (dashboardScreen) dashboardScreen.classList.remove("hidden");
    setGreeting();
    fetchQuickTemplates();
    if (window.lucide) window.lucide.createIcons();
}

function showAuth() {
    if (dashboardScreen) dashboardScreen.classList.add("hidden");
    if (authScreen) authScreen.classList.remove("hidden");
    if (window.lucide) window.lucide.createIcons();
}

function logout() {
    state.token = "";
    state.isDemo = false;
    localStorage.removeItem(AUTH_TOKEN_KEY);
    localStorage.removeItem(DEMO_MODE_KEY);
    showAuth();
    resetExpenseForm();
    setMessage(authMessage, "You have logged out.");
}

function switchAuthTab(tab) {
    const isLogin = tab === "login";
    if (loginTab) loginTab.classList.toggle("active", isLogin);
    if (registerTab) registerTab.classList.toggle("active", !isLogin);
    if (loginForm) loginForm.classList.toggle("hidden", !isLogin);
    if (registerForm) registerForm.classList.toggle("hidden", isLogin);
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

function updatePasswordStrength() {
    if (!registerPasswordInput || !registerStrengthBar || !registerStrengthText) return;
    const val = registerPasswordInput.value;
    let score = 0;
    if (val.length >= 6) score += 1;
    if (/[A-Z]/.test(val) && /[a-z]/.test(val)) score += 1;
    if (/\d/.test(val)) score += 1;
    if (/[^A-Za-z0-9]/.test(val)) score += 1;

    const pcts = ["0%", "25%", "50%", "75%", "100%"];
    const labels = ["Too short", "Weak", "Fair", "Strong", "Super Strong"];
    const colors = ["#ef4444", "#f97316", "#f59e0b", "#10b981", "#059669"];

    registerStrengthBar.style.width = pcts[score];
    registerStrengthBar.style.backgroundColor = colors[score];
    registerStrengthText.textContent = `Strength: ${labels[score]}`;
}

// CSV Export
function exportToCSV() {
    const items = state.expenses;
    if (items.length === 0) {
        alert("No expenses to export!");
        return;
    }

    const headers = ["ID", "Category", "Amount (INR)", "Date", "Note"];
    const rows = items.map(i => [
        i.id,
        i.category,
        i.amount,
        i.date,
        `"${(i.note || "").replace(/"/g, '""')}"`
    ]);

    const csvContent = "data:text/csv;charset=utf-8," + [headers.join(","), ...rows.map(r => r.join(","))].join("\n");
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", `SpendWise_Expenses_${new Date().toISOString().slice(0, 10)}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

// Event Listeners
document.addEventListener("DOMContentLoaded", () => {
    fillCategoryFilterOptions();
    resetExpenseForm();
    setupQuickAddEvents();
    setupTemplateEvents();
    setupPasswordToggles();
    if (window.lucide) window.lucide.createIcons();

    if (loginTab) loginTab.addEventListener("click", () => switchAuthTab("login"));
    if (registerTab) registerTab.addEventListener("click", () => switchAuthTab("register"));
    if (promoRegisterButton) promoRegisterButton.addEventListener("click", () => switchAuthTab("register"));

    if (demoModeButton) {
        demoModeButton.addEventListener("click", () => {
            state.isDemo = true;
            state.username = "Demo User";
            localStorage.setItem(DEMO_MODE_KEY, "true");
            localStorage.setItem(USER_NAME_KEY, "Demo User");
            showDashboard();
            fetchExpenses(0);
            setMessage(dashboardMessage, "⚡ Launched instant Demo Mode!");
        });
    }

    if (loginForm) {
        loginForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            clearMessages();
            const email = document.getElementById("login-email").value.trim();
            const password = document.getElementById("login-password").value;

            try {
                const data = await apiRequest("/auth/login", {
                    method: "POST",
                    body: JSON.stringify({ email, password })
                }, true);

                state.token = data.token || "token";
                state.username = data.username || email.split("@")[0] || "User";
                localStorage.setItem(AUTH_TOKEN_KEY, state.token);
                localStorage.setItem(USER_NAME_KEY, state.username);

                showDashboard();
                await fetchExpenses(0);
                setMessage(dashboardMessage, `Welcome back, ${state.username}!`);
                loginForm.reset();
            } catch (err) {
                setMessage(authMessage, err.message, true);
            }
        });
    }

    if (registerForm) {
        registerForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            clearMessages();
            const username = document.getElementById("register-username").value.trim();
            const email = document.getElementById("register-email").value.trim();
            const password = document.getElementById("register-password").value;

            try {
                await apiRequest("/auth/register", {
                    method: "POST",
                    body: JSON.stringify({ username, email, password })
                }, true);

                setMessage(authMessage, "Account created successfully! Please log in.");
                switchAuthTab("login");
                registerForm.reset();
            } catch (err) {
                setMessage(authMessage, err.message, true);
            }
        });
    }

    if (expenseForm) {
        expenseForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            clearMessages();

            const amount = Number(document.getElementById("expense-amount").value);
            const category = expenseCategoryInput ? expenseCategoryInput.value : state.selectedCategory;
            const date = document.getElementById("expense-date").value;
            const note = document.getElementById("expense-note").value.trim();

            const isEdit = state.editingId !== null;
            const path = isEdit ? `/expenses/${state.editingId}` : "/expenses";
            const method = isEdit ? "PUT" : "POST";

            try {
                const saved = await apiRequest(path, { method, body: JSON.stringify({ amount, category, date, note }) });
                resetExpenseForm();
                await fetchExpenses(0);
                if (isEdit) setMessage(dashboardMessage, "Expense updated successfully.");
                else showUndo(saved, "Expense added successfully.");
            } catch (err) {
                setMessage(dashboardMessage, err.message, true);
            }
        });
    }

    if (cancelEditButton) cancelEditButton.addEventListener("click", resetExpenseForm);
    if (logoutButton) logoutButton.addEventListener("click", logout);
    if (exportCsvBtn) exportCsvBtn.addEventListener("click", exportToCSV);
    if (exportPdfBtn) exportPdfBtn.addEventListener("click", () => window.print());
    if (showFavoritesButton) showFavoritesButton.addEventListener("click", () => { state.favoritesOnly = !state.favoritesOnly; renderQuickTemplates(state.quickTemplates); });
    if (saveCategoryBudgetsBtn) saveCategoryBudgetsBtn.addEventListener("click", () => {
        document.querySelectorAll("[data-category-budget]").forEach(input => { state.categoryBudgets[input.dataset.categoryBudget] = Math.max(0, Number(input.value || 0)); });
        localStorage.setItem(CATEGORY_BUDGETS_KEY, JSON.stringify(state.categoryBudgets));
        renderCategoryBudgets(state.overviewItems);
        setMessage(dashboardMessage, "Category budgets saved.");
    });
    if (emptyAddButton) emptyAddButton.addEventListener("click", () => document.getElementById("expense-amount")?.focus());
    if (entrySuggestionsEl) entrySuggestionsEl.addEventListener("click", e => {
        const button = e.target.closest("[data-note-suggestion]");
        if (button) document.getElementById("expense-note").value = button.dataset.noteSuggestion;
    });

    // Filters & Pagination
    if (resetFilters) {
        resetFilters.addEventListener("click", () => {
            if (filterCategory) filterCategory.value = "";
            if (filterNote) filterNote.value = "";
            if (sortBy) sortBy.value = "date-desc";
            fetchExpenses(0);
        });
    }

    if (filterCategory) filterCategory.addEventListener("change", () => fetchExpenses(0));
    if (sortBy) sortBy.addEventListener("change", () => fetchExpenses(0));
    if (pageSize) pageSize.addEventListener("change", () => {
        state.pagination.size = Number(pageSize.value);
        fetchExpenses(0);
    });

    if (filterNote) {
        filterNote.addEventListener("input", () => {
            if (state.noteDebounceTimer) clearTimeout(state.noteDebounceTimer);
            state.noteDebounceTimer = setTimeout(() => fetchExpenses(0), 250);
        });
    }

    document.getElementById("expense-note")?.addEventListener("input", (e) => suggestCategoryFromNote(e.target.value));

    if (prevPageButton) {
        prevPageButton.addEventListener("click", () => {
            if (!state.pagination.first) fetchExpenses(state.pagination.page - 1);
        });
    }

    if (nextPageButton) {
        nextPageButton.addEventListener("click", () => {
            if (!state.pagination.last) fetchExpenses(state.pagination.page + 1);
        });
    }

    if (registerPasswordInput) registerPasswordInput.addEventListener("input", updatePasswordStrength);

    if (expensesTableBody) {
        expensesTableBody.addEventListener("click", async (e) => {
            const btn = e.target.closest("[data-action]");
            if (!btn) return;
            const action = btn.dataset.action;
            const id = Number(btn.dataset.id);
            const expense = state.expenses.find(i => i.id === id);

            if (action === "edit" && expense) {
                setEditMode(expense);
            } else if (action === "delete") {
                if (confirm("Delete this expense record?")) {
                    try {
                        await apiRequest(`/expenses/${id}`, { method: "DELETE" });
                        if (state.editingId === id) resetExpenseForm();
                        await fetchExpenses();
                        setMessage(dashboardMessage, "Expense deleted.");
                    } catch (err) {
                        setMessage(dashboardMessage, err.message, true);
                    }
                }
            }
        });
    }

    // Budget Modal Logic
    if (budgetEditTrigger && budgetModal) {
        budgetEditTrigger.addEventListener("click", () => {
            budgetModal.classList.remove("hidden");
        });
    }

    if (closeBudgetBtn && budgetModal) {
        closeBudgetBtn.addEventListener("click", () => {
            budgetModal.classList.add("hidden");
        });
    }

    if (saveBudgetBtn && budgetModal && budgetInputVal) {
        saveBudgetBtn.addEventListener("click", () => {
            const newBudget = Number(budgetInputVal.value);
            if (newBudget && newBudget >= 100) {
                state.budget = newBudget;
                localStorage.setItem(BUDGET_KEY, String(newBudget));
                budgetModal.classList.add("hidden");
                fetchExpenses(state.pagination.page);
                setMessage(dashboardMessage, `Monthly budget updated to ${formatINR(newBudget)}.`);
            }
        });
    }

    // Auto load state
    if (state.token || state.isDemo) {
        showDashboard();
        fetchExpenses(0).catch(() => showAuth());
    } else {
        showAuth();
    }
});
