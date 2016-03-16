package org.hidetake.groovy.ssh.session

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.connection.ConnectionManager
import org.hidetake.groovy.ssh.core.settings.CompositeSettings
import org.hidetake.groovy.ssh.operation.DefaultOperations
import org.hidetake.groovy.ssh.operation.DryRunOperations

import static org.hidetake.groovy.ssh.util.Utility.callWithDelegate

/**
 * An executor of session {@link Plan}s.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class Executor {
    /**
     * Execute {@link Plan}s.
     *
     * @param globalSettings
     * @param plans
     * @return results of each plan
     */
    def <T> List<T> execute(CompositeSettings globalSettings, List<Plan<T>> plans) {
        if (globalSettings.dryRun) {
            dryRun(globalSettings, plans)
        } else {
            wetRun(globalSettings, plans)
        }
    }

    private <T> List<T> dryRun(CompositeSettings globalSettings, List<Plan<T>> plans) {
        log.debug("Running ${plans.size()} session(s) as dry-run")
        plans.collect { plan ->
            def operations = new DryRunOperations(plan.remote)
            callWithDelegate(plan.closure, SessionHandler.create(operations, globalSettings))
        }
    }

    private <T> List<T> wetRun(CompositeSettings globalSettings, List<Plan<T>> plans) {
        log.debug("Running ${plans.size()} session(s)")
        def manager = new ConnectionManager(globalSettings.connectionSettings)
        try {
            plans.collect { plan ->
                def connection = manager.connect(plan.remote)
                def operations = new DefaultOperations(connection)
                callWithDelegate(plan.closure, SessionHandler.create(operations, globalSettings))
            }
        } finally {
            log.debug('Waiting for pending sessions')
            manager.waitAndClose()
            log.debug('Completed all sessions')
        }
    }
}
