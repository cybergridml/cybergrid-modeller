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
import org.eclipse.sirius.components.view.form.ContainerBorderStyle;
import org.eclipse.sirius.components.view.form.FlexDirection;
import org.eclipse.sirius.components.view.form.FlexboxContainerDescription;
import org.eclipse.sirius.components.view.form.FormFactory;
import org.eclipse.sirius.components.view.form.GroupDescription;
import org.eclipse.sirius.components.view.form.GroupDisplayMode;
import org.eclipse.sirius.components.view.form.LabelDescription;
import org.eclipse.sirius.components.view.form.LabelDescriptionStyle;
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
    private static final String ATTRIBUTE_CANDIDATE = "attributeCandidate";
    private static final String REFERENCE_CANDIDATE = "referenceCandidate";
    private static final int MAX_PROPERTY_NESTING_DEPTH = 5;

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

        return group;
    }

    private GroupDescription createPropertyGroup() {
        GroupDescription group = FormFactory.eINSTANCE.createGroupDescription();
        group.setDisplayMode(GroupDisplayMode.LIST);
        group.setName("CyberGridMLAttributeProperties");
        group.setLabelExpression("Properties");
        group.setSemanticCandidatesExpression(AQLConstants.AQL_SELF);
        group.getChildren().add(this.createPropertyWidgetsFor(ATTRIBUTE_CANDIDATE,
                AQLUtils.getSelfServiceCallExpression("getPropertyCandidates"), 0));
        return group;
    }

    private FormElementDescription createPropertyWidgetsFor(String variableName, String iterableExpression, int depth) {
        FormElementFor forElt = FormFactory.eINSTANCE.createFormElementFor();
        forElt.setName("Widgets for Attribute Properties (depth " + depth + ")");
        forElt.setIterator(variableName);
        forElt.setIterableExpression(iterableExpression);
        forElt.getChildren().addAll(this.createPropertyWidgets(variableName, depth));
        return forElt;
    }

    private List<FormElementIf> createPropertyWidgets(String variableName, int depth) {
        List<FormElementIf> widgets = new ArrayList<>();

        FormElementIf textAreaIf = FormFactory.eINSTANCE.createFormElementIf();
        textAreaIf.setName("TextAreaProperty_Precondition");
        textAreaIf.setPredicateExpression(AQLUtils.getServiceCallExpression(variableName, "isTextAreaProperty"));
        textAreaIf.getChildren().add(this.createPropertyValueTextAreaWidget(variableName));
        widgets.add(textAreaIf);

        FormElementIf enumIf = FormFactory.eINSTANCE.createFormElementIf();
        enumIf.setName("EnumProperty_Precondition");
        enumIf.setPredicateExpression(AQLUtils.getServiceCallExpression(variableName, "isEnumProperty"));
        enumIf.getChildren().add(this.createPropertyValueSelectWidget(variableName));
        widgets.add(enumIf);

        FormElementIf measurementIf = FormFactory.eINSTANCE.createFormElementIf();
        measurementIf.setName("MeasurementProperty_Precondition");
        measurementIf.setPredicateExpression(AQLUtils.getServiceCallExpression(variableName, "isMeasurementProperty"));
        measurementIf.getChildren().add(this.createMeasurementWidget(variableName, depth));
        widgets.add(measurementIf);

        if (depth < MAX_PROPERTY_NESTING_DEPTH) {
            FormElementIf compositeIf = FormFactory.eINSTANCE.createFormElementIf();
            compositeIf.setName("CompositeProperty_Precondition");
            compositeIf.setPredicateExpression(AQLUtils.getServiceCallExpression(variableName, "isCompositeProperty"));
            compositeIf.getChildren().add(this.createCompositePropertyWidget(variableName, depth));
            widgets.add(compositeIf);
        }

        FormElementIf textFieldIf = FormFactory.eINSTANCE.createFormElementIf();
        textFieldIf.setName("TextFieldProperty_Precondition");
        textFieldIf.setPredicateExpression(AQLConstants.AQL
                + "not(" + variableName + ".isTextAreaProperty()) and not(" + variableName + ".isEnumProperty())"
                + " and not(" + variableName + ".isMeasurementProperty()) and not(" + variableName + ".isCompositeProperty())");
        textFieldIf.getChildren().add(this.createPropertyValueWidget(variableName));
        widgets.add(textFieldIf);

        return widgets;
    }

    private GroupDescription createReferenceGroup() {
        GroupDescription group = FormFactory.eINSTANCE.createGroupDescription();
        group.setDisplayMode(GroupDisplayMode.LIST);
        group.setName("CyberGridMLReferenceProperties");
        group.setLabelExpression("References");
        group.setSemanticCandidatesExpression(AQLConstants.AQL_SELF);
        group.getChildren().add(this.createReferenceWidgetsFor());
        return group;
    }

    private FormElementDescription createReferenceWidgetsFor() {
        FormElementFor forElt = FormFactory.eINSTANCE.createFormElementFor();
        forElt.setName("Widgets for Reference Properties");
        forElt.setIterator(REFERENCE_CANDIDATE);
        forElt.setIterableExpression(AQLUtils.getSelfServiceCallExpression("getReferencePropertyCandidates"));
        forElt.getChildren().addAll(this.createReferenceWidgets());
        return forElt;
    }

    private List<FormElementIf> createReferenceWidgets() {
        List<FormElementIf> widgets = new ArrayList<>();

        FormElementIf singleValuedIf = FormFactory.eINSTANCE.createFormElementIf();
        singleValuedIf.setName("SingleValuedReference_Precondition");
        singleValuedIf.setPredicateExpression(AQLConstants.AQL + "not(" + REFERENCE_CANDIDATE + ".isManyValuedReference())");
        singleValuedIf.getChildren().add(this.createReferencePropertySelectWidget());
        widgets.add(singleValuedIf);

        FormElementIf manyValuedIf = FormFactory.eINSTANCE.createFormElementIf();
        manyValuedIf.setName("ManyValuedReference_Precondition");
        manyValuedIf.setPredicateExpression(AQLUtils.getServiceCallExpression(REFERENCE_CANDIDATE, "isManyValuedReference"));
        manyValuedIf.getChildren().add(this.createReferencePropertyMultiSelectWidget());
        widgets.add(manyValuedIf);

        return widgets;
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

    private WidgetDescription createPropertyValueTextAreaWidget(String variableName) {
        TextAreaDescription textArea = FormFactory.eINSTANCE.createTextAreaDescription();
        textArea.setName("PropertyValueTextAreaWidget");
        textArea.setLabelExpression(AQLUtils.getServiceCallExpression(variableName, "getPropertyLabel"));
        textArea.setValueExpression(AQLUtils.getServiceCallExpression(variableName, "getPropertyValue"));
        textArea.setHelpExpression("Use 'shift + enter' to add new lines");
        textArea.setIsEnabledExpression(AQLConstants.AQL + "not(" + variableName + ".isReadOnly())");
        ChangeContext setTextAreaValueOperation = ViewFactory.eINSTANCE.createChangeContext();
        setTextAreaValueOperation.setExpression(AQLUtils.getServiceCallExpression(variableName, "setPropertyValue",
                ViewFormDescriptionConverter.NEW_VALUE));
        textArea.getBody().add(setTextAreaValueOperation);
        return textArea;
    }

    private WidgetDescription createPropertyValueWidget(String variableName) {
        TextfieldDescription textField = FormFactory.eINSTANCE.createTextfieldDescription();
        textField.setName("PropertyValueTextFieldWidget");
        textField.setLabelExpression(AQLUtils.getServiceCallExpression(variableName, "getPropertyLabel"));
        textField.setValueExpression(AQLUtils.getServiceCallExpression(variableName, "getPropertyValue"));

        ChangeContext setNewValueOperation = ViewFactory.eINSTANCE.createChangeContext();
        setNewValueOperation.setExpression(AQLUtils.getServiceCallExpression(variableName, "setPropertyValue",
                ViewFormDescriptionConverter.NEW_VALUE));
        textField.getBody().add(setNewValueOperation);

        return textField;
    }

    private WidgetDescription createPropertyValueSelectWidget(String variableName) {
        SelectDescription select = FormFactory.eINSTANCE.createSelectDescription();
        select.setName("PropertyValueSelectWidget");
        select.setLabelExpression(AQLUtils.getServiceCallExpression(variableName, "getPropertyLabel"));
        select.setCandidatesExpression(AQLUtils.getServiceCallExpression(variableName, "getEnumCandidates"));
        select.setCandidateLabelExpression("aql:candidate.declaredName");
        select.setValueExpression(AQLUtils.getServiceCallExpression(variableName, "getEnumPropertyValue"));
        select.setIsEnabledExpression(AQLConstants.AQL + "not(" + variableName + ".isReadOnly())");

        ChangeContext setNewValueOperation = ViewFactory.eINSTANCE.createChangeContext();
        setNewValueOperation.setExpression(AQLUtils.getServiceCallExpression(variableName, "setEnumPropertyValue",
                ViewFormDescriptionConverter.NEW_VALUE));
        select.getBody().add(setNewValueOperation);

        return select;
    }

    private WidgetDescription createMeasurementWidget(String variableName, int depth) {
        FlexboxContainerDescription container = FormFactory.eINSTANCE.createFlexboxContainerDescription();
        container.setName("MeasurementWidget");
        container.setFlexDirection(FlexDirection.COLUMN);
        container.getChildren().add(this.createPropertyNameWidget(variableName, depth));
        container.getChildren().add(this.createMeasurementFieldsRow(variableName));
        if (depth < MAX_PROPERTY_NESTING_DEPTH) {
            container.getChildren().add(this.createOptionalNestedPropertyGroup(
                    variableName, variableName + "Extra", "getMeasurementExtraProperties", depth));
        }
        return container;
    }

    private WidgetDescription createPropertyNameWidget(String variableName, int depth) {
        LabelDescription label = FormFactory.eINSTANCE.createLabelDescription();
        label.setName("PropertyNameWidget");
        label.setLabelExpression(AQLUtils.getServiceCallExpression(variableName, "getPropertyLabel"));
        label.setValueExpression("aql:''");
        LabelDescriptionStyle style = FormFactory.eINSTANCE.createLabelDescriptionStyle();
        style.setBold(true);
        style.setFontSize(depth == 0 ? 16 : 13);
        style.setColor(this.createFixedColor(depth == 0 ? "#0B5FFF" : "#6E7781"));
        label.setStyle(style);
        return label;
    }

    private org.eclipse.sirius.components.view.FixedColor createFixedColor(String hexValue) {
        org.eclipse.sirius.components.view.FixedColor color = ViewFactory.eINSTANCE.createFixedColor();
        color.setValue(hexValue);
        return color;
    }

    private WidgetDescription createMeasurementFieldsRow(String variableName) {
        FlexboxContainerDescription row = FormFactory.eINSTANCE.createFlexboxContainerDescription();
        row.setName("MeasurementFieldsRow");
        row.setFlexDirection(FlexDirection.ROW);
        row.getChildren().add(this.createMeasurementValueWidget(variableName));
        row.getChildren().add(this.createMeasurementUnitWidget(variableName));
        return row;
    }

    private WidgetDescription createMeasurementValueWidget(String variableName) {
        TextfieldDescription textField = FormFactory.eINSTANCE.createTextfieldDescription();
        textField.setName("MeasurementValueWidget");
        textField.setLabelExpression("Precision");
        textField.setValueExpression(
                "aql:" + variableName + ".getMeasurementValueAttribute().getPropertyValue()");
        textField.setStyle(this.alignMeasurementField(FormFactory.eINSTANCE.createTextfieldDescriptionStyle()));

        ChangeContext setNewValueOperation = ViewFactory.eINSTANCE.createChangeContext();
        setNewValueOperation.setExpression("aql:" + variableName
                + ".getMeasurementValueAttribute().setPropertyValue(" + ViewFormDescriptionConverter.NEW_VALUE + ")");
        textField.getBody().add(setNewValueOperation);

        return textField;
    }

    private WidgetDescription createMeasurementUnitWidget(String variableName) {
        SelectDescription select = FormFactory.eINSTANCE.createSelectDescription();
        select.setName("MeasurementUnitWidget");
        select.setLabelExpression("Unit");
        select.setCandidatesExpression(
                "aql:" + variableName + ".getMeasurementUnitAttribute().getEnumCandidates()");
        select.setCandidateLabelExpression("aql:candidate.declaredName");
        select.setValueExpression(
                "aql:" + variableName + ".getMeasurementUnitAttribute().getEnumPropertyValue()");
        select.setStyle(this.alignMeasurementField(FormFactory.eINSTANCE.createSelectDescriptionStyle()));

        ChangeContext setNewValueOperation = ViewFactory.eINSTANCE.createChangeContext();
        setNewValueOperation.setExpression("aql:" + variableName
                + ".getMeasurementUnitAttribute().setEnumPropertyValue(" + ViewFormDescriptionConverter.NEW_VALUE + ")");
        select.getBody().add(setNewValueOperation);

        return select;
    }

    private WidgetDescription createCompositePropertyWidget(String variableName, int depth) {
        FlexboxContainerDescription container = FormFactory.eINSTANCE.createFlexboxContainerDescription();
        container.setName("CompositePropertyWidget");
        container.setFlexDirection(FlexDirection.COLUMN);
        container.getChildren().add(this.createPropertyNameWidget(variableName, depth));
        container.getChildren().add(this.createOptionalNestedPropertyGroup(
                variableName, variableName + "Child", "getNestedPropertyCandidates", depth));
        return container;
    }

    private FormElementDescription createOptionalNestedPropertyGroup(String parentVariable, String childVariable,
            String iterableMethodName, int depth) {
        FormElementIf precondition = FormFactory.eINSTANCE.createFormElementIf();
        precondition.setName("NestedPropertyGroup_Precondition");
        precondition.setPredicateExpression(AQLConstants.AQL
                + "not(" + parentVariable + "." + iterableMethodName + "()->isEmpty())");

        FlexboxContainerDescription nestedGroup = FormFactory.eINSTANCE.createFlexboxContainerDescription();
        nestedGroup.setName("NestedPropertyGroup");
        nestedGroup.setFlexDirection(FlexDirection.COLUMN);
        ContainerBorderStyle borderStyle = FormFactory.eINSTANCE.createContainerBorderStyle();
        borderStyle.setBorderSize(1);
        borderStyle.setBorderRadius(6);
        borderStyle.setBorderColor(this.createFixedColor("#D0D7DE"));
        nestedGroup.setBorderStyle(borderStyle);
        nestedGroup.getChildren().add(this.createPropertyWidgetsFor(childVariable,
                AQLUtils.getServiceCallExpression(parentVariable, iterableMethodName), depth + 1));

        precondition.getChildren().add(nestedGroup);
        return precondition;
    }

    private <T extends org.eclipse.sirius.components.view.form.WidgetGridLayout> T alignMeasurementField(T style) {
        style.setGridTemplateColumns("1fr");
        style.setGridTemplateRows("auto auto");
        style.setLabelGridRow("1");
        style.setLabelGridColumn("1");
        style.setWidgetGridRow("2");
        style.setWidgetGridColumn("1");
        style.setGap("4px");
        return style;
    }

    private WidgetDescription createReferencePropertySelectWidget() {
        SelectDescription select = FormFactory.eINSTANCE.createSelectDescription();
        select.setName("ReferencePropertyValueSelectWidget");
        select.setLabelExpression(AQLUtils.getServiceCallExpression(REFERENCE_CANDIDATE, "getPropertyLabel"));
        select.setCandidatesExpression(AQLUtils.getServiceCallExpression(REFERENCE_CANDIDATE, "getReferenceCandidates"));
        select.setCandidateLabelExpression("aql:candidate.declaredName");
        select.setValueExpression(AQLUtils.getServiceCallExpression(REFERENCE_CANDIDATE, "getSingleReferencePropertyValue"));
        select.setIsEnabledExpression(AQLConstants.AQL + "not(" + REFERENCE_CANDIDATE + ".isReadOnly())");

        ChangeContext setNewValueOperation = ViewFactory.eINSTANCE.createChangeContext();
        setNewValueOperation.setExpression(AQLUtils.getServiceCallExpression(REFERENCE_CANDIDATE, "setSingleReferencePropertyValue",
                ViewFormDescriptionConverter.NEW_VALUE));
        select.getBody().add(setNewValueOperation);

        return select;
    }

    private WidgetDescription createReferencePropertyMultiSelectWidget() {
        MultiSelectDescription multiSelect = FormFactory.eINSTANCE.createMultiSelectDescription();
        multiSelect.setName("ReferencePropertyValueMultiSelectWidget");
        multiSelect.setLabelExpression(AQLUtils.getServiceCallExpression(REFERENCE_CANDIDATE, "getPropertyLabel"));
        multiSelect.setCandidatesExpression(AQLUtils.getServiceCallExpression(REFERENCE_CANDIDATE, "getReferenceCandidates"));
        multiSelect.setCandidateLabelExpression("aql:candidate.declaredName");
        multiSelect.setValueExpression(AQLUtils.getServiceCallExpression(REFERENCE_CANDIDATE, "getReferencePropertyValue"));
        multiSelect.setIsEnabledExpression(AQLConstants.AQL + "not(" + REFERENCE_CANDIDATE + ".isReadOnly())");

        ChangeContext setNewValueOperation = ViewFactory.eINSTANCE.createChangeContext();
        setNewValueOperation.setExpression(AQLUtils.getServiceCallExpression(REFERENCE_CANDIDATE, "setReferencePropertyValues",
                ViewFormDescriptionConverter.NEW_VALUE));
        multiSelect.getBody().add(setNewValueOperation);

        return multiSelect;
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

    public List<AttributeUsage> getPropertyCandidates(Element element) {
        if (element instanceof Type type) {
            List<AttributeUsage> nested = type.getOwnedFeature().stream()
                    .filter(AttributeUsage.class::isInstance)
                    .map(AttributeUsage.class::cast)
                    .toList();
            if (!nested.isEmpty()) {
                return nested;
            }
        }
        if (element instanceof AttributeUsage attribute) {
            return List.of(attribute);
        }
        return List.of();
    }

    public List<AttributeUsage> getNestedPropertyCandidates(AttributeUsage attribute) {
        return attribute.getOwnedFeature().stream()
                .filter(AttributeUsage.class::isInstance)
                .map(AttributeUsage.class::cast)
                .toList();
    }

    public List<AttributeUsage> getMeasurementExtraProperties(AttributeUsage attribute) {
        return attribute.getOwnedFeature().stream()
                .filter(AttributeUsage.class::isInstance)
                .map(AttributeUsage.class::cast)
                .filter(child -> !"unit".equals(child.getDeclaredName()) && !"precision".equals(child.getDeclaredName()))
                .toList();
    }

    public boolean isCompositeProperty(Element element) {
        return element instanceof Type type
                && type.getOwnedFeature().stream().anyMatch(AttributeUsage.class::isInstance)
                && !this.isMeasurementProperty(element);
    }

    public boolean isMeasurementProperty(Element element) {
        return element instanceof AttributeUsage attribute
                && this.getMeasurementUnitAttribute(attribute) != null
                && this.getMeasurementValueAttribute(attribute) != null;
    }

    public AttributeUsage getMeasurementUnitAttribute(AttributeUsage attribute) {
        return this.getNestedAttributeByName(attribute, "unit");
    }

    public AttributeUsage getMeasurementValueAttribute(AttributeUsage attribute) {
        return this.getNestedAttributeByName(attribute, "precision");
    }

    private AttributeUsage getNestedAttributeByName(AttributeUsage attribute, String name) {
        return attribute.getOwnedFeature().stream()
                .filter(AttributeUsage.class::isInstance)
                .map(AttributeUsage.class::cast)
                .filter(child -> name.equals(child.getDeclaredName()))
                .findFirst()
                .orElse(null);
    }

    public List<ReferenceUsage> getReferencePropertyCandidates(Element element) {
        if (element instanceof Type type) {
            List<ReferenceUsage> nested = type.getOwnedFeature().stream()
                    .filter(ReferenceUsage.class::isInstance)
                    .map(ReferenceUsage.class::cast)
                    .toList();
            if (!nested.isEmpty()) {
                return nested;
            }
        }
        if (element instanceof ReferenceUsage reference) {
            return List.of(reference);
        }
        return List.of();
    }

    public String getPropertyLabel(Element element) {
        String name = element.getDeclaredName();
        if (name == null || name.isEmpty()) {
            return "";
        }
        String withSpaces = name.replace('_', ' ');
        return withSpaces.substring(0, 1).toUpperCase()
                + withSpaces.substring(1).replaceAll("([A-Z])", " $1").trim();
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
