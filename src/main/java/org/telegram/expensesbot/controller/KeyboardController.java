package org.telegram.expensesbot.controller;


import java.util.List;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

public class KeyboardController implements DefaultKeyboardController {
    private final List<KeyboardRow> keyboard;
    private static final int ROW_BUTTONS_NUMBER = 2;
    private int headerRowsNumber = 2;

    public KeyboardController(List<KeyboardRow> keyboard, int headerRowsNumber) {
        this.keyboard = keyboard;
        this.headerRowsNumber = headerRowsNumber;
    }

    public KeyboardController(List<KeyboardRow> keyboard) {
        this.keyboard = keyboard;
    }

    @Override
    public void addButton(String name) {
        if (!addIfOneButtonRow(name)) {
            addToNewRow(name);
        }
    }

    private boolean addIfOneButtonRow(String name) {
        for (KeyboardRow row : keyboard) {
            boolean isSecondRow = row.equals(keyboard.get(headerRowsNumber - 1));
            if (row.size() < ROW_BUTTONS_NUMBER && !isSecondRow) {
                return row.add(name);
            }
        }
        return false;
    }

    private void addToNewRow(String name) {
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(name);
        keyboard.add(keyboardRow);
    }

    @Override
    public void deleteButton(String name) {
        int nextRowIndex = computeNextRowIndex(name);
        int nextColumnIndex = computeNextColumnIndex(name);
        int movementsNumber = computeMovementsNumber(name);

        pushButtonsText(nextRowIndex, nextColumnIndex, movementsNumber);
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

    private void pushButtonsText(int nextRowIndex, int nextColumnIndex, int movementsNumber) {
        int row = nextRowIndex;
        int column = nextColumnIndex;

        for (int j = 0; j < movementsNumber; j++) {
            changePreviousButtonText(row, column);
            if (column == 1) {
                row++;
            }
            column ^= 1; // 0 -> 1 , 1 -> 0
        }
    }

    private int computeMovementsNumber(String name) {
        int count = 0;
        boolean isFound = false;

        for (KeyboardRow row : keyboard) {
            for (KeyboardButton button : row) {
                if (button.getText().equals(name)) {
                    isFound = true;
                    continue;
                }
                if (isFound) {
                    count++;
                }
            }
        }

        return count;
    }

    private void changePreviousButtonText(int row, int column) {
        if (column == 0) {
            keyboard.get(row - 1).get(ROW_BUTTONS_NUMBER - 1).setText(keyboard.get(row).get(column).getText());
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
