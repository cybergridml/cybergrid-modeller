package cea.fr.CybergridML.edit;

import cea.fr.CybergridML.domain.CybergridMLLibElementType;
import cea.fr.CybergridML.services.CybergridMLElementInitializerSwitch;
import cea.fr.CybergridML.view.representations.CfaRepresentationDescriptionProvider;
import cea.fr.CybergridML.view.representations.CfraRepresentationDescriptionProvider;
import cea.fr.CybergridML.view.representations.CraRepresentationDescriptionProvider;
import cea.fr.CybergridML.view.services.CybergridMLUtilService;
import cea.fr.CybergridML.view.services.CybergridMLViewDefinitionKind;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sirius.components.collaborative.api.IRepresentationMetadataPersistenceService;
import org.eclipse.sirius.components.collaborative.api.IRepresentationPersistenceService;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramCreationService;
import org.eclipse.sirius.components.core.api.*;
import org.eclipse.sirius.components.core.api.labels.StyledString;
import org.eclipse.syson.application.services.SysMLv2EditService;
import org.eclipse.syson.sysml.*;
import org.eclipse.syson.sysml.impl.NamespaceImpl;
import org.eclipse.syson.sysml.util.ElementUtil;
import org.eclipse.syson.util.GetIntermediateContainerCreationSwitch;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class CybergridMLEditService extends SysMLv2EditService {

    public static final String ID_PREFIX = "CybergridMLEditService-";
    private final ILabelService labelService;
    private final IObjectSearchService objectSearchService;
    private final CybergridMLUtilService cybergridMLUtilService = new CybergridMLUtilService();

    public CybergridMLEditService(IDefaultEditService defaultEditService,
                                  ILabelService labelService,
                                  IObjectSearchService objectSearchService,
                                  IDiagramCreationService diagramCreationService,
                                  IRepresentationDescriptionSearchService representationDescriptionSearchService,
                                  IRepresentationMetadataPersistenceService representationMetadataPersistenceService,
                                  IRepresentationPersistenceService representationPersistenceService) {
        super(defaultEditService, labelService, objectSearchService, diagramCreationService, representationDescriptionSearchService, representationMetadataPersistenceService, representationPersistenceService);
        this.labelService = Objects.requireNonNull(labelService);
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
    }

    @Override
    public boolean canHandle(Object object) {
        return object instanceof Element;
    }

    @Override
    public List<ChildCreationDescription> getRootCreationDescriptions(IEditingContext editingContext, String domainId, boolean suggested, String referenceKind) {
        final List<ChildCreationDescription> rootObjectCreationDescription = new ArrayList<>();
        if (SysmlPackage.eNS_URI.equals(domainId)) {
            List<String> iconURL = this.labelService.getImagePaths(EcoreUtil.create(SysmlPackage.eINSTANCE.getPackage()));
            StyledString styledLabel = this.labelService.getStyledLabel(SysmlPackage.eINSTANCE.getPackage());
            String label = "";
            if (styledLabel != null) {
                label = styledLabel.toString();
            }
            String creationDescriptionId = ID_PREFIX + SysmlPackage.eINSTANCE.getPackage().getName();
            ChildCreationDescription creationDescription = new ChildCreationDescription(creationDescriptionId, label, iconURL);
            rootObjectCreationDescription.add(creationDescription);
        }
        return rootObjectCreationDescription;
    }

    @Override
    public List<ChildCreationDescription> getChildCreationDescriptions(IEditingContext editingContext, String containerId, String referenceKind) {
        List<ChildCreationDescription> result = new ArrayList<>();

        var optionalContainer = this.objectSearchService.getObject(editingContext, containerId)
                .filter(EObject.class::isInstance)
                .map(EObject.class::cast);

        if (optionalContainer.isPresent()) {
            EObject container = optionalContainer.get();
            EClass eClass = container.eClass();
            EPackage ePackage = eClass.getEPackage();
            if (SysmlPackage.eNS_PREFIX.equals(ePackage.getNsPrefix())) {
                List<ChildCreationDescription> childCreationDescriptions = new ArrayList<>();
                List<CybergridMLLibElementType> childrenCandidates;
                if (container instanceof ViewUsage viewUsage) {
                    childrenCandidates = this.getChildrenCandidatesForView(viewUsage, editingContext);
                } else {
                    childrenCandidates = new CybergridMLGetChildCreationSwitch().doSwitch(eClass);
                }
                childrenCandidates.forEach(candidate -> {
                    List<String> iconURL = this.labelService.getImagePaths(EcoreUtil.create(candidate.geteClass()));
                    StyledString styledLabel = this.labelService.getStyledLabel(candidate);
                    String label = "";
                    if (styledLabel != null) {
                        label = styledLabel.toString();
                    }
                    ChildCreationDescription childCreationDescription = new ChildCreationDescription(ID_PREFIX + candidate.getName(), label, iconURL);
                    childCreationDescriptions.add(childCreationDescription);
                });
                result = childCreationDescriptions;
            }
        }

        result.sort(Comparator.comparing(ChildCreationDescription::label, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    private List<CybergridMLLibElementType> getChildrenCandidatesForView(ViewUsage viewUsage, IEditingContext editingContext) {
        CybergridMLViewDefinitionKind viewDefinitionKind = this.cybergridMLUtilService.getCybergridMLViewDefinitionKind(viewUsage, List.of(), editingContext);
        if (viewDefinitionKind == null) {
            return List.of();
        }
        return switch (viewDefinitionKind) {
            case CFA_VIEW -> CfaRepresentationDescriptionProvider.ELEMENTS;
            case CRA_VIEW -> CraRepresentationDescriptionProvider.ELEMENTS;
            case CFRA_VIEW -> CfraRepresentationDescriptionProvider.ELEMENTS;
        };
    }

    @Override
    public Optional<Object> createChild(IEditingContext editingContext, Object object, String childCreationDescriptionId) {
        if (childCreationDescriptionId.startsWith(ID_PREFIX) && object instanceof Element parent) {

            CybergridMLLibElementType cybergridMLElementType = CybergridMLLibElementType.getCybergridMLElementFromName(childCreationDescriptionId.substring(ID_PREFIX.length()));
            Element newElement = createElement(parent, cybergridMLElementType);

            if (newElement != null) {
                addElementToParent(parent, cybergridMLElementType, newElement);
                new CybergridMLElementInitializerSwitch(editingContext, cybergridMLElementType).doSwitch(newElement);
                return Optional.of(newElement);
            }
        }
        return Optional.empty();
    }

    private Element createElement(Element parent, CybergridMLLibElementType cybergridMLElementType) {
        if (parent.getClass() == NamespaceImpl.class) {
            return SysmlFactory.eINSTANCE.createPackage();
        } else if (cybergridMLElementType != null) {
            return (Element) SysmlFactory.eINSTANCE.create(cybergridMLElementType.geteClass());
        }
        return null;
    }

    private void addElementToParent(Element parent, CybergridMLLibElementType cybergridMLElementType, Element newElement) {
        Optional<EClass> intermediateContainerClass = getIntermediateContainerClass(parent, cybergridMLElementType);

        if (intermediateContainerClass.isPresent()) {
            EObject intermediateContainerEObject = SysmlFactory.eINSTANCE.create(intermediateContainerClass.get());
            if (intermediateContainerEObject instanceof Relationship intermediateContainer) {
                parent.getOwnedRelationship().add(intermediateContainer);
                intermediateContainer.getOwnedRelatedElement().add(newElement);
            }
        } else if (newElement instanceof Relationship relationship) {
            parent.getOwnedRelationship().add(relationship);
        } else if (parent instanceof Membership membership) {
            membership.getOwnedRelatedElement().add(newElement);
        }
        ElementUtil.setIsImported(newElement.eResource(), false);
    }

    private Optional<EClass> getIntermediateContainerClass(Element parent, CybergridMLLibElementType cybergridMLElementType) {
        Optional<EClass> intermediateContainerClass = Optional.empty();
        if (parent.getClass() == NamespaceImpl.class) {
            intermediateContainerClass = new GetIntermediateContainerCreationSwitch(parent).doSwitch(SysmlPackage.eINSTANCE.getPackage());
        } else if (cybergridMLElementType != null) {
            intermediateContainerClass = new GetIntermediateContainerCreationSwitch(parent).doSwitch(cybergridMLElementType.geteClass());
        }
        return intermediateContainerClass;
    }
}

