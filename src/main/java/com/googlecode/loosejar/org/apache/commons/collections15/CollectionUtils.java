// GenericsNote: Converted.
/*
 *  Copyright 2001-2016 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.googlecode.loosejar.org.apache.commons.collections15;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides utility methods and decorators for {@link Collection} instances.
 *
 * @author Rodney Waldhoff
 * @author Paul Jack
 * @author Stephen Colebourne
 * @author Steve Downey
 * @author Herve Quiroz
 * @author Peter KoBek
 * @author Matthew Hawthorne
 * @author Janek Bogucki
 * @author Phil Steitz
 * @author Steven Melzer
 * @author Matt Hall, John Watkinson, Jon Schewe
 * @version $Revision: 1.1 $ $Date: 2005/10/11 17:05:19 $
 * @since Commons Collections 1.0
 */
public final class CollectionUtils {

    /**
     * Constant to avoid repeated object creation
     */
    private static final Integer INTEGER_ONE = 1;


    /**
     * Returns a {@link Collection} containing the intersection
     * of the given {@link Collection}s.
     * The cardinality of each element in the returned {@link Collection}
     * will be equal to the minimum of the cardinality of that element
     * in the two given {@link Collection}s.
     *
     * @param a the first collection, must not be null
     * @param b the second collection, must not be null
     * @return the intersection of the two collections15
     * @see Collection#retainAll
     */
    public static <E> Collection<E> intersection(final Collection<? extends E> a, final Collection<? extends E> b) {
        List<E> list = new ArrayList<E>();
        Map mapa = getCardinalityMap(a);
        Map mapb = getCardinalityMap(b);
        Set<E> elts = new HashSet<E>(a);
        elts.addAll(b);
        for (E elt : elts) {
            for (int i = 0, m = Math.min(getFreq(elt, mapa), getFreq(elt, mapb)); i < m; i++) {
                list.add(elt);
            }
        }
        return list;
    }

    /**
     * Returns a {@link Map} mapping each unique element in the given
     * {@link Iterable} to an {@link Integer} representing the number
     * of occurrences of that element in the {@link Iterable}.
     * Only those elements present in the Iterable will appear as
     * keys in the map.
     *
     * @param iterable the collection to get the cardinality map for, must not be null
     * @return the populated cardinality map
     */
    public static <E> Map<E, Integer> getCardinalityMap(final Iterable<E> iterable) {
        Map<E, Integer> count = new HashMap<E, Integer>();
        for (E obj : iterable) {
            Integer c = count.get(obj);
            if (c == null) {
                count.put(obj, INTEGER_ONE);
            } else {
                count.put(obj, c + 1);
            }
        }
        return count;
    }


    private static int getFreq(final Object obj, final Map freqMap) {
        Integer count = (Integer) freqMap.get(obj);
        if (count != null) {
            return count;
        }
        return 0;
    }

}
