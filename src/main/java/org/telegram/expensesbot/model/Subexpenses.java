package org.telegram.expensesbot.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class Subexpenses {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @Column
    private long chatId;

    @Column
    private String category;

    @Column
    private int subexpenses;

    @Column
    private String reasons;

    @Column
    private String date;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getSubexpenses() {
        return subexpenses;
    }

    public void setSubexpenses(int subexpenses) {
        this.subexpenses = subexpenses;
    }

    public String getReasons() {
        return reasons;
    }

    public void setReasons(String reasons) {
        this.reasons = reasons;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return String.format("%d - <i>%s</i>, %s", subexpenses, reasons, date);
    }

    public Subexpenses() {
    }

    public Subexpenses(long chatId, String category, int subexpenses, String reasons, String date) {
        this.chatId = chatId;
        this.category = category;
        this.subexpenses = subexpenses;
        this.reasons = reasons;
        this.date = date;
    }
}
