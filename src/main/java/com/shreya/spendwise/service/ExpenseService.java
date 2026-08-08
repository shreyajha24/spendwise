package com.shreya.spendwise.service;

import com.shreya.spendwise.dto.ExpenseFilterRequest;
import com.shreya.spendwise.dto.ExpensePageResponse;
import com.shreya.spendwise.dto.ExpenseRequest;
import com.shreya.spendwise.dto.ExpenseResponse;
import com.shreya.spendwise.dto.CategorySpendInsightResponse;
import com.shreya.spendwise.dto.QuickExpenseTemplateResponse;
import com.shreya.spendwise.dto.WeeklyInsightResponse;
import com.shreya.spendwise.entity.Category;
import com.shreya.spendwise.entity.Expense;
import com.shreya.spendwise.entity.User;
import com.shreya.spendwise.exception.ExpenseNotFoundException;
import com.shreya.spendwise.mapper.ExpenseMapper;
import com.shreya.spendwise.repository.ExpenseRepository;
import com.shreya.spendwise.repository.specification.ExpenseSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ExpenseService {
    private static final List<QuickTemplateDefinition> QUICK_TEMPLATE_DEFINITIONS = List.of(
            new QuickTemplateDefinition("bus", "Bus", Category.TRANSPORT, 60.0, "Bus fare"),
            new QuickTemplateDefinition("coffee", "Coffee", Category.FOOD, 150.0, "Coffee"),
            new QuickTemplateDefinition("lunch", "Lunch", Category.FOOD, 250.0, "Lunch"),
            new QuickTemplateDefinition("breakfast", "Breakfast", Category.FOOD, 180.0, "Breakfast"),
            new QuickTemplateDefinition("snack", "Snack", Category.FOOD, 120.0, "Snack")
    );

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;
    private final CurrentUserService currentUserService;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            ExpenseMapper expenseMapper,
            CurrentUserService currentUserService) {
        this.expenseRepository = expenseRepository;
        this.expenseMapper = expenseMapper;
        this.currentUserService = currentUserService;
    }
    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        Expense expense = expenseMapper.toEntity(request);
        expense.setUser(currentUser);
        Expense savedExpense = expenseRepository.save(expense);
        return expenseMapper.toResponse(savedExpense);
    }

    public List<QuickExpenseTemplateResponse> getQuickExpenseTemplates() {
        return QUICK_TEMPLATE_DEFINITIONS.stream()
                .map(definition -> new QuickExpenseTemplateResponse(
                        definition.key(),
                        definition.label(),
                        definition.category(),
                        definition.amount(),
                        definition.defaultNote()
                ))
                .toList();
    }
    @Transactional
    public ExpenseResponse createExpenseFromTemplate(String templateKey, LocalDate date) {
        User currentUser = currentUserService.getCurrentUser();
        QuickTemplateDefinition definition = resolveTemplate(templateKey);
        LocalDate expenseDate = date == null ? LocalDate.now() : date;

        if (expenseDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date cannot be in the future.");
        }

        Expense expense = new Expense();
        expense.setAmount(definition.amount());
        expense.setCategory(definition.category());
        expense.setDate(expenseDate);
        expense.setNote(definition.defaultNote());
        expense.setUser(currentUser);

        Expense savedExpense = expenseRepository.save(expense);
        return expenseMapper.toResponse(savedExpense);
    }
    @Transactional(readOnly = true)
    public WeeklyInsightResponse getWeeklyInsights() {
        User currentUser = currentUserService.getCurrentUser();
        LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        List<Expense> weeklyExpenses = expenseRepository.findByUser_IdAndDateBetween(
                currentUser.getId(),
                weekStart,
                weekEnd
        );

        double totalSpent = weeklyExpenses.stream().mapToDouble(Expense::getAmount).sum();
        long totalTransactions = weeklyExpenses.size();
        double averageExpense = totalTransactions == 0 ? 0.0 : totalSpent / totalTransactions;

        Map<Category, Double> categoryTotals = new EnumMap<>(Category.class);
        for (Expense expense : weeklyExpenses) {
            categoryTotals.merge(expense.getCategory(), expense.getAmount(), Double::sum);
        }

        List<CategorySpendInsightResponse> breakdown = categoryTotals.entrySet().stream()
                .map(entry -> new CategorySpendInsightResponse(
                        entry.getKey(),
                        entry.getValue(),
                        totalSpent == 0
                                ? 0.0
                                : Math.round((entry.getValue() * 10000.0) / totalSpent) / 100.0
                ))
                .sorted(Comparator.comparing(CategorySpendInsightResponse::getAmount).reversed())
                .toList();

        Category topCategory = breakdown.isEmpty() ? null : breakdown.getFirst().getCategory();
        double topCategorySpent = breakdown.isEmpty() ? 0.0 : breakdown.getFirst().getAmount();

        String summary;
        if (topCategory == null) {
            summary = "No expenses logged this week yet. Use Quick Templates to add daily expenses in one tap.";
        } else {
            summary = "You spent " + formatInr(topCategorySpent) + " on "
                    + toReadableCategory(topCategory)
                    + " this week (" + breakdown.getFirst().getPercentage() + "% of weekly spending).";
        }

        return new WeeklyInsightResponse(
                weekStart,
                weekEnd,
                totalSpent,
                totalTransactions,
                averageExpense,
                topCategory,
                topCategorySpent,
                breakdown,
                summary
        );
    }
    @Transactional(readOnly = true)
    public ExpensePageResponse getExpenses(ExpenseFilterRequest filterRequest) {
        validatePagination(filterRequest.getPage(), filterRequest.getSize());
        User currentUser = currentUserService.getCurrentUser();
        Pageable pageable = PageRequest.of(
                filterRequest.getPage(),
                filterRequest.getSize(),
                resolveSort(filterRequest.getSortBy(), filterRequest.getDirection())
        );

        Specification<Expense> spec = ExpenseSpecifications.byFilter(
                currentUser,
                filterRequest.getCategory(),
                filterRequest.getNote()
        );

        Page<Expense> expensePage = expenseRepository.findAll(spec, pageable);

        return new ExpensePageResponse(
                expensePage.getContent().stream().map(expenseMapper::toResponse).toList(),
                expensePage.getNumber(),
                expensePage.getSize(),
                expensePage.getTotalElements(),
                expensePage.getTotalPages(),
                expensePage.isFirst(),
                expensePage.isLast()
        );
    }
    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long id) {
        Long currentUserId = currentUserService.getCurrentUser().getId();
        return expenseMapper.toResponse(findExpenseById(id, currentUserId));
    }
    @Transactional
    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        Long currentUserId = currentUserService.getCurrentUser().getId();
        Expense expense = findExpenseById(id, currentUserId);
        expenseMapper.updateEntity(request, expense);
        Expense updatedExpense = expenseRepository.save(expense);
        return expenseMapper.toResponse(updatedExpense);
    }
    @Transactional
    public void deleteExpense(Long id) {
        Long currentUserId = currentUserService.getCurrentUser().getId();
        Expense expense = findExpenseById(id, currentUserId);
        expenseRepository.delete(expense);
    }

    private Expense findExpenseById(Long id, Long userId) {
        return expenseRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
    }

    private Sort resolveSort(String sortBy, String direction) {
        String normalizedSortBy = sortBy == null ? "date" : sortBy.trim().toLowerCase();
        String normalizedDirection = direction == null ? "desc" : direction.trim().toLowerCase();

        if (!"asc".equals(normalizedDirection) && !"desc".equals(normalizedDirection)) {
            throw new IllegalArgumentException("Invalid direction value. Allowed values are 'asc' or 'desc'.");
        }

        Sort.Direction sortDirection = "asc".equals(normalizedDirection)
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        if ("date".equals(normalizedSortBy)) {
            return Sort.by(sortDirection, "date").and(Sort.by(sortDirection, "id"));
        }
        if ("amount".equals(normalizedSortBy)) {
            return Sort.by(sortDirection, "amount").and(Sort.by(sortDirection, "id"));
        }
        if ("category".equals(normalizedSortBy)) {
            return Sort.by(sortDirection, "category").and(Sort.by(sortDirection, "id"));
        }
        if ("id".equals(normalizedSortBy)) {
            return Sort.by(sortDirection, "id");
        }

        throw new IllegalArgumentException("Invalid sortBy value: '" + sortBy + "'. Allowed values are 'date', 'amount', 'category', 'id'.");
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be zero or greater.");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Size must be between 1 and 100.");
        }
    }

    private QuickTemplateDefinition resolveTemplate(String templateKey) {
        String normalizedKey = templateKey == null ? "" : templateKey.trim().toLowerCase();
        return QUICK_TEMPLATE_DEFINITIONS.stream()
                .filter(template -> template.key().equals(normalizedKey))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid template key: '" + templateKey + "'. Allowed values are "
                                + QUICK_TEMPLATE_DEFINITIONS.stream()
                                .map(QuickTemplateDefinition::key)
                                .toList() + "."
                ));
    }

    private String formatInr(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        return formatter.format(amount);
    }

    private String toReadableCategory(Category category) {
        String lowercase = category.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(lowercase.charAt(0)) + lowercase.substring(1);
    }

    private record QuickTemplateDefinition(
            String key,
            String label,
            Category category,
            Double amount,
            String defaultNote) {
    }
}
