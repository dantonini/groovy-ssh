package org.hidetake.groovy.ssh.extension

import groovy.transform.EqualsAndHashCode
import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j
import org.codehaus.groovy.tools.Utilities
import org.hidetake.groovy.ssh.core.settings.CompositeSettings
import org.hidetake.groovy.ssh.core.settings.Settings
import org.hidetake.groovy.ssh.extension.settings.SudoSettings
import org.hidetake.groovy.ssh.operation.CommandSettings
import org.hidetake.groovy.ssh.operation.Operations
import org.hidetake.groovy.ssh.session.BadExitStatusException
import org.hidetake.groovy.ssh.session.SessionExtension

/**
 * An extension class of sudo command execution.
 *
 * @author Hidetake Iwata
 */
trait Sudo implements SessionExtension {
    /**
     * Performs a sudo operation, explicitly providing password for the sudo user.
     * This method blocks until channel is closed.
     *
     * @param command
     * @return output value of the command
     */
    String executeSudo(String command) {
        assert command, 'command must be given'
        def executor = new SudoExecutor(operations, globalSettings, new SudoCommandSettings())
        executor.execute(command)
    }

    /**
     * Performs a sudo operation, explicitly providing password for the sudo user.
     * This method blocks until channel is closed.
     *
     * @param settings execution settings
     * @param command
     * @return output value of the command
     */
    String executeSudo(HashMap settings, String command) {
        assert command, 'command must be given'
        assert settings != null, 'settings must not be null'
        def executor = new SudoExecutor(operations, globalSettings, new SudoCommandSettings(settings))
        executor.execute(command)
    }

    /**
     * Performs a sudo operation, explicitly providing password for the sudo user.
     * This method blocks until channel is closed.
     *
     * @param command
     * @param callback closure called with an output value of the command
     */
    void executeSudo(String command, Closure callback) {
        assert command, 'command must be given'
        assert callback, 'callback must be given'
        callback.call(executeSudo(command))
    }

    /**
     * Performs a sudo operation, explicitly providing password for the sudo user.
     * This method blocks until channel is closed.
     *
     * @param settings execution settings
     * @param command
     * @param callback closure called with an output value of the command
     */
    void executeSudo(HashMap settings, String command, Closure callback) {
        assert command, 'command must be given'
        assert callback, 'callback must be given'
        assert settings != null, 'settings must not be null'
        callback.call(executeSudo(settings, command))
    }


    /**
     * Settings class for handling map argument by
     * {@link Sudo#executeSudo(java.util.HashMap, java.lang.String)} and
     * {@link Sudo#executeSudo(java.util.HashMap, java.lang.String, groovy.lang.Closure)}.
     */
    @EqualsAndHashCode
    private static class SudoCommandSettings implements Settings<SudoCommandSettings> {
        @Delegate
        CommandSettings commandSettings = new CommandSettings()

        @Delegate
        SudoSettings sudoSettings = new SudoSettings()

        static DEFAULT = new SudoCommandSettings(
                commandSettings: CommandSettings.DEFAULT,
                sudoSettings: SudoSettings.DEFAULT,
        )

        SudoCommandSettings plus(SudoCommandSettings right) {
            new SudoCommandSettings(
                    commandSettings: right.commandSettings + commandSettings,
                    sudoSettings: right.sudoSettings + sudoSettings,
            )
        }
    }

    @Slf4j
    @TupleConstructor
    private static class SudoExecutor {
        final Operations operations
        final CommandSettings settings
        final String sudoPassword
        final String sudoPath

        def SudoExecutor(Operations operations1, CompositeSettings globalSettings, SudoCommandSettings methodSettings) {
            operations = operations1
            settings = globalSettings.commandSettings + methodSettings.commandSettings
            sudoPassword = methodSettings.sudoPassword ?: operations.remote.sudoPassword ?: operations.remote.password
            sudoPath = methodSettings.sudoPath ?: operations.remote.sudoPath ?: SudoSettings.DEFAULT.sudoPath
            assert operations
            assert settings
            assert sudoPassword
            assert sudoPath
        }

        String execute(String commandLine) {
            final prompt = UUID.randomUUID().toString()
            final lines = []
            final interactionSettings = new CommandSettings(interaction: {
                when(partial: prompt, from: standardOutput) {
                    log.info("Providing password for sudo prompt on $operations.remote.name")
                    standardInput << sudoPassword << '\n'

                    when(nextLine: _, from: standardOutput) {
                        when(nextLine: 'Sorry, try again.') {
                            log.error("Failed sudo authentication on $operations.remote.name")
                            throw new RuntimeException('sudo authentication failed')
                        }
                        when(line: _, from: standardOutput) {
                            log.info("Success sudo authentication on $operations.remote.name")
                            lines << it
                        }
                    }
                }
                when(line: _, from: standardOutput) {
                    lines << it
                }
            })

            final sudoCommandLine = "$sudoPath -S -p '$prompt' $commandLine"
            final exitStatus = operations.command(settings + interactionSettings, sudoCommandLine).startSync()
            if (exitStatus != 0 && !settings.ignoreError) {
                throw new BadExitStatusException("Command returned exit status $exitStatus: $sudoCommandLine", exitStatus)
            }
            lines.join(Utilities.eol())
        }
    }
}
