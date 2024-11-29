package com.github.realzimboguy.jcasflow.engine.repo;


import java.net.InetSocketAddress;
import java.time.Duration;


import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.github.realzimboguy.jcasflow.engine.config.JCasFlowConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

@Service
public class CassandraConnectionPool {

	private final JCasFlowConfig jCasFlowConfig;

	private static CqlSession session;

	public CassandraConnectionPool(JCasFlowConfig jCasFlowConfig) {

		this.jCasFlowConfig = jCasFlowConfig;
	}

	// Initialize the session (singleton for reuse)
	@PostConstruct
	public void setup() {
		session = buildSession();
	}

	private CqlSession buildSession() {
		// Use a configuration loader to manage pooling and timeouts
		DriverConfigLoader loader = DriverConfigLoader.programmaticBuilder()
				.withString(DefaultDriverOption.LOAD_BALANCING_LOCAL_DATACENTER, jCasFlowConfig.getDatabaseDcName())
				.withDuration(DefaultDriverOption.CONNECTION_INIT_QUERY_TIMEOUT, Duration.ofSeconds(10))
				.withInt(DefaultDriverOption.CONNECTION_POOL_LOCAL_SIZE, 4)  // Pool size
				.withInt(DefaultDriverOption.CONNECTION_POOL_REMOTE_SIZE, 2) // For remote DCs
				.build();

		return CqlSession.builder()
				.addContactPoint(new InetSocketAddress(jCasFlowConfig.getDatabaseIp(), jCasFlowConfig.getDatabasePort())) // Replace with your IP
//				.withAuthCredentials("username", "password") // If auth is enabled
//				.withKeyspace(JCasFlowConfig.getDatabaseKeyspace())
				.withConfigLoader(loader)
				.build();
	}

	public static CqlSession getSession() {
		return session;
	}
}
