package org.hidetake.groovy.ssh.core.settings

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import static org.hidetake.groovy.ssh.util.Utility.findNotNull

/**
 * Settings for SSH operations.
 *
 * @author Hidetake Iwata
 */
@EqualsAndHashCode
@ToString
class OperationSettings implements Settings<OperationSettings> {
    /**
     * Dry-run flag.
     * If <code>true</code>, performs no action.
     */
    Boolean dryRun

    /**
     * Ignores the exit status of the command or shell.
     */
    Boolean ignoreError

    /**
     * PTY allocation flag.
     * If <code>true</code>, PTY will be allocated on command execution.
     */
    Boolean pty

    /**
     * A logging method of the remote command or shell.
     */
    LoggingMethod logging

    /**
     * An output stream to forward the standard output.
     */
    OutputStream outputStream

    /**
     * An output stream to forward the standard error.
     */
    OutputStream errorStream

    /**
     * Encoding of input and output stream.
     */
    String encoding

    /**
     * Stream interaction.
     * @see org.hidetake.groovy.ssh.interaction.InteractionHandler
     */
    Closure interaction

    /**
     * File transfer method such as SFTP or SCP.
     */
    FileTransferMethod fileTransfer

    /**
     * Extensions for {@link org.hidetake.groovy.ssh.session.SessionHandler}.
     */
    List extensions = []

    static final DEFAULT = new OperationSettings(
            dryRun: false,
            ignoreError: false,
            pty: false,
            logging: LoggingMethod.slf4j,
            encoding: 'UTF-8',
            fileTransfer: FileTransferMethod.sftp,
            extensions: []
    )

    OperationSettings plus(OperationSettings right) {
        new OperationSettings(
                dryRun:         findNotNull(right.dryRun, dryRun),
                ignoreError: findNotNull(right.ignoreError, ignoreError),
                pty:            findNotNull(right.pty, pty),
                logging:        findNotNull(right.logging, logging),
                encoding:       findNotNull(right.encoding, encoding),
                interaction:    findNotNull(right.interaction, interaction),
                outputStream:   findNotNull(right.outputStream, outputStream),
                errorStream:    findNotNull(right.errorStream, errorStream),
                fileTransfer:   findNotNull(right.fileTransfer, fileTransfer),
                extensions:     extensions + right.extensions
        )
    }
}
