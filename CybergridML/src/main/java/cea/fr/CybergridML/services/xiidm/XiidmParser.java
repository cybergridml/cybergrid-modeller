package cea.fr.CybergridML.services.xiidm;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.springframework.stereotype.Service;

/**
 * Reads an XIIDM (PowSyBl IIDM) XML document into a {@link XiidmNetwork}.
 *
 * <p>The parser is namespace agnostic: it matches on local names only, so it accepts any {@code iidm} schema
 * version. Elements it does not know about are skipped silently; unsupported topologies are reported by
 * {@link XiidmToCybergridMLMapper} rather than here, because only the mapping stage knows what CybergridML can
 * represent.
 */
@Service
public class XiidmParser {

    private static final String ATTR_ID = "id";

    public XiidmNetwork parse(String xiidmContent) throws XiidmParseException {
        if (xiidmContent == null || xiidmContent.isBlank()) {
            throw new XiidmParseException("The XIIDM file is empty.");
        }

        XMLStreamReader reader = null;
        try {
            reader = this.createReader(xiidmContent);
            return this.read(reader);
        } catch (XMLStreamException exception) {
            throw new XiidmParseException("The file is not a valid XIIDM document: " + exception.getMessage(), exception);
        } finally {
            closeQuietly(reader);
        }
    }

    private XMLStreamReader createReader(String xiidmContent) throws XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        // The document comes from an end user: never resolve DTDs or external entities.
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        return factory.createXMLStreamReader(new StringReader(xiidmContent));
    }

    private XiidmNetwork read(XMLStreamReader reader) throws XMLStreamException, XiidmParseException {
        String networkId = null;
        var substations = new ArrayList<XiidmNetwork.Substation>();
        var branches = new ArrayList<XiidmNetwork.Branch>();

        // State of the substation and voltage level currently being read.
        String currentSubstationId = null;
        List<XiidmNetwork.VoltageLevel> currentVoltageLevels = null;
        String currentVoltageLevelId = null;
        Double currentNominalV = null;
        List<XiidmNetwork.Bus> currentBuses = null;
        List<XiidmNetwork.Equipment> currentEquipments = null;

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                switch (reader.getLocalName()) {
                    case "network" -> networkId = attribute(reader, ATTR_ID);
                    case "substation" -> {
                        currentSubstationId = attribute(reader, ATTR_ID);
                        currentVoltageLevels = new ArrayList<>();
                    }
                    case "voltageLevel" -> {
                        currentVoltageLevelId = attribute(reader, ATTR_ID);
                        currentNominalV = doubleAttribute(reader, "nominalV");
                        currentBuses = new ArrayList<>();
                        currentEquipments = new ArrayList<>();
                    }
                    case "bus" -> {
                        if (currentBuses != null) {
                            currentBuses.add(new XiidmNetwork.Bus(attribute(reader, ATTR_ID), attribute(reader, "name"),
                                    doubleAttribute(reader, "v"), doubleAttribute(reader, "angle")));
                        }
                    }
                    case "generator" -> addEquipment(currentEquipments, reader, XiidmNetwork.EquipmentKind.GENERATOR, "targetP", "targetQ");
                    case "load" -> addEquipment(currentEquipments, reader, XiidmNetwork.EquipmentKind.LOAD, "p0", "q0");
                    case "shuntCompensator" -> addEquipment(currentEquipments, reader, XiidmNetwork.EquipmentKind.SHUNT_COMPENSATOR, null, null);
                    case "line" -> branches.add(readBranch(reader, XiidmNetwork.BranchKind.LINE));
                    case "twoWindingsTransformer" -> branches.add(readBranch(reader, XiidmNetwork.BranchKind.TWO_WINDINGS_TRANSFORMER));
                    default -> {
                        // Not a concept CybergridML maps: skip it.
                    }
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                switch (reader.getLocalName()) {
                    case "voltageLevel" -> {
                        if (currentVoltageLevels != null) {
                            currentVoltageLevels.add(new XiidmNetwork.VoltageLevel(currentVoltageLevelId, currentNominalV,
                                    List.copyOf(currentBuses), List.copyOf(currentEquipments)));
                        }
                        currentVoltageLevelId = null;
                        currentNominalV = null;
                        currentBuses = null;
                        currentEquipments = null;
                    }
                    case "substation" -> {
                        substations.add(new XiidmNetwork.Substation(currentSubstationId, List.copyOf(currentVoltageLevels)));
                        currentSubstationId = null;
                        currentVoltageLevels = null;
                    }
                    default -> {
                        // Nothing to close.
                    }
                }
            }
        }

        if (networkId == null && substations.isEmpty()) {
            throw new XiidmParseException("No <network> element found: this does not look like an XIIDM file.");
        }
        return new XiidmNetwork(networkId, List.copyOf(substations), List.copyOf(branches));
    }

    private void addEquipment(List<XiidmNetwork.Equipment> equipments, XMLStreamReader reader, XiidmNetwork.EquipmentKind kind,
            String pAttribute, String qAttribute) {
        if (equipments == null) {
            return;
        }
        equipments.add(new XiidmNetwork.Equipment(kind, attribute(reader, ATTR_ID), busAttribute(reader, "bus", "connectableBus"),
                pAttribute == null ? null : doubleAttribute(reader, pAttribute),
                qAttribute == null ? null : doubleAttribute(reader, qAttribute)));
    }

    private XiidmNetwork.Branch readBranch(XMLStreamReader reader, XiidmNetwork.BranchKind kind) {
        return new XiidmNetwork.Branch(kind, attribute(reader, ATTR_ID),
                busAttribute(reader, "bus1", "connectableBus1"),
                busAttribute(reader, "bus2", "connectableBus2"));
    }

    /**
     * A disconnected element carries only {@code connectableBus}; use it as a fallback so those elements are still
     * imported rather than dropped.
     */
    private String busAttribute(XMLStreamReader reader, String busAttribute, String connectableBusAttribute) {
        String bus = attribute(reader, busAttribute);
        return bus != null ? bus : attribute(reader, connectableBusAttribute);
    }

    private String attribute(XMLStreamReader reader, String name) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            if (name.equals(reader.getAttributeLocalName(i))) {
                return reader.getAttributeValue(i);
            }
        }
        return null;
    }

    private Double doubleAttribute(XMLStreamReader reader, String name) {
        String value = this.attribute(reader, name);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static void closeQuietly(XMLStreamReader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (XMLStreamException exception) {
                // Closing a fully consumed reader cannot fail in a way the caller can act on.
            }
        }
    }
}
