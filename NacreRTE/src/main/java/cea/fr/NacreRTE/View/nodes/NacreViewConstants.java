/*******************************************************************************
 * Copyright (c) 2025, 2024 CEA LIST.
 *
 * Contributors:
 *  AZOUZI Imen - imen.azouzi@cea.fr
 *	MONTAGNE Anne-Lore - anne-lore.montagne@cea.fr
 *******************************************************************************/
package cea.fr.NacreRTE.View.nodes;

import org.springframework.stereotype.Service;

/**
 *
 * @author Imen Azouzi (CEA LIST) <imen.azouzi@cea.fr>
 */
@Service
public class NacreViewConstants {

	private NacreViewConstants() {
	}



	public static final String MAINGRID_NODE_BACKGROUND_COLOR = "grey 100";

	public static final String MAINGRID_NODE_BORDER_COLOR = "grey 800";

	public static final String ZONE_NODE_BACKGROUND_COLOR = "orange 100";

	public static final String ZONE_NODE_BORDER_COLOR = "orange 800";

	public static final String SUBSTATION_NODE_BACKGROUND_COLOR = "green 100";

	public static final String SUBSTATION_NODE_BORDER_COLOR = "green 800";

	public static final String CENTRALCONTROLLER_NODE_BACKGROUND_COLOR = "orange 100";

	public static final String CENTRALCONTROLLER_NODE_BORDER_COLOR = "orange 800";

	public static final String ZONALCONTROLLER_NODE_BACKGROUND_COLOR = "orange 100";

	public static final String ZONALCONTROLLER_NODE_BORDER_COLOR = "orange 800";

	public static final String CONTROLLER_EDGE_COLOR = "orange 800";

}
