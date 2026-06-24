package cea.fr.CybergridML;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;


@SpringBootApplication
@ComponentScan(
		basePackages = {"cea.fr.CybergridML", "org.eclipse.syson", "org.eclipse.sirius.web",
				"org.eclipse.sirius.components"},
		excludeFilters = {
				@ComponentScan.Filter(type = FilterType.REGEX, pattern = {
						".*SysONDefaultLibrariesConfiguration",
						".*SysMLv2ProjectTemplatesProvider",
						".*StateTransitionViewDiagramDescriptionProvider",
						".*ActionFlowViewDiagramDescriptionProvider",
						".*InterconnectionViewDiagramDescriptionProvider"
				})
		}
)
@ConfigurationPropertiesScan
public class CybergridMLApplication {

	public static void main(String[] args) {
		SpringApplication.run(CybergridMLApplication.class, args);
	}
}