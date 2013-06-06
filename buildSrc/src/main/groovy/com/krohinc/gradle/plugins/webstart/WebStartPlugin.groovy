/*
 * Copyright 2013 Andrew Kroh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

/**
 * Gradle plugin for generating a Java web start JNLP file
 * from a template and signing all the runtime jars used in the
 * web start application.
 * 
 * <p>
 * You should place your templated JNLP file in src/main/jnlp and
 * name it [project].jnlp.input. The following placeholders can be
 * used in your template: {@literal @}codebase{@literal @}, 
 * {@literal @}homepage{@literal @}, {@literal @}jnlpname{@literal @}, 
 * {@literal @}mainclass{@literal @}, {@literal @}targetCompatibility{@literal @}, 
 * {@literal @}title{@literal @}, {@literal @}vendor{@literal @}, 
 * {@literal @}version{@literal @}, and {@literal @}jars{@literal @}.
 * </p>
 * 
 * <p>
 * Example build.gradle:
 * </p>
 * <pre>
 * apply plugin: 'webstart'
 * 
 * defaultTasks 'generateJnlp'
 * 
 * webstart {
 *   codebase = 'http://www.host.com/webstart'
 *   homepage = 'http://www.host.com'
 *   mainclass= 'com.host.app.YourClass'
 *   title = 'Sample Application'
 *   vendor = 'Company'
 * }
 * </pre>
 * 
 * @author Andrew Kroh
 */
class WebStartPlugin implements Plugin<Project> 
{
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
        Task signJarsTask = project.task(taskArgs, SIGN_JARS_TASK_NAME) {
            def inputDir = project.file("${project.buildDir}/jars")
            def outputDir = project.file("${project.buildDir}/${project.webstart.output}")
                
            // Define the task's inputs/outputs:
            inputs.dir inputDir
            outputs.dir outputDir
            
            doLast {
                // Determine a list of jars to sign:
                def inputJars = project.fileTree(dir: inputDir, include: '*.jar').files
                
                // Ensure that the output directory exists:
                outputDir.mkdirs()
                
                // Sign the jars:
                inputJars.each {
                    logger.info("Signing $it")
                    
                    project.ant.signjar(
                        destDir: outputDir,
                        jar: it,
                        keystore: project.ext.keystore,
                        storepass: project.ext.storepass,
                        alias: project.ext.keystoreAlias,
                        preservelastmodified: 'true')
                }
            }
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
                     codebase : project.webstart.codebase,
                     homepage : project.webstart.homepage,
                     jnlpname : project.webstart.jnlpname,
                     mainclass : project.webstart.mainclass,
                     targetCompatibility : project.targetCompatibility.toString() + '+',
                     title : project.webstart.title,
                     vendor : project.webstart.vendor,
                     version : project.version.toString()]
                
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
