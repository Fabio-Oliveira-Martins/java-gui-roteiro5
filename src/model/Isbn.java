package model;

import java.util.List;
import java.util.Objects;

// Objeto de valor: um ISBN-13 sempre válido. A regra existe só aqui.
public final class Isbn {
    private static final int LENGTH = 13;
    private static final List<String> PREFIXES = List.of("978", "979"); // para mudar o prefixo, edite só esta linha

    private final String value;

    private Isbn(String value) {
        this.value = value;
    }

    // Aceita hífens e espaços ("978-1-23456-789-0") e guarda só os 13 dígitos
    public static Isbn of(String raw) {
        String digits = raw == null ? "" : raw.replaceAll("[\\s-]", "");
        if (!digits.matches("\\d{" + LENGTH + "}")) {
            throw new IllegalArgumentException("ISBN must have " + LENGTH + " digits");
        }
        if (!PREFIXES.contains(digits.substring(0, 3))) {
            throw new IllegalArgumentException("ISBN must start with " + String.join(" or ", PREFIXES));
        }
        return new Isbn(digits);
    }

    public static boolean isValid(String raw) {
        try {
            of(raw);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public String value() { return value; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Isbn other && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
