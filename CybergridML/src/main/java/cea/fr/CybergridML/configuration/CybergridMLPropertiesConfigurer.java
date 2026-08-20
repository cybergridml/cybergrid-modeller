package cea.fr.CybergridML.configuration;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import cea.fr.CybergridML.domain.CybergridMLLibElementType;
import cea.fr.CybergridML.domain.CybergridMLPropertyUtil;
import cea.fr.CybergridML.util.CybergridMLAQLUtils;
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
import org.eclipse.syson.sysml.Feature;
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
import org.eclipse.syson.util.SysMLMetamodelHelper;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CybergridMLPropertiesConfigurer implements IPropertiesDescriptionRegistryConfigurer {

    private static final String GET_DETAILS_VIEW_LABEL_SERVICE = "getDetailsViewLabel";
    private static final String E_STRUCTURAL_FEATURE = "eStructuralFeature";
    private static final String AQL_NOT_SELF_IS_READ_ONLY_E_STRUCTURAL_FEATURE = "aql:not(self.isReadOnly(eStructuralFeature))";
    private static final String AQL_NOT_SELF_IS_READ_ONLY = "aql:not(self.isReadOnly())";
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
        pageCore.getGroups().add(this.createChildrenGroup());
        pageCore.getGroups().add(this.createReferenceGroup());

        form.getPages().add(pageCore);
        return form;
    }

    private GroupDescription createCommonGroup() {
        GroupDescription group = FormFactory.eINSTANCE.createGroupDescription();
        group.setDisplayMode(GroupDisplayMode.LIST);
        group.setName("Properties");
        group.setLabelExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("getLabel"));
        group.setSemanticCandidatesExpression(AQLConstants.AQL_SELF);

        group.getChildren().add(this.createCoreWidgets());
        group.getChildren().add(this.createDocumentationWidget());

        FormElementIf typedByIf = FormFactory.eINSTANCE.createFormElementIf();
        typedByIf.setName("TypedBy_Precondition");
        typedByIf.setPredicateExpression(AQLConstants.AQL + "not(self.isTextAreaProperty())");
        typedByIf.getChildren().add(this.createTypedByWidget());
        group.getChildren().add(typedByIf);

        group.getChildren().add(this.createOwnedReferencesIf());

        return group;
    }

    private FormElementDescription createOwnedReferencesIf() {
        FormElementIf precondition = FormFactory.eINSTANCE.createFormElementIf();
        precondition.setName("OwnedReferences_Precondition");
        precondition.setPredicateExpression(AQLConstants.AQL
                + "self.ownedFeature->exists(e | e.isReferenceProperty()) and self.getReferencePropertyCandidates()->isEmpty()");
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

    private GroupDescription createPropertyGroup() {
        GroupDescription group = FormFactory.eINSTANCE.createGroupDescription();
        group.setDisplayMode(GroupDisplayMode.LIST);
        group.setName("CyberGridMLAttributeProperties");
        group.setLabelExpression("Properties");
        group.setSemanticCandidatesExpression(AQLConstants.AQL_SELF);
        group.getChildren().add(this.createPropertyWidgetsFor(
                CybergridMLAQLUtils.getSelfServiceCallExpression("getPropertyCandidates"), 0));
        return group;
    }

    private FormElementDescription createPropertyWidgetsFor(String iterableExpression, int depth) {
        FormElementFor forElt = FormFactory.eINSTANCE.createFormElementFor();
        forElt.setName("Widgets for Attribute Properties (depth " + depth + ")");
        forElt.setIterator(AQLConstants.SELF);
        forElt.setIterableExpression(iterableExpression);
        forElt.getChildren().addAll(this.createPropertyWidgets(depth));
        return forElt;
    }

    private List<FormElementIf> createPropertyWidgets(int depth) {
        List<FormElementIf> widgets = new ArrayList<>();

        FormElementIf textAreaIf = FormFactory.eINSTANCE.createFormElementIf();
        textAreaIf.setName("TextAreaProperty_Precondition");
        textAreaIf.setPredicateExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("isTextAreaProperty"));
        textAreaIf.getChildren().add(this.createPropertyValueTextAreaWidget());
        widgets.add(textAreaIf);

        FormElementIf enumIf = FormFactory.eINSTANCE.createFormElementIf();
        enumIf.setName("EnumProperty_Precondition");
        enumIf.setPredicateExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("isEnumProperty"));
        enumIf.getChildren().add(this.createPropertyValueSelectWidget());
        widgets.add(enumIf);

        FormElementIf measurementIf = FormFactory.eINSTANCE.createFormElementIf();
        measurementIf.setName("MeasurementProperty_Precondition");
        measurementIf.setPredicateExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("isMeasurementProperty"));
        measurementIf.getChildren().add(this.createMeasurementWidget(depth));
        widgets.add(measurementIf);

        if (depth < MAX_PROPERTY_NESTING_DEPTH) {
            FormElementIf compositeIf = FormFactory.eINSTANCE.createFormElementIf();
            compositeIf.setName("CompositeProperty_Precondition");
            compositeIf.setPredicateExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("isCompositeProperty"));
            compositeIf.getChildren().add(this.createCompositePropertyWidget(depth));
            widgets.add(compositeIf);
        }

        FormElementIf textFieldIf = FormFactory.eINSTANCE.createFormElementIf();
        textFieldIf.setName("TextFieldProperty_Precondition");
        textFieldIf.setPredicateExpression(AQLConstants.AQL
                + "not(self.isTextAreaProperty()) and not(self.isEnumProperty())"
                + " and not(self.isMeasurementProperty()) and not(self.isCompositeProperty())");
        textFieldIf.getChildren().add(this.createPropertyValueWidget());
        widgets.add(textFieldIf);

        return widgets;
    }

    /**
     * Group listing the parts directly nested under the current element (e.g. the Buses of a
     * Substation, or the Terminals/Generators/Loads of a Bus), so the hierarchy stays navigable
     * without inlining each child's own properties here.
     *
     * <p>Uses a plain "for" loop over a Java service (like the Properties/References groups above)
     * rather than a {@code ReferenceWidget}: three different ReferenceWidget configurations (derived
     * feature, real stored feature, and after adding the missing converter/GraphQL modules to the
     * POM) all rendered nothing in this deployment, so it is not usable here for now. Click-to-select
     * for these children still works through the standard Explorer tree.</p>
     */
    private GroupDescription createChildrenGroup() {
        GroupDescription group = FormFactory.eINSTANCE.createGroupDescription();
        group.setDisplayMode(GroupDisplayMode.LIST);
        group.setName("CyberGridMLChildElements");
        group.setLabelExpression("Children");
        group.setSemanticCandidatesExpression(AQLConstants.AQL_SELF
                + "->select(e | not(e.getChildPartCandidates()->isEmpty()))");
        group.getChildren().add(this.createChildElementWidgetsFor());
        return group;
    }

    private FormElementDescription createChildElementWidgetsFor() {
        FormElementFor forElt = FormFactory.eINSTANCE.createFormElementFor();
        forElt.setName("Widgets for Child Elements");
        forElt.setIterator(AQLConstants.SELF);
        forElt.setIterableExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("getChildPartCandidates"));
        forElt.getChildren().add(this.createChildElementWidget());
        return forElt;
    }

    private WidgetDescription createChildElementWidget() {
        LabelDescription label = FormFactory.eINSTANCE.createLabelDescription();
        label.setName("ChildElementLabel");
        label.setLabelExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("getChildElementKind"));
        label.setValueExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("getLabel"));
        return label;
    }

    private GroupDescription createReferenceGroup() {
        GroupDescription group = FormFactory.eINSTANCE.createGroupDescription();
        group.setDisplayMode(GroupDisplayMode.LIST);
        group.setName("CyberGridMLReferenceProperties");
        group.setLabelExpression("References");
        group.setSemanticCandidatesExpression(AQLConstants.AQL_SELF
                + "->select(e | not(e.getReferencePropertyCandidates()->isEmpty()))");
        group.getChildren().add(this.createReferenceWidgetsFor());
        return group;
    }

    private FormElementDescription createReferenceWidgetsFor() {
        FormElementFor forElt = FormFactory.eINSTANCE.createFormElementFor();
        forElt.setName("Widgets for Reference Properties");
        forElt.setIterator(AQLConstants.SELF);
        forElt.setIterableExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("getReferencePropertyCandidates"));
        forElt.getChildren().addAll(this.createReferenceWidgets());
        return forElt;
    }

    private List<FormElementIf> createReferenceWidgets() {
        List<FormElementIf> widgets = new ArrayList<>();

        FormElementIf singleValuedIf = FormFactory.eINSTANCE.createFormElementIf();
        singleValuedIf.setName("SingleValuedReference_Precondition");
        singleValuedIf.setPredicateExpression(AQLConstants.AQL + "not(self.isManyValuedReference())");
        singleValuedIf.getChildren().add(this.createReferencePropertySelectWidget());
        widgets.add(singleValuedIf);

        FormElementIf manyValuedIf = FormFactory.eINSTANCE.createFormElementIf();
        manyValuedIf.setName("ManyValuedReference_Precondition");
        manyValuedIf.setPredicateExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("isManyValuedReference"));
        manyValuedIf.getChildren().add(this.createReferencePropertyMultiSelectWidget());
        widgets.add(manyValuedIf);

        return widgets;
    }

    private FormElementDescription createCoreWidgets() {
        FormElementFor forElt = FormFactory.eINSTANCE.createFormElementFor();
        forElt.setName("Widgets for Core Group");
        forElt.setIterator(E_STRUCTURAL_FEATURE);
        forElt.setIterableExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("getCoreFeatures"));
        forElt.getChildren().addAll(this.createWidgets());
        return forElt;
    }

    private List<FormElementIf> createWidgets() {
        List<FormElementIf> widgets = new ArrayList<>();

        FormElementIf label = FormFactory.eINSTANCE.createFormElementIf();
        label.setName("Read-only String Attributes");
        label.setPredicateExpression(
                CybergridMLAQLUtils.getSelfServiceCallExpression("isReadOnlyStringAttribute", E_STRUCTURAL_FEATURE));
        label.getChildren().add(this.createLabelWidget());
        widgets.add(label);

        FormElementIf textfield = FormFactory.eINSTANCE.createFormElementIf();
        textfield.setName("String Attributes");
        textfield.setPredicateExpression(
                CybergridMLAQLUtils.getSelfServiceCallExpression("isStringAttribute", E_STRUCTURAL_FEATURE));
        textfield.getChildren().add(this.createTextfieldWidget());
        widgets.add(textfield);

        return widgets;
    }

    private WidgetDescription createLabelWidget() {
        LabelDescription label = FormFactory.eINSTANCE.createLabelDescription();
        label.setName("LabelWidget");
        label.setLabelExpression(
                CybergridMLAQLUtils.getSelfServiceCallExpression(GET_DETAILS_VIEW_LABEL_SERVICE, E_STRUCTURAL_FEATURE));
        label.setValueExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("eGet", E_STRUCTURAL_FEATURE));
        return label;
    }

    private WidgetDescription createTextfieldWidget() {
        TextfieldDescription textfield = FormFactory.eINSTANCE.createTextfieldDescription();
        textfield.setName("TextfieldWidget");
        textfield.setLabelExpression(
                CybergridMLAQLUtils.getSelfServiceCallExpression(GET_DETAILS_VIEW_LABEL_SERVICE, E_STRUCTURAL_FEATURE));
        textfield.setValueExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("eGet", E_STRUCTURAL_FEATURE));
        textfield.setIsEnabledExpression(AQL_NOT_SELF_IS_READ_ONLY_E_STRUCTURAL_FEATURE);
        ChangeContext setNewValueOperation = ViewFactory.eINSTANCE.createChangeContext();
        setNewValueOperation.setExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("setNewValue",
                List.of(E_STRUCTURAL_FEATURE, ViewFormDescriptionConverter.NEW_VALUE)));
        textfield.getBody().add(setNewValueOperation);
        return textfield;
    }

    private FormElementDescription createDocumentationWidget() {
        TextAreaDescription textarea = FormFactory.eINSTANCE.createTextAreaDescription();
        textarea.setName("DocumentationWidget");
        textarea.setLabelExpression("Documentation");
        textarea.setValueExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("getDocumentation"));
        textarea.setHelpExpression("Use 'shift + enter' to add new lines");
        textarea.setIsEnabledExpression(AQLConstants.AQL + "not(self.isReadOnly())");
        ChangeContext setNewValueOperation = ViewFactory.eINSTANCE.createChangeContext();
        setNewValueOperation.setExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("setNewDocumentationValue",
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
        textArea.setLabelExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("getPropertyLabel"));
        textArea.setValueExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("getPropertyValue"));
        textArea.setHelpExpression("Use 'shift + enter' to add new lines");
        textArea.setIsEnabledExpression(AQL_NOT_SELF_IS_READ_ONLY);
        ChangeContext setTextAreaValueOperation = ViewFactory.eINSTANCE.createChangeContext();
        setTextAreaValueOperation.setExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("setPropertyValue",
                ViewFormDescriptionConverter.NEW_VALUE));
        textArea.getBody().add(setTextAreaValueOperation);
        return textArea;
    }

    private WidgetDescription createPropertyValueWidget() {
        TextfieldDescription textField = FormFactory.eINSTANCE.createTextfieldDescription();
        textField.setName("PropertyValueTextFieldWidget");
        textField.setLabelExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("getPropertyLabel"));
        textField.setValueExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("getPropertyValue"));

        ChangeContext setNewValueOperation = ViewFactory.eINSTANCE.createChangeContext();
        setNewValueOperation.setExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("setPropertyValue",
                ViewFormDescriptionConverter.NEW_VALUE));
        textField.getBody().add(setNewValueOperation);

        return textField;
    }

    private WidgetDescription createPropertyValueSelectWidget() {
        SelectDescription select = FormFactory.eINSTANCE.createSelectDescription();
        select.setName("PropertyValueSelectWidget");
        select.setLabelExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("getPropertyLabel"));
        select.setCandidatesExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("getEnumCandidates"));
        select.setCandidateLabelExpression("aql:candidate.declaredName");
        select.setValueExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("getEnumPropertyValue"));
        select.setIsEnabledExpression(AQL_NOT_SELF_IS_READ_ONLY);

        ChangeContext setNewValueOperation = ViewFactory.eINSTANCE.createChangeContext();
        setNewValueOperation.setExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("setEnumPropertyValue",
                ViewFormDescriptionConverter.NEW_VALUE));
        select.getBody().add(setNewValueOperation);

        return select;
    }

    private WidgetDescription createMeasurementWidget(int depth) {
        FlexboxContainerDescription container = FormFactory.eINSTANCE.createFlexboxContainerDescription();
        container.setName("MeasurementWidget");
        container.setFlexDirection(FlexDirection.COLUMN);
        container.getChildren().add(this.createPropertyNameWidget(depth));
        container.getChildren().add(this.createMeasurementFieldsRow());
        if (depth < MAX_PROPERTY_NESTING_DEPTH) {
            container.getChildren().add(this.createOptionalNestedPropertyGroup("getMeasurementExtraProperties", depth));
        }
        return container;
    }

    private WidgetDescription createPropertyNameWidget(int depth) {
        LabelDescription label = FormFactory.eINSTANCE.createLabelDescription();
        label.setName("PropertyNameWidget");
        label.setLabelExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("getPropertyLabel"));
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

    private WidgetDescription createMeasurementFieldsRow() {
        FlexboxContainerDescription row = FormFactory.eINSTANCE.createFlexboxContainerDescription();
        row.setName("MeasurementFieldsRow");
        row.setFlexDirection(FlexDirection.ROW);
        row.getChildren().add(this.createMeasurementValueWidget());
        row.getChildren().add(this.createMeasurementUnitWidget());
        return row;
    }

    private WidgetDescription createMeasurementValueWidget() {
        TextfieldDescription textField = FormFactory.eINSTANCE.createTextfieldDescription();
        textField.setName("MeasurementValueWidget");
        textField.setLabelExpression("Precision");
        textField.setValueExpression("aql:self.getMeasurementValueAttribute().getPropertyValue()");
        textField.setStyle(this.alignMeasurementField(FormFactory.eINSTANCE.createTextfieldDescriptionStyle()));

        ChangeContext setNewValueOperation = ViewFactory.eINSTANCE.createChangeContext();
        setNewValueOperation.setExpression("aql:self.getMeasurementValueAttribute().setPropertyValue("
                + ViewFormDescriptionConverter.NEW_VALUE + ")");
        textField.getBody().add(setNewValueOperation);

        return textField;
    }

    private WidgetDescription createMeasurementUnitWidget() {
        SelectDescription select = FormFactory.eINSTANCE.createSelectDescription();
        select.setName("MeasurementUnitWidget");
        select.setLabelExpression("Unit");
        select.setCandidatesExpression("aql:self.getMeasurementUnitAttribute().getEnumCandidates()");
        select.setCandidateLabelExpression("aql:candidate.declaredName");
        select.setValueExpression("aql:self.getMeasurementUnitAttribute().getEnumPropertyValue()");
        select.setStyle(this.alignMeasurementField(FormFactory.eINSTANCE.createSelectDescriptionStyle()));

        ChangeContext setNewValueOperation = ViewFactory.eINSTANCE.createChangeContext();
        setNewValueOperation.setExpression("aql:self.getMeasurementUnitAttribute().setEnumPropertyValue("
                + ViewFormDescriptionConverter.NEW_VALUE + ")");
        select.getBody().add(setNewValueOperation);

        return select;
    }

    private WidgetDescription createCompositePropertyWidget(int depth) {
        FlexboxContainerDescription container = FormFactory.eINSTANCE.createFlexboxContainerDescription();
        container.setName("CompositePropertyWidget");
        container.setFlexDirection(FlexDirection.COLUMN);
        container.getChildren().add(this.createPropertyNameWidget(depth));
        container.getChildren().add(this.createOptionalNestedPropertyGroup("getNestedPropertyCandidates", depth));
        return container;
    }

    private FormElementDescription createOptionalNestedPropertyGroup(String iterableMethodName, int depth) {
        FormElementIf precondition = FormFactory.eINSTANCE.createFormElementIf();
        precondition.setName("NestedPropertyGroup_Precondition");
        precondition.setPredicateExpression(AQLConstants.AQL
                + "not(self." + iterableMethodName + "()->isEmpty())");

        FlexboxContainerDescription nestedGroup = FormFactory.eINSTANCE.createFlexboxContainerDescription();
        nestedGroup.setName("NestedPropertyGroup");
        nestedGroup.setFlexDirection(FlexDirection.COLUMN);
        ContainerBorderStyle borderStyle = FormFactory.eINSTANCE.createContainerBorderStyle();
        borderStyle.setBorderSize(1);
        borderStyle.setBorderRadius(6);
        borderStyle.setBorderColor(this.createFixedColor("#D0D7DE"));
        nestedGroup.setBorderStyle(borderStyle);
        nestedGroup.getChildren().add(this.createPropertyWidgetsFor(
                CybergridMLAQLUtils.getSelfServiceCallExpression(iterableMethodName), depth + 1));

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
        select.setLabelExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("getPropertyLabel"));
        select.setCandidatesExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("getReferenceCandidates"));
        select.setCandidateLabelExpression("aql:candidate.declaredName");
        select.setValueExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("getSingleReferencePropertyValue"));
        select.setIsEnabledExpression(AQL_NOT_SELF_IS_READ_ONLY);

        ChangeContext setNewValueOperation = ViewFactory.eINSTANCE.createChangeContext();
        setNewValueOperation.setExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("setSingleReferencePropertyValue",
                ViewFormDescriptionConverter.NEW_VALUE));
        select.getBody().add(setNewValueOperation);

        return select;
    }

    private WidgetDescription createReferencePropertyMultiSelectWidget() {
        MultiSelectDescription multiSelect = FormFactory.eINSTANCE.createMultiSelectDescription();
        multiSelect.setName("ReferencePropertyValueMultiSelectWidget");
        multiSelect.setLabelExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("getPropertyLabel"));
        multiSelect.setCandidatesExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("getReferenceCandidates"));
        multiSelect.setCandidateLabelExpression("aql:candidate.declaredName");
        multiSelect.setValueExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("getReferencePropertyValue"));
        multiSelect.setIsEnabledExpression(AQL_NOT_SELF_IS_READ_ONLY);

        ChangeContext setNewValueOperation = ViewFactory.eINSTANCE.createChangeContext();
        setNewValueOperation.setExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("setReferencePropertyValues",
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
        setRefWidget.setExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("handleFeatureTypingNewValue",
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

    public List<PartUsage> getChildPartCandidates(Element element) {
        if (element instanceof org.eclipse.syson.sysml.Usage usage) {
            return usage.getNestedPart();
        }
        return List.of();
    }

    public String getChildElementKind(Element element) {
        if (element instanceof Feature feature) {
            CybergridMLLibElementType type = CybergridMLLibElementType.getCybergridMLElementFromUsage(feature);
            if (type != null) {
                return type.getName();
            }
        }
        return element.eClass().getName();
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
                    .filter(this::isEditableReference)
                    .toList();
            if (!nested.isEmpty()) {
                return nested;
            }
        }
        if (element instanceof ReferenceUsage reference && this.isEditableReference(reference)) {
            return List.of(reference);
        }
        return List.of();
    }

    private boolean isEditableReference(ReferenceUsage usage) {
        Type owningType = usage.getOwningType();
        return owningType instanceof Feature owningFeature
                && owningFeature.getType().stream()
                        .map(Type::getQualifiedName)
                        .anyMatch(CybergridMLLibElementType.REDUNDANCYCLUSTER.getCybergridMLLibDefinitionName()::equals);
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
