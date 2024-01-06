package org.nature.common.db.builder.util;

import org.nature.common.db.builder.model.Mapping;

import java.util.List;
import java.util.function.Function;

public class TextUtil {

    public static String columns(List<Mapping> mappings) {
        return mappings(mappings, Mapping::getColumn);
    }

    public static String properties(List<Mapping> mappings) {
        return mappings(mappings, i -> "?");
    }

    public static String conditions(List<Mapping> mappings) {
        return mappings(mappings, i -> i.getColumn() + "=?");
    }

    private static String mappings(List<Mapping> mappings, Function<Mapping, String> func) {
        StringBuilder builder = new StringBuilder();
        int size = mappings.size();
        for (int i = 0; i < size; i++) {
            Mapping mapping = mappings.get(i);
            builder.append(func.apply(mapping));
            if (i < size - 1) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

}
