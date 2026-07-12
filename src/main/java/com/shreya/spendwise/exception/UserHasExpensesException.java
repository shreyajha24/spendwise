package com.shreya.spendwise.exception;

public class UserHasExpensesException extends RuntimeException {

    public UserHasExpensesException(Long id) {
        super("Cannot delete user with id " + id + " because they have associated expenses");
    }
}
