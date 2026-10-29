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
import java.util.Objects;
import java.util.Optional;

import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.RepresentationMetadata;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.tree.explorer.services.api.ISysONExplorerFragment;

/**
 * Explorer folder holding a range of the children of an element (e.g. "101 – 200"), so that elements with thousands
 * of children (like the CRA view after an XIIDM import) can be browsed without sending them all to the browser.
 *
 * <p>It only exists in the explorer, like the SysON "Libraries" folders: nothing is added to the model.
 */
public record ExplorerPage(Element owner, String ownerId, int from, int to) implements ISysONExplorerFragment {

    public static final String KIND = "CybergridMLExplorerPage";

    private static final String ID_PREFIX = "cybergridml-explorer-page:";

    public ExplorerPage {
        Objects.requireNonNull(owner);
        Objects.requireNonNull(ownerId);
    }

    /** Parses a page id into its range and the id of the paged element, e.g. {@code cybergridml-explorer-page:100-200:<ownerId>}. */
    public static Optional<ParsedId> parseId(String treeItemId) {
        if (treeItemId == null || !treeItemId.startsWith(ID_PREFIX)) {
            return Optional.empty();
        }
        String rest = treeItemId.substring(ID_PREFIX.length());
        int separator = rest.indexOf(':');
        int dash = rest.indexOf('-');
        if (separator < 0 || dash < 0 || dash > separator) {
            return Optional.empty();
        }
        try {
            int from = Integer.parseInt(rest.substring(0, dash));
            int to = Integer.parseInt(rest.substring(dash + 1, separator));
            return Optional.of(new ParsedId(from, to, rest.substring(separator + 1)));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    @Override
    public String getId() {
        return ID_PREFIX + this.from + "-" + this.to + ":" + this.ownerId;
    }

    @Override
    public String getTooltip() {
        return "Elements " + this.getLabel();
    }

    @Override
    public String getLabel() {
        return (this.from + 1) + " – " + this.to;
    }

    @Override
    public String getKind() {
        return KIND;
    }

    @Override
    public Object getParent() {
        var path = ExplorerPaging.path(this.owner.getOwnedRelationship().size(), this.from);
        int index = path.indexOf(new ExplorerPaging.Range(this.from, this.to));
        if (index > 0) {
            var parent = path.get(index - 1);
            return new ExplorerPage(this.owner, this.ownerId, parent.from(), parent.to());
        }
        return this.owner;
    }

    @Override
    public List<String> getIconURL() {
        return List.of("/images/explorer_page.svg");
    }

    @Override
    public boolean hasChildren(IEditingContext editingContext, List<RepresentationMetadata> existingRepresentations, List<String> expandedIds, List<String> activeFilterIds) {
        return true;
    }

    @Override
    public List<Object> getChildren(IEditingContext editingContext, List<RepresentationMetadata> existingRepresentations, List<String> expandedIds, List<String> activeFilterIds) {
        var ownedRelationships = this.owner.getOwnedRelationship();
        int end = Math.min(this.to, ownedRelationships.size());
        if (this.from >= end) {
            return List.of();
        }
        if (ExplorerPaging.isPaged(end - this.from)) {
            return ExplorerPaging.split(this.from, end).stream()
                    .<Object>map(range -> new ExplorerPage(this.owner, this.ownerId, range.from(), range.to()))
                    .toList();
        }
        // The explorer filters (e.g. "Hide memberships") are applied to these children by the default explorer service.
        return new ArrayList<>(ownedRelationships.subList(this.from, end));
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public boolean isDeletable() {
        return false;
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    /** Range and paged element encoded in a page id. */
    public record ParsedId(int from, int to, String ownerId) {
    }
}
