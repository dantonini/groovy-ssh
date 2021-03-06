package org.hidetake.groovy.ssh.extension

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.operation.SftpFailureException
import org.hidetake.groovy.ssh.session.SessionExtension

import static org.hidetake.groovy.ssh.util.Utility.currySelf

/**
 * An extension class to get a file or directory via SFTP.
 *
 * @author Hidetake Iwata
 */
@Slf4j
trait SftpGet implements SessionExtension {
    @ToString
    private static class GetOptions {
        def from
        def into

        static usage = '''get() accepts following signatures:
get(from: String, into: String or File) // get a file or directory recursively
get(from: String, into: OutputStream)   // get a file into the stream
get(from: String)                       // get a file and return the content'''

        static create(HashMap map) {
            try {
                assert map.from, 'from must be given'
                new GetOptions(map)
            } catch (MissingPropertyException e) {
                throw new IllegalArgumentException(usage, e)
            } catch (AssertionError e) {
                throw new IllegalArgumentException(usage, e)
            }
        }
    }

    /**
     * Get file(s) or content from the remote host.
     *
     * @param map {@link GetOptions}
     * @returns content as a string if <code>into</into> is not given
     */
    String get(HashMap map) {
        def options = GetOptions.create(map)
        if (options.into) {
            get(options.from, options.into)
        } else {
            def stream = new ByteArrayOutputStream()
            get(options.from, stream)
            new String(stream.toByteArray())
        }
    }

    /**
     * Get a file from the remote host.
     *
     * @param remotePath
     * @param stream
     */
    void get(String remotePath, OutputStream stream) {
        assert remotePath, 'remote path must be given'
        assert stream,  'output stream must be given'
        sftp {
            getContent(remotePath, stream)
        }
        log.info("Received content from $remote.name: $remotePath")
    }

    /**
     * Get a file or directory from the remote host.
     *
     * @param remotePath
     * @param localPath
     */
    void get(String remotePath, String localPath) {
        assert remotePath, 'remote path must be given'
        assert localPath,  'local path must be given'
        get(remotePath, new File(localPath))
    }

    /**
     * Get a file or directory from the remote host.
     *
     * @param remotePath
     * @param localFile
     */
    void get(String remotePath, File localFile) {
        assert remotePath, 'remote path must be given'
        assert localFile,  'local file must be given'
        try {
            sftp {
                getFile(remotePath, localFile.path)
            }
            log.info("Received file from $remote.name: $remotePath -> $localFile.path")
        } catch (SftpFailureException e) {
            if (e.cause.message.startsWith('not supported to get directory')) {
                log.debug("Found directory on $remote.name: $remotePath")
                getRecursive(remotePath, localFile)
                log.info("Received directory $remote.name: $remotePath -> $localFile.path")
            } else {
                throw e
            }
        }
    }

    private void getRecursive(String baseRemoteDir, File baseLocalDir) {
        sftp {
            currySelf { Closure self, String remoteDir, File localDir ->
                def remoteDirName = remoteDir.find(~'[^/]+/?$')
                def localChildDir = new File(localDir, remoteDirName)
                localChildDir.mkdirs()

                log.debug("Entering directory on $remote.name: $remoteDir")
                cd(remoteDir)

                ls('.').each { child ->
                    if (!child.attrs.dir) {
                        getFile(child.filename, localChildDir.path)
                    } else if (child.filename in ['.', '..']) {
                        // ignore directory entries
                    } else {
                        self.call(self, child.filename, localChildDir)
                    }
                }

                log.debug("Leaving directory on $remote.name: $remoteDir")
                cd('..')
            }.call(baseRemoteDir, baseLocalDir)
        }
    }
}
