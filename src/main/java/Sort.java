import java.util.Objects;

/**
 * Class that represents a sort.
 * Sorts are identified by a name.
 *
 * @author Stan Roelofs
 * @version 1.0
 */
class Sort {

    private String name;

    Sort(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        this.name = name;
    }

    String getName() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Sort)) {
            return false;
        }

        Sort sort = (Sort) o;
        return Objects.equals(this.name, sort.getName());
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.name.hashCode();
        return result;
    }
}
