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

/**
 * Plugin extension object that becomes available as a project
 * property named 'webstart'. It should be used to define the values
 * that will be used in populating the JNLP file for the Java web
 * start application.
 * 
 * @see com.krohinc.gradle.plugins.webstart.WebStartPlugin
 */
class WebStartPluginExtension 
{
    String codebase = ''
    String homepage = ''
    String jnlpname = ''
    String mainclass = ''
    String output = 'webstart'
    String source = 'src/main/jnlp'
    String title = ''
    String vendor = ''
}
