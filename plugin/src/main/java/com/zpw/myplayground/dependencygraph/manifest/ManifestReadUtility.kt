package com.zpw.myplayground.dependencygraph.manifest

import com.google.gson.Gson
import com.zpw.myplayground.dependencygraph.model.Manifest
import com.zpw.myplayground.dependencygraph.model.PomDependencies
import com.zpw.myplayground.dependencygraph.model.PomDependency
import com.zpw.myplayground.dependencygraph.model.PomProject
import com.zpw.myplayground.logger
import org.xml.sax.InputSource
import java.io.File
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Node

object ManifestReadUtility {
    /**
     * 将manifest.xml文件转换成javabean
     */
    fun readManifest(file: File) {
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val xmlInput = InputSource(StringReader(file.readText()))
        val doc = dBuilder.parse(xmlInput)
        val xmlNodes = doc.documentElement.childNodes
        val manifest = Manifest()
        for (i in 0 until xmlNodes.length) {
            val xmlNode = xmlNodes.item(i)
            if (xmlNode.nodeType == Node.ELEMENT_NODE) {
                if (xmlNode.nodeName == "manifest") {
                    val packageValue = xmlNode.attributes.getNamedItem("package").nodeValue
                    val versionCode = xmlNode.attributes.getNamedItem("android:versionCode").nodeValue
                    val versionName = xmlNode.attributes.getNamedItem("android:versionName").nodeValue
                    logger.log("packageValue is ${packageValue}, versionCode is ${versionCode}, versionName is ${versionName}")
                    val dependencies = PomDependencies()
                    val dependenciesNodes = xmlNode.childNodes
                    for (i in 0 until dependenciesNodes.length) {
                        if (dependenciesNodes.item(i).nodeType == Node.ELEMENT_NODE) {
                            val dependency = PomDependency()
                            val dependencyNode = dependenciesNodes.item(i).childNodes
                            for (i in 0 until dependencyNode.length) {
                            logger.log("${dependencyNode.item(i).nodeName}")
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
                } else if (xmlNode.nodeName == "modelVersion") {
//                    logger.log("${xmlNode.textContent}")
                } else if (xmlNode.nodeName == "groupId") {
//                    logger.log("${xmlNode.textContent}")
                } else if (xmlNode.nodeName == "artifactId") {
//                    logger.log("${xmlNode.textContent}")
                } else if (xmlNode.nodeName == "version") {
//                    logger.log("${xmlNode.textContent}")
                } else if (xmlNode.nodeName == "packaging") {
//                    logger.log("${xmlNode.textContent}")
                }
            }
        }
        logger.log("manifest is ${Gson().toJson(manifest)}")
    }
}