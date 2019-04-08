/*
   Copyright 2010 Dmitry Naumenko (dm.naumenko@gmail.com)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package difflib;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Describes the patch holding all deltas between the original and revised texts.
 * 
 * @author <a href="dm.naumenko@gmail.com">Dmitry Naumenko</a>
 */
public class Patch<T> {
    private List<Delta<T>> deltas = new LinkedList<Delta<T>>();

    /**
     * Apply this patch to the given target, producing a new list as result
     * @return the patched text
     * @throws PatchFailedException if can't apply patch
     */
    public List<T> applyTo(List<T> target) throws PatchFailedException {
        List<T> result = new LinkedList<T>(target);
        ListIterator<Delta<T>> it = getDeltas().listIterator(deltas.size());
        while (it.hasPrevious()) {
            Delta<T> delta = it.previous();
            delta.applyTo(result);
        }
        return result;
    }
    
    public boolean isEmpty() {
        return deltas.isEmpty();
    }
    
    /**
     * Apply this patch to the given target in-place, updating the <code>target</code> list
     * @return the patched text
     * @throws PatchFailedException if can't apply patch
     */
    public List<T> applyToInPlace(List<T> target) throws PatchFailedException {
        List<T> result = target;
        ListIterator<Delta<T>> it = getDeltas().listIterator(deltas.size());
        while (it.hasPrevious()) {
            Delta<T> delta = it.previous();
            delta.applyTo(result);
        }
        return result;
    }
    
    /**
     * Restore the text to original. Opposite to applyTo() method.
     * @param target the given target
     * @return the restored text
     */
    public List<T> restore(List<T> target) {
        List<T> result = new LinkedList<T>(target);
        ListIterator<Delta<T>> it = getDeltas().listIterator(deltas.size());
        while (it.hasPrevious()) {
            Delta<T> delta = it.previous();
            delta.restore(result);
        }
        return result;
    }
    
    /**
     * Add the given delta to this patch
     * @param delta the given delta
     */
    public void addDelta(Delta<T> delta) {
        deltas.add(delta);
    }

    /**
     * Get the list of computed deltas
     * @return the deltas
     */
    public List<Delta<T>> getDeltas() {
        Collections.sort(deltas, new DeltaComparator<T>());
        return deltas;
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append('[');
        boolean first = true;
        for (Delta<T> delta : getDeltas()) {
            if (first) {
                first = false;
            } else {
                result.append(", ");
            }
            result.append(delta);
        }
        result.append(']');
        return result.toString();
    }
}
