package life.saqtapro.actuarial;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Таблицы смертности из калькулятора ПА (лист "ПА-Алгоритм для АИС", B123:K233).
 * qx хранятся в ‰, как в Excel; {@link #qx} отдаёт долю (‰ / 1000).
 */
public enum MortalityTable {

    STANDARD(1),
    DISABILITY_GROUP_3(3),
    DISABILITY_GROUP_2(5),
    DISABILITY_GROUP_1(7),
    /** "Без инвалидности/ОППВ" (5-29%) — для тех, кто платил ОППВ не меньше 60 месяцев */
    OPPV(9);

    public static final int MAX_AGE = 110;
    private static final String RESOURCE = "/data/saqtapro/pension-annuity/mortality-qx-per-mille.csv";

    /** Колонка мужской таблицы в CSV (женская — следующая) */
    private final int maleColumn;

    MortalityTable(int maleColumn) {
        this.maleColumn = maleColumn;
    }

    /**
     * Выбор таблицы как в Excel (M122 / R122 и формулы B7:C117): таблица ОППВ — только
     * без инвалидности и при ОППВ за 60+ месяцев; при инвалидности всегда таблица группы.
     */
    public static MortalityTable select(String disability, boolean oppvAtLeast60Months) {
        if ("NONE".equals(disability)) {
            return oppvAtLeast60Months ? OPPV : STANDARD;
        }
        // Значения disability для групп в SaqtaPro ещё не сняты с фронта — добавить, когда появятся тесты
        throw new IllegalArgumentException("Нет соответствия таблицы смертности для disability=" + disability);
    }

    /** Вероятность смерти в течение года для возраста age (0..110) */
    public double qx(int age, boolean female) {
        return Data.ROWS.get(age)[maleColumn + (female ? 1 : 0)] / 1000.0;
    }

    /** Ленивая загрузка CSV один раз на JVM */
    private static final class Data {
        static final List<double[]> ROWS = load();

        private static List<double[]> load() {
            try (InputStream in = MortalityTable.class.getResourceAsStream(RESOURCE)) {
                if (in == null) {
                    throw new IllegalStateException("Не найден ресурс " + RESOURCE);
                }
                List<double[]> rows = new ArrayList<>();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                String line;
                boolean header = true;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank() || line.startsWith("#")) {
                        continue;
                    }
                    if (header) {
                        header = false;
                        continue;
                    }
                    String[] cells = line.split(",");
                    double[] row = new double[cells.length];
                    for (int i = 0; i < cells.length; i++) {
                        row[i] = Double.parseDouble(cells[i].trim());
                    }
                    if ((int) row[0] != rows.size()) {
                        throw new IllegalStateException("Строки таблицы смертности должны идти подряд с возраста 0");
                    }
                    rows.add(row);
                }
                if (rows.size() != MAX_AGE + 1) {
                    throw new IllegalStateException("Ожидалось " + (MAX_AGE + 1) + " возрастов, а в файле " + rows.size());
                }
                return rows;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
