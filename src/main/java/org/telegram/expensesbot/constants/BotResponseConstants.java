package org.telegram.expensesbot.constants;

public class BotResponseConstants {

    public static final String HELP_INFO = "<b>===[ Как пользоваться ботом ]===</b>\n"
        + "<code>/start</code> - начало работы с ботом. \n"
        + "При повторном вводе все сохранённые данные удалятся.\n"
        + "\n"
        + "<b>Создайте категорию</b>, нажав на кнопку <code>'Управление категориями' » 'Новая категория'</code>.\n"
        + "Дайте название категории.\n"
        + "❕Название должно быть уникальным и не начинаться с одинарной скобки: ' .\n"
        + "Созданная категория добавится в клавиатуру бота.\n\n"
        + "Для того, чтобы <b>добавить расходы</b> в категорию, нажмите на название необходимой категории.\n"
        + "Далее, отправьте расходы в формате:\n  <i>[сумма] - [причина расходов]</i>.\n"
        + "❕Сумма расходов должна быть целым числом и не равна 0.\n\n"
        + "Пример:\n"
        + "<code>\n"
        + "100 - кофейня \n"
        + "1000 - ресторан\n"
        + "250 - корм для собачки\n"
        + "</code>\n\n"
        + "Чтобы <b>получить отчёт</b> о расходах, для конкретной категории, нужно нажать на кнопку с категорией "
        + "и выбрать период времени, а потом формат отчёта.\n\n"
        + "При нажатии на кнопку <code>\"Суммарно\"</code> будет сформирован отчёт по всем категориям.\n\n"
        + "Можно <b>обнулить затраты</b> на клавиатуре, нажав <code>'Управление категориями' » 'Обнулить счета'</code>.\n"
        + "При этом, вы все равно сможете получить отчет по расходам.\n\n"
        + "Любую, добавленную вами категорию, можно <b>удалить</b>, "
        + "нажав <code>'Управление категориями' » 'Удалить категорию'</code>.\n"
        + "Вместе с ней, удаляется вся информация, что пренадлежала этой категории.";

    public static final String START_WORK = "<i><b>Добро пожаловать!</b></i>\uD83D\uDE0A\n\n" + HELP_INFO;

    public static final String CHOOSE_ACTION = "Выберите действие";

    public static final String SUMMARY_TIME_PERIOD_INFO = "Выберите период времени, за который\n" +
        "вы хотите получить отчёт по всем категориям";

    public static final String CHOOSE_CATEGORY_TO_DELETE = "Выберите категорию для удаления";

    public static final String CHOOSE_REPORT_FORMAT = "Выберете формат отчёта";

    public static final String ADD_SUCCESSFUL = "✅Добавление успешно";

    public static final String DELETE_SUCCESSFUL = "✅Удаление успешно";

    public static final String CHANGED = "✅Изменено";

    public static final String BILLS_RESET = "Счета обнулены\nПоздравляем с новым периодом в жизни!\uD83C\uDF89";

    public static final String ADD_EXPENSES_OR_GET_REPORT_INFO = "Для добавления расходов\nпришлите строку или строки\n"
        + "в формате:\n сумма - название.\n"
        + "Или выберите период времени,\nдля получения отчёта";

    public static final String REPORT_FILE = "Файл с отчётом";

    public static final String SEND_CATEGORY_NAME = "Пришлите имя категории";

    public static final String TOO_BIG_EXPENSES_WARNING = "❗Расходы, больше чем 999999999\nне были добавлены";

    public static final String NO_SEPARATOR_ERROR = "Отсутствует разделитель: '-'";

    public static final String NEGATIVE_EXPENSES_ERROR = "Недопустимы значения расходов, меньше чем 1";

    public static final String TOO_MANY_SEPARATORS_ERROR = "Недопустимо больше одного разделителя";

    public static final String WRONG_FORMAT_ERROR = "\uD83D\uDED1Неверный формат\n";

    public static final String WRONG_CATEGORY_NAME_ERROR = "\uD83D\uDED1Недопустимое имя категории";
}
