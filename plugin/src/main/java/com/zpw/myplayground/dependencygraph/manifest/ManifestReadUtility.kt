package com.zpw.myplayground.dependencygraph.manifest

import com.google.gson.Gson
import com.zpw.myplayground.dependencygraph.model.*
import com.zpw.myplayground.logger
import org.xml.sax.InputSource
import java.io.File
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Node
import kotlin.math.min

object ManifestReadUtility {
    /**
     * 将manifest.xml文件转换成javabean
     */
    fun readManifest(file: File) {
        val manifest = Manifest()
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val xmlInput = InputSource(StringReader(file.readText()))
        val doc = dBuilder.parse(xmlInput)
        val documentNode = doc.documentElement
        if (documentNode.nodeType == Node.ELEMENT_NODE) {
            val packageValue = documentNode.attributes.getNamedItem("package")?.nodeValue ?: ""
            val versionCode = documentNode.attributes.getNamedItem("android:versionCode")?.nodeValue ?: ""
            val versionName = documentNode.attributes.getNamedItem("android:versionName")?.nodeValue ?: ""
            manifest.packageName = packageValue
            manifest.versionCode = versionCode
            manifest.versionName = versionName
        }
        val xmlNodes = documentNode.childNodes
        for (i in 0 until xmlNodes.length) {
            val xmlNode = xmlNodes.item(i)
            if (xmlNode.nodeType == Node.ELEMENT_NODE) {
                if (xmlNode.nodeName == "uses-sdk") {
                    val usesSdk = UsesSdk()
                    val minSdkVersion = xmlNode.attributes.getNamedItem("android:minSdkVersion")?.nodeValue ?: ""
                    val targetSdkVersion = xmlNode.attributes.getNamedItem("android:targetSdkVersion")?.nodeValue ?: ""
                    usesSdk.minSdkVersion = minSdkVersion
                    usesSdk.targetSdkVersion = targetSdkVersion
                    manifest.usesSdk = usesSdk
                } else if (xmlNode.nodeName == "uses-permission") {
                    val usePermission = UsePermission()
                    val permission = xmlNode.attributes.getNamedItem("android:name")?.nodeValue ?: ""
                    usePermission.name = permission
                    manifest.usePermission.add(usePermission)
                } else if (xmlNode.nodeName == "uses-feature") {
                    val usesFeature = UsesFeature()
                    val permission = xmlNode.attributes.getNamedItem("android:name")?.nodeValue ?: ""
                    usesFeature.name = permission
                    manifest.usesFeature.add(usesFeature)
                } else if (xmlNode.nodeName == "application") {
                    val applicationNodes = xmlNode.childNodes
                    for (i in 0 until applicationNodes.length) {
                        val applicationNode = applicationNodes.item(i)
                        if (applicationNode.nodeType == Node.ELEMENT_NODE) {
                            if (applicationNode.nodeName == "activity") {
                                val name = applicationNode.attributes.getNamedItem("android:name")?.nodeValue ?: ""
                                val exported = applicationNode.attributes.getNamedItem("android:exported")?.nodeValue ?: ""
                                val activity = Activity()
                                activity.name = name
                                activity.expoeted = exported
                                manifest.application.activityList.add(activity)
                            } else if (applicationNode.nodeName == "service") {
                                val name = applicationNode.attributes.getNamedItem("android:name")?.nodeValue ?: ""
                                val exported = applicationNode.attributes.getNamedItem("android:exported")?.nodeValue ?: ""
                                val service = Service()
                                service.name = name
                                service.expoeted = exported
                                manifest.application.serviceList.add(service)
                            } else if (applicationNode.nodeName == "provider") {
                                val name = applicationNode.attributes.getNamedItem("android:name")?.nodeValue ?: ""
                                val exported = applicationNode.attributes.getNamedItem("android:exported")?.nodeValue ?: ""
                                val provider = Provider()
                                provider.name = name
                                provider.expoeted = exported
                                manifest.application.providerList.add(provider)
                            } else if (applicationNode.nodeName == "receiver") {
                                val name = applicationNode.attributes.getNamedItem("android:name")?.nodeValue ?: ""
                                val exported = applicationNode.attributes.getNamedItem("android:exported")?.nodeValue ?: ""
                                val receiver = Receiver()
                                receiver.name = name
                                receiver.expoeted = exported
                                manifest.application.receiverList.add(receiver)
                            }
                        }
                    }
                }
            }
        }
        logger.log("manifest is ${Gson().toJson(manifest)}")
    }
}