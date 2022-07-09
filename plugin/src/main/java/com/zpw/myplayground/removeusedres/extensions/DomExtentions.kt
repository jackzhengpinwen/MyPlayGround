package com.zpw.myplayground.removeusedres.extensions

import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import javax.xml.namespace.QName
import javax.xml.stream.events.Attribute
import javax.xml.stream.events.StartElement

internal fun NodeList.toSequence(): Sequence<Node> {
    return (0 until length).asSequence().map { item(it) }
}

internal fun Node.getElements(tagName: String): Sequence<Element> {
    return childNodes.toSequence().filter {
        (it.nodeType == Node.ELEMENT_NODE) && (it.nodeName == tagName)
    }.map {
        it as Element
    }
}

internal fun Node.getAttributeText(name: String): String? {
    return attributes?.getNamedItem(name)?.nodeValue
}

fun StartElement.hasAttribute(name: String): Boolean {
    return attributes.asSequence().any {
        (it as Attribute).name.localPart == name
    }
}

fun StartElement.getAttributeValue(name: String): String? {
    return getAttributeByName(QName(name))?.value
}

fun StartElement.getAttributeValue(name: QName): String? {
    return getAttributeByName(name)?.value
}