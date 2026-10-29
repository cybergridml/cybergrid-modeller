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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.trees.TreeItem;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.RepresentationMetadata;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Relationship;
import org.eclipse.syson.tree.explorer.services.api.ISysONDefaultExplorerService;
import org.eclipse.syson.tree.explorer.services.api.ISysONExplorerServiceDelegate;
import org.springframework.stereotype.Service;

/**
 * Shows the children of elements with more than {@link ExplorerPaging#PAGE_SIZE} children as {@link ExplorerPage}s
 * in the explorer, instead of sending them all to the browser at once (which crashes it with e.g. the ~100 000
 * children of the CRA view after a large XIIDM import).
 *
 * <p>Everything else is left to the default SysON explorer service.
 */
@Service
public class CybergridMLExplorerPagingDelegate implements ISysONExplorerServiceDelegate {

    private final ISysONDefaultExplorerService defaultExplorerService;

    public CybergridMLExplorerPagingDelegate(ISysONDefaultExplorerService defaultExplorerService) {
        this.defaultExplorerService = Objects.requireNonNull(defaultExplorerService);
    }

    /**
     * Handles paged elements (their children), the direct children of paged elements (their parent is a page) and the
     * editing context (to resolve page ids). Kept cheap: it is called for every tree item.
     */
    @Override
    public boolean canHandle(Object self) {
        return self instanceof IEditingContext
                || isPaged(self)
                || self instanceof EObject eObject && isPaged(eObject.eContainer());
    }

    @Override
    public boolean hasChildren(Object self, IEditingContext editingContext, List<RepresentationMetadata> existingRepresentations, List<String> expandedIds, List<String> activeFilterIds) {
        if (isPaged(self)) {
            return true;
        }
        return this.defaultExplorerService.hasChildren(self, editingContext, existingRepresentations, expandedIds, activeFilterIds);
    }

    @Override
    public List<Object> getChildren(Object self, IEditingContext editingContext, List<RepresentationMetadata> existingRepresentations, List<String> expandedIds, List<String> activeFilterIds) {
        if (self instanceof Element owner && isPaged(owner)) {
            String ownerId = this.defaultExplorerService.getTreeItemId(owner);
            if (!expandedIds.contains(ownerId)) {
                return List.of();
            }
            List<Object> children = new ArrayList<>();
            // Representations (e.g. the CRA diagram) stay on top, outside of the pages, like in the default explorer.
            existingRepresentations.stream()
                    .filter(representation -> ownerId.equals(representation.getTargetObjectId()))
                    .sorted(Comparator.comparing(RepresentationMetadata::getLabel))
                    .forEachOrdered(children::add);
            ExplorerPaging.split(0, owner.getOwnedRelationship().size())
                    .forEach(range -> children.add(new ExplorerPage(owner, ownerId, range.from(), range.to())));
            return children;
        }
        return this.defaultExplorerService.getChildren(self, editingContext, existingRepresentations, expandedIds, activeFilterIds);
    }

    /** The parent of a child of a paged element is the page holding it, so that revealing it (e.g. from a search) opens that page. */
    @Override
    public Object getParent(Object self, String treeItemId, IEditingContext editingContext) {
        if (self instanceof Relationship relationship && relationship.eContainer() instanceof Element owner && isPaged(owner)) {
            int index = owner.getOwnedRelationship().indexOf(relationship);
            if (index >= 0) {
                var path = ExplorerPaging.path(owner.getOwnedRelationship().size(), index);
                var page = path.get(path.size() - 1);
                return new ExplorerPage(owner, this.defaultExplorerService.getTreeItemId(owner), page.from(), page.to());
            }
        }
        return this.defaultExplorerService.getParent(self, treeItemId, editingContext);
    }

    @Override
    public Object getTreeItemObject(String treeItemId, IEditingContext editingContext) {
        var parsedId = ExplorerPage.parseId(treeItemId);
        if (parsedId.isPresent()) {
            var id = parsedId.get();
            if (this.defaultExplorerService.getTreeItemObject(id.ownerId(), editingContext) instanceof Element owner && isPaged(owner)) {
                int to = Math.min(id.to(), owner.getOwnedRelationship().size());
                return new ExplorerPage(owner, id.ownerId(), id.from(), to);
            }
            return null;
        }
        return this.defaultExplorerService.getTreeItemObject(treeItemId, editingContext);
    }

    /** "Expand all" on a paged element would send all its children to the browser again. */
    @Override
    public boolean canExpandAll(TreeItem treeItem, IEditingContext editingContext) {
        if (ExplorerPage.KIND.equals(treeItem.getKind()) || isPaged(this.defaultExplorerService.getTreeItemObject(treeItem.getId(), editingContext))) {
            return false;
        }
        return this.defaultExplorerService.canExpandAll(treeItem, editingContext);
    }

    @Override
    public String getTreeItemId(Object self) {
        return this.defaultExplorerService.getTreeItemId(self);
    }

    @Override
    public String getTreeItemTooltip(Object self) {
        return this.defaultExplorerService.getTreeItemTooltip(self);
    }

    @Override
    public String getKind(Object self) {
        return this.defaultExplorerService.getKind(self);
    }

    @Override
    public String getLabel(Object self) {
        return this.defaultExplorerService.getLabel(self);
    }

    @Override
    public boolean isEditable(Object self) {
        return this.defaultExplorerService.isEditable(self);
    }

    @Override
    public boolean isDeletable(Object self) {
        return this.defaultExplorerService.isDeletable(self);
    }

    @Override
    public boolean isSelectable(Object self) {
        return this.defaultExplorerService.isSelectable(self);
    }

    @Override
    public List<String> getImageURL(Object self) {
        return this.defaultExplorerService.getImageURL(self);
    }

    @Override
    public boolean canCreateNewObjectsFromText(Object self) {
        return this.defaultExplorerService.canCreateNewObjectsFromText(self);
    }

    @Override
    public List<Object> getElements(IEditingContext editingContext, List<String> activeFilterIds) {
        return this.defaultExplorerService.getElements(editingContext, activeFilterIds);
    }

    @Override
    public String getType(Object self) {
        return this.defaultExplorerService.getType(self);
    }

    @Override
    public String getShortName(Object self) {
        return this.defaultExplorerService.getShortName(self);
    }

    @Override
    public String getReadOnlyTag(Object self) {
        return this.defaultExplorerService.getReadOnlyTag(self);
    }

    @Override
    public String getLibraryLabel(Object self) {
        return this.defaultExplorerService.getLibraryLabel(self);
    }

    private static boolean isPaged(Object object) {
        return object instanceof Element element && ExplorerPaging.isPaged(element.getOwnedRelationship().size());
    }
}
