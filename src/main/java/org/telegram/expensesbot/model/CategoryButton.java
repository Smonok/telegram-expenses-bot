package org.telegram.expensesbot.model;

import javax.persistence.*;

@Entity
@Table
public class CategoryButton {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @Column
    private long chatId;

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

    public long getchatId() {
        return chatId;
    }

    public void setchatId(long chatId) {
        this.chatId = chatId;
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

    public CategoryButton() {
    }

    public CategoryButton(long userId, String category, int expenses) {
        this.chatId = userId;
        this.category = category;
        this.expenses = expenses;
    }
}
