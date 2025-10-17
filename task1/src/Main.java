package com.range;

import java.util.*;

public class RangeMap<K extends Comparable<K>, V> {
    private final TreeMap<K, RangeValue<K, V>> ranges;

    private static class RangeValue<K extends Comparable<K>, V> {
        final Range<K> range;
        final V value;

        RangeValue(Range<K> range, V value) {
            this.range = range;
            this.value = value;
        }
    }

    public RangeMap() {
        this.ranges = new TreeMap<>();
    }

    public void put(Range<K> range, V value) {
        // Удаляем пересекающиеся диапазоны
        removeOverlapping(range);
        ranges.put(range.getStart(), new RangeValue<>(range, value));
    }

    public V get(K key) {
        Map.Entry<K, RangeValue<K, V>> floorEntry = ranges.floorEntry(key);
        if (floorEntry != null && floorEntry.getValue().range.contains(key)) {
            return floorEntry.getValue().value;
        }
        return null;
    }

    public void remove(Range<K> range) {
        Iterator<Map.Entry<K, RangeValue<K, V>>> iterator = ranges.entrySet().iterator();
        List<RangeValue<K, V>> toAdd = new ArrayList<>();

        while (iterator.hasNext()) {
            RangeValue<K, V> existing = iterator.next().getValue();
            if (range.overlaps(existing.range)) {
                iterator.remove();

                // Левый остаток
                if (existing.range.getStart().compareTo(range.getStart()) < 0) {
                    toAdd.add(new RangeValue<>(
                            new Range<>(existing.range.getStart(), range.getStart(),
                                    existing.range.isStartInclusive(), false),
                            existing.value
                    ));
                }

                // Правый остаток
                if (existing.range.getEnd().compareTo(range.getEnd()) > 0) {
                    toAdd.add(new RangeValue<>(
                            new Range<>(range.getEnd(), existing.range.getEnd(),
                                    false, existing.range.isEndInclusive()),
                            existing.value
                    ));
                }
            }
        }

        toAdd.forEach(rv -> ranges.put(rv.range.getStart(), rv));
    }

    private void removeOverlapping(Range<K> range) {
        Iterator<Map.Entry<K, RangeValue<K, V>>> iterator = ranges.entrySet().iterator();
        List<RangeValue<K, V>> toAdd = new ArrayList<>();

        while (iterator.hasNext()) {
            RangeValue<K, V> existing = iterator.next().getValue();
            if (range.overlaps(existing.range)) {
                iterator.remove();

                // Сохраняем непересекающиеся части
                if (existing.range.getStart().compareTo(range.getStart()) < 0) {
                    toAdd.add(new RangeValue<>(
                            new Range<>(existing.range.getStart(), range.getStart(),
                                    existing.range.isStartInclusive(), false),
                            existing.value
                    ));
                }

                if (existing.range.getEnd().compareTo(range.getEnd()) > 0) {
                    toAdd.add(new RangeValue<>(
                            new Range<>(range.getEnd(), existing.range.getEnd(),
                                    false, existing.range.isEndInclusive()),
                            existing.value
                    ));
                }
            }
        }

        toAdd.forEach(rv -> ranges.put(rv.range.getStart(), rv));
    }

    public Set<Map.Entry<Range<K>, V>> entrySet() {
        Set<Map.Entry<Range<K>, V>> result = new HashSet<>();
        for (RangeValue<K, V> rv : ranges.values()) {
            result.add(new AbstractMap.SimpleEntry<>(rv.range, rv.value));
        }
        return result;
    }

    public boolean isEmpty() {
        return ranges.isEmpty();
    }

    public void clear() {
        ranges.clear();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (RangeValue<K, V> rv : ranges.values()) {
            sb.append(rv.range).append(" -> ").append(rv.value).append("\n");
        }
        return sb.toString();
    }
}