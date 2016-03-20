package org.hidetake.groovy.ssh.operation

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.core.settings.OperationSettings
import org.hidetake.groovy.ssh.extension.settings.LocalPortForwardSettings
import org.hidetake.groovy.ssh.extension.settings.RemotePortForwardSettings

/**
 * Dry-run implementation of {@link Operations}.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class DryRunOperations implements Operations {
    final Remote remote

    def DryRunOperations(Remote remote1) {
        remote = remote1
        assert remote
    }

    @Override
    Operation shell(OperationSettings settings) {
        log.info("Executing shell on $remote")
        new DryRunOperation()
    }

    @Override
    Operation command(OperationSettings settings, String commandLine) {
        log.info("Executing command on $remote: $commandLine")
        new DryRunOperation()
    }

    @Override
    int forwardLocalPort(LocalPortForwardSettings settings) {
        log.info("Requesting port forwarding from " +
                 "local (${settings.bind}:${settings.port}) to remote (${settings.host}:${settings.hostPort})" +
                 "on $remote")
        0
    }

    @Override
    void forwardRemotePort(RemotePortForwardSettings settings) {
        log.info("Requesting port forwarding from " +
                 "remote (${settings.bind}:${settings.port}) to local (${settings.host}:${settings.hostPort})" +
                 "on $remote")
    }

    @Override
    def <T> T sftp(Closure<T> closure) {
        log.info("Requesting SFTP subsystem on $remote")
        null
    }
}
