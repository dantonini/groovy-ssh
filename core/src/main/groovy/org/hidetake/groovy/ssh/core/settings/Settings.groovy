package org.hidetake.groovy.ssh.core.settings

/**
 * Represents a settings class.
 *
 * @param < T > implemented type
 */
trait Settings<T> {
    /**
     * Compute a merged settings.
     *
     * @param right
     * @return
     */
    abstract T plus(T right)

    /**
     * Returns a string representation of this settings.
     * Class should implements method <code>toString__<i>propertyName</i></code>
     * to exclude or customize property representation.
     * See test for details of specification.
     *
     * @returns string representation of this settings
     */
    @Override
    String toString() {
        '{' + properties.findAll { key, value ->
            // prevents recursion
            !(value instanceof Settings) &&
            // excludes class
            key != 'class' &&
            // excludes if value is null
            value != null
        }.collectEntries { key, value ->
            try {
                [(key): "toString__$key"()]
            } catch (MissingMethodException ignore) {
                [(key): value]
            }
        }.findAll { key, value ->
            // excludes if the formatter returns null
            value != null
        }.collect { key, value ->
            "$key=${value.toString()}"
        }.join(', ') + '}'
    }
}
