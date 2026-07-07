package cea.fr.CybergridML.configuration;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import cea.fr.CybergridML.domain.util.CybergridMLPropertyUtil;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.sirius.components.collaborative.forms.services.api.IPropertiesDescriptionRegistry;
import org.eclipse.sirius.components.collaborative.forms.services.api.IPropertiesDescriptionRegistryConfigurer;
import org.eclipse.sirius.components.core.api.IReadOnlyObjectPredicate;
import org.eclipse.sirius.components.emf.services.IDAdapter;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.sirius.components.interpreter.AQLInterpreter;
import org.eclipse.sirius.components.view.ChangeContext;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.ViewFactory;
import org.eclipse.sirius.components.view.emf.form.ViewFormDescriptionConverter;
import org.eclipse.sirius.components.view.form.FormDescription;
import org.eclipse.sirius.components.view.form.FormElementDescription;
import org.eclipse.sirius.components.view.form.FormElementFor;
import org.eclipse.sirius.components.view.form.FormElementIf;
import org.eclipse.sirius.components.view.form.FormFactory;
import org.eclipse.sirius.components.view.form.GroupDescription;
import org.eclipse.sirius.components.view.form.GroupDisplayMode;
import org.eclipse.sirius.components.view.form.LabelDescription;
import org.eclipse.sirius.components.view.form.MultiSelectDescription;
import org.eclipse.sirius.components.view.form.PageDescription;
import org.eclipse.sirius.components.view.form.SelectDescription;
import org.eclipse.sirius.components.view.form.TextAreaDescription;
import org.eclipse.sirius.components.view.form.TextfieldDescription;
import org.eclipse.sirius.components.view.form.WidgetDescription;
import org.eclipse.sirius.components.view.widget.reference.ReferenceFactory;
import org.eclipse.sirius.components.view.widget.reference.ReferenceWidgetDescription;
import org.eclipse.syson.services.UtilService;
import org.eclipse.syson.sysml.AttributeUsage;
import org.eclipse.syson.sysml.Comment;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.EnumerationDefinition;
import org.eclipse.syson.sysml.EnumerationUsage;
import org.eclipse.syson.sysml.Expression;
import org.eclipse.syson.sysml.FeatureTyping;
import org.eclipse.syson.sysml.FeatureValue;
import org.eclipse.syson.sysml.LiteralInfinity;
import org.eclipse.syson.sysml.LiteralInteger;
import org.eclipse.syson.sysml.LiteralString;
import org.eclipse.syson.sysml.MultiplicityRange;
import org.eclipse.syson.sysml.OwningMembership;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.ReferenceUsage;
import org.eclipse.syson.sysml.Subsetting;
import org.eclipse.syson.sysml.SysmlFactory;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.sysml.Type;
import org.eclipse.syson.util.AQLConstants;
import org.eclipse.syson.util.AQLUtils;
import org.eclipse.syson.util.SysMLMetamodelHelper;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CybergridMLPropertiesConfigurer implements IPropertiesDescriptionRegistryConfigurer {

    private static final String GET_DETAILS_VIEW_LABEL_SERVICE = "getDetailsViewLabel";
    private static final String E_STRUCTURAL_FEATURE = "eStructuralFeature";
    private static final String AQL_NOT_SELF_IS_READ_ONLY_E_STRUCTURAL_FEATURE = "aql:not(self.isReadOnly(eStructuralFeature))";
    private static final String AQL_NOT_SELF_IS_READ_ONLY = "aql:not(self.isReadOnly())";

    private final ViewFormDescriptionConverter converter;
    private final IReadOnlyObjectPredicate readOnlyObjectPredicate;
    private final UtilService utilService;

    public CybergridMLPropertiesConfigurer(ViewFormDescriptionConverter converter,
                                           IReadOnlyObjectPredicate readOnlyObjectPredicate) {
        this.converter = converter;
        this.readOnlyObjectPredicate = readOnlyObjectPredicate;
        this.utilService = new UtilService();
    }

    @Override
    public void addPropertiesDescriptions(IPropertiesDescriptionRegistry registry) {
        FormDescription viewFormDescription = this.createCybergridMLPropertiesViewForElement();

        URI uri = URI.createURI(IEMFEditingContext.RESOURCE_SCHEME + ":///"
                + UUID.nameUUIDFromBytes(CybergridMLPropertiesConfigurer.class.getCanonicalName().getBytes()));
        Resource resource = new XMIResourceImpl(uri);
        View view = ViewFactory.eINSTANCE.createView();
        view.getDescriptions().add(viewFormDescription);

        view.eAllContents().forEachRemaining(eObject -> {
            eObject.eAdapters()
                    .add(new IDAdapter(UUID.nameUUIDFromBytes(EcoreUtil.getURI(eObject).toString().getBytes())));
        });

        resource.getContents().add(view);

        // 'this' provides all AQL service methods used in the form expressions
        AQLInterpreter interpreter = new AQLInterpreter(List.of(),
                List.of(this),
                List.of(SysmlPackage.eINSTANCE));

        var result = this.converter.convert(viewFormDescription, List.of(), interpreter);
        if (result.representationDescription() instanceof org.eclipse.sirius.components.forms.description.FormDescription formDescription) {
            formDescription.getPageDescriptions().forEach(registry::add);
        }
    }


    private FormDescription createCybergridMLPropertiesViewForElement() {
        String domainType = SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getElement());
        FormDescription form = FormFactory.eINSTANCE.createFormDescription();
        form.setName("CyberGridML ");
        form.setDomainType(domainType);
        form.setTitleExpression("CyberGridML Properties View");

        PageDescription pageCore = FormFactory.eINSTANCE.createPageDescription();
        pageCore.setName("CyberGridML -PropertiesView");
        pageCore.setDomainType(domainType);
        pageCore.setLabelExpression("CyberGridML");
        pageCore.getGroups().add(this.createCommonGroup());
        pageCore.getGroups().add(this.createPropertyGroup());
        pageCore.getGroups().add(this.createReferenceGroup());

        form.getPages().add(pageCore);
        return form;
    }

    private GroupDescription createCommonGroup() {
        GroupDescription group = FormFactory.eINSTANCE.createGroupDescription();
        group.setDisplayMode(GroupDisplayMode.LIST);
        group.setName("Properties");
        group.setLabelExpression(AQLUtils.getSelfServiceCallExpression("getLabel"));
        group.setSemanticCandidatesExpression(AQLConstants.AQL_SELF);

        group.getChildren().add(this.createCoreWidgets());
        group.getChildren().add(this.createDocumentationWidget());

        FormElementIf typedByIf = FormFactory.eINSTANCE.createFormElementIf();
        typedByIf.setName("TypedBy_Precondition");
        typedByIf.setPredicateExpression(AQLConstants.AQL + "not(self.isTextAreaProperty())");
        typedByIf.getChildren().add(this.createTypedByWidget());
        group.getChildren().add(typedByIf);

        group.getChildren().add(this.createOwnedPropertiesIf());
        group.getChildren().add(this.createOwnedReferencesIf());

        return group;
    }

    private GroupDescription createPropertyGroup() {
        GroupDescription group = FormFactory.eINSTANCE.createGroupDescription();
        group.setDisplayMode(GroupDisplayMode.LIST);
        group.setName("CyberGridMLAttributeProperties");
        group.setLabelExpression(AQLUtils.getSelfServiceCallExpression("getLabel"));
        group.setSemanticCandidatesExpression(AQLConstants.AQL_SELF + "->select(e | e.isProperty())");

        FormElementIf textAreaIf = FormFactory.eINSTANCE.createFormElementIf();
        textAreaIf.setName("TextAreaProperty_Precondition");
        textAreaIf.setPredicateExpression(AQLUtils.getSelfServiceCallExpression("isTextAreaProperty"));
        textAreaIf.getChildren().add(this.createPropertyValueTextAreaWidget());
        group.getChildren().add(textAreaIf);

        FormElementIf enumIf = FormFactory.eINSTANCE.createFormElementIf();
        enumIf.setName("EnumProperty_Precondition");
        enumIf.setPredicateExpression(AQLUtils.getSelfServiceCallExpression("isEnumProperty"));
        enumIf.getChildren().add(this.createPropertyValueSelectWidget());
        group.getChildren().add(enumIf);

        FormElementIf textFieldIf = FormFactory.eINSTANCE.createFormElementIf();
        textFieldIf.setName("TextFieldProperty_Precondition");
        textFieldIf.setPredicateExpression(AQLConstants.AQL
                + "not(self.isTextAreaProperty()) and not(self.isEnumProperty())");
        textFieldIf.getChildren().add(this.createPropertyValueWidget());
        group.getChildren().add(textFieldIf);

        return group;
    }

    private GroupDescription createReferenceGroup() {
        GroupDescription group = FormFactory.eINSTANCE.createGroupDescription();
        group.setDisplayMode(GroupDisplayMode.LIST);
        group.setName("CyberGridMLReferenceProperties");
        group.setLabelExpression(AQLUtils.getSelfServiceCallExpression("getLabel"));
        group.setSemanticCandidatesExpression(AQLConstants.AQL_SELF + "->select(e | e.isReferenceProperty())");

        FormElementIf singleValuedIf = FormFactory.eINSTANCE.createFormElementIf();
        singleValuedIf.setName("SingleValuedReference_Precondition");
        singleValuedIf.setPredicateExpression(AQLConstants.AQL + "not(self.isManyValuedReference())");
        singleValuedIf.getChildren().add(this.createReferencePropertySelectWidget());
        group.getChildren().add(singleValuedIf);

        FormElementIf manyValuedIf = FormFactory.eINSTANCE.createFormElementIf();
        manyValuedIf.setName("ManyValuedReference_Precondition");
        manyValuedIf.setPredicateExpression(AQLUtils.getSelfServiceCallExpression("isManyValuedReference"));
        manyValuedIf.getChildren().add(this.createReferencePropertyMultiSelectWidget());
        group.getChildren().add(manyValuedIf);

        return group;
    }

    private FormElementDescription createCoreWidgets() {
        FormElementFor forElt = FormFactory.eINSTANCE.createFormElementFor();
        forElt.setName("Widgets for Core Group");
        forElt.setIterator(E_STRUCTURAL_FEATURE);
        forElt.setIterableExpression(AQLUtils.getSelfServiceCallExpression("getCoreFeatures"));
        forElt.getChildren().addAll(this.createWidgets());
        return forElt;
    }

    private List<FormElementIf> createWidgets() {
        List<FormElementIf> widgets = new ArrayList<>();

        FormElementIf label = FormFactory.eINSTANCE.createFormElementIf();
        label.setName("Read-only String Attributes");
        label.setPredicateExpression(
                AQLUtils.getSelfServiceCallExpression("isReadOnlyStringAttribute", E_STRUCTURAL_FEATURE));
        label.getChildren().add(this.createLabelWidget());
        widgets.add(label);

        FormElementIf textfield = FormFactory.eINSTANCE.createFormElementIf();
        textfield.setName("String Attributes");
        textfield.setPredicateExpression(
                AQLUtils.getSelfServiceCallExpression("isStringAttribute", E_STRUCTURAL_FEATURE));
        textfield.getChildren().add(this.createTextfieldWidget());
        widgets.add(textfield);

        return widgets;
    }

    private WidgetDescription createLabelWidget() {
        LabelDescription label = FormFactory.eINSTANCE.createLabelDescription();
        label.setName("LabelWidget");
        label.setLabelExpression(
                AQLUtils.getSelfServiceCallExpression(GET_DETAILS_VIEW_LABEL_SERVICE, E_STRUCTURAL_FEATURE));
        label.setValueExpression(AQLUtils.getSelfServiceCallExpression("eGet", E_STRUCTURAL_FEATURE));
        return label;
    }

    private WidgetDescription createTextfieldWidget() {
        TextfieldDescription textfield = FormFactory.eINSTANCE.createTextfieldDescription();
        textfield.setName("TextfieldWidget");
        textfield.setLabelExpression(
                AQLUtils.getSelfServiceCallExpression(GET_DETAILS_VIEW_LABEL_SERVICE, E_STRUCTURAL_FEATURE));
        textfield.setValueExpression(AQLUtils.getSelfServiceCallExpression("eGet", E_STRUCTURAL_FEATURE));
        textfield.setIsEnabledExpression(AQL_NOT_SELF_IS_READ_ONLY_E_STRUCTURAL_FEATURE);
        ChangeContext setNewValueOperation = ViewFactory.eINSTANCE.createChangeContext();
        setNewValueOperation.setExpression(AQLUtils.getSelfServiceCallExpression("setNewValue",
                List.of(E_STRUCTURAL_FEATURE, ViewFormDescriptionConverter.NEW_VALUE)));
        textfield.getBody().add(setNewValueOperation);
        return textfield;
    }

    private FormElementDescription createDocumentationWidget() {
        TextAreaDescription textarea = FormFactory.eINSTANCE.createTextAreaDescription();
        textarea.setName("DocumentationWidget");
        textarea.setLabelExpression("Documentation");
        textarea.setValueExpression(AQLUtils.getSelfServiceCallExpression("getDocumentation"));
        textarea.setHelpExpression("Use 'shift + enter' to add new lines");
        textarea.setIsEnabledExpression(AQLConstants.AQL + "not(self.isReadOnly())");
        ChangeContext setNewValueOperation = ViewFactory.eINSTANCE.createChangeContext();
        setNewValueOperation.setExpression(AQLUtils.getSelfServiceCallExpression("setNewDocumentationValue",
                ViewFormDescriptionConverter.NEW_VALUE));
        textarea.getBody().add(setNewValueOperation);
        FormElementIf precondition = FormFactory.eINSTANCE.createFormElementIf();
        precondition.getChildren().add(textarea);
        precondition.setName("DocumentationWidget_Precondition");
        precondition.setPredicateExpression(AQLConstants.AQL + "not(self.oclIsKindOf(sysml::AnnotatingElement)) and not(self.isTextAreaProperty())");
        return precondition;
    }

    private WidgetDescription createPropertyValueTextAreaWidget() {
        TextAreaDescription textArea = FormFactory.eINSTANCE.createTextAreaDescription();
        textArea.setName("PropertyValueTextAreaWidget");
        textArea.setLabelExpression("Value");
        textArea.setValueExpression(AQLUtils.getSelfServiceCallExpression("getPropertyValue"));
        textArea.setHelpExpression("Use 'shift + enter' to add new lines");
        textArea.setIsEnabledExpression(AQL_NOT_SELF_IS_READ_ONLY);
        ChangeContext setTextAreaValueOperation = ViewFactory.eINSTANCE.createChangeContext();
        setTextAreaValueOperation.setExpression(AQLUtils.getSelfServiceCallExpression("setPropertyValue",
                ViewFormDescriptionConverter.NEW_VALUE));
        textArea.getBody().add(setTextAreaValueOperation);
        return textArea;
    }

    private WidgetDescription createPropertyValueWidget() {
        TextfieldDescription textField = FormFactory.eINSTANCE.createTextfieldDescription();
        textField.setName("PropertyValueTextFieldWidget");
        textField.setLabelExpression("Value");
        textField.setValueExpression(AQLUtils.getSelfServiceCallExpression("getPropertyValue"));

        ChangeContext setNewValueOperation = ViewFactory.eINSTANCE.createChangeContext();
        setNewValueOperation.setExpression(AQLUtils.getSelfServiceCallExpression("setPropertyValue",
                ViewFormDescriptionConverter.NEW_VALUE));
        textField.getBody().add(setNewValueOperation);

        return textField;
    }

    private WidgetDescription createPropertyValueSelectWidget() {
        SelectDescription select = FormFactory.eINSTANCE.createSelectDescription();
        select.setName("PropertyValueSelectWidget");
        select.setLabelExpression("Value");
        select.setCandidatesExpression(AQLUtils.getSelfServiceCallExpression("getEnumCandidates"));
        select.setCandidateLabelExpression("aql:candidate.declaredName");
        select.setValueExpression(AQLUtils.getSelfServiceCallExpression("getEnumPropertyValue"));
        select.setIsEnabledExpression(AQL_NOT_SELF_IS_READ_ONLY);

        ChangeContext setNewValueOperation = ViewFactory.eINSTANCE.createChangeContext();
        setNewValueOperation.setExpression(AQLUtils.getSelfServiceCallExpression("setEnumPropertyValue",
                ViewFormDescriptionConverter.NEW_VALUE));
        select.getBody().add(setNewValueOperation);

        return select;
    }

    private WidgetDescription createReferencePropertySelectWidget() {
        SelectDescription select = FormFactory.eINSTANCE.createSelectDescription();
        select.setName("ReferencePropertyValueSelectWidget");
        select.setLabelExpression("Value");
        select.setCandidatesExpression(AQLUtils.getSelfServiceCallExpression("getReferenceCandidates"));
        select.setCandidateLabelExpression("aql:candidate.declaredName");
        select.setValueExpression(AQLUtils.getSelfServiceCallExpression("getSingleReferencePropertyValue"));
        select.setIsEnabledExpression(AQL_NOT_SELF_IS_READ_ONLY);

        ChangeContext setNewValueOperation = ViewFactory.eINSTANCE.createChangeContext();
        setNewValueOperation.setExpression(AQLUtils.getSelfServiceCallExpression("setSingleReferencePropertyValue",
                ViewFormDescriptionConverter.NEW_VALUE));
        select.getBody().add(setNewValueOperation);

        return select;
    }

    private WidgetDescription createReferencePropertyMultiSelectWidget() {
        MultiSelectDescription multiSelect = FormFactory.eINSTANCE.createMultiSelectDescription();
        multiSelect.setName("ReferencePropertyValueMultiSelectWidget");
        multiSelect.setLabelExpression("Value");
        multiSelect.setCandidatesExpression(AQLUtils.getSelfServiceCallExpression("getReferenceCandidates"));
        multiSelect.setCandidateLabelExpression("aql:candidate.declaredName");
        multiSelect.setValueExpression(AQLUtils.getSelfServiceCallExpression("getReferencePropertyValue"));
        multiSelect.setIsEnabledExpression(AQL_NOT_SELF_IS_READ_ONLY);

        ChangeContext setNewValueOperation = ViewFactory.eINSTANCE.createChangeContext();
        setNewValueOperation.setExpression(AQLUtils.getSelfServiceCallExpression("setReferencePropertyValues",
                ViewFormDescriptionConverter.NEW_VALUE));
        multiSelect.getBody().add(setNewValueOperation);

        return multiSelect;
    }

    private FormElementDescription createOwnedPropertiesIf() {
        FormElementIf precondition = FormFactory.eINSTANCE.createFormElementIf();
        precondition.setName("OwnedProperties_Precondition");
        precondition.setPredicateExpression("aql:self.ownedFeature->exists(e | e.isProperty())");
        precondition.getChildren().add(this.createOwnedPropertiesWidget());
        return precondition;
    }

    private WidgetDescription createOwnedPropertiesWidget() {
        ReferenceWidgetDescription refWidget = ReferenceFactory.eINSTANCE.createReferenceWidgetDescription();
        refWidget.setName("OwnedPropertiesReferenceWidget");
        refWidget.setLabelExpression("Properties");
        refWidget.setReferenceOwnerExpression(AQLConstants.AQL_SELF);
        refWidget.setReferenceNameExpression(SysmlPackage.eINSTANCE.getUsage_NestedAttribute().getName());
        refWidget.setIsEnabledExpression("aql:false");
        return refWidget;
    }

    private FormElementDescription createOwnedReferencesIf() {
        FormElementIf precondition = FormFactory.eINSTANCE.createFormElementIf();
        precondition.setName("OwnedReferences_Precondition");
        precondition.setPredicateExpression("aql:self.ownedFeature->exists(e | e.isReferenceProperty())");
        precondition.getChildren().add(this.createOwnedReferencesWidget());
        return precondition;
    }

    private WidgetDescription createOwnedReferencesWidget() {
        ReferenceWidgetDescription refWidget = ReferenceFactory.eINSTANCE.createReferenceWidgetDescription();
        refWidget.setName("OwnedReferencesReferenceWidget");
        refWidget.setLabelExpression("References");
        refWidget.setReferenceOwnerExpression(AQLConstants.AQL_SELF);
        refWidget.setReferenceNameExpression(SysmlPackage.eINSTANCE.getUsage_NestedReference().getName());
        refWidget.setIsEnabledExpression("aql:false");
        return refWidget;
    }

    private WidgetDescription createTypedByWidget() {
        ReferenceWidgetDescription refWidget = ReferenceFactory.eINSTANCE.createReferenceWidgetDescription();
        refWidget.setName("ExtraReferenceWidget");
        refWidget.setLabelExpression("Typed by");
        refWidget.setReferenceNameExpression(SysmlPackage.eINSTANCE.getFeature_Type().getName());
        refWidget.setReferenceOwnerExpression(AQLConstants.AQL_SELF);
        refWidget.setIsEnabledExpression(AQL_NOT_SELF_IS_READ_ONLY);
        ChangeContext setRefWidget = ViewFactory.eINSTANCE.createChangeContext();
        setRefWidget.setExpression(AQLUtils.getSelfServiceCallExpression("handleFeatureTypingNewValue",
                ViewFormDescriptionConverter.NEW_VALUE));
        refWidget.getBody().add(setRefWidget);

        return refWidget;
    }


    public String getLabel(Element element) {
        String name = element.getDeclaredName();
        return (name != null && !name.isBlank()) ? name : element.eClass().getName();
    }

    public String getDetailsViewLabel(Element element, EStructuralFeature feature) {
        String name = feature.getName();
        if (name == null || name.isEmpty()) {
            return "";
        }
        return name.substring(0, 1).toUpperCase()
                + name.substring(1).replaceAll("([A-Z])", " $1").trim();
    }


    public List<EStructuralFeature> getCoreFeatures(Element element) {
        List<EStructuralFeature> features = new ArrayList<>();
        element.eClass().getEAllStructuralFeatures().stream()
                .filter(EAttribute.class::isInstance)
                .filter(f -> !f.isDerived() && !f.isTransient())
                .filter(f -> "declaredName".equals(f.getName()))
                .forEach(features::add);
        return features;
    }

    public boolean isStringAttribute(Element element, EStructuralFeature feature) {
        return feature instanceof EAttribute attr
                && !attr.isDerived()
                && !attr.isTransient()
                && attr.isChangeable()
                && String.class.equals(attr.getEAttributeType().getInstanceClass());
    }

    public boolean isReadOnlyStringAttribute(Element element, EStructuralFeature feature) {
        return feature instanceof EAttribute attr
                && (attr.isDerived() || !attr.isChangeable())
                && String.class.equals(attr.getEAttributeType().getInstanceClass());
    }

    public boolean isReadOnly(Element element) {
        return this.readOnlyObjectPredicate.test(element);
    }

    public boolean isReadOnly(Element element, EStructuralFeature feature) {
        return this.readOnlyObjectPredicate.test(element) || feature.isDerived() || !feature.isChangeable();
    }

    public Object eGet(Element element, EStructuralFeature feature) {
        return element.eGet(feature);
    }

    public Element setNewValue(Element element, EStructuralFeature feature, String newValue) {
        if (!this.readOnlyObjectPredicate.test(element) && feature.isChangeable() && !feature.isDerived()) {
            element.eSet(feature, newValue);
        }
        return element;
    }

    public String getDocumentation(Element element) {
        return element.getOwnedRelationship().stream()
                .filter(OwningMembership.class::isInstance)
                .map(OwningMembership.class::cast)
                .flatMap(m -> m.getOwnedRelatedElement().stream())
                .filter(Comment.class::isInstance)
                .map(Comment.class::cast)
                .map(Comment::getBody)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));
    }

    public Element setNewDocumentationValue(Element element, String newValue) {
        if (this.readOnlyObjectPredicate.test(element)) {
            return element;
        }
        Comment existing = element.getOwnedRelationship().stream()
                .filter(OwningMembership.class::isInstance)
                .map(OwningMembership.class::cast)
                .flatMap(m -> m.getOwnedRelatedElement().stream())
                .filter(Comment.class::isInstance)
                .map(Comment.class::cast)
                .findFirst()
                .orElse(null);
        if (existing != null) {
            existing.setBody(newValue);
        } else {
            Comment comment = SysmlFactory.eINSTANCE.createComment();
            comment.setBody(newValue);
            OwningMembership membership = SysmlFactory.eINSTANCE.createOwningMembership();
            membership.getOwnedRelatedElement().add(comment);
            element.getOwnedRelationship().add(membership);
        }
        return element;
    }

    public Element handleFeatureTypingNewValue(Element element, Object newValue) {
        if (this.readOnlyObjectPredicate.test(element) || !(element instanceof org.eclipse.syson.sysml.Usage usage)) {
            return element;
        }
        if (newValue instanceof Type type) {
            this.utilService.setFeatureTyping(usage, type);
        } else if (newValue == null) {
            List<FeatureTyping> toRemove = usage.getOwnedRelationship().stream()
                    .filter(FeatureTyping.class::isInstance)
                    .map(FeatureTyping.class::cast)
                    .toList();
            usage.getOwnedRelationship().removeAll(toRemove);
        }
        return element;
    }

    public boolean isProperty(Element element) {
        return element instanceof AttributeUsage;
    }

    public boolean isTextAreaProperty(Element element) {
        if (!(element instanceof AttributeUsage au)) return false;
        String name = au.getDeclaredName();
        return "description".equals(name) || "justification".equals(name);
    }

    public boolean isEnumProperty(Element element) {
        return element instanceof AttributeUsage usage && this.getEnumerationDefinition(usage) != null;
    }

    private EnumerationDefinition getEnumerationDefinition(AttributeUsage usage) {
        return usage.getType().stream()
                .filter(EnumerationDefinition.class::isInstance)
                .map(EnumerationDefinition.class::cast)
                .findFirst()
                .orElse(null);
    }

    public List<EnumerationUsage> getEnumCandidates(AttributeUsage usage) {
        EnumerationDefinition enumerationDefinition = this.getEnumerationDefinition(usage);
        return enumerationDefinition != null ? enumerationDefinition.getEnumeratedValue() : List.of();
    }

    public EnumerationUsage getEnumPropertyValue(AttributeUsage usage) {
        String currentValue = this.getPropertyValue(usage);
        return this.getEnumCandidates(usage).stream()
                .filter(literal -> literal.getDeclaredName().equals(currentValue))
                .findFirst()
                .orElse(null);
    }

    public AttributeUsage setEnumPropertyValue(AttributeUsage usage, EnumerationUsage newValue) {
        if (newValue != null) {
            this.setPropertyValue(usage, newValue.getDeclaredName());
        }
        return usage;
    }

    public boolean isReferenceProperty(Element element) {
        return element instanceof ReferenceUsage;
    }

    public boolean isManyValuedReference(Element element) {
        if (!(element instanceof ReferenceUsage usage)) {
            return false;
        }
        if (!(usage.getMultiplicity() instanceof MultiplicityRange range)) {
            return false;
        }
        Expression upper = range.getUpperBound();
        if (upper instanceof LiteralInfinity) {
            return true;
        }
        return upper instanceof LiteralInteger literalInteger && literalInteger.getValue() > 1;
    }

    public List<PartUsage> getReferenceCandidates(ReferenceUsage usage) {
        Type type = this.getExplicitType(usage);
        if (type == null) {
            return List.of();
        }
        String qualifiedName = type.getQualifiedName();
        if (qualifiedName == null) {
            return List.of();
        }
        EObject root = EcoreUtil.getRootContainer(usage);
        List<PartUsage> candidates = new ArrayList<>();
        root.eAllContents().forEachRemaining(eObject -> {
            if (eObject instanceof PartUsage candidate && this.hasType(candidate, qualifiedName)) {
                candidates.add(candidate);
            }
        });
        return candidates;
    }

    private boolean hasType(PartUsage candidate, String qualifiedName) {
        try {
            return candidate.getType().stream()
                    .filter(Objects::nonNull)
                    .map(Type::getQualifiedName)
                    .filter(Objects::nonNull)
                    .anyMatch(qualifiedName::equals);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private Type getExplicitType(ReferenceUsage usage) {
        return usage.getOwnedRelationship().stream()
                .filter(FeatureTyping.class::isInstance)
                .map(FeatureTyping.class::cast)
                .map(FeatureTyping::getType)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private List<Subsetting> getOwnedSubsettings(ReferenceUsage usage) {
        return usage.getOwnedRelationship().stream()
                .filter(Subsetting.class::isInstance)
                .map(Subsetting.class::cast)
                .toList();
    }

    public List<PartUsage> getReferencePropertyValue(ReferenceUsage usage) {
        return this.getOwnedSubsettings(usage).stream()
                .map(Subsetting::getSubsettedFeature)
                .filter(PartUsage.class::isInstance)
                .map(PartUsage.class::cast)
                .toList();
    }

    public PartUsage getSingleReferencePropertyValue(ReferenceUsage usage) {
        return this.getReferencePropertyValue(usage).stream().findFirst().orElse(null);
    }

    public ReferenceUsage setReferencePropertyValues(ReferenceUsage usage, List<PartUsage> newValues) {
        if (this.readOnlyObjectPredicate.test(usage)) {
            return usage;
        }
        usage.getOwnedRelationship().removeAll(this.getOwnedSubsettings(usage));
        if (newValues != null) {
            newValues.forEach(target -> {
                Subsetting subsetting = SysmlFactory.eINSTANCE.createSubsetting();
                usage.getOwnedRelationship().add(subsetting);
                subsetting.setSubsettingFeature(usage);
                subsetting.setSubsettedFeature(target);
            });
        }
        return usage;
    }

    public ReferenceUsage setSingleReferencePropertyValue(ReferenceUsage usage, PartUsage newValue) {
        return this.setReferencePropertyValues(usage, newValue != null ? List.of(newValue) : List.of());
    }

    public String getPropertyValue(AttributeUsage usage) {
        Object value = CybergridMLPropertyUtil.getAttributeValue(usage);
        return value != null ? value.toString() : "";
    }

    public AttributeUsage setPropertyValue(AttributeUsage usage, String newValue) {
        if (this.readOnlyObjectPredicate.test(usage)) {
            return usage;
        }
        List<FeatureValue> toRemove = usage.getOwnedRelationship().stream()
                .filter(FeatureValue.class::isInstance)
                .map(FeatureValue.class::cast)
                .toList();
        usage.getOwnedRelationship().removeAll(toRemove);

        LiteralString literalValue = SysmlFactory.eINSTANCE.createLiteralString();
        literalValue.setValue(newValue);
        FeatureValue featureValueMembership = SysmlFactory.eINSTANCE.createFeatureValue();
        usage.getOwnedRelationship().add(featureValueMembership);
        featureValueMembership.getOwnedRelatedElement().add(literalValue);
        return usage;
    }
}
