package com.krohinc.gradle.plugins.webstart

import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCopyDetails;
import org.gradle.api.internal.file.DefaultSourceDirectorySet
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.GradleException

class WebStartPlugin implements Plugin<Project> {
    
    private static final String COPY_JARS_TASK_NAME = 'copyJars'
    
    private static final String SIGN_JARS_TASK_NAME = 'signJars'
    
    private static final String GENERATE_JNLP_TASK_NAME = 'generateJnlp'
    
    void apply(Project project) {
        // Ensure that the project has a dependency
        // on the Java plugin.
        project.apply plugin: 'java'
        
        // Add the 'webstart' extension object:
        project.extensions.create('webstart', WebStartPluginExtension)
        
        addCopyJarsTask(project)
        addSignJarsTask(project)
        addGenerateJnlpTask(project)
    }

    void addCopyJarsTask(final Project project)
    {
        final Task jarTask = project.tasks[JavaPlugin.JAR_TASK_NAME]
        
        def taskArgs = [type: Copy, dependsOn: jarTask]
        Task copyJarsTask = project.task(taskArgs, COPY_JARS_TASK_NAME) {
            from jarTask
            from project.configurations.runtime
            into "${project.buildDir}/jars"
            
            eachFile { FileCopyDetails details ->
                logger.info("Copying ${details.name}")
            }
        }
    }
    
    void addSignJarsTask(final Project project)
    {
        final Task copyJarsTask = project.tasks[COPY_JARS_TASK_NAME]
        
        def taskArgs = [dependsOn: copyJarsTask]
        Task signJarsTask = project.task(taskArgs, SIGN_JARS_TASK_NAME) << {
            // Determine a list of jars to sign:
            def inputDir = project.file("${project.buildDir}/jars")
            def inputJars = project.fileTree(dir: inputDir, include: '*.jar').files
            
            // Create the destination directory for the signed jars:
            def destDir = project.file("${project.buildDir}/${project.webstart.output}")
            destDir.mkdirs()
            
            // Sign the jars:
            inputJars.each {
                logger.info("Signing $it")
                
                project.ant.signjar(
                    destDir: destDir,
                    jar: it,
                    keystore: project.ext.keystore,
                    storepass: project.ext.storepass,
                    alias: project.ext.keystoreAlias,
                    preservelastmodified: 'true')
            }

            // Define the task's inputs/outputs:
            inputs.files(inputJars)
            outputs.files(project.fileTree(dir: destDir, include: '*.jar').files)
        }
    }
    
    void addGenerateJnlpTask(final Project project)
    {
        final Task signJarsTask = project.tasks[SIGN_JARS_TASK_NAME]
        
        def taskArgs = [type: Copy, dependsOn: signJarsTask]
        Task jnlpTask = project.task(taskArgs, GENERATE_JNLP_TASK_NAME)
        
        project.afterEvaluate {
            // Default the JNLP name match the jar name:
            if (project.webstart.jnlpname.empty)
            {
                final Task jarTask = project.tasks[JavaPlugin.JAR_TASK_NAME]
                project.webstart.jnlpname = jarTask.archiveName.replaceAll('.jar', '.jnlp')
            }
            
            // Configure the jnlpTask after evaluation because it relies on
            // the plugin's extensions being configured:
            jnlpTask.configure {
                from(project.webstart.source) {
                    include '**/*.jnlp.input'
                }
                
                // Rename the file to include the version:
                rename { String fileName ->
                    project.webstart.jnlpname
                }
                
                into "${project.buildDir}/${project.webstart.output}"
                
                def substitutionValues = [ 
                     vendor : project.webstart.vendor,
                     title : project.webstart.title,
                     jnlpname : project.webstart.jnlpname,
                     mainclass : project.webstart.mainclass,
                     codebase : project.webstart.codebase,
                     version : project.version.toString(),
                     homepage : project.webstart.homepage,
                     targetCompatibility : project.targetCompatibility.toString() + '+' ]
                
                filter(ReplaceTokens, tokens: substitutionValues)
                
                // Filter '@jars@' and replace it with the required runtime jars:
                filter { String line ->
                    if (line.trim().equals('@jars@'))
                    {
                        // Determine the indentation of the original line and use
                        // it on all the newly inserted lines:
                        int indentSize = line.length() - line.stripIndent().length()
                        def indent = line.substring(0, indentSize)
                        
                        def jarHrefs = ''
                        signJarsTask.outputs.files*.name.each { String jarName ->
                            def href = indent + "<jar href=\"$jarName\"/>\n"
                            jarHrefs += href
                        }
                        
                        // Remove the last newline:
                        jarHrefs = jarHrefs.substring(0, jarHrefs.length() - 1)
                    }
                    else
                    {
                        // Just return the unchanged line:
                        line
                    }
                }
            }
        }
    }
}
