package org.telegram.expensesbot.constants.sql;

public class SubexpensesQueryConstants {
    public static final String FIND_AFTER_SUBTRACTION = "select * from subexpenses\n"
        + "where\n"
        + " chat_id = ?1 AND\n"
        + " to_date(date, 'DD.MM.YYYY') >= (select now() - ( ?2 )\\:\\:interval)\n"
        + "order by category\n";

    public static final String FIND_SUM = "select sum(subexpenses) from (" + FIND_AFTER_SUBTRACTION + ") n1\n";

    public static final String FIND_AFTER_SUBTRACTION_BY_CATEGORY = "select * from subexpenses\n"
        + "where\n"
        + " chat_id = ?1 and category like ?2 and\n"
        + " to_date(date, 'DD.MM.YYYY') >= (select now() - ( ?3 )\\:\\:interval)\n"
        + "order by category, to_date(date, 'DD.MM.YYYY') desc\n";

    public static final String FIND_SUM_BY_CATEGORY =
        "select sum(subexpenses) from (\n" + FIND_AFTER_SUBTRACTION_BY_CATEGORY + ") n2\n";

    public static final String FIND_AFTER_SUBTRACTION_BY_MONTH_YEAR =
        "select * from (\n" + FIND_AFTER_SUBTRACTION_BY_CATEGORY
            + ") n3 where date_part('month', (date)\\:\\:timestamp) = ?4 and\n"
            + "date_part('year', (date)\\:\\:timestamp) = ?5\n"
            + "order by to_date(date, 'DD.MM.YYYY') desc\n";

    public static final String FIND_SUM_BY_MONTH_YEAR = "select sum(subexpenses) from(\n"
        + FIND_AFTER_SUBTRACTION_BY_MONTH_YEAR + ") n4\n";
}
