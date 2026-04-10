package cea.fr.NacreRTE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;


@SpringBootApplication
@ComponentScan(
		basePackages = {"cea.fr.NacreRTE", "org.eclipse.syson", "org.eclipse.sirius.web",
				"org.eclipse.sirius.components"},
		excludeFilters = {
				@ComponentScan.Filter(type = FilterType.REGEX, pattern = {
						".*SysONDefaultLibrariesConfiguration",
						".*SysMLv2ProjectTemplatesProvider"
				})
		}
)
@ConfigurationPropertiesScan
public class NacreRteApplication {

	public static void main(String[] args) {
		SpringApplication.run(NacreRteApplication.class, args);
	}
}