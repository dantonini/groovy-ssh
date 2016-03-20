package org.hidetake.groovy.ssh.extension.settings

import groovy.transform.EqualsAndHashCode
import org.hidetake.groovy.ssh.core.settings.Settings

import static org.hidetake.groovy.ssh.util.Utility.findNotNull

@EqualsAndHashCode
class SudoSettings implements Settings<SudoSettings> {
    /**
     * Sudo password.
     */
    String sudoPassword

    /**
     * Sudo executable path.
     */
    String sudoPath

    static final DEFAULT = new SudoSettings(
            sudoPassword: null,
            sudoPath: 'sudo',
    )

    SudoSettings plus(SudoSettings right) {
        new SudoSettings(
                sudoPassword: findNotNull(right.sudoPassword, sudoPassword),
                sudoPath: findNotNull(right.sudoPath, sudoPath),
        )
    }
}
