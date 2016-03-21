package org.hidetake.groovy.ssh.core.settings

import spock.lang.Specification
import spock.lang.Unroll

class SettingsSpec extends Specification {

    static class ExampleSettings implements Settings<ExampleSettings> {
        String name
        Integer count

        @Override
        ExampleSettings plus(ExampleSettings right) {
            null
        }
    }

    static class SecretSettings implements Settings<SecretSettings> {
        String name
        String password
        final toString__password() { '...' }

        @Override
        SecretSettings plus(SecretSettings right) {
            null
        }
    }

    static class ConstantSettings implements Settings<ConstantSettings> {
        String name
        final constValue = 'constant'
        final toString__constValue() {}

        @Override
        ConstantSettings plus(ConstantSettings right) {
            null
        }
    }

    static class CompositeSettings implements Settings<CompositeSettings> {
        @Delegate
        ExampleSettings exampleSettings = new ExampleSettings()

        @Delegate
        SecretSettings secretSettings = new SecretSettings()

        @Delegate
        ConstantSettings constantSettings = new ConstantSettings()

        @Override
        CompositeSettings plus(CompositeSettings right) {
            null
        }
    }

    def "toString should contains each key and value of properties"() {
        given:
        def settings = new ExampleSettings(name: 'foo', count: 1000)

        when:
        def string = settings.toString()

        then:
        string in ['{count=1000, name=foo}', '{name=foo, count=1000}']
    }

    def "toString should exclude keys if each value is null"() {
        given:
        def settings = new ExampleSettings(name: 'foo')

        when:
        def string = settings.toString()

        then:
        string == '{name=foo}'
    }

    @Unroll
    def "toString should use formatter if defined"() {
        given:
        def settings = new SecretSettings(name: 'foo', password: password)

        when:
        def string = settings.toString()

        then:
        string == mapString

        where:
        password | mapString
        'SECRET' | '{password=..., name=foo}'
        ''       | '{password=..., name=foo}'
        null     | '{name=foo}'
    }

    def "toString should exclude key if formatter returns null"() {
        given:
        def settings = new ConstantSettings(name: 'foo')

        when:
        def string = settings.toString()

        then:
        string == '{name=foo}'
    }

    def "toString should contains each key and value of properties on delegated class"() {
        given:
        def settings = new CompositeSettings(name: 'foo', count: 1000)

        when:
        def string = settings.toString()

        then:
        string in ['{count=1000, name=foo}', '{name=foo, count=1000}']
    }

    def "toString should exclude keys if each value is null on delegated class"() {
        given:
        def settings = new CompositeSettings(name: 'foo')

        when:
        def string = settings.toString()

        then:
        string == '{name=foo}'
    }

    @Unroll
    def "toString should use formatter if defined on delegated class"() {
        given:
        def settings = new CompositeSettings(name: 'foo', password: password)

        when:
        def string = settings.toString()

        then:
        string == mapString

        where:
        password | mapString
        'SECRET' | '{password=..., name=foo}'
        ''       | '{password=..., name=foo}'
        null     | '{name=foo}'
    }

    def "toString should exclude key if formatter returns null on delegated class"() {
        given:
        def settings = new CompositeSettings(name: 'foo')

        when:
        def string = settings.toString()

        then:
        string == '{name=foo}'
    }

}
