package com.zpw.myplayground.model.manifest

import com.zpw.myplayground.constants.ModelConstants.APPLICATION_TAG
import com.zpw.myplayground.constants.ModelConstants.MANIFEST_PACKAGE_ATTRIBUTE
import com.zpw.myplayground.constants.ModelConstants.MANIFEST_TAG
import javax.xml.bind.annotation.XmlAccessType.FIELD
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = MANIFEST_TAG)
@XmlAccessorType(FIELD)
data class Manifest(
    @field:XmlAttribute(name = MANIFEST_PACKAGE_ATTRIBUTE)
    val packageName: String,
    @field:XmlElement(name = APPLICATION_TAG)
    val application: Application
) {
    @Suppress("unused")
    constructor() : this(packageName = "", application = Application())
}
