package cea.fr.NacreRTE.ChildCreation;

import cea.fr.NacreRTE.library.NacreLibElementType;
import cea.fr.NacreRTE.library.util.NacreLibraryUtil;
import cea.fr.NacreRTE.services.NacreElementInitializerSwitch;
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
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.impl.NamespaceImpl;
import org.eclipse.syson.sysml.util.ElementUtil;
import org.eclipse.syson.util.GetIntermediateContainerCreationSwitch;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service to handle LCA SysMLv2 elements.
 * It replaces Syson's service by excluding its bean from the Spring context (see <code>EconovaeApplication</code>)
 */
@Service
public class NacreEditService extends SysMLv2EditService {

    public static final String ID_PREFIX = "NacreEditService-";
    private final ILabelService labelService;
    private final IObjectSearchService objectSearchService;


    public NacreEditService(IDefaultEditService defaultEditService,
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
        return object instanceof Element element ;//&& NacreLibraryUtil.isNacreElement(element);
    }

    /**
     * Gather creation descriptions displayed in the object creation menu when clicking on a document of the tree explorer.
     * Only the {@link Package} creation description is returned.
     */
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

    /**
     * Gather creation descriptions displayed in the object creation menu when clicking on a child element of the tree explorer.
     * A creation description is returned for each LCA element that can be created as a child of the clicked element.</br>
     * See {@link NacreGetChildCreationSwitch} which returns a list of candidates depending on the element clicked.
     */
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
                //we do not have the info on whether the parent is LCA or not, we can only deduce candidates from the parent's SysML class
                List<NacreLibElementType> childrenCandidates = new NacreGetChildCreationSwitch().doSwitch(eClass);
                childrenCandidates.forEach(candidate -> {
                    // uses the SysML class icons
                    List<String> iconURL = this.labelService.getImagePaths(EcoreUtil.create(candidate.geteClass()));
                    // label that will be displayed in the object creation drop down menu
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

    /**
     * This method is called when the user has selected a type in the object creation drop down menu.
     * We guess the type of element created using the {@link ChildCreationDescription}.
     */
    @Override
    public Optional<Object> createChild(IEditingContext editingContext, Object object, String childCreationDescriptionId) {
        if (childCreationDescriptionId.startsWith(ID_PREFIX) && object instanceof Element parent) {

            NacreLibElementType nacreElementType = NacreLibElementType.getNacreElementFromName(childCreationDescriptionId.substring(ID_PREFIX.length()));
            Element newElement = createElement(parent, nacreElementType);

            if(newElement != null) {
                addElementToParent(parent, nacreElementType, newElement);
                new NacreElementInitializerSwitch(editingContext, nacreElementType).doSwitch(newElement);
                return Optional.of(newElement);
            }
        }
        return Optional.empty();
    }

    private Element createElement(Element parent, NacreLibElementType nacreElementType) {

        if (parent.getClass() == NamespaceImpl.class) {
            return SysmlFactory.eINSTANCE.createPackage();
        } else if (nacreElementType != null) {
            return (Element) SysmlFactory.eINSTANCE.create(nacreElementType.geteClass());
        }
        return null;
    }

    private void addElementToParent(Element parent, NacreLibElementType nacreElementType, Element newElement) {
        Optional<EClass> intermediateContainerClass = getIntermediateContainerClass(parent, nacreElementType);

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
        // Updating an element in an imported model removes its imported flag. This ensures that creating a library
        // model from an imported SysML file does not make it an imported user library.
        ElementUtil.setIsImported(newElement.eResource(), false);
    }

    private Optional<EClass> getIntermediateContainerClass(Element parent, NacreLibElementType lcaElementType) {
        Optional<EClass> intermediateContainerClass = Optional.empty();
        if (parent.getClass() == NamespaceImpl.class) {
            intermediateContainerClass = new GetIntermediateContainerCreationSwitch(parent).doSwitch(SysmlPackage.eINSTANCE.getPackage());
        } else if (lcaElementType != null) {
            intermediateContainerClass = new GetIntermediateContainerCreationSwitch(parent).doSwitch(lcaElementType.geteClass());
        }
        return intermediateContainerClass;
    }

}
