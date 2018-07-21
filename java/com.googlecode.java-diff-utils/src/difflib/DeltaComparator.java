package difflib;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author mksenzov
 */
public class DeltaComparator<T> implements Comparator<Delta<T>>, Serializable {
    private static final long serialVersionUID = 1L;

    public DeltaComparator() {
    }

    public int compare(final Delta<T> a, final Delta<T> b) {
        final int posA = a.getOriginal().getPosition();
        final int posB = b.getOriginal().getPosition();
        if (posA > posB) {
            return 1;
        } else if (posA < posB) {
            return -1;
        }
        return 0;
    }
}