package nl.shashi.playground.rest.large.files.config;



import lombok.extern.slf4j.Slf4j;
import nl.shashi.playground.rest.large.files.util.AtomicFileMover;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.jdbc.lock.DefaultLockRepository;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.integration.jdbc.lock.LockRepository;
import org.springframework.integration.leader.event.DefaultLeaderEventPublisher;
import org.springframework.integration.metadata.ConcurrentMetadataStore;
import org.springframework.integration.metadata.SimpleMetadataStore;
import org.springframework.integration.support.leader.LockRegistryLeaderInitiator;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.UUID;


@Slf4j
@Configuration
public class PollerConfiguration {

    private static final String LEADER_TABLE_PREFIX = "FILE_POLLER_LEADER_";

    private final PublishSubscribeChannel errorChannel;

    @Autowired
    public PollerConfiguration(PublishSubscribeChannel errorChannel) {
        this.errorChannel = errorChannel;
    }


    @Bean
    public IntegrationFlow errorHandlingFlow() {
        return IntegrationFlows.from(errorChannel)
                .handle(this::errorHandler)
                .get();
    }

    private void errorHandler(Message<?> message) {

        String uuid = Optional.ofNullable(message)
                .map(Message::getHeaders)
                .map(MessageHeaders::getId)
                .map(UUID::toString)
                .orElse("fileUUID could not be found");
        String payload = Optional.ofNullable(message)
                .map(Message::getPayload)
                .map(Object::toString)
                .orElse("no payload");
        log.error("[File poller ] Dispatcher failed to deliver Message,UUID:{},payload{}",uuid,payload);
    }



    /**
     * NO SUPPORTED by IN-MEMORY DB
     * Leader initiator lock registry leader initiator.
     *
     * @param lockRegistry              the lock registry
     * @param applicationEventPublisher the application event publisher
     * @return the lock registry leader initiator
     * Check Table : INT_LOCK for lock
     * When we run the FilePollerApplication multiple instance, it will look into table init_lock  and decide who have that lock,
     * if lock is not shared, new running instance will become "leader" else leader will be "false"
     * eg.. DefaultCandidate{role=leader, id=58da6068-3372-4043-86fe-c67ac44ef768} has been granted leadership; context:
     *  LockContext{role=leader, id=58da6068-3372-4043-86fe-c67ac44ef768, isLeader=true}
     *
     *
     */
    @Bean
    public LockRegistryLeaderInitiator leaderInitiator(LockRegistry lockRegistry, ApplicationEventPublisher applicationEventPublisher) {
        var lockRegistryLeaderInitiator = new LockRegistryLeaderInitiator(lockRegistry);
        lockRegistryLeaderInitiator.setLeaderEventPublisher(new DefaultLeaderEventPublisher(applicationEventPublisher));
        return lockRegistryLeaderInitiator;
    }


    @Bean
    public LockRegistry lockRegistry(LockRepository lockRepository) {
        return new JdbcLockRegistry(lockRepository);
    }

    @Bean
    public LockRepository lockRepository(DataSource dataSource) {
        var lockRepository = new DefaultLockRepository(dataSource);
        lockRepository.setPrefix(LEADER_TABLE_PREFIX);
        return lockRepository;
    }


    @Bean
    public ConcurrentMetadataStore concurrentMetadataStore() {
        return new SimpleMetadataStore();
    }
}
