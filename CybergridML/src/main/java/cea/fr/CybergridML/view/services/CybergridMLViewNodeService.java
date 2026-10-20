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

package cea.fr.CybergridML.view.services;

import cea.fr.CybergridML.view.CybergridMLViewFilterSwitch;
import org.springframework.stereotype.Service;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Expose;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.LibraryPackage;
import org.eclipse.syson.sysml.MembershipExpose;
import org.eclipse.syson.sysml.Namespace;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.sysml.ViewUsage;
import org.eclipse.syson.sysml.helper.EMFUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class CybergridMLViewNodeService {

    private Set<Element> getDirectExposedElements(ViewUsage viewUsage) {
        Set<Element> directExposedElements = this.computeDirectExposedElements(viewUsage);
        return directExposedElements.stream()
                .filter(element -> directExposedElements.stream()
                        .noneMatch(other -> !Objects.equals(other, element) && EMFUtils.isAncestor(other, element)))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<Element> computeDirectExposedElements(ViewUsage viewUsage) {
        var directExposedElements = new HashSet<Element>();
        List<Expose> exposedElements = viewUsage.getOwnedRelationship().stream()
                .filter(Expose.class::isInstance)
                .map(Expose.class::cast)
                .toList();
        for (Expose exposedElement : exposedElements) {
            Element importedElement = exposedElement.getImportedElement();
            if (importedElement instanceof Package) {
                directExposedElements.add(importedElement);
            } else if (exposedElement instanceof MembershipExpose membershipExpose) {
                var importedMembership = membershipExpose.getImportedMembership();
                if (importedMembership != null) {
                    var memberElement = importedMembership.getMemberElement();
                    if (memberElement != null && !this.isChildOfExposedPackage(memberElement, exposedElements)) {
                        directExposedElements.add(memberElement);
                        if (exposedElement.isIsRecursive()) {
                            directExposedElements.addAll(this.getRecursiveContents(memberElement));
                        }
                    }
                }
            }
        }
        return directExposedElements;
    }

    private boolean isChildOfExposedPackage(Element element, List<Expose> exposedElements) {
        boolean isChildOfExposedElement = false;
        for (Expose exposedElement : exposedElements) {
            Element importedElement = exposedElement.getImportedElement();
            if (importedElement instanceof Package && !Objects.equals(element, importedElement) && EMFUtils.isAncestor(importedElement, element)) {
                isChildOfExposedElement = true;
                break;
            }
        }
        return isChildOfExposedElement;
    }

    private List<Element> getRecursiveContents(Element element) {
        var contents = new ArrayList<Element>();
        var ownedElements = element.getOwnedElement().stream().filter(Objects::nonNull).toList();
        contents.addAll(ownedElements);
        ownedElements.forEach(oe -> contents.addAll(this.getRecursiveContents(oe)));
        return contents;
    }

    private boolean isTypeOf(Element element, EClass domainType) {
        if (element instanceof LibraryPackage) {
            return Objects.equals(SysmlPackage.eINSTANCE.getPackage(), domainType);
        }
        return element != null && Objects.equals(element.eClass(), domainType);
    }

    public List<Element> getExposedCybergridMLElements(Element element, String cybergridMLElementTypeName, EClass domainType, List<Object> ancestors, IEditingContext editingContext, DiagramContext diagramContext) {
        return this.getExposedCybergridMLElements(element, null, cybergridMLElementTypeName, domainType, ancestors, editingContext, diagramContext);
    }

    public List<Element> getExposedCybergridMLElements(Element element, Element parent, String cybergridMLElementTypeName, EClass domainType, List<Object> ancestors, IEditingContext editingContext, DiagramContext diagramContext) {
        List<Element> elementsToExpose = new ArrayList<>();
        if (element instanceof ViewUsage viewUsage) {
            var exposedElements = new ArrayList<Element>();
            if (parent == null) {
                exposedElements.addAll(this.getDirectExposedElements(viewUsage));
            } else {
                exposedElements.addAll(viewUsage.getExposedElement());
            }
            var filteredExposedElements = exposedElements.stream()
                    .filter(elt -> this.isTypeOf(elt, domainType)
                            && this.isTypedWithCybergridMLDefinition(elt, cybergridMLElementTypeName)
                            && isAncestor(parent, elt))
                    .toList();
            elementsToExpose.addAll(filteredExposedElements);

            for (Element filteredExposedElement : filteredExposedElements) {
                boolean canBeDisplayed = new CybergridMLViewFilterSwitch().doSwitch(filteredExposedElement);
                if (!canBeDisplayed) {
                    elementsToExpose.remove(filteredExposedElement);
                }
            }
        } else if (element instanceof Namespace namespace && (ancestors == null || ancestors.isEmpty())) {
            namespace.getOwnedMember().stream()
                    .filter(elt -> this.isTypeOf(elt, domainType))
                    .filter(elt -> this.isTypedWithCybergridMLDefinition(elt, cybergridMLElementTypeName))
                    .forEach(elementsToExpose::add);
        } else if (ancestors != null) {
            var viewUsageContainingElement = ancestors.stream()
                    .filter(ViewUsage.class::isInstance)
                    .map(ViewUsage.class::cast)
                    .findFirst();
            if (viewUsageContainingElement.isPresent()) {
                List<Element> exposedCybergridMLElements = this.getExposedCybergridMLElements(viewUsageContainingElement.get(), element, cybergridMLElementTypeName, domainType, ancestors, editingContext, diagramContext);
                elementsToExpose.addAll(exposedCybergridMLElements);
            }
        }
        return elementsToExpose;
    }

    private boolean isTypedWithCybergridMLDefinition(Element elt, String cybergridMLElementTypeName) {
        if (cybergridMLElementTypeName == null || cybergridMLElementTypeName.isEmpty()) {
            return true;
        }
        if (!(elt instanceof Feature feature)) {
            return false;
        }
        return feature.getType().stream()
                .filter(Objects::nonNull)
                .anyMatch(t -> cybergridMLElementTypeName.equals(t.getQualifiedName()));
    }

    private static boolean isAncestor(Element parent, Element elt) {
        return parent == null || (!Objects.equals(parent, elt) && EMFUtils.isAncestor(parent, elt));
    }
}
