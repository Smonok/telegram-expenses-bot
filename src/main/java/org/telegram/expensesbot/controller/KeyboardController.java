package org.telegram.expensesbot.controller;


import java.util.List;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

public class KeyboardController {
    private int rowButtonsNumber = 2;
    private int headerRowsNumber = 2;
    private final List<KeyboardRow> keyboard;

    public KeyboardController(List<KeyboardRow> keyboard, int rowButtonsNumber, int headerRowsNumber) {
        this.keyboard = keyboard;
        this.rowButtonsNumber = rowButtonsNumber;
        this.headerRowsNumber = headerRowsNumber;
    }

    public KeyboardController(List<KeyboardRow> keyboard) {
        this.keyboard = keyboard;
    }

    public void addButton(String name) {
        if (!addIfOneButtonRow(name)) {
            addToNewRow(name);
        }
    }

    private boolean addIfOneButtonRow(String name) {
        for (KeyboardRow row : keyboard) {
            boolean isSecondRow = row.equals(keyboard.get(headerRowsNumber - 1));
            if (row.size() < rowButtonsNumber && !isSecondRow) {
                return row.add(name);
            }
        }
        return false;
    }

    private boolean addToNewRow(String name) {
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(name);
        return keyboard.add(keyboardRow);
    }

    public void deleteButton(String name) {
        int nextRowIndex = computeNextRowIndex(name);
        int nextColumnIndex = computeNextColumnIndex(name);

        pushButtonsText(nextRowIndex, nextColumnIndex);
        removeLastButton();
    }

    private int computeNextRowIndex(String name) {
        int columnIndex = computeNextColumnIndex(name);
        for (KeyboardRow row : keyboard) {
            if (row.contains(name)) {
                return columnIndex == 0 ? keyboard.indexOf(row) + 1 : keyboard.indexOf(row);
            }
        }
        return -1;
    }

    private int computeNextColumnIndex(String name) {
        for (KeyboardRow row : keyboard) {
            if (row.contains(name)) {
                return row.indexOf(name) == row.size() - 1 ? 0 : row.indexOf(name) + 1;
            }
        }
        return -1;
    }

    private void pushButtonsText(int nextRowIndex, int nextColumnIndex) {
        for (int i = nextRowIndex; i < keyboard.size(); i++) {
            changeRowButtonsText(i, nextColumnIndex);
        }
    }

    private void changeRowButtonsText(int row, int columnIndex) {
        for (int j = columnIndex; j < keyboard.get(row).size(); j++) {
            changePreviousButtonText(row, j);
        }
    }

    private void changePreviousButtonText(int row, int column) {
        if (column == 0) {
            keyboard.get(row - 1).get(rowButtonsNumber - 1).setText(keyboard.get(row).get(column).getText());
        } else {
            keyboard.get(row).get(column - 1).setText(keyboard.get(row).get(column).getText());
        }
    }

    private void removeLastButton() {
        int lastRowIndex = keyboard.size() - 1;
        int lastRowSize = keyboard.get(lastRowIndex).size();

        if (lastRowSize == 1) {
            keyboard.remove(lastRowIndex);
        } else {
            keyboard.get(lastRowIndex).remove(1);
        }
    }
}
