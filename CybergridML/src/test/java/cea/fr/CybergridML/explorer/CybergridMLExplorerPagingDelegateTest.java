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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import cea.fr.CybergridML.explorer.ExplorerPaging.Range;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.RepresentationMetadata;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.FeatureMembership;
import org.eclipse.syson.sysml.SysmlFactory;
import org.eclipse.syson.sysml.ViewUsage;
import org.eclipse.syson.tree.explorer.services.api.ISysONDefaultExplorerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CybergridMLExplorerPagingDelegateTest {

    private static final String VIEW_ID = "cra-view";

    private ISysONDefaultExplorerService defaultExplorerService;

    private IEditingContext editingContext;

    private CybergridMLExplorerPagingDelegate delegate;

    @BeforeEach
    void setUp() {
        this.defaultExplorerService = mock(ISysONDefaultExplorerService.class);
        this.editingContext = mock(IEditingContext.class);
        this.delegate = new CybergridMLExplorerPagingDelegate(this.defaultExplorerService);
    }

    @Test
    void splitsChildrenInPagesOfOneHundred() {
        assertThat(ExplorerPaging.isPaged(100)).isFalse();
        assertThat(ExplorerPaging.isPaged(101)).isTrue();
        assertThat(ExplorerPaging.split(0, 250)).containsExactly(new Range(0, 100), new Range(100, 200), new Range(200, 250));
    }

    @Test
    void groupsPagesInPagesWhenThereAreTooManyOfThem() {
        // ~107 000 children (the CRA view of a large XIIDM import): 11 top-level pages of 10 000, then pages of 100.
        var topLevel = ExplorerPaging.split(0, 107_000);
        assertThat(topLevel).hasSize(11).startsWith(new Range(0, 10_000)).endsWith(new Range(100_000, 107_000));
        assertThat(ExplorerPaging.split(10_000, 20_000)).hasSize(100).startsWith(new Range(10_000, 10_100));

        assertThat(ExplorerPaging.path(107_000, 12_345)).containsExactly(new Range(10_000, 20_000), new Range(12_300, 12_400));
        assertThat(ExplorerPaging.path(250, 42)).containsExactly(new Range(0, 100));
        assertThat(ExplorerPaging.path(50, 42)).isEmpty();
    }

    @Test
    void showsRepresentationsThenPagesUnderAnElementWithManyChildren() {
        ViewUsage view = this.viewWithChildren(250);
        var diagram = mock(RepresentationMetadata.class);
        when(diagram.getTargetObjectId()).thenReturn(VIEW_ID);
        when(diagram.getLabel()).thenReturn("CRA");

        List<Object> children = this.delegate.getChildren(view, this.editingContext, List.of(diagram), List.of(VIEW_ID), List.of());

        assertThat(children).hasSize(4);
        assertThat(children.get(0)).isSameAs(diagram);
        assertThat(children.subList(1, 4)).extracting(page -> ((ExplorerPage) page).getLabel())
                .containsExactly("1 – 100", "101 – 200", "201 – 250");

        var lastPage = (ExplorerPage) children.get(3);
        assertThat(lastPage.getChildren(this.editingContext, List.of(), List.of(), List.of()))
                .containsExactlyElementsOf(view.getOwnedRelationship().subList(200, 250));
        assertThat(lastPage.getParent()).isSameAs(view);
    }

    @Test
    void leavesElementsWithFewChildrenToTheDefaultExplorer() {
        ViewUsage view = this.viewWithChildren(100);

        assertThat(this.delegate.canHandle(view)).isFalse();
        assertThat(this.delegate.canHandle(view.getOwnedRelationship().get(0))).isFalse();
    }

    @Test
    void revealsAChildInsideItsPage() {
        ViewUsage view = this.viewWithChildren(250);
        var membership = view.getOwnedRelationship().get(142);

        assertThat(this.delegate.canHandle(membership)).isTrue();
        assertThat(this.delegate.getParent(membership, "membership-id", this.editingContext))
                .isEqualTo(new ExplorerPage(view, VIEW_ID, 100, 200));
    }

    @Test
    void resolvesPageIdsBackToPages() {
        ViewUsage view = this.viewWithChildren(250);
        when(this.defaultExplorerService.getTreeItemObject(VIEW_ID, this.editingContext)).thenReturn(view);
        var page = new ExplorerPage(view, VIEW_ID, 200, 250);

        assertThat(this.delegate.getTreeItemObject(page.getId(), this.editingContext)).isEqualTo(page);
        assertThat(this.delegate.getTreeItemObject("not-a-page", this.editingContext)).isNull();
    }

    private ViewUsage viewWithChildren(int count) {
        ViewUsage view = SysmlFactory.eINSTANCE.createViewUsage();
        when(this.defaultExplorerService.getTreeItemId(view)).thenReturn(VIEW_ID);
        when(this.defaultExplorerService.getTreeItemObject(anyString(), any())).thenAnswer(invocation -> null);
        for (int i = 0; i < count; i++) {
            FeatureMembership membership = SysmlFactory.eINSTANCE.createFeatureMembership();
            Element part = SysmlFactory.eINSTANCE.createPartUsage();
            part.setDeclaredName("SR_" + i);
            membership.getOwnedRelatedElement().add(part);
            view.getOwnedRelationship().add(membership);
        }
        return view;
    }
}
