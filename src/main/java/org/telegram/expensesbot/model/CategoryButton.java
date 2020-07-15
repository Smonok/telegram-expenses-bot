package org.telegram.expensesbot.model;

import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CategoryButton categoryButton = (CategoryButton) o;
        return id == categoryButton.id &&
            chatId == categoryButton.chatId &&
            expenses == categoryButton.expenses &&
            Objects.equals(category, categoryButton.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, chatId, category, expenses);
    }

    public CategoryButton() {
    }

    public CategoryButton(long userId, String category, int expenses) {
        this.chatId = userId;
        this.category = category;
        this.expenses = expenses;
    }
}
