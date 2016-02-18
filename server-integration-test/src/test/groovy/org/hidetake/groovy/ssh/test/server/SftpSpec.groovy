package org.hidetake.groovy.ssh.test.server

import com.jcraft.jsch.JSchException
import org.apache.sshd.server.sftp.SftpSubsystem
import org.hidetake.groovy.ssh.operation.SftpException

class SftpSpec extends AbstractFileTransferSpecification {

    def setupSpec() {
        server.subsystemFactories = [new SftpSubsystem.Factory()]
        server.start()
    }


    def "sftp.mkdir() should fail if directory already exists"() {
        given:
        def folder = temporaryFolder.newFolder()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                sftp {
                    mkdir folder.path
                }
            }
        }

        then:
        SftpException e = thrown()
        e.message.contains('create a directory')
    }

    def "sftp should fail if sftp subsystem is disabled"() {
        given:
        server.stop(true)
        server.subsystemFactories.clear()
        server.start()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                sftp {
                    ls('.')
                }
            }
        }

        then:
        JSchException e = thrown()
        e.message == 'failed to send channel request'
    }

}
