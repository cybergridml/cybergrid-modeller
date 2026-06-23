package cea.fr.CybergridML;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CybergridMLApplicationTests {

	@MockitoBean("createWebSocketContainer")
	private ServletServerContainerFactoryBean createWebSocketContainer;

	@Test
	void contextLoads() {
	}

}
