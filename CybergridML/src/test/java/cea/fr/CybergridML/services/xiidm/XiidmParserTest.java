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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Reads the IEEE-14 network shipped as a test resource, the same file the hand-made
 * {@code sysml.cybergridML.libraries/ieee14.sysml} was transcribed from.
 */
class XiidmParserTest {

    private XiidmParser parser;

    @BeforeEach
    void setUp() {
        this.parser = new XiidmParser();
    }

    @Test
    void readsEverySubstationBusAndBranchOfIeee14() throws Exception {
        XiidmNetwork network = this.parser.parse(this.ieee14());

        assertThat(network.id()).isEqualTo("ieee14cdf");
        assertThat(network.substations()).extracting(XiidmNetwork.Substation::id)
                .containsExactly("S1", "S2", "S3", "S4", "S5", "S8", "S10", "S11", "S12", "S13", "S14");

        List<XiidmNetwork.Bus> buses = network.substations().stream()
                .flatMap(substation -> substation.voltageLevels().stream())
                .flatMap(voltageLevel -> voltageLevel.buses().stream())
                .toList();
        assertThat(buses).hasSize(14);

        // 17 lines plus 3 two windings transformers.
        assertThat(network.branches()).hasSize(20);
        assertThat(network.branches()).filteredOn(branch -> branch.kind() == XiidmNetwork.BranchKind.TWO_WINDINGS_TRANSFORMER)
                .extracting(XiidmNetwork.Branch::id)
                .containsExactlyInAnyOrder("T4-7-1", "T4-9-1", "T5-6-1");
    }

    @Test
    void flattensTheThreeVoltageLevelsOfSubstationS4() throws Exception {
        XiidmNetwork network = this.parser.parse(this.ieee14());

        XiidmNetwork.Substation s4 = network.substations().stream()
                .filter(substation -> "S4".equals(substation.id()))
                .findFirst()
                .orElseThrow();

        assertThat(s4.voltageLevels()).extracting(XiidmNetwork.VoltageLevel::id).containsExactly("VL4", "VL7", "VL9");
        assertThat(s4.voltageLevels()).extracting(XiidmNetwork.VoltageLevel::nominalV).containsExactly(135.0, 14.0, 12.0);
        assertThat(s4.voltageLevels().stream().flatMap(voltageLevel -> voltageLevel.buses().stream()))
                .extracting(XiidmNetwork.Bus::id)
                .containsExactly("B4", "B7", "B9");
    }

    @Test
    void readsGeneratorsLoadsAndShuntCompensatorsWithTheirValues() throws Exception {
        XiidmNetwork network = this.parser.parse(this.ieee14());

        List<XiidmNetwork.Equipment> equipments = network.substations().stream()
                .flatMap(substation -> substation.voltageLevels().stream())
                .flatMap(voltageLevel -> voltageLevel.equipments().stream())
                .toList();

        assertThat(equipments).filteredOn(equipment -> equipment.kind() == XiidmNetwork.EquipmentKind.GENERATOR).hasSize(5);
        assertThat(equipments).filteredOn(equipment -> equipment.kind() == XiidmNetwork.EquipmentKind.LOAD).hasSize(11);
        assertThat(equipments).filteredOn(equipment -> equipment.kind() == XiidmNetwork.EquipmentKind.SHUNT_COMPENSATOR)
                .extracting(XiidmNetwork.Equipment::id)
                .containsExactly("B9-SH");

        XiidmNetwork.Equipment firstGenerator = equipments.get(0);
        assertThat(firstGenerator.id()).isEqualTo("B1-G");
        assertThat(firstGenerator.busId()).isEqualTo("B1");
        assertThat(firstGenerator.p()).isEqualTo(232.4);
        assertThat(firstGenerator.q()).isEqualTo(-16.9);

        XiidmNetwork.Equipment b2Load = equipments.stream()
                .filter(equipment -> "B2-L".equals(equipment.id()))
                .findFirst()
                .orElseThrow();
        assertThat(b2Load.p()).isEqualTo(21.7);
        assertThat(b2Load.q()).isEqualTo(12.7);
    }

    @Test
    void readsBothEndsOfEveryBranch() throws Exception {
        XiidmNetwork network = this.parser.parse(this.ieee14());

        XiidmNetwork.Branch line = network.branches().stream()
                .filter(branch -> "L1-2-1".equals(branch.id()))
                .findFirst()
                .orElseThrow();
        assertThat(line.kind()).isEqualTo(XiidmNetwork.BranchKind.LINE);
        assertThat(line.bus1()).isEqualTo("B1");
        assertThat(line.bus2()).isEqualTo("B2");

        XiidmNetwork.Branch transformer = network.branches().stream()
                .filter(branch -> "T4-7-1".equals(branch.id()))
                .findFirst()
                .orElseThrow();
        assertThat(transformer.bus1()).isEqualTo("B4");
        assertThat(transformer.bus2()).isEqualTo("B7");
    }

    @Test
    void rejectsContentThatIsNotAnXiidmNetwork() {
        assertThatThrownBy(() -> this.parser.parse("<?xml version=\"1.0\"?><something/>"))
                .isInstanceOf(XiidmParseException.class)
                .hasMessageContaining("does not look like an XIIDM file");

        assertThatThrownBy(() -> this.parser.parse("   "))
                .isInstanceOf(XiidmParseException.class)
                .hasMessageContaining("empty");

        assertThatThrownBy(() -> this.parser.parse("<network"))
                .isInstanceOf(XiidmParseException.class)
                .hasMessageContaining("not a valid XIIDM document");
    }

    private String ieee14() throws IOException {
        try (InputStream inputStream = XiidmParserTest.class.getResourceAsStream("/xiidm/ieee14.xiidm")) {
            assertThat(inputStream).as("test resource /xiidm/ieee14.xiidm").isNotNull();
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
