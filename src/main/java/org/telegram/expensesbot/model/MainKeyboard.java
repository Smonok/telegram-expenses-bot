package org.telegram.expensesbot.model;

import javax.persistence.*;

@Entity
@Table(name = "main_keyboard")
public class MainKeyboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private long userId;

    @Column
    private String category;

    @Column
    private int expenses;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getExpenses() {
        return expenses;
    }

    public void setExpenses(int expenses) {
        this.expenses = expenses;
    }

    public MainKeyboard() {
    }

    /*public MainKeyboard(long userId, String category, int expenses) {
        this.userId = userId;
        this.category = category;
        this.expenses = expenses;
    }*/
}
