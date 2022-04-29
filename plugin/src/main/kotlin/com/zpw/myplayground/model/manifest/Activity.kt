package com.zpw.myplayground.model.manifest

import com.zpw.myplayground.constants.ModelConstants.ACTIVITY_NAME_ATTRIBUTE
import com.zpw.myplayground.constants.ModelConstants.ACTIVITY_TAG
import com.zpw.myplayground.constants.ModelConstants.ANDROID_NAMESPACE
import com.zpw.myplayground.constants.ModelConstants.METADATA_TAG
import javax.xml.bind.annotation.XmlAccessType.FIELD
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = ACTIVITY_TAG)
@XmlAccessorType(FIELD)
data class Activity(
    @field:XmlElement(name = ACTIVITY_NAME_ATTRIBUTE, namespace = ANDROID_NAMESPACE)
    val className: String,
    @field:XmlElement(name = METADATA_TAG)
    val metaDataList: MutableList<MetaData>
) {
    @Suppress("unused")
    constructor() : this(className = "", metaDataList = mutableListOf())
}
