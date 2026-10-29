/*******************************************************************************
 * Copyright (c) 2026 CEA LIST - RTE.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     CEA LIST - Initial API and implementation
 *******************************************************************************/

package cea.fr.CybergridML.explorer;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits the children of an element into pages for the explorer.
 *
 * <p>A level of the tree never shows more than {@link #PAGE_SIZE} items: past that, children are grouped in pages of
 * {@link #PAGE_SIZE}, and when there are too many pages, pages are themselves grouped in pages of
 * {@link #PAGE_SIZE} pages (1 – 10000, then 1 – 100 inside it), and so on.
 */
public final class ExplorerPaging {

    public static final int PAGE_SIZE = 100;

    private ExplorerPaging() {
    }

    public static boolean isPaged(int childCount) {
        return childCount > PAGE_SIZE;
    }

    /** Pages directly splitting the children {@code [from, to)}. */
    public static List<Range> split(int from, int to) {
        var pages = new ArrayList<Range>();
        int width = pageWidth(to - from);
        for (int start = from; start < to; start += width) {
            pages.add(new Range(start, Math.min(start + width, to)));
        }
        return pages;
    }

    /** Nested pages leading to the child at {@code index}, outermost first: the last one directly holds the child. */
    public static List<Range> path(int childCount, int index) {
        var path = new ArrayList<Range>();
        int from = 0;
        int to = childCount;
        while (isPaged(to - from)) {
            int width = pageWidth(to - from);
            from += (index - from) / width * width;
            to = Math.min(from + width, to);
            path.add(new Range(from, to));
        }
        return path;
    }

    /** Number of children in each page splitting {@code size} children, so that there are at most PAGE_SIZE pages. */
    static int pageWidth(int size) {
        int width = PAGE_SIZE;
        while ((size + width - 1) / width > PAGE_SIZE) {
            width *= PAGE_SIZE;
        }
        return width;
    }

    /** Children {@code [from, to)} of an element, 0-based, {@code to} excluded. */
    public record Range(int from, int to) {
    }
}
