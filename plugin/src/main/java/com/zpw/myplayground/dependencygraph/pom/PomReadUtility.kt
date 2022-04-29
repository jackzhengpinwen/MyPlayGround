package com.zpw.myplayground.dependencygraph.pom

import com.google.gson.Gson
import com.zpw.myplayground.dependencygraph.model.PomDependencies
import com.zpw.myplayground.dependencygraph.model.PomDependency
import com.zpw.myplayground.dependencygraph.model.PomProject
import com.zpw.myplayground.logger
import org.xml.sax.InputSource
import java.io.File
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Node

object PomReadUtility {
    /**
     * 将pom.xml文件转换成javabean
     */
    private fun readPom(file: File) {
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val xmlInput = InputSource(StringReader(file.readText()))
        val doc = dBuilder.parse(xmlInput)
        val xmlNodes = doc.documentElement.childNodes
        val pomProject = PomProject()
        for (i in 0 until xmlNodes.length) {
            val xmlNode = xmlNodes.item(i)
            if (xmlNode.nodeType == Node.ELEMENT_NODE) {
                if (xmlNode.nodeName == "dependencies") {
                    val dependencies = PomDependencies()
                    val dependenciesNodes = xmlNode.childNodes
                    for (i in 0 until dependenciesNodes.length) {
                        if (dependenciesNodes.item(i).nodeType == Node.ELEMENT_NODE) {
                            val dependency = PomDependency()
                            val dependencyNode = dependenciesNodes.item(i).childNodes
                            for (i in 0 until dependencyNode.length) {
//                            logger.log("${dependencyNode.item(i).nodeName}")
                                if (dependencyNode.item(i).nodeType == Node.ELEMENT_NODE) {
                                    if (dependencyNode.item(i).nodeName == "groupId") {
                                        dependency.groupId = dependencyNode.item(i).textContent
                                    } else if (dependencyNode.item(i).nodeName == "artifactId") {
                                        dependency.artifactId = dependencyNode.item(i).textContent
                                    } else if (dependencyNode.item(i).nodeName == "version") {
                                        dependency.version = dependencyNode.item(i).textContent
                                    } else if (dependencyNode.item(i).nodeName == "scope") {
                                        dependency.scope = dependencyNode.item(i).textContent
                                    }
                                }
                            }
                            dependencies.dependency.add(dependency)
                        }
                    }
                    pomProject.dependencies = dependencies
                } else if (xmlNode.nodeName == "modelVersion") {
//                    logger.log("${xmlNode.textContent}")
                    pomProject.modelVersion = xmlNode.textContent
                } else if (xmlNode.nodeName == "groupId") {
//                    logger.log("${xmlNode.textContent}")
                    pomProject.groupId = xmlNode.textContent
                } else if (xmlNode.nodeName == "artifactId") {
//                    logger.log("${xmlNode.textContent}")
                    pomProject.artifactId = xmlNode.textContent
                } else if (xmlNode.nodeName == "version") {
//                    logger.log("${xmlNode.textContent}")
                    pomProject.version = xmlNode.textContent
                } else if (xmlNode.nodeName == "packaging") {
//                    logger.log("${xmlNode.textContent}")
                    pomProject.packaging = xmlNode.textContent
                }
            }
        }
        logger.log("pomProject is ${Gson().toJson(pomProject)}")
    }
}