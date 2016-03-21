package org.hidetake.groovy.ssh.core

import org.hidetake.groovy.ssh.core.settings.CompositeSettings
import org.hidetake.groovy.ssh.session.Plan
import org.hidetake.groovy.ssh.session.SessionHandler

import static org.hidetake.groovy.ssh.util.Utility.callWithDelegate

/**
 * A handler of {@link Service#run(groovy.lang.Closure)}.
 *
 * @author Hidetake Iwata
 */
class RunHandler {
    /**
     * Per service settings.
     */
    final CompositeSettings settings = new CompositeSettings()

    /**
     * Sessions added in the closure of {@link Service#run(groovy.lang.Closure)}.
     */
    final List<Plan> sessions = []

    /**
     * Configure per service settings.
     *
     * @param closure closure for {@link CompositeSettings}
     */
    void settings(@DelegatesTo(CompositeSettings) Closure closure) {
        assert closure, 'closure must be given'
        callWithDelegate(closure, settings)
    }

    /**
     * Add a session.
     *
     * @param remote the {@link Remote}
     * @param closure closure for {@link SessionHandler}
     */
    void session(Remote remote, @DelegatesTo(SessionHandler) Closure closure) {
        assert remote, 'remote must be given'
        assert remote.host, "host must be given for the remote ${remote.name}"
        assert closure, 'closure must be given'
        sessions.add(new Plan(remote, closure))
    }

    /**
     * Add sessions.
     *
     * @param remotes collection of {@link Remote}s
     * @param closure closure for {@link SessionHandler}
     */
    void session(Collection<Remote> remotes, @DelegatesTo(SessionHandler) Closure closure) {
        assert remotes, 'at least one remote must be given'
        remotes.each { remote -> session(remote, closure) }
    }

    /**
     * Add a session.
     * This method creates a {@link Remote} instance and add a session with it.
     *
     * @param properties properties of a {@link Remote}
     * @param closure closure for {@link SessionHandler}
     */
    void session(Map properties, @DelegatesTo(SessionHandler) Closure closure) {
        assert properties, 'properties of a remote must be given'
        session(new Remote(properties), closure)
    }

    /**
     * Add sessions.
     * This is a last resort method and allows only below arguments.
     *
     * @param args elements except last must be {@link Remote}s and last must be a closure
     * @throws IllegalArgumentException if wrong arguments are given.
     */
    void session(Object[] args) {
        if (args.last() instanceof Closure) {
            def remotes = args.take(args.length - 1) as Collection<Remote>
            def closure = args.last() as Closure
            session(remotes, closure)
        } else {
            throw new IllegalArgumentException('''session() allows following arguments:
session(remote) {}
session(remote1, remote2, ...) {}
session([remote1, remote2, ...]) {}
session(host: 'myHost', user: 'myUser', ...) {}''')
        }
    }
}
