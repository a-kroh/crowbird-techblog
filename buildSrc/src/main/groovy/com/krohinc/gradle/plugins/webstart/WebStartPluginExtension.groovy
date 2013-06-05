package com.krohinc.gradle.plugins.webstart

/**
 * Plugin extension object that becomes available as a project
 * property named 'webstart'. It can be used
 * to override the default settings for the plugin.
 */
class WebStartPluginExtension {
    String codebase = ''
    String homepage = ''
    String jnlpname = ''
    String mainclass = ''
    String output = 'webstart'
    String source = 'src/main/jnlp'
    String title = ''
    String vendor = ''
}
