package org.hidetake.groovy.ssh.core.settings

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.hidetake.groovy.ssh.connection.ConnectionSettings
import org.hidetake.groovy.ssh.operation.CommandSettings
import org.hidetake.groovy.ssh.operation.ShellSettings
import org.hidetake.groovy.ssh.session.SessionSettings

/**
 * Represents overall settings configurable in
 * {@link org.hidetake.groovy.ssh.core.Service#settings} and
 * {@link org.hidetake.groovy.ssh.core.RunHandler#settings}.
 *
 * @author Hidetake Iwata
 */
@EqualsAndHashCode
@ToString
class CompositeSettings implements Settings<CompositeSettings> {
    @Delegate
    ConnectionSettings connectionSettings = new ConnectionSettings()

    @Delegate
    SessionSettings sessionSettings = new SessionSettings()

    @Delegate
    ShellSettings shellSettings = new ShellSettings()

    @Delegate
    CommandSettings commandSettings = new CommandSettings()

    static final DEFAULT = new CompositeSettings(
            connectionSettings: ConnectionSettings.DEFAULT,
            sessionSettings: SessionSettings.DEFAULT,
            shellSettings: ShellSettings.DEFAULT,
            commandSettings: CommandSettings.DEFAULT,
    )

    @Override
    CompositeSettings plus(CompositeSettings right) {
        new CompositeSettings(
                connectionSettings: connectionSettings + right.connectionSettings,
                sessionSettings: sessionSettings + right.sessionSettings,
                shellSettings: shellSettings + right.shellSettings,
                commandSettings: commandSettings + right.commandSettings,
        )
    }
}
