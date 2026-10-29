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

package cea.fr.CybergridML.services.xiidm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.FeatureMembership;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.SysmlFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Checks the structure produced for the IEEE-14 network against the hand-made
 * {@code sysml.cybergridML.libraries/ieee14.sysml} reference.
 *
 * <p>The editing context is a stub, so the CybergridML library is not resolved and the created parts carry neither
 * feature typing nor attributes. Everything this test asserts — containment, naming and counts — is independent of
 * the library, and is exactly what used to be done by hand.
 */
class XiidmToCybergridMLMapperTest {

    private XiidmToCybergridMLMapper mapper;

    private IEditingContext editingContext;

    private PartUsage grid;

    @BeforeEach
    void setUp() {
        this.mapper = new XiidmToCybergridMLMapper();
        this.editingContext = mock(IEditingContext.class);
        this.grid = SysmlFactory.eINSTANCE.createPartUsage();
        this.grid.setDeclaredName("maingrid1");
    }

    @Test
    void createsTheWholeNetworkUnderTheGrid() throws Exception {
        XiidmImportResult result = this.importIeee14();

        assertThat(result.substations()).isEqualTo(11);
        // Only the first bus of each substation is imported:
        assertThat(result.buses()).isEqualTo(11);
        // B6-G, B6-L and B9-L are connected to buses that are not imported.
        assertThat(result.generators()).isEqualTo(4);
        assertThat(result.loads()).isEqualTo(9);
        // 17 lines and 3 transformers, one terminal at either end, except the 11 ends on B6, B7 and B9.
        assertThat(result.terminals()).isEqualTo(40 - 11);
        // Each generator, load, and terminal gets a LocalController.
        assertThat(result.localControllers()).isEqualTo(4 + 9 + 29);
        // ComputingResources and SoftwareResources are created under CRA view (not available in test stub).
        assertThat(result.computingResources()).isEqualTo(0);
        assertThat(result.softwareResources()).isEqualTo(0);

        assertThat(result.createdRootElements()).hasSize(11);
    }

    @Test
    void importsOnlyTheFirstBusOfEachSubstation() throws Exception {
        this.importIeee14();

        // S4 has three voltage levels (B4, B7, B9) and S5 two (B5, B6) in the XIIDM file: only one level matters.
        assertThat(names(this.child(this.grid, "S4"))).containsExactly("B4");
        assertThat(names(this.child(this.grid, "S5"))).containsExactly("B5");
        assertThat(names(this.child(this.grid, "S1"))).containsExactly("B1");
    }

    @Test
    void createsGeneratorsLoadsAndOneTerminalPerBranchEndUnderTheirBus() throws Exception {
        this.importIeee14();

        Element b1 = this.child(this.child(this.grid, "S1"), "B1");
        assertThat(names(b1)).containsExactly("B1_G", "L1_2_1_1", "L1_5_1_1");

        Element b2 = this.child(this.child(this.grid, "S2"), "B2");
        assertThat(names(b2)).containsExactly("B2_G", "B2_L", "L1_2_1_2", "L2_3_1_1", "L2_4_1_1", "L2_5_1_1");

        // Transformers are mapped exactly like lines; their ends on B7 and B9 are not imported with those buses.
        Element b4 = this.child(this.child(this.grid, "S4"), "B4");
        assertThat(names(b4)).contains("T4_7_1_1", "T4_9_1_1").doesNotContain("T4_7_1_2", "T4_9_1_2");
    }

    @Test
    void createsLocalControllerUnderEachEquipmentAndTerminal() throws Exception {
        this.importIeee14();

        // Check that each generator has a LocalController child
        Element b1 = this.child(this.child(this.grid, "S1"), "B1");
        Element generator = this.child(b1, "B1_G");
        assertThat(names(generator)).containsExactly("LC_B1_G");

        // Check that each load has a LocalController child
        Element b2 = this.child(this.child(this.grid, "S2"), "B2");
        Element load = this.child(b2, "B2_L");
        assertThat(names(load)).containsExactly("LC_B2_L");

        // Check that each terminal has a LocalController child
        Element terminal = this.child(b1, "L1_2_1_1");
        assertThat(names(terminal)).containsExactly("LC_L1_2_1_1");
    }

    @Test
    void reportsWhatItCouldNotMap() throws Exception {
        XiidmImportResult result = this.importIeee14();

        assertThat(result.warnings()).containsExactly(
                "Shunt compensator 'B9-SH' has no CybergridML equivalent and was skipped.",
                "Only the first bus of each substation is imported: 14 equipment(s) and terminal(s) on the other buses were skipped.");
    }

    @Test
    void skipsBranchEndsPointingAtAnUnknownBus()  {
        var network = new XiidmNetwork("n", List.of(new XiidmNetwork.Substation("S1",
                List.of(new XiidmNetwork.VoltageLevel("VL1", 135.0,
                        List.of(new XiidmNetwork.Bus("B1", "Bus 1", null, null)), List.of())))),
                List.of(new XiidmNetwork.Branch(XiidmNetwork.BranchKind.LINE, "L1-99-1", "B1", "B99")));

        XiidmImportResult result = this.mapper.importInto(this.grid, network, this.editingContext);

        assertThat(result.terminals()).isEqualTo(1);
        assertThat(names(this.child(this.child(this.grid, "S1"), "B1"))).containsExactly("L1_99_1_1");
        assertThat(result.warnings())
                .containsExactly("'L1-99-1' references the unknown bus 'B99' on side 2; that terminal was skipped.");
    }

    private XiidmImportResult importIeee14() throws Exception {
        var network = new XiidmParser().parse(this.ieee14());
        return this.mapper.importInto(this.grid, network, this.editingContext);
    }

    private Element child(Element owner, String declaredName) {
        return children(owner).stream()
                .filter(element -> declaredName.equals(element.getDeclaredName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No child named '%s' under '%s'".formatted(declaredName, owner.getDeclaredName())));
    }

    private static List<String> names(Element owner) {
        return children(owner).stream().map(Element::getDeclaredName).toList();
    }

    private static List<Element> children(Element owner) {
        return owner.getOwnedRelationship().stream()
                .filter(FeatureMembership.class::isInstance)
                .map(FeatureMembership.class::cast)
                .flatMap(featureMembership -> featureMembership.getOwnedRelatedElement().stream())
                .toList();
    }

    private String ieee14() throws IOException {
        try (InputStream inputStream = XiidmToCybergridMLMapperTest.class.getResourceAsStream("/xiidm/ieee14.xiidm")) {
            assert inputStream != null;
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
